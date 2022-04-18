// Released under the MIT license.  See the LICENSE file for details.
package net.groboclown.anhinga.analysis.inspection;

import javax.annotation.Nonnull;

public interface ClassRepository {
    /**
     * Record that something referenced a specific class.
     * @param className name of the referenced class
     */
    void markReferencedClass(@Nonnull String className);
}
