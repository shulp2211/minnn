package com.milaboratory.mist.pattern;

import cc.redberry.pipe.OutputPort;
import com.milaboratory.core.sequence.NSequenceWithQuality;
import com.milaboratory.core.sequence.NucleotideSequence;
import com.milaboratory.test.TestUtil;
import org.junit.Test;

import static com.milaboratory.mist.util.CommonTestUtils.*;
import static org.junit.Assert.*;

public class StickFilterTest {
    @Test
    public void randomTest() throws Exception {
        for (int i = 0; i < 1000; i++) {
            boolean left = rg.nextBoolean();
            int position = rg.nextInt(30);
            String seq = TestUtil.randomSequence(NucleotideSequence.ALPHABET, 1, 300).toString();
            NSequenceWithQuality target = new NSequenceWithQuality(seq);
            NucleotideSequence motif = TestUtil.randomSequence(NucleotideSequence.ALPHABET, 1, 20);
            FuzzyMatchPattern pattern = new FuzzyMatchPattern(getTestPatternAligner(rg.nextInt(5)), motif);
            FilterPattern filterPattern = new FilterPattern(getTestPatternAligner(),
                    new StickFilter(left, position), pattern);
            Match patternBestMatch = pattern.match(target).getBestMatch(true);
            boolean mustMatch = (patternBestMatch != null)
                    && ((left && (patternBestMatch.getRange().getFrom() == position))
                    || (!left && (patternBestMatch.getRange().getTo() == position + 1)));

            assertTrue(!mustMatch || (filterPattern.match(target).getBestMatch(true) != null));
            assertTrue(countMatches(pattern.match(target), true)
                    >= countMatches(filterPattern.match(target), true));
            OutputPort<Match> filteredPort = filterPattern.match(target).getMatches(rg.nextBoolean());
            streamPort(filteredPort).forEach(match -> assertTrue(
                    (left && (match.getRange().getFrom() == position))
                            || (!left && (match.getRange().getTo() == position + 1))));
        }
    }
}
