// Released under the MIT license.  See the LICENSE file for details.
package net.groboclown.anhinga.analysis.model;

import javax.annotation.Nonnull;
import java.util.List;

public class MethodTrace {

    public static class Argument {
        private final String name;
        private final int index;
        private final String type;

        public Argument(String name, int index, String type) {
            this.name = name;
            this.index = index;
            this.type = type;
        }

        public String getName() {
            return name;
        }

        public int getIndex() {
            return index;
        }

        public String getType() {
            return type;
        }
    }

    private final String name;
    private final String signature;
    private final List<Argument> arguments;

    private final List<EndpointInvocation> endpointInvocations;

    public MethodTrace(
            @Nonnull String name,
            @Nonnull String signature,
            @Nonnull List<Argument> arguments,
            @Nonnull List<EndpointInvocation> endpointInvocations) {
        this.name = name;
        this.signature = signature;
        this.arguments = List.copyOf(arguments);
        this.endpointInvocations = List.copyOf(endpointInvocations);
    }

    public String getName() {
        return name;
    }

    public String getSignature() {
        return signature;
    }

    public List<Argument> getArguments() {
        return arguments;
    }
}
