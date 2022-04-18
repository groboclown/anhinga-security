// Released under the MIT license.  See the LICENSE file for details.
package net.groboclown.anhinga.analysis.inspection;

import net.groboclown.anhinga.analysis.model.ClassTrace;
import net.groboclown.retval.RetVal;

import javax.annotation.Nonnull;
import java.io.File;
import java.util.Collection;

/**
 * Inspects a Java jar file.
 */
public interface JarInspector {
    /**
     * Inspects the class files contained in the jar file.
     *
     * @param file the Jar file on disk.
     * @return the traces for call contained class files.
     */
    @Nonnull
    RetVal<Collection<ClassTrace>> inspectJar(
            @Nonnull final File file,
            @Nonnull final ClassInspector classInspector,
            @Nonnull final MethodInspector methodInspector,
            @Nonnull final ClassRepository repository);
}
