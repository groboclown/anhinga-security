// Released under the MIT license.  See the LICENSE file for details.
package net.groboclown.anhinga.analysis.inspection.impl;

import net.groboclown.anhinga.analysis.inspection.ClassPathInspector;
import net.groboclown.anhinga.analysis.inspection.MethodInspector;
import net.groboclown.anhinga.analysis.inspection.MockClassRepository;
import net.groboclown.anhinga.analysis.ResourceUtil;
import net.groboclown.anhinga.analysis.model.ClassTrace;
import net.groboclown.anhinga.analysis.model.FieldTrace;
import net.groboclown.anhinga.analysis.model.MethodTrace;
import net.groboclown.retval.RetVal;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.objectweb.asm.tree.MethodNode;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;

class DefaultClassInspectorTest {

    @Test
    void inspectClassFile() {
        final byte[] classFile = ResourceUtil.getResourceAsBytes(ClassPathInspector.class, "SocketAppender.class");
        final DefaultClassInspector inspector = new DefaultClassInspector();
        final MethodInspector methodInspector = Mockito.mock(MethodInspector.class);
        final MockClassRepository repository = new MockClassRepository();
        Mockito.when(methodInspector.inspectMethod(
                        Mockito.any(), Mockito.any(), Mockito.any(),
                        Mockito.same(repository)))
                .then((a) -> RetVal.ok(new MethodTrace(
                        a.getArgument(0, MethodNode.class).name,
                        a.getArgument(0, MethodNode.class).desc,
                        List.of(), List.of())));
        final RetVal<ClassTrace> res = inspector.inspectClassFile(
                "SocketAppender", classFile, methodInspector, repository);
        assertEquals(List.of(), res.anyProblems());
        assertEquals(Set.of(), repository.marked);

        final ClassTrace trace = res.result();
        assertEquals("org/apache/logging/log4j/core/appender/SocketAppender", trace.getName());
        assertEquals("org/apache/logging/log4j/core/appender/AbstractOutputStreamAppender", trace.getSuperName());
        final Set<String> expectedFields = new HashSet<>(Set.of(
                "advertisement:Ljava/lang/Object;",
                "advertiser:Lorg/apache/logging/log4j/core/net/Advertiser;"
        ));
        for (final FieldTrace field : trace.getFields()) {
            final String key = field.getFieldName() + ':' + field.getType();
            assertTrue(
                    expectedFields.remove(key),
                    "Extra field reported: " + key);
        }
        assertEquals(Set.of(), expectedFields);

        final Set<String> expectedMethods = new HashSet<>(Set.of(
                "newBuilder:()Lorg/apache/logging/log4j/core/appender/SocketAppender$Builder;",
                "<init>:(Ljava/lang/String;Lorg/apache/logging/log4j/core/Layout;Lorg/apache/logging/log4j/core/Filter;Lorg/apache/logging/log4j/core/net/AbstractSocketManager;ZZLorg/apache/logging/log4j/core/net/Advertiser;[Lorg/apache/logging/log4j/core/config/Property;)V",
                "<init>:(Ljava/lang/String;Lorg/apache/logging/log4j/core/Layout;Lorg/apache/logging/log4j/core/Filter;Lorg/apache/logging/log4j/core/net/AbstractSocketManager;ZZLorg/apache/logging/log4j/core/net/Advertiser;)V",
                "stop:(JLjava/util/concurrent/TimeUnit;)Z",
                "createAppender:(Ljava/lang/String;ILorg/apache/logging/log4j/core/net/Protocol;Lorg/apache/logging/log4j/core/net/ssl/SslConfiguration;IIZLjava/lang/String;ZZLorg/apache/logging/log4j/core/Layout;Lorg/apache/logging/log4j/core/Filter;ZLorg/apache/logging/log4j/core/config/Configuration;)Lorg/apache/logging/log4j/core/appender/SocketAppender;",
                "createAppender:(Ljava/lang/String;Ljava/lang/String;Ljava/lang/String;Lorg/apache/logging/log4j/core/net/ssl/SslConfiguration;ILjava/lang/String;Ljava/lang/String;Ljava/lang/String;Ljava/lang/String;Ljava/lang/String;Lorg/apache/logging/log4j/core/Layout;Lorg/apache/logging/log4j/core/Filter;Ljava/lang/String;Lorg/apache/logging/log4j/core/config/Configuration;)Lorg/apache/logging/log4j/core/appender/SocketAppender;",
                "createSocketManager:(Ljava/lang/String;Lorg/apache/logging/log4j/core/net/Protocol;Ljava/lang/String;IILorg/apache/logging/log4j/core/net/ssl/SslConfiguration;IZLorg/apache/logging/log4j/core/Layout;I)Lorg/apache/logging/log4j/core/net/AbstractSocketManager;",
                "createSocketManager:(Ljava/lang/String;Lorg/apache/logging/log4j/core/net/Protocol;Ljava/lang/String;IILorg/apache/logging/log4j/core/net/ssl/SslConfiguration;IZLorg/apache/logging/log4j/core/Layout;ILorg/apache/logging/log4j/core/net/SocketOptions;)Lorg/apache/logging/log4j/core/net/AbstractSocketManager;",
                "directEncodeEvent:(Lorg/apache/logging/log4j/core/LogEvent;)V",
                "access$000:()Lorg/apache/logging/log4j/Logger;",
                "access$100:()Lorg/apache/logging/log4j/Logger;"
        ));
        for (final MethodTrace method : trace.getMethods()) {
            final String key = method.getName() + ':' + method.getSignature();
            assertTrue(
                    expectedMethods.remove(key),
                    "Extra method reported: " + key);
        }
        assertEquals(Set.of(), expectedMethods);
    }
}
