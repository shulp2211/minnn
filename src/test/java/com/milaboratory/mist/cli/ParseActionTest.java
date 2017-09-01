package com.milaboratory.mist.cli;

import org.junit.BeforeClass;
import org.junit.Test;

import java.io.File;

import static com.milaboratory.mist.cli.CommandLineTestUtils.*;
import static com.milaboratory.mist.util.CommonTestUtils.*;
import static com.milaboratory.mist.util.DebugUtils.*;
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
        for (String suffix : new String[] {"", ".gz"}) {
            String testInputR1 = TEST_RESOURCES_PATH + "sample_r1.fastq" + suffix;
            String testInputR2 = TEST_RESOURCES_PATH + "sample_r2.fastq" + suffix;
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
                    "--input", testInputR1, testInputR2, "--output", testOutput1R1, testOutput1R2,
                    "--devel-parser-syntax", "--penalty-threshold", "0"};
            main(args1);

            String[] args2 = {"parse", "--penalty-threshold", "0", "--devel-parser-syntax", "--match-score", "0",
                    "--oriented", "--pattern", inQuotes("FuzzyMatchPattern(ATTAGACA, 0, 0, -1, -1)"),
                    "--input", testInputR1, "--output", testOutput1Single};
            main(args2);

            String[] args3 = {"parse", "--pattern", "<GA(UMI:AG)CA \\ AA",
                    "--input", testInputR1, testInputR2, "--output", testOutput2R1, testOutput2R2,
                    "--penalty-threshold", "0"};
            main(args3);

            String[] args4 = {"parse", "--penalty-threshold", "0", "--match-score", "0", "--oriented",
                    "--pattern", "ATTAGACA", "--input", testInputR1, "--output", testOutput2Single};
            main(args4);

            assertFileEquals(testOutput1R1, testOutput2R1);
            assertFileEquals(testOutput1R2, testOutput2R2);
            assertFileEquals(testOutput1Single, testOutput2Single);
            for (String fileName : new String[] {
                    testOutput1R1, testOutput2R1, testOutput1R2, testOutput2R2, testOutput1Single, testOutput2Single })
                assertTrue(new File(fileName).delete());
        }
    }

    @Test
    public void examplesTest() throws Exception {
        String posR1 = EXAMPLES_PATH + "positional/polyfid10_R1.fastq.gz";
        String posR2 = EXAMPLES_PATH + "positional/polyfid10_R2.fastq.gz";
        String outPosR1 = TEMP_DIR + "outputPosR1.fastq";
        String outPosR2 = TEMP_DIR + "outputPosR2.fastq";
        String positionalArgs = "parse --input " + posR1 + " " + posR2 + " --output " + outPosR1 + " " + outPosR2;

        String oneR1 = EXAMPLES_PATH + "onesided/sl4000_R1.fastq.gz";
        String oneR2 = EXAMPLES_PATH + "onesided/sl4000_R2.fastq.gz";
        String outOneR1 = TEMP_DIR + "outputOneR1.fastq";
        String outOneR2 = TEMP_DIR + "outputOneR2.fastq";
        String onesidedArgs = "parse --input " + oneR1 + " " + oneR2 + " --output " + outOneR1 + " " + outOneR2;

        String twoR1 = EXAMPLES_PATH + "twosided/p109_R1.fastq.gz";
        String twoR2 = EXAMPLES_PATH + "twosided/p109_R2.fastq.gz";
        String outTwoR1 = TEMP_DIR + "outputTwoR1.fastq";
        String outTwoR2 = TEMP_DIR + "outputTwoR2.fastq";
        String twosidedArgs = "parse --input " + twoR1 + " " + twoR2 + " --output " + outTwoR1 + " " + outTwoR2;

        String configuration = " --input-format fastq --output-format fastq --copy-original-comments "
                + "--match-score 0 --mismatch-score -7 --gap-score -11 --single-overlap-penalty -10 "
                + "--bitap-max-errors 3 --threads 1 --first-read-number 1";

        String patternPos = "^(UMI:N{14})n{22}(SB:N{4}) \\ *";
        String penaltyPos = " --penalty-threshold -200";

        String patternOne = "^(SB:N{5})[aagc || c]agtggtatcaacgcagagt(UMI:N{14}) \\ *";
        String penaltyOne = " --penalty-threshold -100";

        String patternTwo = "^(SB1:N{5})[aagc || c]agtggtatcaacgcagagt(UMI:N{14}) \\ ^(SB2:N{5})gtcacatttctcagatcct";
        String penaltyTwo = " --penalty-threshold -130";

        printExecutionTime("total", () -> {
            exec(positionalArgs + configuration + penaltyPos + " --pattern " + inQuotes(patternPos));
            exec(onesidedArgs + configuration + penaltyOne + " --pattern " + inQuotes(patternOne));
            exec(twosidedArgs + configuration + penaltyTwo + " --pattern " + inQuotes(patternTwo));
            return null;
        });

        System.out.println("sorting: " + callCounter.get("sorting") + " calls, " + timeCounter.get("sorting") + " ms");
        System.out.println("alignment: " + callCounter.get("alignment") + " calls, " + timeCounter.get("alignment") + " ms");
        System.out.println("generate: " + callCounter.get("generate") + " calls, " + timeCounter.get("generate") + " ms");
        System.out.println("combine: " + callCounter.get("combine") + " calls, " + timeCounter.get("combine") + " ms");
        System.out.println("take: " + callCounter.get("take") + " calls, " + timeCounter.get("take") + " ms");
        System.out.println("calculate: " + callCounter.get("calculate") + " calls, " + timeCounter.get("calculate") + " ms");
        System.out.println("stage1: " + timeCounter.get("stage1") + " ms");
        System.out.println("stage2: " + timeCounter.get("stage2") + " ms");
        System.out.println("stage3: " + timeCounter.get("stage3") + " ms");
        System.out.println("isCompatible: " + callCounter.get("isCompatible") + " calls, "
                + timeCounter.get("isCompatible") + " ms");
        System.out.println("find: " + callCounter.get("find") + " calls, " + timeCounter.get("find") + " ms");
        System.out.println("maxSize: " + maxSize);
    }
}
