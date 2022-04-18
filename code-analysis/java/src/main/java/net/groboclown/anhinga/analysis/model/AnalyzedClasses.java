// Released under the MIT license.  See the LICENSE file for details.
package net.groboclown.anhinga.analysis.model;

import javax.annotation.Nonnull;
import java.util.List;
import java.util.Set;


/**
 * A complete description of analyzed classes.
 */
public interface AnalyzedClasses {
    /**
     * Get the full trace of all analyzed classes.
     * @return
     */
    @Nonnull
    List<ClassTrace> getAnalyzedClasses();

    /**
     * Get class names that were referenced but not analyzed.
     * @return
     */
    @Nonnull
    Set<String> getReferencedClassNames();
}
