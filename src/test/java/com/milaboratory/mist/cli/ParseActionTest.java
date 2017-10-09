package com.milaboratory.mist.cli;

import org.junit.BeforeClass;
import org.junit.Test;

import java.io.File;

import static com.milaboratory.mist.cli.CommandLineTestUtils.*;
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
                + "--bitap-max-errors 3 --threads 4 --first-read-number 1";

        String patternPos = "^(UMI:N{14})n{22}(SB:N{4}) \\ *";
        String penaltyPos = " --penalty-threshold -200";

        String patternOne = "^(SB:N{5})[aagc || c]agtggtatcaacgcagagt(UMI:N{14}) \\ *";
        String penaltyOne = " --penalty-threshold -100";

        String patternTwo = "^(SB1:N{5})[aagc || c]agtggtatcaacgcagagt(UMI:N{14}) \\ ^(SB2:N{5})gtcacatttctcagatcct";
        String penaltyTwo = " --penalty-threshold -130";

        exec(positionalArgs + configuration + penaltyPos + " --pattern " + inQuotes(patternPos));
        exec(onesidedArgs + configuration + penaltyOne + " --pattern " + inQuotes(patternOne));
        exec(twosidedArgs + configuration + penaltyTwo + " --pattern " + inQuotes(patternTwo));
    }

    @Test
    public void specialCaseTest1() throws Exception {
        String R1 = EXAMPLES_PATH + "positional/polyfid10_R1.fastq.gz";
        String R2 = EXAMPLES_PATH + "positional/polyfid10_R2.fastq.gz";
        String outR1 = TEMP_DIR + "outputR1.fastq";
        String outR2 = TEMP_DIR + "outputR2.fastq";
        String argsIO = "parse --input " + R1 + " " + R2 + " --output " + outR1 + " " + outR2;
        String query = argsIO + " --pattern \"NN(G1:N{12})N{22}TCAG\\NN(G2:N{12})N{22}TCAG\" --threads 3 "
                + "--bitap-max-errors 1 --mismatch-score -1 --penalty-threshold -73";
        exec(query);
    }
}
