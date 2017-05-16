package com.milaboratory.mist.pattern;

import cc.redberry.pipe.OutputPort;
import com.milaboratory.core.sequence.NSequenceWithQuality;
import com.milaboratory.core.sequence.NucleotideSequence;
import com.milaboratory.core.sequence.SequenceQuality;
import com.milaboratory.test.TestUtil;
import org.junit.Test;

import java.util.Random;

import static com.milaboratory.mist.pattern.MatchUtils.countMatches;
import static org.junit.Assert.*;

public class ScoreFilterTest {
    @Test
    public void randomTest() throws Exception {
        Random randomGenerator = new Random();
        int its = TestUtil.its(1000, 2000);
        for (int i = 0; i < its; ++i) {
            float scoreThreshold = randomGenerator.nextFloat() * 100 - 50;
            NucleotideSequence target = TestUtil.randomSequence(NucleotideSequence.ALPHABET, 1, 1000);
            NucleotideSequence motif = TestUtil.randomSequence(NucleotideSequence.ALPHABET, 1, 10);
            NSequenceWithQuality targetQ = new NSequenceWithQuality(target,
                    SequenceQuality.getUniformQuality(SequenceQuality.GOOD_QUALITY_VALUE, target.getSequence().size()));
            FuzzyMatchPattern pattern = new FuzzyMatchPattern(motif);
            FilterPattern filterPattern = new FilterPattern(new ScoreFilter(scoreThreshold), pattern);
            boolean isMatching = pattern.match(targetQ).isFound()
                    && pattern.match(targetQ).getBestMatch().getScore() >= scoreThreshold;

            assertEquals(isMatching, filterPattern.match(targetQ).isFound());
            assertTrue(countMatches(pattern.match(targetQ)) >= countMatches(filterPattern.match(targetQ)));
            Match currentMatch;
            for (OutputPort<Match> filteredPort = filterPattern.match(targetQ).getMatches(
                    randomGenerator.nextBoolean(), randomGenerator.nextBoolean()); (currentMatch = filteredPort.take()) != null;)
                assertTrue(currentMatch.getScore() >= scoreThreshold);
        }
    }
}
