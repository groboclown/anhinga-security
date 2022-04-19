// Released under the MIT license.  See the LICENSE file for details.
package net.groboclown.anhinga.analysis.inspection.impl;

import net.groboclown.anhinga.analysis.ResourceUtil;
import net.groboclown.anhinga.analysis.inspection.*;
import net.groboclown.anhinga.analysis.model.ClassTrace;
import net.groboclown.anhinga.analysis.model.MethodTrace;
import net.groboclown.retval.RetVal;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import java.io.File;
import java.io.IOException;
import java.util.*;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Test {@link JarInspector}
 */
public class DefaultJarInspectorTest {
    File tmpFile;

    @Test
    void inspectLog4j() throws IOException {
        tmpFile = File.createTempFile("log4j", ".jar");
        ResourceUtil.saveResourceAs(ClassPathInspector.class, "log4j-core-2.17.2.jar", tmpFile);
        final DefaultJarInspector inspector = new DefaultJarInspector();
        final ClassInspector classInspector = new DefaultClassInspector();
        final MethodInspector methodInspector = Mockito.mock(MethodInspector.class);
        final MockClassRepository repository = new MockClassRepository();

        Mockito.when(methodInspector.inspectMethod(
                Mockito.any(), Mockito.any(), Mockito.any(),
                Mockito.same(repository)))
            .then((a) ->
                RetVal.ok(new MethodTrace(a.getArgument(0).toString(), "", List.of(), List.of())));
        RetVal<Collection<ClassTrace>> res = inspector.inspectJar(
                tmpFile, classInspector, methodInspector, repository);
        assertEquals(List.of(), res.anyProblems());
        final List<String> expectedClassNames = new ArrayList<>(ResourceUtil.getResourceLines(ClassPathInspector.class, "log4j-core-2.17.2.jar.class-list.txt"));
        for (final ClassTrace ct : res.result()) {
            // There's a duplicate org/apache/logging/log4j/core/util/SystemClock
            // class in this jar file, one in the jar, one in the META-INF/versions/9
            assertTrue(
                expectedClassNames.remove(ct.getName()),
                "Duplicate class or unknown class " + ct.getName());
        }
        assertEquals(List.of(), expectedClassNames);
    }

    @AfterEach
    void afterClean() throws IOException {
        if (tmpFile != null && tmpFile.isFile()) {
            if (!tmpFile.delete()) {
                throw new IOException("could not delete " + tmpFile);
            }
        }
    }
}
