package com.milaboratory.mist.pattern;

import cc.redberry.pipe.OutputPort;
import com.milaboratory.core.sequence.MultiNSequenceWithQuality;
import com.milaboratory.core.sequence.NSequenceWithQuality;
import com.milaboratory.core.sequence.NucleotideSequence;
import com.milaboratory.test.TestUtil;
import org.junit.Test;

import static com.milaboratory.mist.pattern.MatchUtils.countMatches;
import static com.milaboratory.mist.util.CommonTestUtils.*;
import static org.junit.Assert.*;

public class ScoreFilterTest {
    @Test
    public void randomTest() throws Exception {
        for (int i = 0; i < 1000; i++) {
            int scoreThreshold = -rg.nextInt(100);
            NSequenceWithQuality target = new NSequenceWithQuality(TestUtil.randomSequence(NucleotideSequence.ALPHABET,
                    1, 300).toString());
            MultiNSequenceWithQuality multiTarget = new MultiNSequenceWithQuality() {
                @Override
                public int numberOfSequences() {
                    return 2;
                }
                @Override
                public NSequenceWithQuality get(int id) {
                    return target;
                }};
            NucleotideSequence motif = TestUtil.randomSequence(NucleotideSequence.ALPHABET, 1, 20);
            FuzzyMatchPattern pattern = new FuzzyMatchPattern(getTestPatternAligner(rg.nextInt(5)), motif);
            FilterPattern filterPattern = new FilterPattern(getTestPatternAligner(), new ScoreFilter(scoreThreshold), pattern);
            MultiPattern multiPattern = new MultiPattern(getTestPatternAligner(), pattern, filterPattern);
            MultipleReadsFilterPattern mFilterPattern = new MultipleReadsFilterPattern(getTestPatternAligner(),
                    new ScoreFilter(scoreThreshold * 2), multiPattern);
            AndOperator andOperator = new AndOperator(getTestPatternAligner(), multiPattern, mFilterPattern);
            Match patternBestMatch = pattern.match(target).getBestMatch(true);
            boolean isMatching = (patternBestMatch != null) && (patternBestMatch.getScore() >= scoreThreshold);

            assertEquals(isMatching, filterPattern.match(target).getBestMatch(true) != null);
            if (countMatches(multiPattern.match(multiTarget), true) < 300)
                assertEquals(isMatching, andOperator.match(multiTarget).getBestMatch(true) != null);
            assertTrue(countMatches(pattern.match(target), true)
                    >= countMatches(filterPattern.match(target), true));
            Match currentMatch;
            for (OutputPort<Match> filteredPort = filterPattern.match(target).getMatches(
                    rg.nextBoolean(), rg.nextBoolean()); (currentMatch = filteredPort.take()) != null;)
                assertTrue(currentMatch.getScore() >= scoreThreshold);
        }
    }
}
