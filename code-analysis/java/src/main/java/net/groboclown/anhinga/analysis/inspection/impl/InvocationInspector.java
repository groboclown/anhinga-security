// Released under the MIT license.  See the LICENSE file for details.
package net.groboclown.anhinga.analysis.inspection.impl;

import net.groboclown.anhinga.analysis.model.MethodTrace;
import net.groboclown.anhinga.analysis.problems.AnalysisProblem;
import net.groboclown.retval.ProblemCollector;
import net.groboclown.retval.Ret;
import net.groboclown.retval.RetVal;
import net.groboclown.retval.RetVoid;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.Type;
import org.objectweb.asm.tree.*;
import org.objectweb.asm.tree.analysis.*;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Function;

/**
 * A stateful inspector.
 */
public class InvocationInspector {
    private final String className;
    private final MethodNode method;
    private List<MethodTrace.Argument> arguments;
    private List<InvocationArguments> invocations;

    public static RetVal<Inspection> runInspections(
            @Nonnull final String owningClass,
            @Nonnull final MethodNode method) {
System.err.println("  - method " + method.name + method.desc + " (" + method.instructions.size() + " instructions)");
        final InvocationInspector inspector = new InvocationInspector(owningClass, method);
        return
                inspector.loadArguments()
            .thenVoid(
                inspector::loadAnalysis)
            .then(
                inspector::createInspection);
    }

    // Package protected for tests
    InvocationInspector(@Nonnull final String className,
                        @Nonnull final MethodNode method) {
        this.className = className;
        this.method = method;
    }

    @Nonnull
    public String getClassName() {
        return className;
    }

    @Nonnull
    public String getMethodName() {
        return method.name;
    }

    @Nonnull
    public String getMethodSignature() {
        return method.desc;
    }

    @Nonnull
    RetVoid loadArguments() {
        final RetVal<List<MethodTrace.Argument>> res = parseArguments();
        if (res.hasProblems()) {
            return res.forwardVoidProblems();
        }
        this.arguments = res.result();
        return RetVoid.ok();
    }

    @Nonnull
    RetVoid loadAnalysis() {
        final RetVal<List<InvocationArguments>> res = runAnalysis();
        if (res.hasProblems()) {
            return res.forwardVoidProblems();
        }
        this.invocations = res.result();
        return RetVoid.ok();
    }

    @Nonnull
    RetVal<Inspection> createInspection() {
        return RetVal.ok(new Inspection(
                className, method, arguments, invocations));
    }

    RetVal<List<InvocationArguments>> runAnalysis() {
        JoinedInterpreter interpreter = new JoinedInterpreter();
        final Analyzer<JoinedValue> analyzer = new Analyzer<>(interpreter);
        try {
            final Frame<JoinedValue>[] frames = analyzer.analyzeAndComputeMaxs(className, method);
            return RetVal.ok(interpreter.getInvocations(frames, method.instructions));
        } catch (AnalyzerException e) {
            return RetVal.fromProblem(AnalysisProblem.fromMethodAnalysis(className, method, e));
        }
    }

    RetVal<List<MethodTrace.Argument>> parseArguments() {
        if (method.parameters == null) {
            return RetVal.ok(List.of());
        }
        final List<MethodTrace.Argument> arguments = new ArrayList<>();
        ProblemCollector problems = Ret.collectProblems();
        for (int i = 0; i < method.parameters.size(); i++) {
            final int index = i;
            final ParameterNode param = method.parameters.get(i);
            final List<AnnotationNode> visibleAnnotations = findParameterAnnotations(i, method.visibleParameterAnnotations);
            final List<AnnotationNode> invisibleAnnotations = findParameterAnnotations(i, method.invisibleParameterAnnotations);
            final RetVal<String> signature = findParameterSignature(i);
            problems.with(signature, (s) -> {
                arguments.add(new MethodTrace.Argument(param.name, index, s));
            });
        }
        return problems.thenValue(() -> arguments);
    }

