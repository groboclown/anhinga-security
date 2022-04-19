// Released under the MIT license.  See the LICENSE file for details.
package net.groboclown.anhinga.analysis.inspection;

import net.groboclown.anhinga.analysis.inspection.impl.DefaultClassInspector;
import net.groboclown.anhinga.analysis.inspection.impl.DefaultJarInspector;
import net.groboclown.anhinga.analysis.inspection.impl.DefaultMethodInspector;
import net.groboclown.anhinga.analysis.model.AnalyzedClasses;
import net.groboclown.anhinga.analysis.model.ClassTrace;
import net.groboclown.retval.ProblemCollector;
import net.groboclown.retval.Ret;
import net.groboclown.retval.RetVal;
import net.groboclown.retval.problems.FileProblem;

import javax.annotation.Nonnull;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.*;

/**
 * Inspects a full classpath.
 */
public class ClassPathInspector {
    public RetVal<AnalyzedClasses> analyzeClassPath(@Nonnull List<File> classPath) {
        return analyzeClassPath(classPath,
                new DefaultClassInspector(),
                new DefaultJarInspector(),
                new DefaultMethodInspector());
    }


    public RetVal<AnalyzedClasses> analyzeClassPath(
            @Nonnull List<File> classPath,
            @Nonnull ClassInspector classInspector,
            @Nonnull JarInspector jarInspector,
            @Nonnull MethodInspector methodInspector) {
        final Repository repo = new Repository();
        final ProblemCollector problems = Ret.collectProblems();
        for (final File item : loadJarsClasses(classPath)) {
            if (item.getName().endsWith(".class")) {
                try (InputStream inp = new FileInputStream(item)) {
                    problems.with(
                            classInspector.inspectClassStream(item.getPath(), inp, methodInspector, repo),
                            repo::addClass
                    );
                } catch (IOException e) {
                    problems.add(FileProblem.from(item, e));
                }
            } else if (item.getName().endsWith(".jar")) {
                problems.with(
                        jarInspector.inspectJar(item, classInspector, methodInspector, repo),
                        repo::addClasses
                );
            }
        }
        return problems.thenValue(() -> repo);
    }


    static class Repository implements ClassRepository, AnalyzedClasses {
        private final Set<String> referenced = new HashSet<>();
        private final Map<String, ClassTrace> classes = new HashMap<>();

        @Override
        public void markReferencedClass(@Nonnull String className) {
            if (!classes.containsKey(className)) {
                referenced.add(className);
            }
        }

        public void addClasses(@Nonnull Collection<ClassTrace> classTraces) {
            for (final ClassTrace ct : classTraces) {
                addClass(ct);
            }
        }

        public void addClass(@Nonnull ClassTrace classTrace) {
            if (classes.containsKey(classTrace.getName())) {
                throw new IllegalStateException("Already added trace for " + classTrace.getName());
            }
            classes.put(classTrace.getName(), classTrace);
        }

        @Override
        @Nonnull
        public List<ClassTrace> getAnalyzedClasses() {
            return List.copyOf(classes.values());
        }

        @Override
        @Nonnull
        public Set<String> getReferencedClassNames() {
            return Set.copyOf(referenced);
        }
    }


    private List<File> loadJarsClasses(@Nonnull final List<File> classPath) {
        final List<File> ret = new ArrayList<>();
        for (final File item : classPath) {
            if (item.isFile() && item.getName().endsWith(".jar")) {
                ret.add(item);
            } else if (item.isDirectory()) {
                ret.addAll(findClassesIn(item));
            }
        }
        return ret;
    }


    private List<File> findClassesIn(@Nonnull final File rootPath) {
        final List<File> stack = new LinkedList<>();
        final List<File> classFiles = new ArrayList<>();
        stack.add(rootPath);
        while (!stack.isEmpty()) {
            final File next = stack.remove(0);
            if (next.isDirectory()) {
                final File[] children = next.listFiles();
                if (children != null) {
                    for (final File child : children) {
                        if (child.isDirectory()) {
                            stack.add(child);
                        } else if (child.isFile() && child.getName().endsWith(".class")) {
                            classFiles.add(child);
                        }
                    }
                }
            }
        }
        return classFiles;
    }
}
