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
        SinglePattern[] patterns = new SinglePattern[4];
        NSequenceWithQuality[] targets = new NSequenceWithQuality[1];
        ApproximateSorterConfiguration[] configurations = new ApproximateSorterConfiguration[4];

        patterns[0] = new FuzzyMatchPattern(patternAligner, new NucleotideSequence("ATTAGACA"));
        patterns[1] = new FuzzyMatchPattern(patternAligner, new NucleotideSequence("GT"));
        patterns[2] = new RepeatPattern(patternAligner, new NucleotideSequence("T"), 2, 8);
        patterns[3] = new FuzzyMatchPattern(patternAligner, new NucleotideSequence("A"));

        targets[0] = new NSequenceWithQuality("ACTGCGATAAATTAGACAGTACGTATTAGACATTATTATTAGACAGAGACAGT");

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
    }

    @Test
    public void randomTest() throws Exception {

    }
}