    RetVal<String> findParameterSignature(int index) {
        // Parameters are encoded in the local variables.
        // If the method is not static, then the first parameter is the second local variable.
        final List<LocalVariableNode> localVariables = method.localVariables;
        int paramIndex = index;
        if (!isStatic(method)) {
            paramIndex = paramIndex + 1;
        }
        if (paramIndex >= 0 && localVariables != null && paramIndex < localVariables.size()) {
            return RetVal.ok(localVariables.get(paramIndex).desc);
        }

        return RetVal.fromProblem(AnalysisProblem.fromDiscovery(this.className, method,
                "No parameter found at index " + index));
    }

    List<AnnotationNode> findParameterAnnotations(
            int index,
            @Nullable List<AnnotationNode>[] annotations) {
        if (
                annotations == null
                        || annotations.length <= index
                        || index < 0
                        || annotations[index] == null) {
            return List.of();
        }
        return annotations[index];
    }

    private static boolean isStatic(@Nonnull MethodNode node) {
        return (node.access & Opcodes.ACC_STATIC) != 0;
    }

    static <V extends Value> Frame<V> copyFrame(
            @Nonnull Frame<JoinedValue> source,
            @Nonnull Function<JoinedValue, V> extractor) {
        // Computed max stack is sometimes huge and causes out-of-memory issues.
        final Frame<V> ret = new Frame<>(source.getLocals(), source.getStackSize());
        for (int i = 0; i < source.getLocals(); i++) {
            ret.setLocal(i, extractor.apply(source.getLocal(i)));
        }
        for (int i = 0; i < source.getStackSize(); i++) {
            ret.setStack(i, extractor.apply(source.getStack(i)));
        }
        return ret;
    }

    @Nullable
    static <V extends Value> V getStackValue(@Nonnull Frame<V> frame, int index) {
        int top = frame.getStackSize() - 1;
        return index <= top ? frame.getStack(top - index) : null;
    }

    @Nonnull
    static <V extends Value> List<V> extractStackValues(@Nonnull Frame<V> frame, int count) {
        // The stack is backwards from what's expected...
        final List<V> ret = new ArrayList<>();
        while (count > 0) {
            ret.add(getStackValue(frame, --count));
        }
        return ret;
    }

    static class JoinedInterpreter extends Interpreter<JoinedValue> {
        private final BasicVerifier basic;
        private final SourceInterpreter source;
        private final List<DynamicInvokeArgs> dynamicArgs = new ArrayList<>();

        JoinedInterpreter() {
            super(Opcodes.ASM9);
            this.basic = new BasicVerifier();
            this.source = new SourceInterpreter();
        }

        @Nonnull
        List<InvocationArguments> getInvocations(Frame<JoinedValue>[] frames, InsnList instructions) {
            // All done parsing.
            final List<InvocationArguments> invocations = new ArrayList<>();
            int lineNumber = 0;
            for (int i = 0; i < instructions.size(); i++) {
                final AbstractInsnNode insn = instructions.get(i);
                if (insn instanceof LineNumberNode) {
                    lineNumber = ((LineNumberNode) insn).line;
                    continue;
                }
                final Frame<JoinedValue> frame = frames[i];
                if (frame != null) {
                    if (insn instanceof MethodInsnNode) {
                        final MethodInsnNode mi = (MethodInsnNode) insn;
                        final Type[] argumentTypes = Type.getArgumentTypes(mi.desc);
                        List<JoinedValue> argumentValues = extractStackValues(frame, argumentTypes.length);
                        if (insn.getOpcode() == Opcodes.INVOKESTATIC) {
                            invocations.add(new InvocationArguments(
                                    lineNumber, mi, null, argumentValues));
                        } else {
                            invocations.add(new InvocationArguments(
                                    lineNumber, mi, getStackValue(frame, argumentTypes.length), argumentValues));
                        }
                    } else if (insn instanceof InvokeDynamicInsnNode) {
                        final InvokeDynamicInsnNode idi = (InvokeDynamicInsnNode) insn;
                        boolean found = false;
                        for (final DynamicInvokeArgs dyn : dynamicArgs) {
                            if (dyn.insn == insn) {
                                // May want to do some additional checks on the arguments here.
                                invocations.add(new InvocationArguments(lineNumber, idi, extractStackValues(frame, dyn.values.size())));
                                found = true;
                                break;
                            }
                        }
                        if (!found) {
                            // TODO need to figure out the number of arguments for this call
                            //   For the moment, we just grab all the stack items.
                            invocations.add(new InvocationArguments(lineNumber, idi, extractStackValues(frame, frame.getStackSize())));
                        }
                    }
                }

            }
            return invocations;
        }

