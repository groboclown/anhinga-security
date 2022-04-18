// Released under the MIT license.  See the LICENSE file for details.
package net.groboclown.anhinga.analysis.model;

import javax.annotation.Nonnull;
import java.util.List;

/**
 * Defines an invocation of an API.
 */
public class EndpointInvocation {

    public static class ArgumentValue {

    }
    public static class InvokedArgument {

    }

    private final String className;
    private final String methodName;
    private final List<InvokedArgument> arguments;

    public EndpointInvocation(@Nonnull String className, @Nonnull String methodName, @Nonnull List<InvokedArgument> arguments) {
        this.className = className;
        this.methodName = methodName;
        this.arguments = List.copyOf(arguments);
    }
}
