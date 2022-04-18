// Released under the MIT license.  See the LICENSE file for details.
package net.groboclown.anhinga.analysis.model;

import javax.annotation.Nonnull;

public class FieldTrace {
    public static final String UNKNOWN_FIELD_TYPE = "[UNKNOWN]";

    private final String owningClassName;
    private final String fieldName;
    private String type;
    private Object defaultValue;

    public FieldTrace(
            @Nonnull String owningClassName, @Nonnull String fieldName, @Nonnull String type) {
        this.owningClassName = owningClassName;
        this.fieldName = fieldName;
        this.type = type;
    }

    void updateFieldType(@Nonnull String fieldType) {
        if (UNKNOWN_FIELD_TYPE.equals(type)) {
            this.type = fieldType;
            return;
        }
        if (this.type.equals(fieldType)) {
            return;
        }
        throw new IllegalStateException("Field already has assigned type " + type +
                "; requested to change to " + fieldType);
    }

    public Object getDefaultValue() {
        return defaultValue;
    }

    public void setDefaultValue(Object defaultValue) {
        this.defaultValue = defaultValue;
    }

    public String getFieldName() {
        return fieldName;
    }

    public String getOwningClassName() {
        return owningClassName;
    }

    public String getType() {
        return type;
    }
}
