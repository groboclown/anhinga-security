// Released under the MIT license.  See the LICENSE file for details.
package net.groboclown.anhinga.analysis.problems;

import net.groboclown.anhinga.analysis.util.CollectionHelper;
import net.groboclown.retval.SourcedProblem;
import org.objectweb.asm.tree.MethodNode;
import org.objectweb.asm.tree.analysis.AnalyzerException;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.Collection;

public class AnalysisProblem implements SourcedProblem, ThrowableProblem {
    private final String source;
    private final String analysisIssue;
    private final Throwable error;

    public static AnalysisProblem fromMethodAnalysis(
            @Nonnull String owningClassName,
            @Nonnull MethodNode method,
            @Nonnull AnalyzerException err) {
        return new AnalysisProblem(
                owningClassName + '#' + method.name + method.desc,
                err.getMessage(),
                err);
    }

    public static AnalysisProblem fromDiscovery(
            @Nonnull String owningClassName,
            @Nonnull MethodNode method,
            @Nonnull String localProblem) {
        return new AnalysisProblem(
                owningClassName + '#' + method.name + method.desc,
                localProblem, null);
    }

    private AnalysisProblem(
            @Nonnull String source,
            @Nonnull String analysisIssue,
            @Nullable Throwable error) {
        this.source = source;
        this.analysisIssue = analysisIssue;
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
        return analysisIssue;
    }

    @Nonnull
    @Override
    public Collection<Throwable> getErrors() {
        return CollectionHelper.optionalSingleton(error);
    }
}
