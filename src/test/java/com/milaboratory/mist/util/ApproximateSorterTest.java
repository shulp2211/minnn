package com.milaboratory.mist.util;

import com.milaboratory.core.sequence.*;
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
        MultipleReadsOperator[] mPatterns = new MultipleReadsOperator[5];
        NSequenceWithQuality[] targets = new NSequenceWithQuality[2];
        MultiNSequenceWithQuality[] mTargets = new MultiNSequenceWithQuality[3];
        ApproximateSorterConfiguration[] configurations = new ApproximateSorterConfiguration[28];

        patterns[0] = new FuzzyMatchPattern(patternAligner, new NucleotideSequence("ATTAGACA"));
        patterns[1] = new FuzzyMatchPattern(patternAligner, new NucleotideSequence("GT"));
        patterns[2] = new RepeatPattern(patternAligner, new NucleotideSequence("T"), 2, 8);
        patterns[3] = new FuzzyMatchPattern(patternAligner, new NucleotideSequence("A"));
        patterns[4] = new FuzzyMatchPattern(patternAligner, new NucleotideSequence("ATT"));
        patterns[5] = new FuzzyMatchPattern(patternAligner, new NucleotideSequence("GCC"));

        mPatterns[0] = new MultiPattern(patternAligner, patterns[0], patterns[3]);
        mPatterns[1] = new MultiPattern(patternAligner, patterns[2], patterns[5]);
        mPatterns[2] = new MultiPattern(patternAligner, patterns[2], patterns[0], patterns[0]);
        mPatterns[3] = new MultiPattern(patternAligner, patterns[3], patterns[3], patterns[3]);
        mPatterns[4] = new MultiPattern(patternAligner, patterns[0], patterns[0]);

        targets[0] = new NSequenceWithQuality("ACTGCGATAAATTAGACAGTACGTATTAGACATTATTATTAGACAGAGACAGT");
        targets[1] = new NSequenceWithQuality("ATTATTGCCGCCATTATTGCCGCC");

        mTargets[0] = new MultiNSequenceWithQualityImpl(targets);
        mTargets[1] = new MultiNSequenceWithQualityImpl(targets[0], targets[0], targets[0]);
        mTargets[2] = new MultiNSequenceWithQualityImpl(targets[1], targets[1]);

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
                patternAligner, true, true, ORDER, 0,
                patterns[4], patterns[5]);
        configurations[5] = new ApproximateSorterConfiguration(targets[0], 26, 40,
                patternAligner, true, true, ORDER, 0,
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

        // AndPattern
        configurations[8] = new ApproximateSorterConfiguration(targets[0], 0, targets[0].size(),
                patternAligner, true, true, INTERSECTION, 0,
                patterns[1], patterns[2], patterns[3]);
        configurations[9] = new ApproximateSorterConfiguration(targets[1], 5, 23,
                patternAligner, true, true, INTERSECTION, 0,
                patterns[4], patterns[5]);
        configurations[10] = new ApproximateSorterConfiguration(targets[0], 0, targets[0].size(),
                patternAligner, true, false, INTERSECTION, 100,
                patterns[1], patterns[2], patterns[3]);
        configurations[11] = new ApproximateSorterConfiguration(targets[1], 5, 23,
                patternAligner, true, false, INTERSECTION, 20,
                patterns[4], patterns[5]);
        assertEquals(315, countPortValues(new ApproximateSorter(configurations[8]).getOutputPort()));
        assertEquals(6, countPortValues(new ApproximateSorter(configurations[9]).getOutputPort()));
        assertEquals(100, countPortValues(new ApproximateSorter(configurations[10]).getOutputPort()));
        assertEquals(6, countPortValues(new ApproximateSorter(configurations[11]).getOutputPort()));

        // OrPattern
        configurations[12] = new ApproximateSorterConfiguration(targets[0], 7, 15,
                patternAligner, false, true, FIRST, 0,
                patterns[2], patterns[4]);
        configurations[13] = new ApproximateSorterConfiguration(targets[1], 0, targets[1].size(),
                patternAligner, false, true, FIRST, 0,
                patterns[5], patterns[4], patterns[2]);
        configurations[14] = new ApproximateSorterConfiguration(targets[0], 7, 15,
                patternAligner, false, false, FIRST, 20,
                patterns[2], patterns[4]);
        configurations[15] = new ApproximateSorterConfiguration(targets[1], 0, targets[1].size(),
                patternAligner, false, false, FIRST, 20,
                patterns[5], patterns[4], patterns[2]);
        assertEquals(1, countPortValues(new ApproximateSorter(configurations[12]).getOutputPort()));
        assertEquals(64, countPortValues(new ApproximateSorter(configurations[13]).getOutputPort()));
        assertEquals(1, countPortValues(new ApproximateSorter(configurations[14]).getOutputPort()));
        assertEquals(20, countPortValues(new ApproximateSorter(configurations[15]).getOutputPort()));

        // MultiPattern
        configurations[16] = new ApproximateSorterConfiguration(mTargets[0], patternAligner, true,
                true, true, LOGICAL_AND, 0,
                patterns[3], patterns[4]);
        configurations[17] = new ApproximateSorterConfiguration(mTargets[1], patternAligner, true,
                true, true, LOGICAL_AND, 0,
                patterns[4], patterns[2], patterns[0]);
        configurations[18] = new ApproximateSorterConfiguration(mTargets[0], patternAligner, true,
                true, false, LOGICAL_AND, 20,
                patterns[3], patterns[4]);
        configurations[19] = new ApproximateSorterConfiguration(mTargets[1], patternAligner, true,
                true, false, LOGICAL_AND, 100,
                patterns[4], patterns[2], patterns[0]);
        assertEquals(84, countPortValues(new ApproximateSorter(configurations[16]).getOutputPort()));
        assertEquals(75, countPortValues(new ApproximateSorter(configurations[17]).getOutputPort()));
        assertEquals(20, countPortValues(new ApproximateSorter(configurations[18]).getOutputPort()));
        assertEquals(75, countPortValues(new ApproximateSorter(configurations[19]).getOutputPort()));

        // AndOperator
        configurations[20] = new ApproximateSorterConfiguration(mTargets[0], patternAligner, false,
                true, true, LOGICAL_AND, 0,
                mPatterns[0], mPatterns[1]);
        configurations[21] = new ApproximateSorterConfiguration(mTargets[1], patternAligner, false,
                true, true, LOGICAL_AND, 0,
                mPatterns[2], mPatterns[3]);
        configurations[22] = new ApproximateSorterConfiguration(mTargets[0], patternAligner, false,
                true, false, LOGICAL_AND, 100,
                mPatterns[0], mPatterns[1]);
        configurations[23] = new ApproximateSorterConfiguration(mTargets[1], patternAligner, false,
                true, false, LOGICAL_AND, 100,
                mPatterns[2], mPatterns[3], mPatterns[3], mPatterns[3]);
        assertEquals(240, countPortValues(new ApproximateSorter(configurations[20]).getOutputPort()));
        assertEquals(416745, countPortValues(new ApproximateSorter(configurations[21]).getOutputPort()));
        assertEquals(100, countPortValues(new ApproximateSorter(configurations[22]).getOutputPort()));
        assertEquals(100, countPortValues(new ApproximateSorter(configurations[23]).getOutputPort()));

        // OrOperator
        configurations[24] = new ApproximateSorterConfiguration(mTargets[2], patternAligner, false,
                false, true, LOGICAL_OR, 0,
                mPatterns[4], mPatterns[4]);
        configurations[25] = new ApproximateSorterConfiguration(mTargets[1], patternAligner, false,
                false, true, LOGICAL_OR, 0,
                mPatterns[2], mPatterns[3]);
        configurations[26] = new ApproximateSorterConfiguration(mTargets[2], patternAligner, false,
                false, false, LOGICAL_OR, 100,
                mPatterns[4], mPatterns[4]);
        configurations[27] = new ApproximateSorterConfiguration(mTargets[1], patternAligner, false,
                false, false, LOGICAL_OR, 100,
                mPatterns[2], mPatterns[3], mPatterns[3], mPatterns[3]);
        assertEquals(0, countPortValues(new ApproximateSorter(configurations[24]).getOutputPort()));
        assertEquals(416745, countPortValues(new ApproximateSorter(configurations[25]).getOutputPort()));
        assertEquals(0, countPortValues(new ApproximateSorter(configurations[26]).getOutputPort()));
        assertEquals(100, countPortValues(new ApproximateSorter(configurations[27]).getOutputPort()));
    }

    @Test
    public void randomTest() throws Exception {

    }
}
