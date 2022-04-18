// Released under the MIT license.  See the LICENSE file for details.
package net.groboclown.anhinga.analysis.util;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.Collection;
import java.util.Collections;
import java.util.Optional;

/**
 * Helper functions with collections.
 */
public class CollectionHelper {
    /**
     * Returns a collection that contains zero items if the value is null, or
     * one item if it is not null.
     *
     * @param value optional value.
     * @return a zero or one element item.
     * @param <T> type
     */
    public static <T> Collection<T> optionalSingleton(@Nullable T value) {
        if (value == null) {
            return Collections.emptyList();
        }
        return Collections.singleton(value);
    }

    /**
     * Returns a collection that contains zero items if the value is null, or
     * one item if it is not null.
     *
     * @param value optional value.
     * @return a zero or one element item.
     * @param <T> type
     */
    public static <T> Collection<T> optionalSingleton(@Nonnull Optional<T> value) {
        if (value.isEmpty()) {
            return Collections.emptyList();
        }
        return Collections.singleton(value.get());
    }
}
