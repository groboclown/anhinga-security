// Released under the MIT license.  See the LICENSE file for details.
package net.groboclown.anhinga.analysis.inspection;

import net.groboclown.anhinga.analysis.model.EndpointInvocation;
import net.groboclown.anhinga.analysis.model.FieldSet;
import net.groboclown.anhinga.analysis.model.MethodTrace;
import net.groboclown.retval.RetVal;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.tree.*;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.List;

public class DefaultMethodInspector implements MethodInspector {
    @Nonnull
    public RetVal<MethodTrace> inspectMethod(
            @Nonnull MethodNode method, @Nonnull FieldSet fields,
            @Nonnull ClassRepository repository) {

        final List<ValueInfluence> valueInfluence = new ArrayList<>();
        final List<EndpointInvocation> invocations = new ArrayList<>();
        int lineNumber = 0;
        for (final AbstractInsnNode ins : method.instructions) {
            if (ins instanceof LineNumberNode) {
                final LineNumberNode lineNode = (LineNumberNode) ins;
                lineNumber = lineNode.line;
            } else {
                handleInstruction(
                        lineNumber, isStatic(method),
                        ins, fields, repository, method.localVariables,
                        invocations, valueInfluence);
            }
        }

        return RetVal.ok(createMethodTrace(method, invocations));
    }

    @Nonnull
    private MethodTrace createMethodTrace(
            @Nonnull final MethodNode method,
            @Nonnull final List<EndpointInvocation> invocations) {
        final List<MethodTrace.Argument> arguments = new ArrayList<>();
        for (int i = 0; i < method.parameters.size(); i++) {
            final ParameterNode param = method.parameters.get(i);
            List<AnnotationNode> visibleAnnotations = findParameterAnnotations(i, method.visibleParameterAnnotations);
            List<AnnotationNode> invisibleAnnotations = findParameterAnnotations(i, method.invisibleParameterAnnotations);
            final String signature = findParameterSignature(method, i);
            arguments.add(new MethodTrace.Argument(param.name, i, signature));
        }
        return new MethodTrace(method.name, method.desc, arguments, invocations);
    }

    private void handleInstruction(
            final int lastLineNumber, final boolean isStatic,
            @Nonnull final AbstractInsnNode ins,
            @Nonnull final FieldSet fields,
            @Nonnull final ClassRepository repository,
            @Nonnull final List<LocalVariableNode> localVariables,
            @Nonnull final List<EndpointInvocation> invocations,
            @Nonnull final List<ValueInfluence> valueInfluence) {
        if (ins instanceof VarInsnNode) {
            final VarInsnNode vnode = (VarInsnNode) ins;
            if (!isStatic && vnode.var == 0) {
                // This reference.  Ignoring this for now.
            } else if (vnode.var >= localVariables.size()) {
                // Stack reference.
            } else {
                LocalVariableNode localVariable = localVariables.get(vnode.var);
                valueInfluence.add(new LocalVariableInfluence(ins, lastLineNumber, vnode.var, localVariable.name, localVariable.desc));
            }
            return;
        }
        if (ins instanceof FieldInsnNode) {
            final FieldInsnNode fnode = (FieldInsnNode) ins;
            repository.markReferencedClass(fnode.owner);
            fields.setFieldType(fnode.owner, fnode.name, fnode.desc);
            valueInfluence.add(new FieldValueInfluence(ins, lastLineNumber, fnode.owner, fnode.name));
            return;
        }
        if (ins instanceof InvokeDynamicInsnNode) {
            final InvokeDynamicInsnNode dnode = (InvokeDynamicInsnNode) ins;
            repository.markReferencedClass(dnode.bsm.getOwner());
            invocations.add(new EndpointInvocation(
                    // TODO find out the right way to parse a dynamic invocation.
                    dnode.bsm.getOwner(), dnode.name, List.of()
                    // Need to get the stack frame for this...
            ));
        }
        if (ins instanceof MethodInsnNode) {
            final MethodInsnNode mnode = (MethodInsnNode) ins;
            repository.markReferencedClass(mnode.owner);
            invocations.add(new EndpointInvocation(
                    mnode.owner, mnode.name, List.of()
                    // Need to get the stack frame for this...
                    // It also needs to pull from the "variable influence" list.
            ));
        }
        switch (ins.getOpcode()) {
            case Opcodes.INVOKEVIRTUAL:
            case Opcodes.INVOKESPECIAL:
            case Opcodes.INVOKESTATIC:
            case Opcodes.INVOKEINTERFACE:
            case Opcodes.INVOKEDYNAMIC:
            case Opcodes.NEW:


                break;
        }
    }

    private List<AnnotationNode> findParameterAnnotations(
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

    private String findParameterSignature(@Nonnull MethodNode method, int index) {
        // Parameters are encoded in the local variables.
        // If the method is not static, then the first parameter is the second local variable.
        final List<LocalVariableNode> localVariables = method.localVariables;

        if (!isStatic(method)) {
            index = index + 1;
        }
        if (index >= 0 && index < localVariables.size()) {
            return localVariables.get(index).desc;
        }

        // TODO return RetVal problem.
        return "[UNKNOWN]";
    }

    private boolean isStatic(@Nonnull MethodNode node) {
        return (node.access & Opcodes.ACC_STATIC) != 0;
    }

    static abstract class ValueInfluence {
        final AbstractInsnNode ins;
        final int lineNumber;

        protected ValueInfluence(AbstractInsnNode ins, int lineNumber) {
            this.ins = ins;
            this.lineNumber = lineNumber;
        }
    }

    static class FieldValueInfluence extends ValueInfluence {
        final String className;
        final String fieldName;

        FieldValueInfluence(AbstractInsnNode ins, int lineNumber, String className, String fieldName) {
            super(ins, lineNumber);
            this.className = className;
            this.fieldName = fieldName;
        }
    }

    static class LocalVariableInfluence extends ValueInfluence {
        final int index;
        final String name;
        final String desc;

        LocalVariableInfluence(AbstractInsnNode ins, int lineNumber, int index, String name, String desc) {
            super(ins, lineNumber);
            this.index = index;
            this.name = name;
            this.desc = desc;
        }
    }
}
