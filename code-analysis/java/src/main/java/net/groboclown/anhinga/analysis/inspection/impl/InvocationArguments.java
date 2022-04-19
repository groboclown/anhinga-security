// Released under the MIT license.  See the LICENSE file for details.
package net.groboclown.anhinga.analysis.inspection.impl;

import org.objectweb.asm.Type;
import org.objectweb.asm.tree.InvokeDynamicInsnNode;
import org.objectweb.asm.tree.MethodInsnNode;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.Arrays;
import java.util.List;

class InvocationArguments {
    final int lineNumber;
    final InvokeDynamicInsnNode dynamic;
    final MethodInsnNode method;
    final JoinedValue objectRef;
    final List<? extends JoinedValue> arguments;
    final List<Type> knownArgumentTypes;

    InvocationArguments(
            int lineNumber,
            @Nonnull InvokeDynamicInsnNode dynamic, @Nonnull List<? extends JoinedValue> values) {
        // Invoke Dynamic means we can't tell in advance what will be called.
        this.lineNumber = lineNumber;
        this.dynamic = dynamic;
        this.method = null;
        this.objectRef = null;
        this.arguments = List.copyOf(values);
        this.knownArgumentTypes = null;
    }

    <T extends JoinedValue> InvocationArguments(
            int lineNumber,
            @Nonnull MethodInsnNode method,
            @Nullable T objectRef,
            @Nonnull List<T> arguments) {
        this.lineNumber = lineNumber;
        this.dynamic = null;
        this.method = method;
        this.objectRef = objectRef;
        this.arguments = List.copyOf(arguments);
        this.knownArgumentTypes = Arrays.asList(Type.getArgumentTypes(method.desc));

        // TODO for each argument, use the instruction list to map it to
        //   a line number.
    }
}
