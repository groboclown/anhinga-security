// Released under the MIT license.  See the LICENSE file for details.
package net.groboclown.anhinga.analysis.model;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.List;

/**
 * Defines an invocation of an API.
 */
public class EndpointInvocation {

    public static class ArgumentValue {
        private final String sourceClassName;
        private final String sourceFieldName;
        private final Object sourceConstant;

        // TODO fill this in
        private final String sourceFile = null;
        private final int lineNo = -1;

        public ArgumentValue(String sourceClassName, String sourceFieldName) {
            this.sourceClassName = sourceClassName;
            this.sourceFieldName = sourceFieldName;
            this.sourceConstant = null;
        }

        public ArgumentValue(Object sourceConstant) {
            this.sourceClassName = null;
            this.sourceFieldName = null;
            this.sourceConstant = sourceConstant;
        }
    }
    public static class InvokedArgument {
        private final List<ArgumentValue> sourceValues;

        public InvokedArgument(@Nonnull final List<ArgumentValue> sourceValues) {
            this.sourceValues = List.copyOf(sourceValues);
        }
    }

    private final String className;
    private final String methodName;
    private final String methodSignature;
    private final InvokedArgument objectRef;
    private final List<InvokedArgument> arguments;

    public EndpointInvocation(
            @Nonnull final String className, @Nonnull final String methodName,
            @Nonnull final String methodSignature,
            @Nullable final InvokedArgument objectRef,
            @Nonnull final List<InvokedArgument> arguments) {
        this.className = className;
        this.methodName = methodName;
        this.methodSignature = methodSignature;
        this.objectRef = objectRef;
        this.arguments = List.copyOf(arguments);
    }

    public String getClassName() {
        return className;
    }

    public String getMethodName() {
        return methodName;
    }

    public String getMethodSignature() {
        return methodSignature;
    }

    public InvokedArgument getObjectRef() {
        return objectRef;
    }

    public List<InvokedArgument> getArguments() {
        return arguments;
    }
}
