package com.milaboratory.mist.pattern;

import cc.redberry.pipe.OutputPort;
import com.milaboratory.core.sequence.*;
import com.milaboratory.test.TestUtil;
import org.junit.*;

import static com.milaboratory.mist.util.CommonTestUtils.*;
import static org.junit.Assert.*;

public class StickFilterTest {
    @BeforeClass
    public static void init() throws Exception {
        PatternAligner.allowValuesOverride();
    }

    @Test
    public void randomTest() throws Exception {
        for (int i = 0; i < 1000; i++) {
            boolean left = rg.nextBoolean();
            int position = rg.nextInt(30);
            PatternAligner.init(getTestScoring(), -1, rg.nextInt(5), -1);
            String seq = TestUtil.randomSequence(NucleotideSequence.ALPHABET, 1, 300).toString();
            NSequenceWithQuality target = new NSequenceWithQuality(seq);
            NucleotideSequenceCaseSensitive motif = TestUtil.randomSequence(NucleotideSequenceCaseSensitive.ALPHABET,
                    1, 20);
            FuzzyMatchPattern pattern = new FuzzyMatchPattern(Long.MIN_VALUE, motif);
            FilterPattern filterPattern = new FilterPattern(Long.MIN_VALUE, new StickFilter(left, position), pattern);
            MatchIntermediate patternBestMatch = pattern.match(target).getBestMatch(true);
            boolean mustMatch = (patternBestMatch != null)
                    && ((left && (patternBestMatch.getRange().getFrom() == position))
                    || (!left && (patternBestMatch.getRange().getTo() == position + 1)));

            assertTrue(!mustMatch || (filterPattern.match(target).getBestMatch(true) != null));
            assertTrue(countMatches(pattern.match(target), true)
                    >= countMatches(filterPattern.match(target), true));
            OutputPort<MatchIntermediate> filteredPort = filterPattern.match(target).getMatches(rg.nextBoolean());
            streamPort(filteredPort).forEach(match -> assertTrue(
                    (left && (match.getRange().getFrom() == position))
                            || (!left && (match.getRange().getTo() == position + 1))));
        }
    }
}