        @Override
        public JoinedValue newValue(Type type) {
            return JoinedValue.create(
                    source.newValue(type),
                    basic.newValue(type));
        }

        public JoinedValue newParameterValue(final boolean isInstanceMethod, final int local, final Type type) {
            return JoinedValue.create(
                    source.newParameterValue(isInstanceMethod, local, type),
                    basic.newParameterValue(isInstanceMethod, local, type));
        }

        public JoinedValue newReturnTypeValue(final Type type) {
            return JoinedValue.create(
                    source.newReturnTypeValue(type),
                    basic.newReturnTypeValue(type));
        }

        public JoinedValue newEmptyValue(final int local) {
            return JoinedValue.create(
                    source.newEmptyValue(local),
                    basic.newEmptyValue(local));
        }

        public JoinedValue newExceptionValue(
                final TryCatchBlockNode tryCatchBlockNode,
                final Frame<JoinedValue> handlerFrame,
                final Type exceptionType) {
            return JoinedValue.create(
                    source.newExceptionValue(tryCatchBlockNode, copyFrame(handlerFrame, (v) -> v.getSourceValue()), exceptionType),
                    basic.newExceptionValue(tryCatchBlockNode, copyFrame(handlerFrame, (v) -> v.getBasicValue()), exceptionType));
        }

        // ACONST_NULL, ICONST_M1, ICONST_0, ICONST_1, ICONST_2, ICONST_3, ICONST_4, ICONST_5,
        // LCONST_0, LCONST_1, FCONST_0, FCONST_1, FCONST_2, DCONST_0, DCONST_1, BIPUSH, SIPUSH, LDC, JSR,
        // GETSTATIC, NEW
        public JoinedValue newOperation(AbstractInsnNode insn) throws AnalyzerException {
            return JoinedValue.create(
                    source.newOperation(insn),
                    basic.newOperation(insn));
        }

        // ILOAD, LLOAD, FLOAD, DLOAD, ALOAD, ISTORE, LSTORE, FSTORE, DSTORE, ASTORE, DUP, DUP_X1,
        // DUP_X2, DUP2, DUP2_X1, DUP2_X2, SWAP
        public JoinedValue copyOperation(AbstractInsnNode insn, JoinedValue value) throws AnalyzerException {
            return JoinedValue.derive(
                    source.copyOperation(insn, value.getSourceValue()),
                    basic.copyOperation(insn, value.getBasicValue()),
                    value);
        }

        // INEG, LNEG, FNEG, DNEG, IINC, I2L, I2F, I2D, L2I, L2F, L2D, F2I, F2L, F2D, D2I, D2L, D2F,
        // I2B, I2C, I2S, IFEQ, IFNE, IFLT, IFGE, IFGT, IFLE, TABLESWITCH, LOOKUPSWITCH, IRETURN, LRETURN,
        // FRETURN, DRETURN, ARETURN, PUTSTATIC, GETFIELD, NEWARRAY, ANEWARRAY, ARRAYLENGTH, ATHROW,
        // CHECKCAST, INSTANCEOF, MONITORENTER, MONITOREXIT, IFNULL, IFNONNULL
        public JoinedValue unaryOperation(AbstractInsnNode insn, JoinedValue value) throws AnalyzerException {
            return JoinedValue.derive(
                    source.unaryOperation(insn, value.getSourceValue()),
                    basic.unaryOperation(insn, value.getBasicValue()),
                    value);
        }

