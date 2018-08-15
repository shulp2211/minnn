package com.milaboratory.minnn.cli;

import org.junit.*;

import java.io.File;

import static com.milaboratory.minnn.cli.CommandLineTestUtils.*;
import static com.milaboratory.minnn.util.CommonTestUtils.*;
import static com.milaboratory.minnn.util.SystemUtils.*;
import static org.junit.Assert.*;

public class GenerateDocsActionTest {
    @BeforeClass
    public static void init() {
        exitOnError = false;
        File outputFilesDirectory = new File(TEMP_DIR);
        if (!outputFilesDirectory.exists())
            throw exitWithError("Directory for temporary output files " + TEMP_DIR + " does not exist!");
    }

    @Test
    public void simpleTest() throws Exception {
        String tempFile = TEMP_DIR + "generateDocsTest.rst";
        exec("docs --output " + tempFile);
        assertTrue(new File(tempFile).delete());
    }
}
