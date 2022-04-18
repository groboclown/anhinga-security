// Released under the MIT license.  See the LICENSE file for details.
package net.groboclown.anhinga.analysis.problems;

import net.groboclown.retval.Problem;

import javax.annotation.Nonnull;
import java.util.Collection;

/**
 * A problem that includes an error.
 */
public interface ThrowableProblem extends Problem {
    /**
     * The underlying errors that caused the problem.
     *
     * @return the problem errors.
     */
    @Nonnull
    Collection<Throwable> getErrors();
}
