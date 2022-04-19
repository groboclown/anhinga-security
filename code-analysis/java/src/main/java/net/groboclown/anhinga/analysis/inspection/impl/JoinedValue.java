// Released under the MIT license.  See the LICENSE file for details.
package net.groboclown.anhinga.analysis.inspection.impl;

import org.objectweb.asm.tree.analysis.BasicValue;
import org.objectweb.asm.tree.analysis.BasicVerifier;
import org.objectweb.asm.tree.analysis.SourceValue;
import org.objectweb.asm.tree.analysis.Value;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;

class JoinedValue implements Value {
    private final SourceValue sourceValue;
    private final BasicValue basicValue;
    private final List<JoinedValue> mergedFrom;

    @Nullable
    static JoinedValue create(
            @Nullable final SourceValue sourceValue,
            @Nullable final BasicValue basicValue) {
        if (sourceValue == null && basicValue == null) {
            return null;
        }
        // basic is sometimes null
        return new JoinedValue(sourceValue, basicValue);
    }

    @Nullable
    static JoinedValue derive(
            @Nullable final SourceValue sourceValue,
            @Nullable final BasicValue basicValue,
            final JoinedValue... sources) {
        return derive(sourceValue, basicValue, Arrays.asList(sources));
    }

    @Nullable
    static JoinedValue derive(
            @Nullable final SourceValue sourceValue,
            @Nullable final BasicValue basicValue,
            final List<? extends JoinedValue> sources) {
        if (
                sources.size() == 1
                && Objects.equals(basicValue, sources.get(0).getBasicValue())
                && Objects.equals(sourceValue, sources.get(0).getSourceValue())
                && sources.get(0).getMergedFrom().isEmpty()) {
            // The source is this same value, so return the original.
            return sources.get(0);
        }
        if (sourceValue == null && basicValue == null) {
            return null;
        }
        // basic is sometimes null
        List<JoinedValue> sourceList = new ArrayList<>();
        for (final JoinedValue jv : sources) {
            if (jv != null) {
                sourceList.add(jv);
            }
        }
        return new JoinedValue(sourceValue, basicValue, sourceList);
    }

    private JoinedValue(
            @Nullable final SourceValue sourceValue,
            @Nullable final BasicValue basicValue) {
        this.sourceValue = sourceValue;
        this.basicValue = basicValue;
        this.mergedFrom = List.of();
    }

    private JoinedValue(
            @Nullable final SourceValue sourceValue,
            @Nullable final BasicValue basicValue,
            @Nonnull final List<? extends JoinedValue> sources) {
        this.sourceValue = sourceValue;
        this.basicValue = basicValue;
        this.mergedFrom = List.copyOf(sources);
    }

    @Override
    public int getSize() {
        return basicValue.getSize();
    }

    @Nonnull
    public SourceValue getSourceValue() {
        return sourceValue;
    }

    @Nonnull
    public BasicValue getBasicValue() {
        return basicValue;
    }

    @Override
    public boolean equals(final Object value) {
        if (value == this) {
            return true;
        }
        if (!(value instanceof JoinedValue)) {
            return false;
        }
        final JoinedValue joinedValue = (JoinedValue) value;
        return
                Objects.equals(joinedValue.basicValue, basicValue)
                && Objects.equals(joinedValue.sourceValue, sourceValue);
    }

    @Override
    public int hashCode() {
        if (sourceValue != null) {
            return sourceValue.hashCode();
        }
        return basicValue.hashCode();
    }

    public List<JoinedValue> getMergedFrom() {
        return mergedFrom;
    }
}