        // IALOAD, LALOAD, FALOAD, DALOAD, AALOAD, BALOAD, CALOAD, SALOAD, IADD, LADD, FADD, DADD,
        // ISUB, LSUB, FSUB, DSUB, IMUL, LMUL, FMUL, DMUL, IDIV, LDIV, FDIV, DDIV, IREM, LREM, FREM, DREM,
        // ISHL, LSHL, ISHR, LSHR, IUSHR, LUSHR, IAND, LAND, IOR, LOR, IXOR, LXOR, LCMP, FCMPL, FCMPG,
        // DCMPL, DCMPG, IF_ICMPEQ, IF_ICMPNE, IF_ICMPLT, IF_ICMPGE, IF_ICMPGT, IF_ICMPLE, IF_ACMPEQ,
        // IF_ACMPNE, PUTFIELD
        public JoinedValue binaryOperation(AbstractInsnNode insn, JoinedValue value1, JoinedValue value2)
                throws AnalyzerException {
            return JoinedValue.derive(
                    source.binaryOperation(insn, value1.getSourceValue(), value2.getSourceValue()),
                    basic.binaryOperation(insn, value1.getBasicValue(), value2.getBasicValue()),
                    value1, value2);
        }

        // IASTORE, LASTORE, FASTORE, DASTORE, AASTORE, BASTORE, CASTORE, SASTORE
        public JoinedValue ternaryOperation(AbstractInsnNode insn, JoinedValue value1, JoinedValue value2, JoinedValue value3)
                throws AnalyzerException {
            return JoinedValue.derive(
                    source.ternaryOperation(insn, value1.getSourceValue(), value2.getSourceValue(), value3.getSourceValue()),
                    basic.ternaryOperation(insn, value1.getBasicValue(), value2.getBasicValue(), value3.getBasicValue()),
                    value1, value2, value3);
        }

        // INVOKEVIRTUAL, INVOKESPECIAL, INVOKESTATIC, INVOKEINTERFACE, MULTIANEWARRAY and
        // INVOKEDYNAMIC
        public JoinedValue naryOperation(AbstractInsnNode insn, List<? extends JoinedValue> values)
                throws AnalyzerException {
            // This is invoked at least twice per instruction...
            if (insn instanceof InvokeDynamicInsnNode) {
                // System.err.println("Invoked dynamic with " + values.size() + " arguments");
                dynamicArgs.add(new DynamicInvokeArgs((InvokeDynamicInsnNode) insn, values));
            }

            final List<SourceValue> sourceVals = new ArrayList<>();
            final List<BasicValue> basicVals = new ArrayList<>();
            for (final JoinedValue val : values) {
                sourceVals.add(val.getSourceValue());
                basicVals.add(val.getBasicValue());
            }
            return JoinedValue.derive(
                    source.naryOperation(insn, sourceVals),
                    basic.naryOperation(insn, basicVals),
                    values);
        }

        // IRETURN, LRETURN, FRETURN, DRETURN, ARETURN
        public void returnOperation(AbstractInsnNode insn, JoinedValue value, JoinedValue expected)
                throws AnalyzerException {
            source.returnOperation(insn, value.getSourceValue(), expected.getSourceValue());
            basic.returnOperation(insn, value.getBasicValue(), expected.getBasicValue());
        }

        public JoinedValue merge(JoinedValue value1, JoinedValue value2) {
            return JoinedValue.derive(
                    source.merge(value1.getSourceValue(), value2.getSourceValue()),
                    basic.merge(value1.getBasicValue(), value2.getBasicValue()),
                    value1, value2);
        }
    }

    static class DynamicInvokeArgs {
        final InvokeDynamicInsnNode insn;
        final List<? extends JoinedValue> values;

        DynamicInvokeArgs(InvokeDynamicInsnNode insn, List<? extends JoinedValue> values) {
            this.insn = insn;
            this.values = List.copyOf(values);
        }
    }
}
