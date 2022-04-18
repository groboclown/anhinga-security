// Released under the MIT license.  See the LICENSE file for details.
package net.groboclown.anhinga.analysis.inspection;

import javax.annotation.Nonnull;
import java.util.HashSet;
import java.util.Set;

public class MockClassRepository implements ClassRepository {
    public final Set<String> marked = new HashSet<>();

    @Override
    public void markReferencedClass(@Nonnull String className) {
        marked.add(className);
    }
}
