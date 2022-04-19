package net.groboclown.anhinga.analysis.inspection;

import net.groboclown.anhinga.analysis.ResourceUtil;
import net.groboclown.anhinga.analysis.model.AnalyzedClasses;
import net.groboclown.retval.RetVal;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;

import java.io.File;
import java.io.IOException;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

// Released under the MIT license.  See the LICENSE file for details.
class ClassPathInspectorTest {
    File tmpFile;

    @Test
    void analyzeClassPath1() throws IOException {
        tmpFile = File.createTempFile("log4j", ".jar");
        ResourceUtil.saveResourceAs(ClassPathInspector.class, "log4j-core-2.17.2.jar", tmpFile);
        RetVal<AnalyzedClasses> res = new ClassPathInspector().analyzeClassPath(List.of(tmpFile));
        assertEquals(List.of(), res.anyProblems());
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