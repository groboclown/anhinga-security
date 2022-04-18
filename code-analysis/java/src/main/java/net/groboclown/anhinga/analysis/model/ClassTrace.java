// Released under the MIT license.  See the LICENSE file for details.
package net.groboclown.anhinga.analysis.model;

import javax.annotation.Nonnull;
import java.util.List;

/**
 * Records how code is invoked through a class file.
 */
public class ClassTrace {
    private final String name;
    private final String superName;
    private final List<MethodTrace> methods;

    private final List<FieldTrace> fields;

    public ClassTrace(
            @Nonnull String name,
            @Nonnull String superName,
            @Nonnull List<MethodTrace> methods,
            @Nonnull List<FieldTrace> fields) {
        this.name = name;
        this.superName = superName;
        this.methods = List.copyOf(methods);
        this.fields = List.copyOf(fields);
    }

    @Nonnull
    public String getName() {
        return name;
    }

    @Nonnull
    public String getSuperName() {
        return superName;
    }

    @Nonnull
    public List<MethodTrace> getMethods() {
        return methods;
    }

    public List<FieldTrace> getFields() {
        return fields;
    }
}
