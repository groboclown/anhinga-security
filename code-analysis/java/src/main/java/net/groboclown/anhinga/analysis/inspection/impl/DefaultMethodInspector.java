// Released under the MIT license.  See the LICENSE file for details.
package net.groboclown.anhinga.analysis.inspection.impl;

import net.groboclown.anhinga.analysis.inspection.ClassRepository;
import net.groboclown.anhinga.analysis.inspection.MethodInspector;
import net.groboclown.anhinga.analysis.model.EndpointInvocation;
import net.groboclown.anhinga.analysis.model.FieldSet;
import net.groboclown.anhinga.analysis.model.MethodTrace;
import net.groboclown.retval.ProblemCollector;
import net.groboclown.retval.Ret;
import net.groboclown.retval.RetNullable;
import net.groboclown.retval.RetVal;
import org.objectweb.asm.tree.*;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.List;


public class DefaultMethodInspector implements MethodInspector {
    @Nonnull
    public RetVal<MethodTrace> inspectMethod(
            @Nonnull final String ownerClass,
            @Nonnull final MethodNode method, @Nonnull final FieldSet fields,
            @Nonnull final ClassRepository repository) {
        return InvocationInspector
                .runInspections(ownerClass, method)
            .then((i) -> createMethodTrace(i, fields, repository));
    }

    @Nonnull
    private RetVal<MethodTrace> createMethodTrace(
            @Nonnull final Inspection inspection,
            @Nonnull final FieldSet fields,
            @Nonnull final ClassRepository repository) {
        return inspectInvocations(inspection, fields, repository)
                .map((invokes) ->
            new MethodTrace(
                inspection.getMethodName(), inspection.getMethodSignature(),
                inspection.getArguments(), invokes));
    }

    private RetVal<List<EndpointInvocation>> inspectInvocations(
            @Nonnull final Inspection inspection,
            @Nonnull final FieldSet fields,
            @Nonnull final ClassRepository repository) {
        final ProblemCollector problems = Ret.collectProblems();
        final List<EndpointInvocation> ret = new ArrayList<>();
        for (final InvocationArguments inv : inspection.getInvocations()) {
            problems.with(
                    analyzeInvocation(inv, inspection, fields, repository),
                    ret::add);
        }
        return problems.thenValue(() -> ret);
    }

    private RetVal<EndpointInvocation> analyzeInvocation(
            @Nonnull final InvocationArguments inv,
            @Nonnull final Inspection inspection,
            @Nonnull final FieldSet fields,
            @Nonnull final ClassRepository repository) {
        if (inv.method != null) {
            return analyzeInvocationArguments(inv, inspection, fields, repository)
                    .map((args) ->
                new EndpointInvocation(
                    inv.method.owner, inv.method.name, inv.method.desc,
                    args.objectRef, args.args));
        } else if (inv.dynamic != null) {
            return analyzeDynamicInvocationArguments(inv, inspection, fields, repository)
                    .map((args) ->
                new EndpointInvocation(
                    inspection.getClassName(), inv.dynamic.name, inv.dynamic.desc, null, args));
        }
        throw new IllegalStateException("Both method and dynamic are null");
    }

    private RetVal<InvokeValues> analyzeInvocationArguments(
            @Nonnull final InvocationArguments inv,
            @Nonnull final Inspection inspection,
            @Nonnull final FieldSet fields,
            @Nonnull final ClassRepository repository) {
        final InvokeValues ret = new InvokeValues();
        final ProblemCollector problems = Ret.collectProblems();
        problems.with(
                analyzeInvocationArgument(inv, inv.objectRef, inspection, fields, repository),
                ret::setObjectRef);
        inv.arguments.forEach((a) ->
                problems.with(
                        analyzeInvocationArgument(inv, a, inspection, fields, repository),
                        ret.args::add));
        return problems.thenValue(() -> ret);
    }

    private RetVal<List<EndpointInvocation.InvokedArgument>> analyzeDynamicInvocationArguments(
            @Nonnull final InvocationArguments inv,
            @Nonnull final Inspection inspection,
            @Nonnull final FieldSet fields,
            @Nonnull final ClassRepository repository) {
        final List<EndpointInvocation.InvokedArgument> ret = new ArrayList<>();
        final ProblemCollector problems = Ret.collectProblems();
        inv.arguments.forEach((a) ->
                problems.with(
                        analyzeInvocationArgument(inv, a, inspection, fields, repository),
                        ret::add));
        return problems.thenValue(() -> ret);
    }

    @Nonnull
    private RetNullable<EndpointInvocation.InvokedArgument> analyzeInvocationArgument(
            @Nonnull final InvocationArguments inv,
            @Nullable final JoinedValue value,
            @Nonnull final Inspection inspection,
            @Nonnull final FieldSet fields,
            @Nonnull final ClassRepository repository) {
        if (value == null) {
            return RetNullable.ok(null);
        }
        final List<EndpointInvocation.ArgumentValue> sourceValues = new ArrayList<>();
        for (final AbstractInsnNode insn: value.getSourceValue().insns) {
            if (insn instanceof FieldInsnNode) {
                // field
                final FieldInsnNode field = (FieldInsnNode) insn;
                fields.setFieldType(field.owner, field.name, field.desc);
                repository.markReferencedClass(field.owner);
                sourceValues.add(new EndpointInvocation.ArgumentValue(
                        field.owner, field.name));
            } else if (insn instanceof LdcInsnNode) {
                // constant
                final LdcInsnNode ldc = (LdcInsnNode) insn;
                sourceValues.add(new EndpointInvocation.ArgumentValue(ldc.cst));
            }
            // Else ignore.
        }
        return RetNullable.ok(new EndpointInvocation.InvokedArgument(sourceValues));
    }

    static class InvokeValues {
        EndpointInvocation.InvokedArgument objectRef = null;
        List<EndpointInvocation.InvokedArgument> args = new ArrayList<>();

        void setObjectRef(@Nullable EndpointInvocation.InvokedArgument ref) {
            this.objectRef = ref;
        }
    }
}
