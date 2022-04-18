// Released under the MIT license.  See the LICENSE file for details.
package net.groboclown.anhinga.analysis.model;

import org.objectweb.asm.tree.ClassNode;
import org.objectweb.asm.tree.FieldNode;

import javax.annotation.Nonnull;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Keeps track of the fields on a class.  This can also include references
 * to parent class fields, but those are not declared initially.
 */
public class FieldSet {
    // Maps from class name -> field name -> field trace.
    private final Map<String, Map<String, FieldTrace>> fields = new HashMap<>();

    @Nonnull
    public static FieldSet fromClassNode(@Nonnull final ClassNode node) {
        final FieldSet ret = new FieldSet();

        for (final FieldNode field : node.fields) {
            FieldTrace fieldTrace = ret.getField(node.name, field.name);
            fieldTrace.updateFieldType(field.desc);
            fieldTrace.setDefaultValue(field.value);
        }
        return ret;
    }

    public void setFieldType(@Nonnull final String className, @Nonnull String fieldName, @Nonnull String fieldType) {
        getField(className, fieldName).updateFieldType(fieldType);
    }

    @Nonnull
    public FieldTrace getField(@Nonnull String className, @Nonnull String fieldName) {
        final Map<String, FieldTrace> classFields = fields.computeIfAbsent(
                className, k -> new HashMap<>());
        return classFields.computeIfAbsent(fieldName,
                k -> new FieldTrace(className, fieldName, FieldTrace.UNKNOWN_FIELD_TYPE));
    }

    @Nonnull
    public List<FieldTrace> getFields() {
        final List<FieldTrace> ret = new ArrayList<>();
        for (Map<String, FieldTrace> fieldMap : fields.values()) {
            ret.addAll(fieldMap.values());
        }
        return ret;
    }
}
