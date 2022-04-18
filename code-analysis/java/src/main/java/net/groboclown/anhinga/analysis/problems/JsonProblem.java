// Released under the MIT license.  See the LICENSE file for details.
package net.groboclown.anhinga.analysis.problems;

import com.google.gson.JsonParseException;
import net.groboclown.anhinga.analysis.util.CollectionHelper;
import net.groboclown.retval.SourcedProblem;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.Collection;


/**
 * A Problem that came from loading a JSON file.
 */
public class JsonProblem implements SourcedProblem, ThrowableProblem {
    private final String source;
    private final String localMessage;
    private final Throwable error;

    public static JsonProblem from(@Nonnull String source, @Nonnull JsonParseException e) {
        return new JsonProblem(source, e.getMessage(), e);
    }

    private JsonProblem(@Nonnull String source, @Nonnull String localMessage, @Nullable Throwable error) {
        this.source = source;
        this.localMessage = localMessage;
        this.error = error;
    }

    @Nonnull
    @Override
    public String getSource() {
        return source;
    }

    @Nonnull
    @Override
    public String localMessage() {
        return localMessage;
    }

    @Nullable
    public Throwable getError() {
        return error;
    }

    @Nonnull
    @Override
    public Collection<Throwable> getErrors() {
        return CollectionHelper.optionalSingleton(error);
    }
}
