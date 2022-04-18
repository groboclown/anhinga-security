// Released under the MIT license.  See the LICENSE file for details.
package net.groboclown.anhinga.analysis.problems;

import net.groboclown.anhinga.analysis.util.CollectionHelper;
import net.groboclown.retval.SourcedProblem;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.Collection;


/**
 * A problem with a class file format.
 */
public class ClassFormatProblem implements SourcedProblem, ThrowableProblem {
    private final String classFile;
    private final Exception error;
    private final String message;

    public static ClassFormatProblem fromError(
            @Nonnull String classFile, @Nonnull Exception error) {
        return new ClassFormatProblem(classFile, error.getMessage(), error);
    }

    private ClassFormatProblem(
            @Nonnull String classFile,
            @Nonnull String message,
            @Nullable Exception error) {
        this.classFile = classFile;
        this.error = error;
        this.message = message;
    }

    @Nonnull
    @Override
    public String getSource() {
        return classFile;
    }

    @Nonnull
    @Override
    public String localMessage() {
        return message;
    }

    @Nonnull
    @Override
    public Collection<Throwable> getErrors() {
        return CollectionHelper.optionalSingleton(error);
    }
}
