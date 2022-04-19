// Released under the MIT license.  See the LICENSE file for details.
package net.groboclown.anhinga.analysis.inspection.impl;

import net.groboclown.anhinga.analysis.inspection.ClassInspector;
import net.groboclown.anhinga.analysis.inspection.ClassRepository;
import net.groboclown.anhinga.analysis.inspection.JarInspector;
import net.groboclown.anhinga.analysis.inspection.MethodInspector;
import net.groboclown.anhinga.analysis.model.ClassTrace;
import net.groboclown.retval.ProblemCollector;
import net.groboclown.retval.Ret;
import net.groboclown.retval.RetVal;
import net.groboclown.retval.problems.FileProblem;

import javax.annotation.Nonnull;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Enumeration;
import java.util.List;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;

/**
 * Inspects a Java jar file.
 */
public class DefaultJarInspector implements JarInspector {
    @Override
    @Nonnull
    public RetVal<Collection<ClassTrace>> inspectJar(
            @Nonnull final File file,
            @Nonnull final ClassInspector classInspector,
            @Nonnull final MethodInspector methodInspector,
            @Nonnull final ClassRepository repository) {
        final List<ClassTrace> ret = new ArrayList<>();
        final ProblemCollector problems = Ret.collectProblems();
        try (JarFile jarFile = new JarFile(file, true, JarFile.OPEN_READ)) {
            final Enumeration<JarEntry> entries = jarFile.entries();
            while (entries.hasMoreElements()) {
                final JarEntry entry = entries.nextElement();
                if (isClassFile(entry)) {
                    // The jar can contain duplicate entries if there are
                    // multiple versions.  See multi-versioned jar files for more
                    // information.
System.err.println("Analyzing " + entry.getName() + " (" + entry.getSize() + " bytes)");
                    final RetVal<ClassTrace> res = classInspector.inspectClassStream(
                            entry.getName(), jarFile.getInputStream(entry),
                            methodInspector, repository);
                    problems.with(res, ret::add);
                }
            }
        } catch (IOException e) {
            problems.add(FileProblem.from(file, e));
        }
        return problems.thenValue(() -> ret);
    }

    private boolean isClassFile(final JarEntry entry) {
        if (entry.isDirectory()) {
            return false;
        }
        if (entry.getSize() <= 0) {
            return false;
        }
        return entry.getName().endsWith(".class");
    }
}
