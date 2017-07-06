package com.milaboratory.mist.cli;

import org.junit.BeforeClass;
import org.junit.Test;

import static com.milaboratory.mist.util.CommonTestUtils.inQuotes;
import static com.milaboratory.mist.util.SystemUtils.exitOnError;
import static com.milaboratory.mist.util.TestSettings.*;
import static org.junit.Assert.*;

public class ActionParseTest {
    @BeforeClass
    public static void init() {
        exitOnError = false;
    }

    @Test
    public void simpleTest() throws Exception {
        String testInputR1 = inQuotes(TEST_SEQUENCES_PATH + "sample_r1.fastq");
        String testInputR2 = inQuotes(TEST_SEQUENCES_PATH + "sample_r2.fastq");
        String testOutputR1 = inQuotes(TEST_OUTPUT_FILES_PATH + "output_r1.fastq");
        String testOutputR2 = inQuotes(TEST_OUTPUT_FILES_PATH + "output_r2.fastq");
        String testOutputSingle = inQuotes(TEST_OUTPUT_FILES_PATH + "output_single.fastq");
//        String[] args1 = {"parse", "--pattern", "\"^N{0:5} ATTAGACA (ID:N{4}) W (UMI:N{7}) / ^GACATAT (R2:N{:})\"",
//                "--input", "input_R1.fastq.gz", "input_R2.fastq.gz", "--output", "output_R1.fastq.gz", "output_R2.fastq.gz"};
        String[] args1 = {"parse", "--pattern",
                inQuotes("OrOperator([AndOperator([MultiPattern([OrPattern([PlusPattern([AndPattern([FuzzyMatchPattern(ATTG, -1, -1), FuzzyMatchPattern(ATTG, -1, -1)]), FuzzyMatchPattern(ATTG, -1, -1)]), AndPattern([FuzzyMatchPattern(ATTG, -1, -1), FuzzyMatchPattern(ATTG, -1, -1)])]), FilterPattern(BorderFilter(true, ATTA, 4, false), PlusPattern([AndPattern([FuzzyMatchPattern(ATTG, -1, -1), FuzzyMatchPattern(ATTG, -1, -1)]), FuzzyMatchPattern(ATTG, -1, -1)])), FuzzyMatchPattern(GTGGTTGTGTTGT, -1, -1, [GroupEdgePosition(GroupEdge('ABC', true), 1), GroupEdgePosition(GroupEdge('ABC', false), 3), GroupEdgePosition(GroupEdge('DEF', true), 6), GroupEdgePosition(GroupEdge('DEF', false), 7), GroupEdgePosition(GroupEdge('GH', true), 10), GroupEdgePosition(GroupEdge('GH', false), 11)]), AndPattern([FuzzyMatchPattern(ATTG, -1, -1), FuzzyMatchPattern(ATTG, -1, -1)])]), MultiPattern([FilterPattern(BorderFilter(true, ATTA, 4, false), PlusPattern([AndPattern([FuzzyMatchPattern(ATTG, -1, -1), FuzzyMatchPattern(ATTG, -1, -1)]), FuzzyMatchPattern(ATTG, -1, -1)])), FuzzyMatchPattern(ATTG, -1, -1), AndPattern([FuzzyMatchPattern(ATTG, -1, -1), FuzzyMatchPattern(ATTG, -1, -1)])])]), NotOperator(MultipleReadsFilterPattern(ScoreFilter(-3), AndOperator([MultiPattern([FilterPattern(BorderFilter(true, ATTA, 4, false), PlusPattern([AndPattern([FuzzyMatchPattern(ATTG, -1, -1), FuzzyMatchPattern(ATTG, -1, -1)]), FuzzyMatchPattern(ATTG, -1, -1)])), FuzzyMatchPattern(ATTG, -1, -1), AndPattern([FuzzyMatchPattern(ATTG, -1, -1), FuzzyMatchPattern(ATTG, -1, -1)])]), MultiPattern([FilterPattern(BorderFilter(true, ATTA, 4, false), PlusPattern([AndPattern([FuzzyMatchPattern(ATTG, -1, -1), FuzzyMatchPattern(ATTG, -1, -1)]), FuzzyMatchPattern(ATTG, -1, -1)])), FuzzyMatchPattern(ATTG, -1, -1), AndPattern([FuzzyMatchPattern(ATTG, -1, -1), FuzzyMatchPattern(ATTG, -1, -1)])])]))), MultipleReadsFilterPattern(ScoreFilter(-3), AndOperator([MultiPattern([FilterPattern(BorderFilter(true, ATTA, 4, false), PlusPattern([AndPattern([FuzzyMatchPattern(ATTG, -1, -1), FuzzyMatchPattern(ATTG, -1, -1)]), FuzzyMatchPattern(ATTG, -1, -1)])), FuzzyMatchPattern(ATTG, -1, -1), AndPattern([FuzzyMatchPattern(ATTG, -1, -1), FuzzyMatchPattern(ATTG, -1, -1)])]), MultiPattern([FilterPattern(BorderFilter(true, ATTA, 4, false), PlusPattern([AndPattern([FuzzyMatchPattern(ATTG, -1, -1), FuzzyMatchPattern(ATTG, -1, -1)]), FuzzyMatchPattern(ATTG, -1, -1)])), FuzzyMatchPattern(ATTG, -1, -1), AndPattern([FuzzyMatchPattern(ATTG, -1, -1), FuzzyMatchPattern(ATTG, -1, -1)])])]))])"),
                "--input", testInputR1, testInputR2, "--output", testOutputR1, testOutputR2};
        CommandLineParser commandLineParser1 = new CommandLineParser(args1);
        commandLineParser1.parseAndExecute();

//        String[] args2 = {"parse", "--scoring", "0", "--oriented", "--pattern", "ATTAGACA", "--input", "input",
//                "--output", "output"};
        String[] args2 = {"parse", "--scoring", "0", "--oriented", "--pattern", inQuotes(
                "FuzzyMatchPattern(ATTAGACA, -1, -1)") , "--input", testInputR1, "--output", testOutputSingle};
        CommandLineParser commandLineParser2 = new CommandLineParser(args2);
        commandLineParser2.parseAndExecute();
    }
}
