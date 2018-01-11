package com.milaboratory.mist.pattern;

import cc.redberry.pipe.OutputPort;
import com.milaboratory.core.sequence.*;
import com.milaboratory.test.TestUtil;
import org.junit.*;

import static com.milaboratory.mist.util.CommonTestUtils.*;
import static org.junit.Assert.*;

public class ScoreFilterTest {
    @BeforeClass
    public static void init() throws Exception {
        PatternAligner.allowValuesOverride();
    }

    @Test
    public void randomTest() throws Exception {
        for (int i = 0; i < 1000; i++) {
            int scoreThreshold = -rg.nextInt(100);
            PatternAligner.init(getTestScoring(), -1, rg.nextInt(5), -1);
            String seq = TestUtil.randomSequence(NucleotideSequence.ALPHABET, 1, 300).toString();
            NSequenceWithQuality target = new NSequenceWithQuality(seq);
            MultiNSequenceWithQuality multiTarget = createMultiNSeq(seq, 2);
            NucleotideSequenceCaseSensitive motif = TestUtil.randomSequence(NucleotideSequenceCaseSensitive.ALPHABET,
                    1, 20);
            FuzzyMatchPattern pattern = new FuzzyMatchPattern(Long.MIN_VALUE, motif);
            FilterPattern filterPattern = new FilterPattern(Long.MIN_VALUE, new ScoreFilter(scoreThreshold), pattern);
            MultiPattern multiPattern = new MultiPattern(Long.MIN_VALUE, pattern, filterPattern);
            MultipleReadsFilterPattern mFilterPattern = new MultipleReadsFilterPattern(Long.MIN_VALUE,
                    new ScoreFilter(scoreThreshold * 2), multiPattern);
            AndOperator andOperator = new AndOperator(Long.MIN_VALUE, multiPattern, mFilterPattern);
            Match patternBestMatch = pattern.match(target).getBestMatch(true);
            boolean isMatching = (patternBestMatch != null) && (patternBestMatch.getScore() >= scoreThreshold);

            assertEquals(isMatching, filterPattern.match(target).getBestMatch(true) != null);
            if (countMatches(multiPattern.match(multiTarget), true) < 300)
                assertEquals(isMatching, andOperator.match(multiTarget).getBestMatch(true) != null);
            assertTrue(countMatches(pattern.match(target), true)
                    >= countMatches(filterPattern.match(target), true));
            OutputPort<MatchIntermediate> filteredPort = filterPattern.match(target).getMatches(rg.nextBoolean());
            streamPort(filteredPort).forEach(match -> assertTrue(match.getScore() >= scoreThreshold));
        }
    }
}
