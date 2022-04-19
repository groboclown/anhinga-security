package net.groboclown.anhinga.analysis.inspection.impl;

import net.groboclown.anhinga.analysis.inspection.ClassPathInspector;
import net.groboclown.anhinga.analysis.inspection.MockClassRepository;
import net.groboclown.anhinga.analysis.ResourceUtil;
import net.groboclown.anhinga.analysis.model.EndpointInvocation;
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
        final byte[] classFile = ResourceUtil.getResourceAsBytes(ClassPathInspector.class, "SocketAppender.class");
        final ClassReader reader = new ClassReader(classFile);
        final ClassNode classNode = new ClassNode();
        reader.accept(classNode, ClassReader.EXPAND_FRAMES);
        final MethodNode node = findMethodNode(classNode, "stop", "(JLjava/util/concurrent/TimeUnit;)Z");
        final DefaultMethodInspector inspector = new DefaultMethodInspector();
        final FieldSet fieldSet = FieldSet.fromClassNode(classNode);
        final MockClassRepository repository = new MockClassRepository();
        RetVal<MethodTrace> res = inspector.inspectMethod(classNode.name, node, fieldSet, repository);
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

    // This method is complex and can cause out-of-memory issues if
    // the JoinedValue's "equals" is not implemented right.
    @Test
    void inspectMethodComplex() {
        final byte[] classFile = ResourceUtil.getResourceAsBytes(ClassPathInspector.class, "CompositeAction.class");
        final ClassReader reader = new ClassReader(classFile);
        final ClassNode classNode = new ClassNode();
        reader.accept(classNode, ClassReader.EXPAND_FRAMES);
        final MethodNode node = findMethodNode(classNode, "execute", "()Z");
        final DefaultMethodInspector inspector = new DefaultMethodInspector();
        final FieldSet fieldSet = FieldSet.fromClassNode(classNode);
        final MockClassRepository repository = new MockClassRepository();
        RetVal<MethodTrace> res = inspector.inspectMethod(classNode.name, node, fieldSet, repository);
        assertEquals(List.of(), res.anyProblems());
        final MethodTrace trace = res.result();
        assertEquals("execute", trace.getName());
        assertEquals("()Z", trace.getSignature());
        assertEquals(0, trace.getArguments().size());

        assertEquals(2, trace.getEndpointInvocations().size());
        {
            final EndpointInvocation invoke0 = trace.getEndpointInvocations().get(0);
            assertEquals("org/apache/logging/log4j/core/appender/rolling/action/Action", invoke0.getClassName());
            assertEquals("execute", invoke0.getMethodName());
            assertEquals("()Z", invoke0.getMethodSignature());
        }

        {
            final EndpointInvocation invoke1 = trace.getEndpointInvocations().get(1);
            assertEquals("org/apache/logging/log4j/core/appender/rolling/action/Action", invoke1.getClassName());
            assertEquals("execute", invoke1.getMethodName());
            assertEquals("()Z", invoke1.getMethodSignature());
        }
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