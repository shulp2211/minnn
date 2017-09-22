package com.milaboratory.mist.util;

import com.milaboratory.core.sequence.NSequenceWithQuality;
import com.milaboratory.core.sequence.NucleotideSequence;
import com.milaboratory.mist.pattern.*;
import org.junit.Test;

import static com.milaboratory.mist.pattern.MatchValidationType.*;
import static com.milaboratory.mist.util.CommonTestUtils.*;
import static org.junit.Assert.assertEquals;

public class ApproximateSorterTest {
    @Test
    public void simpleTest() throws Exception {
        PatternAligner patternAligner = getTestPatternAligner(true);
        SinglePattern[] patterns = new SinglePattern[6];
        NSequenceWithQuality[] targets = new NSequenceWithQuality[2];
        ApproximateSorterConfiguration[] configurations = new ApproximateSorterConfiguration[8];

        patterns[0] = new FuzzyMatchPattern(patternAligner, new NucleotideSequence("ATTAGACA"));
        patterns[1] = new FuzzyMatchPattern(patternAligner, new NucleotideSequence("GT"));
        patterns[2] = new RepeatPattern(patternAligner, new NucleotideSequence("T"), 2, 8);
        patterns[3] = new FuzzyMatchPattern(patternAligner, new NucleotideSequence("A"));
        patterns[4] = new FuzzyMatchPattern(patternAligner, new NucleotideSequence("ATT"));
        patterns[5] = new FuzzyMatchPattern(patternAligner, new NucleotideSequence("GCC"));

        targets[0] = new NSequenceWithQuality("ACTGCGATAAATTAGACAGTACGTATTAGACATTATTATTAGACAGAGACAGT");
        targets[1] = new NSequenceWithQuality("ATTATTGCCGCCATTATTGCCGCC");

        // SequencePattern
        configurations[0] = new ApproximateSorterConfiguration(targets[0], 0, targets[0].size(),
                patternAligner, true, true, FOLLOWING, 0,
                patterns[0], patterns[1]);
        configurations[1] = new ApproximateSorterConfiguration(targets[0], 26, 40,
                patternAligner, true, true, FOLLOWING, 0,
                patterns[2], patterns[3]);
        configurations[2] = new ApproximateSorterConfiguration(targets[0], 0, targets[0].size(),
                patternAligner, true, false, FOLLOWING, 20,
                patterns[0], patterns[1]);
        configurations[3] = new ApproximateSorterConfiguration(targets[0], 26, 40,
                patternAligner, true, false, FOLLOWING, 20,
                patterns[2], patterns[3]);
        assertEquals(1, countPortValues(new ApproximateSorter(configurations[0]).getOutputPort()));
        assertEquals(2, countPortValues(new ApproximateSorter(configurations[1]).getOutputPort()));
        assertEquals(1, countPortValues(new ApproximateSorter(configurations[2]).getOutputPort()));
        assertEquals(2, countPortValues(new ApproximateSorter(configurations[3]).getOutputPort()));

        // PlusPattern
        configurations[4] = new ApproximateSorterConfiguration(targets[1], 0, targets[1].size(),
                patternAligner, true, true, ORDER, 20,
                patterns[4], patterns[5]);
        configurations[5] = new ApproximateSorterConfiguration(targets[0], 26, 40,
                patternAligner, true, true, ORDER, 20,
                patterns[2], patterns[3]);
        configurations[6] = new ApproximateSorterConfiguration(targets[1], 0, targets[1].size(),
                patternAligner, true, false, ORDER, 20,
                patterns[4], patterns[5]);
        configurations[7] = new ApproximateSorterConfiguration(targets[0], 26, 40,
                patternAligner, true, false, ORDER, 20,
                patterns[2], patterns[3]);
        assertEquals(12, countPortValues(new ApproximateSorter(configurations[4]).getOutputPort()));
        assertEquals(3, countPortValues(new ApproximateSorter(configurations[5]).getOutputPort()));
        assertEquals(12, countPortValues(new ApproximateSorter(configurations[6]).getOutputPort()));
        assertEquals(3, countPortValues(new ApproximateSorter(configurations[7]).getOutputPort()));
    }

    @Test
    public void randomTest() throws Exception {

    }
}
