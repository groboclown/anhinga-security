// Released under the MIT license.  See the LICENSE file for details.
package net.groboclown.anhinga.analysis.inspection;

import net.groboclown.anhinga.analysis.model.ClassTrace;
import net.groboclown.retval.RetVal;
import org.objectweb.asm.tree.ClassNode;

import javax.annotation.Nonnull;
import java.io.InputStream;

/**
 * Inspects a Java class file for usage of API invocations, and traces their calls.
 * <p>
 * The inspected classes will need another pass over their contents to get a full
 * picture from the parent classes.
 */
public interface ClassInspector {
    /**
     * Inspect a class encoded in a byte stream.
     *
     * @param name name of the class stream
     * @param classStream stream of the class file bytes.
     * @return the trace
     */
    @Nonnull
    RetVal<ClassTrace> inspectClassStream(
            @Nonnull final String name,
            @Nonnull final InputStream classStream,
            @Nonnull final MethodInspector methodInspector,
            @Nonnull final ClassRepository repository);

    /**
     * Inspect the class file, stored in-memory as a byte array.
     *
     * @param classFile byte array of the class file contents.
     * @return the class trace.
     */
    @Nonnull
    RetVal<ClassTrace> inspectClassFile(
            @Nonnull final String name, @Nonnull final byte[] classFile,
            @Nonnull final MethodInspector methodInspector,
            @Nonnull final ClassRepository repository);

    /**
     * Inspect the parsed class tree.
     *
     * @param node the parsed class tree
     * @return the class trace.
     */
    @Nonnull
    RetVal<ClassTrace> inspectClassNode(
            @Nonnull final ClassNode node,
            @Nonnull final MethodInspector methodInspector,
            @Nonnull final ClassRepository repository);
}
