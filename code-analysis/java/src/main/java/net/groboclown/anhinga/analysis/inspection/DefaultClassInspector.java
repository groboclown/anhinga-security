// Released under the MIT license.  See the LICENSE file for details.
package net.groboclown.anhinga.analysis.inspection;

import net.groboclown.anhinga.analysis.model.ClassTrace;
import net.groboclown.anhinga.analysis.model.FieldSet;
import net.groboclown.anhinga.analysis.model.MethodTrace;
import net.groboclown.anhinga.analysis.problems.ClassFormatProblem;
import net.groboclown.retval.ProblemCollector;
import net.groboclown.retval.Ret;
import net.groboclown.retval.RetVal;
import org.objectweb.asm.ClassReader;
import org.objectweb.asm.tree.ClassNode;
import org.objectweb.asm.tree.MethodNode;

import javax.annotation.Nonnull;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

/**
 * Inspects a Java class file for usage of API invocations, and traces their calls.
 * <p>
 * The inspected classes will need another pass over their contents to get a full
 * picture from the parent classes.
 */
public class DefaultClassInspector implements ClassInspector {
    @Override
    @Nonnull
    public RetVal<ClassTrace> inspectClassStream(
            @Nonnull final String name,
            @Nonnull final InputStream classStream,
            @Nonnull final MethodInspector methodInspector,
            @Nonnull final ClassRepository repository) {
        final ClassNode node;
        try {
            final ClassReader reader = new ClassReader(classStream);
            node = new ClassNode();
            reader.accept(node, ClassReader.EXPAND_FRAMES);
        } catch (Exception e) {
            return RetVal.fromProblem(ClassFormatProblem.fromError(name, e));
        }
        return inspectClassNode(node, methodInspector, repository);
    }

    @Override
    @Nonnull
    public RetVal<ClassTrace> inspectClassFile(
            @Nonnull final String name, @Nonnull final byte[] classFile,
            @Nonnull final MethodInspector methodInspector,
            @Nonnull final ClassRepository repository) {
        final ClassNode node;
        try {
            final ClassReader reader = new ClassReader(classFile);
            node = new ClassNode();
            reader.accept(node, ClassReader.EXPAND_FRAMES);
        } catch (RuntimeException e) {
            return RetVal.fromProblem(ClassFormatProblem.fromError(name, e));
        }
        return inspectClassNode(node, methodInspector, repository);
    }

    @Override
    @Nonnull
    public RetVal<ClassTrace> inspectClassNode(
            @Nonnull final ClassNode node,
            @Nonnull final MethodInspector methodInspector,
            @Nonnull final ClassRepository repository) {
        final ProblemCollector problems = Ret.collectProblems();
        final List<MethodTrace> methods = new ArrayList<>();
        final FieldSet fields = FieldSet.fromClassNode(node);
        for (final MethodNode method: node.methods) {
            final RetVal<MethodTrace> res = methodInspector.inspectMethod(
                    method, fields, repository);
            problems.with(res, methods::add);
        }
        return problems.thenValue(() ->
            new ClassTrace(
                node.name,
                node.superName,
                methods,
                fields.getFields()));
    }
}
