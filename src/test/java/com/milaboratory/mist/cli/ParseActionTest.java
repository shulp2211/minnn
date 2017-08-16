package com.milaboratory.mist.cli;

import org.junit.BeforeClass;
import org.junit.Test;

import java.io.File;

import static com.milaboratory.mist.util.CommonTestUtils.*;
import static com.milaboratory.mist.util.SystemUtils.*;
import static com.milaboratory.mist.util.TestSettings.*;
import static com.milaboratory.mist.Main.main;
import static org.junit.Assert.*;

public class ParseActionTest {
    @BeforeClass
    public static void init() {
        exitOnError = false;
        File outputFilesDirectory = new File(TEMP_DIR);
        if (!outputFilesDirectory.exists())
            throw exitWithError("Directory for temporary output files " + TEMP_DIR + " does not exist!");
    }

    @Test
    public void simpleTest() throws Exception {
        String testInputR1 = TEST_RESOURCES_PATH + "sample_r1.fastq";
        String testInputR2 = TEST_RESOURCES_PATH + "sample_r2.fastq";
        String testOutput1R1 = TEMP_DIR + "output1_r1.fastq";
        String testOutput1R2 = TEMP_DIR + "output1_r2.fastq";
        String testOutput1Single = TEMP_DIR + "output1_single.fastq";
        String testOutput2R1 = TEMP_DIR + "output2_r1.fastq";
        String testOutput2R2 = TEMP_DIR + "output2_r2.fastq";
        String testOutput2Single = TEMP_DIR + "output2_single.fastq";

        String[] args1 = {"parse", "--pattern",
                inQuotes("MultiPattern([FuzzyMatchPattern(GAAGCA, 1, 0, -1, -1, [GroupEdgePosition(" +
                        "GroupEdge('UMI', true), 2), GroupEdgePosition(GroupEdge('UMI', false), 4)]), " +
                        "FuzzyMatchPattern(AA, 0, 0, -1, -1)])"),
                "--input", testInputR1, testInputR2, "--output", testOutput1R1, testOutput1R2, "--devel-parser-syntax"};
        main(args1);

        String[] args2 = {"parse", "--devel-parser-syntax", "--match-score", "0", "--oriented", "--pattern", inQuotes(
                "FuzzyMatchPattern(ATTAGACA, 0, 0, -1, -1)"), "--input", testInputR1, "--output", testOutput1Single};
        main(args2);

        String[] args3 = {"parse", "--pattern", "<GA(UMI:AG)CA \\ AA",
                "--input", testInputR1, testInputR2, "--output", testOutput2R1, testOutput2R2};
        main(args3);

        String[] args4 = {"parse", "--match-score", "0", "--oriented", "--pattern", "ATTAGACA",
                "--input", testInputR1, "--output", testOutput2Single};
        main(args4);

        assertFileEquals(testOutput1R1, testOutput2R1);
        assertFileEquals(testOutput1R2, testOutput2R2);
        assertFalse(new File(testOutput1Single).exists());
        assertFalse(new File(testOutput2Single).exists());
        for (String fileName : new String[] {testOutput1R1, testOutput2R1, testOutput1R2, testOutput2R2})
            assertTrue(new File(fileName).delete());
    }
}
