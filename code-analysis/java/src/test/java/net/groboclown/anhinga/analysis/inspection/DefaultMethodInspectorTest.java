package net.groboclown.anhinga.analysis.inspection;

import net.groboclown.anhinga.analysis.model.FieldSet;
import net.groboclown.anhinga.analysis.model.MethodTrace;
import net.groboclown.retval.RetVal;
import org.junit.jupiter.api.Test;
import org.objectweb.asm.ClassReader;
import org.objectweb.asm.tree.ClassNode;
import org.objectweb.asm.tree.MethodNode;

import javax.annotation.Nonnull;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

// Released under the MIT license.  See the LICENSE file for details.
class DefaultMethodInspectorTest {
    @Test
    void inspectMethod() {
        final byte[] classFile = ResourceUtil.getResourceAsBytes("SocketAppender.class");
        final ClassReader reader = new ClassReader(classFile);
        final ClassNode classNode = new ClassNode();
        reader.accept(classNode, ClassReader.EXPAND_FRAMES);
        final MethodNode node = findMethodNode(classNode, "stop", "(JLjava/util/concurrent/TimeUnit;)Z");
        final DefaultMethodInspector inspector = new DefaultMethodInspector();
        final FieldSet fieldSet = FieldSet.fromClassNode(classNode);
        final MockClassRepository repository = new MockClassRepository();
        RetVal<MethodTrace> res = inspector.inspectMethod(node, fieldSet, repository);
        assertEquals(List.of(), res.anyProblems());
        final MethodTrace trace = res.result();
        assertEquals("stop", trace.getName());
        assertEquals("(JLjava/util/concurrent/TimeUnit;)Z", trace.getSignature());
        assertEquals(2, trace.getArguments().size());
        final MethodTrace.Argument arg0 = trace.getArguments().get(0);
        assertEquals("timeout", arg0.getName());
        assertEquals(0, arg0.getIndex());
        assertEquals("J", arg0.getType());
        final MethodTrace.Argument arg1 = trace.getArguments().get(1);
        assertEquals("timeUnit", arg1.getName());
        assertEquals(1, arg1.getIndex());
        assertEquals("Ljava/util/concurrent/TimeUnit;", arg1.getType());
    }

    @Nonnull
    MethodNode findMethodNode(
            @Nonnull final ClassNode node, @Nonnull final String name, @Nonnull final String sig) {
        for (MethodNode mn : node.methods) {
            if (mn.name.equals(name) && mn.desc.equals(sig)) {
                return mn;
            }
        }
        throw new IllegalStateException("No such method " + name + sig);
    }
}