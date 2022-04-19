// Released under the MIT license.  See the LICENSE file for details.
package net.groboclown.anhinga.analysis.inspection.impl;

import net.groboclown.anhinga.analysis.model.MethodTrace;
import org.objectweb.asm.tree.MethodNode;

import javax.annotation.Nonnull;
import java.util.List;
import java.util.Objects;

public class Inspection {
    private final String className;
    private final MethodNode method;
    private final List<MethodTrace.Argument> arguments;
    private final List<InvocationArguments> invocations;

    Inspection(
            @Nonnull String className,
            @Nonnull MethodNode method,
            @Nonnull List<MethodTrace.Argument> arguments,
            @Nonnull List<InvocationArguments> invocations) {
        this.className = className;
        this.method = method;
        this.arguments = List.copyOf(arguments);
        this.invocations = List.copyOf(invocations);
    }

    @Nonnull
    public String getClassName() {
        return className;
    }

    @Nonnull
    public MethodNode getMethod() {
        return method;
    }

    @Nonnull
    public String getMethodName() {
        return Objects.requireNonNull(method.name);
    }

    @Nonnull
    public String getMethodSignature() {
        return Objects.requireNonNull(method.desc);
    }

    @Nonnull
    public List<MethodTrace.Argument> getArguments() {
        return arguments;
    }

    @Nonnull
    public List<InvocationArguments> getInvocations() {
        return invocations;
    }
}
