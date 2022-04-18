// Released under the MIT license.  See the LICENSE file for details.
package net.groboclown.anhinga.analysis.inspection;

import net.groboclown.anhinga.analysis.model.FieldSet;
import net.groboclown.anhinga.analysis.model.MethodTrace;
import net.groboclown.retval.RetVal;
import org.objectweb.asm.tree.MethodNode;

import javax.annotation.Nonnull;

/**
 * Inspects a method for the complete trace of the method.  Will also indicate
 * class fields referenced that might be in other classes.
 */
public interface MethodInspector {
    @Nonnull
    RetVal<MethodTrace> inspectMethod(
            @Nonnull MethodNode method, @Nonnull FieldSet fields,
            @Nonnull ClassRepository repository);
}
