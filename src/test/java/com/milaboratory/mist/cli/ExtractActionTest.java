package com.milaboratory.mist.cli;

import org.junit.BeforeClass;
import org.junit.Ignore;
import org.junit.Test;

import java.io.File;

import static com.milaboratory.mist.cli.CommandLineTestUtils.*;
import static com.milaboratory.mist.util.CommonTestUtils.*;
import static com.milaboratory.mist.util.SystemUtils.*;
import static com.milaboratory.mist.util.TestSettings.*;
import static com.milaboratory.mist.Main.main;
import static org.junit.Assert.*;

public class ExtractActionTest {
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
            String testOutput1Double = TEMP_DIR + "output1_double.mif";
            String testOutput1Single = TEMP_DIR + "output1_single.mif";
            String testOutput2Double = TEMP_DIR + "output2_double.mif";
            String testOutput2Single = TEMP_DIR + "output2_single.mif";

            String[] args1 = {"extract", "--pattern",
                    inQuotes("MultiPattern([FuzzyMatchPattern(GAAGCA, 1, 0, -1, -1, [GroupEdgePosition(" +
                            "GroupEdge('UMI', true), 2), GroupEdgePosition(GroupEdge('UMI', false), 4)]), " +
                            "FuzzyMatchPattern(AA, 0, 0, -1, -1)])"),
                    "--input", testInputR1, testInputR2, "--output", testOutput1Double,
                    "--devel-parser-syntax", "--penalty-threshold", "0"};
            main(args1);

            String[] args2 = {"extract", "--penalty-threshold", "0", "--devel-parser-syntax", "--match-score", "0",
                    "--oriented", "--pattern", inQuotes("FuzzyMatchPattern(ATTAGACA, 0, 0, -1, -1)"),
                    "--input", testInputR1, "--output", testOutput1Single};
            main(args2);

            String[] args3 = {"extract", "--pattern", "<GA(UMI:AG)CA \\ AA",
                    "--input", testInputR1, testInputR2, "--output", testOutput2Double, "--penalty-threshold", "0"};
            main(args3);

            String[] args4 = {"extract", "--penalty-threshold", "0", "--match-score", "0", "--oriented",
                    "--pattern", "ATTAGACA", "--input", testInputR1, "--output", testOutput2Single};
            main(args4);

            assertFileEquals(testOutput1Double, testOutput2Double);
            assertFileEquals(testOutput1Single, testOutput2Single);
            for (String fileName : new String[] {
                    testOutput1Double, testOutput2Double, testOutput1Single, testOutput2Single })
                assertTrue(new File(fileName).delete());
        }
    }

    @Test
    public void examplesTest() throws Exception {
        String posR1 = EXAMPLES_PATH + "positional/polyfid10_R1.fastq.gz";
        String posR2 = EXAMPLES_PATH + "positional/polyfid10_R2.fastq.gz";
        String outPos = TEMP_DIR + "outputPos.mif";
        String positionalArgs = "extract --input " + posR1 + " " + posR2 + " --output " + outPos;

        String oneR1 = EXAMPLES_PATH + "onesided/sl4000_R1.fastq.gz";
        String oneR2 = EXAMPLES_PATH + "onesided/sl4000_R2.fastq.gz";
        String outOne = TEMP_DIR + "outputOne.mif";
        String onesidedArgs = "extract --input " + oneR1 + " " + oneR2 + " --output " + outOne;

        String twoR1 = EXAMPLES_PATH + "twosided/p109_R1.fastq.gz";
        String twoR2 = EXAMPLES_PATH + "twosided/p109_R2.fastq.gz";
        String outTwo = TEMP_DIR + "outputTwo.mif";
        String twosidedArgs = "extract --input " + twoR1 + " " + twoR2 + " --output " + outTwo;

        String configuration = " --input-format fastq --match-score 0 --mismatch-score -7 --gap-score -11 "
                + "--single-overlap-penalty -10 --bitap-max-errors 3 --threads 4";

        String patternPos = "^(UMI:N{14})n{22}(SB:N{4}) \\ *";
        String penaltyPos = " --penalty-threshold -200";

        String patternOne = "(SB:N{5})[aagc || c]agtggtatcaacgcagagt(UMI:N{14}) \\ *";
        String penaltyOne = " --penalty-threshold -100";

        String patternTwo = "(SB1:N{5})[aagc || c]agtggtatcaacgcagagt(UMI:N{14}) \\ (SB2:N{5})gtcacatttctcagatcct";
        String penaltyTwo = " --penalty-threshold -130";

        exec(positionalArgs + configuration + penaltyPos + " --pattern " + inQuotes(patternPos));
        exec(onesidedArgs + configuration + penaltyOne + " --pattern " + inQuotes(patternOne));
        exec(twosidedArgs + configuration + penaltyTwo + " --pattern " + inQuotes(patternTwo));
    }

    @Test
    public void specialCaseTest1() throws Exception {
        String R1 = EXAMPLES_PATH + "positional/polyfid10_R1.fastq.gz";
        String R2 = EXAMPLES_PATH + "positional/polyfid10_R2.fastq.gz";
        String out = TEMP_DIR + "outputSCT1.mif";
        String argsIO = "extract --input " + R1 + " " + R2 + " --output " + out;
        String query = argsIO + " --pattern \"NN(G1:N{12})N{22}TCAG\\NN(G2:N{12})N{22}TCAG\" --threads 3 "
                + "--bitap-max-errors 1 --mismatch-score -1 --penalty-threshold -73";
        exec(query);
    }
}