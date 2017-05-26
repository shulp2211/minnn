package com.milaboratory.mist.pattern;

import cc.redberry.pipe.OutputPort;
import com.milaboratory.core.Range;
import com.milaboratory.core.sequence.NSequenceWithQuality;
import com.milaboratory.core.sequence.NucleotideSequence;
import com.milaboratory.core.sequence.SequenceQuality;
import com.milaboratory.core.sequence.SequencesUtils;
import com.milaboratory.test.TestUtil;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;

import java.util.ArrayList;
import java.util.Random;

import static com.milaboratory.mist.pattern.MatchUtils.countMatches;
import static com.milaboratory.mist.util.CommonTestUtils.getRandomSubsequence;
import static com.milaboratory.mist.util.CommonTestUtils.getTestPatternAligner;
import static com.milaboratory.mist.util.CommonTestUtils.makeRandomErrors;
import static com.milaboratory.mist.util.RangeTools.getIntersectionLength;
import static org.junit.Assert.*;

public class PlusPatternTest {
    @Rule
    public final ExpectedException exception = ExpectedException.none();

    @Test
    public void simpleTest() throws Exception {
        FuzzyMatchPattern pattern1 = new FuzzyMatchPattern(getTestPatternAligner(), new NucleotideSequence("ATTAGACA"));
        FuzzyMatchPattern pattern2 = new FuzzyMatchPattern(getTestPatternAligner(), new NucleotideSequence("TTAG"));
        FuzzyMatchPattern pattern3 = new FuzzyMatchPattern(getTestPatternAligner(), new NucleotideSequence("AGTA"));
        FuzzyMatchPattern pattern4 = new FuzzyMatchPattern(getTestPatternAligner(), new NucleotideSequence("AGTAG"));
        NSequenceWithQuality nseq1 = new NSequenceWithQuality("ACTGCGATAAATTAGACAGTACGTA");
        NSequenceWithQuality nseq2 = new NSequenceWithQuality("TTAGTAGAGTATTTAGAGA");
        NSequenceWithQuality nseq3 = new NSequenceWithQuality("ATTAGACAAGTAATTAGACATTAG");
        PlusPattern plusPattern1 = new PlusPattern(getTestPatternAligner(), pattern1, pattern3);
        PlusPattern plusPattern2 = new PlusPattern(getTestPatternAligner(), pattern2, pattern3);
        PlusPattern plusPattern3 = new PlusPattern(getTestPatternAligner(), pattern2, pattern1, pattern3);
        PlusPattern plusPattern4 = new PlusPattern(getTestPatternAligner(), pattern4);
        PlusPattern plusPattern5 = new PlusPattern(getTestPatternAligner());
        PlusPattern plusPattern6 = new PlusPattern(getTestPatternAligner(), pattern1);
        PlusPattern plusPattern7 = new PlusPattern(getTestPatternAligner(), pattern1, pattern3, pattern2);

        assertEquals(false, plusPattern1.match(nseq1).isFound());
        assertEquals(false, plusPattern1.match(nseq1, 0, 25, (byte)1).isFound());
        assertEquals(false, plusPattern1.match(nseq1, new Range(0, 25)).isFound());
        assertEquals(true, plusPattern2.match(nseq1).isFound());
        assertEquals(true, plusPattern2.match(nseq1, 0, 25, (byte)1).isFound());
        assertEquals(true, plusPattern2.match(nseq1, new Range(0, 25)).isFound());
        assertEquals(false, plusPattern4.match(nseq3, new Range(0, 24), (byte)-1).isFound());
        assertEquals(false, plusPattern3.match(nseq3, new Range(0, 24), (byte)-1).isFound());
        assertEquals(true, plusPattern7.match(nseq3, new Range(0, 24), (byte)-1).isFound());
        assertEquals(false, plusPattern3.match(nseq1).isFound());
        assertEquals(false, plusPattern6.match(nseq2).isFound());
        assertEquals(true, plusPattern6.match(nseq1).isFound());
        assertEquals(false, plusPattern2.match(nseq1, new Range(12, 21)).isFound());

        assertEquals(new Range(0, 17), plusPattern7.match(nseq3, new Range(0, 24), (byte)-1).getBestMatch().getRange());
        assertEquals(new Range(11, 21), plusPattern2.match(nseq1, new Range(1, 21)).getBestMatch().getRange());
        assertEquals(null, plusPattern2.match(nseq1, new Range(11, 20)).getBestMatch());

        exception.expect(IllegalArgumentException.class);
        plusPattern5.match(nseq1).getBestMatch();
    }

    @Test
    public void randomMatchTest() throws Exception {
        int its = TestUtil.its(1000, 10000);
        for (int i = 0; i < its; ++i) {
            NucleotideSequence seqLeft = TestUtil.randomSequence(NucleotideSequence.ALPHABET, 0, 40);
            NucleotideSequence seqMotif1 = TestUtil.randomSequence(NucleotideSequence.ALPHABET, 1, 60);
            NucleotideSequence seqMiddle = TestUtil.randomSequence(NucleotideSequence.ALPHABET, 0, 40);
            NucleotideSequence seqMotif2 = TestUtil.randomSequence(NucleotideSequence.ALPHABET, 1, 60);
            NucleotideSequence seqRight = TestUtil.randomSequence(NucleotideSequence.ALPHABET, 0, 40);
            NucleotideSequence fullSeq = SequencesUtils.concatenate(seqLeft, seqMotif1, seqMiddle, seqMotif2, seqRight);
            NSequenceWithQuality target = new NSequenceWithQuality(fullSeq,
                    SequenceQuality.getUniformQuality(SequenceQuality.GOOD_QUALITY_VALUE, fullSeq.getSequence().size()));
            FuzzyMatchPattern patternMotif1 = new FuzzyMatchPattern(getTestPatternAligner(), seqMotif1);
            FuzzyMatchPattern patternMotif2 = new FuzzyMatchPattern(getTestPatternAligner(), seqMotif2);
            PlusPattern plusPattern = new PlusPattern(getTestPatternAligner(), patternMotif1, patternMotif2);
            assertTrue(plusPattern.match(target).isFound());
            assertNotNull(plusPattern.match(target).getBestMatch(false));
            assertNotNull(plusPattern.match(target).getMatches(true, false).take());
            assertNotNull(plusPattern.match(target).getMatches(false, false).take());
            assertNotNull(plusPattern.match(target).getBestMatch(true));
            assertNotNull(plusPattern.match(target).getMatches(true, true).take());
            assertNotNull(plusPattern.match(target).getMatches(false, true).take());

            NSequenceWithQuality foundSequence = plusPattern.match(target).getBestMatch(true).getValue();
            assertTrue(patternMotif1.match(foundSequence).isFound());
            assertTrue(patternMotif2.match(foundSequence).isFound());

            foundSequence = plusPattern.match(target).getBestMatch(false).getValue();
            assertTrue(patternMotif1.match(foundSequence).isFound());
            assertTrue(patternMotif2.match(foundSequence).isFound());
        }
    }

    @Test
    public void allMatchesTest() throws Exception {
        FuzzyMatchPattern pattern1 = new FuzzyMatchPattern(getTestPatternAligner(), new NucleotideSequence("ATTA"));
        FuzzyMatchPattern pattern2 = new FuzzyMatchPattern(getTestPatternAligner(), new NucleotideSequence("GACA"));
        NSequenceWithQuality nseq = new NSequenceWithQuality("GACATTATTATTAGACAGACATTAGACATTATTAGACAGACATTAATTA");
        PlusPattern plusPattern1 = new PlusPattern(getTestPatternAligner(), pattern1, pattern2);
        PlusPattern plusPattern2 = new PlusPattern(getTestPatternAligner(), pattern1, pattern1, pattern2);
        assertNotNull(plusPattern1.match(nseq).getBestMatch());
        assertNotNull(plusPattern2.match(nseq).getBestMatch());
        assertEquals(22, countMatches(plusPattern1.match(nseq), true));
        assertEquals(30, countMatches(plusPattern2.match(nseq), true));
        for (boolean byScore : new boolean[] {true, false}) {
            OutputPort<Match> matchesPattern1 = plusPattern1.match(nseq).getMatches(byScore, true);
            OutputPort<Match> matchesPattern2 = plusPattern2.match(nseq).getMatches(byScore, true);
            for (int i = 0; i < 22; i++) {
                assertNotNull(matchesPattern1.take().getValue());
            }
            assertNull(matchesPattern1.take());
            for (int i = 0; i < 30; i++) {
                assertNotNull(matchesPattern2.take().getValue());
            }
            assertNull(matchesPattern2.take());
        }
    }

    @Test
    public void matchesIntersectionTest() throws Exception {
        FuzzyMatchPattern pattern1 = new FuzzyMatchPattern(getTestPatternAligner(), new NucleotideSequence("ATA"));
        FuzzyMatchPattern pattern2 = new FuzzyMatchPattern(getTestPatternAligner(), new NucleotideSequence("TAT"));
        PlusPattern plusPattern = new PlusPattern(getTestPatternAligner(), pattern1, pattern2);
        NSequenceWithQuality nseq = new NSequenceWithQuality("ATATATATTATA");
        OutputPort<Match> matches = plusPattern.match(nseq).getMatches(false, true);
        while (true) {
            Match match = matches.take();
            if (match == null) break;
            String seq = match.getValue().getSequence().toString();
            Range range = match.getRange();
            System.out.println(seq + " " + range.getLower() + " " + range.getUpper());
        }
        assertEquals(6, countMatches(plusPattern.match(nseq), true));
    }

    @Test
    public void quickSearchTest() throws Exception {
        FuzzyMatchPattern pattern1 = new FuzzyMatchPattern(getTestPatternAligner(), new NucleotideSequence("ATA"));
        FuzzyMatchPattern pattern2 = new FuzzyMatchPattern(getTestPatternAligner(), new NucleotideSequence("TAT"));
        PlusPattern plusPattern = new PlusPattern(getTestPatternAligner(), pattern1, pattern2);
        NSequenceWithQuality nseq1 = new NSequenceWithQuality("ATATATATTATA");
        NSequenceWithQuality nseq2 = new NSequenceWithQuality("GCGGTGCGTATAGCG");
        MatchingResult match1 = plusPattern.match(nseq1);
        MatchingResult match2 = plusPattern.match(nseq2);
        assertEquals(true, match1.isFound());
        assertEquals(false, match2.isFound());
        assertEquals(6, countMatches(match1, true));
        assertEquals(0, countMatches(match2, true));
        match1 = plusPattern.match(nseq1);
        match2 = plusPattern.match(nseq2);
        assertEquals(true, match1.isFound());
        assertEquals(false, match2.isFound());
        assertNotNull(match1.getMatches(true, true).take());
        assertNotNull(match1.getMatches(false, true).take());
        assertNotNull(match1.getMatches().take());
        assertNotNull(match2.getMatches(true, true));
        assertNotNull(match2.getMatches(false, true));
        assertNotNull(match2.getMatches());
        assertNull(match2.getMatches().take());
        assertEquals(6, countMatches(match1, true));
        assertEquals(0, countMatches(match2, true));
    }

    @Test
    public void groupsTest() throws Exception {
        ArrayList<GroupEdgePosition> groupsEdgePositions1 = new ArrayList<GroupEdgePosition>() {{
            add(new GroupEdgePosition(new GroupEdge("1", true), 0));
            add(new GroupEdgePosition(new GroupEdge("1", false), 1));
            add(new GroupEdgePosition(new GroupEdge("2", true), 1));
            add(new GroupEdgePosition(new GroupEdge("2", false), 3));
            add(new GroupEdgePosition(new GroupEdge("4", false), 5));
        }};

        ArrayList<GroupEdgePosition> groupsEdgePositions2 = new ArrayList<GroupEdgePosition>() {{
            add(new GroupEdgePosition(new GroupEdge("3", true), 1));
            add(new GroupEdgePosition(new GroupEdge("3", false), 3));
            add(new GroupEdgePosition(new GroupEdge("4", true), 4));
            add(new GroupEdgePosition(new GroupEdge("5", true), 5));
            add(new GroupEdgePosition(new GroupEdge("5", false), 6));
        }};

        FuzzyMatchPattern pattern1 = new FuzzyMatchPattern(getTestPatternAligner(),
                new NucleotideSequence("TAGCC"), groupsEdgePositions1);
        FuzzyMatchPattern pattern2 = new FuzzyMatchPattern(getTestPatternAligner(),
                new NucleotideSequence("CAGATGCA"), groupsEdgePositions2);
        PlusPattern plusPattern = new PlusPattern(getTestPatternAligner(), pattern2, pattern1);
        NSequenceWithQuality nseq = new NSequenceWithQuality("AAACAGATGCAGACATAGCC");
        MatchingResult result = plusPattern.match(nseq);
        OutputPort<Match> matchOutputPort = result.getMatches(false, true);
        Match match = matchOutputPort.take();
        assertEquals(16, match.getMatchedGroupEdge("2", true)
                .getPosition());
        assertEquals(20, match.getMatchedGroupEdge("4", false)
                .getPosition());
        assertEquals("3", match.getMatchedGroupEdge("3", true)
                .getGroupName());
        assertNull(matchOutputPort.take());
    }

    @Test
    public void maxErrorsRandomTest() throws Exception {
        int its = TestUtil.its(300, 500);
        Random randomGenerator = new Random();
        for (int i = 0; i < its; ++i) {
            int targetLength = randomGenerator.nextInt(63) + 1;
            NucleotideSequence target = TestUtil.randomSequence(NucleotideSequence.ALPHABET, targetLength, targetLength);
            NucleotideSequence motif1 = TestUtil.randomSequence(NucleotideSequence.ALPHABET, 1, 50);
            NucleotideSequence motif2 = getRandomSubsequence(target);
            NSequenceWithQuality targetQ = new NSequenceWithQuality(target,
                    SequenceQuality.getUniformQuality(SequenceQuality.GOOD_QUALITY_VALUE, target.getSequence().size()));
            int maxErrors = randomGenerator.nextInt(10);
            NucleotideSequence motif1WithErrors = makeRandomErrors(motif1, maxErrors);
            NucleotideSequence motif2WithErrors = makeRandomErrors(motif2, maxErrors);
            FuzzyMatchPattern pattern1 = new FuzzyMatchPattern(getTestPatternAligner(maxErrors), motif1WithErrors);
            FuzzyMatchPattern pattern2 = new FuzzyMatchPattern(getTestPatternAligner(maxErrors), motif2WithErrors);
            boolean targetContainsPattern1 = target.toString().contains(motif1.toString());
            boolean isMatchingPattern1 = pattern1.match(targetQ).isFound();

            if (targetContainsPattern1) {
                assertTrue(pattern1.match(targetQ).isFound());
                assertTrue(pattern1.match(targetQ).getBestMatch(false) != null);
                assertTrue(pattern1.match(targetQ).getMatches(true, false).take() != null);
                assertTrue(pattern1.match(targetQ).getMatches(false, false).take() != null);
                assertTrue(pattern1.match(targetQ).getBestMatch(true) != null);
                assertTrue(pattern1.match(targetQ).getMatches(true, true).take() != null);
                assertTrue(pattern1.match(targetQ).getMatches(false, true).take() != null);
            }

            assertTrue(pattern2.match(targetQ).isFound());
            assertTrue(pattern2.match(targetQ).getBestMatch(false) != null);
            assertTrue(pattern2.match(targetQ).getMatches(true, false).take() != null);
            assertTrue(pattern2.match(targetQ).getMatches(false, false).take() != null);
            assertTrue(pattern2.match(targetQ).getBestMatch(true) != null);
            assertTrue(pattern2.match(targetQ).getMatches(true, true).take() != null);
            assertTrue(pattern2.match(targetQ).getMatches(false, true).take() != null);

            int plusMaxErrors;
            if (isMatchingPattern1)
                plusMaxErrors = maxErrors * 2 + getIntersectionLength(pattern1.match(targetQ).getBestMatch(false)
                        .getRange(), pattern2.match(targetQ).getBestMatch(false).getRange());
            else {
                plusMaxErrors = maxErrors;
                if ((targetLength <= maxErrors) || (motif1WithErrors.size() <= maxErrors))
                    plusMaxErrors = 0;
            }
            /* TODO: make this test to check score threshold instead of max errors */
            PlusPattern plusPattern = new PlusPattern(getTestPatternAligner(), pattern1, pattern2);

            boolean misplacedPattern = isMatchingPattern1 && pattern1.match(targetQ).getBestMatch(false)
                    .getRange().getLower() >= pattern2.match(targetQ).getBestMatch(false).getRange().getLower();

            if (!misplacedPattern) {
                assertEquals(isMatchingPattern1, plusPattern.match(targetQ).isFound());
                assertEquals(isMatchingPattern1, plusPattern.match(targetQ).getBestMatch(false) != null);
                assertEquals(isMatchingPattern1, plusPattern.match(targetQ).getMatches(true, false).take() != null);
                assertEquals(isMatchingPattern1, plusPattern.match(targetQ).getMatches(false, false).take() != null);
                assertEquals(isMatchingPattern1, plusPattern.match(targetQ).getBestMatch(true) != null);
                assertEquals(isMatchingPattern1, plusPattern.match(targetQ).getMatches(true, true).take() != null);
                assertEquals(isMatchingPattern1, plusPattern.match(targetQ).getMatches(false, true).take() != null);
            } else {
                AndPattern andPattern = new AndPattern(getTestPatternAligner(), pattern1, pattern2);
                assertTrue(countMatches(andPattern.match(targetQ)) > countMatches(plusPattern.match(targetQ)));
            }
        }
    }

    @Test
    public void scoringRandomTest() throws Exception {
        int its = TestUtil.its(100, 200);
        Random randomGenerator = new Random();
        for (int i = 0; i < its; ++i) {
            int errorScorePenalty = -randomGenerator.nextInt(1000);
            NucleotideSequence leftPart = TestUtil.randomSequence(NucleotideSequence.ALPHABET, 5, 50);
            NucleotideSequence middleLetter = TestUtil.randomSequence(NucleotideSequence.ALPHABET, 1, 1);
            NucleotideSequence rightPart = TestUtil.randomSequence(NucleotideSequence.ALPHABET, 5, 50);
            NucleotideSequence motif1 = SequencesUtils.concatenate(leftPart, middleLetter);
            NucleotideSequence motif2 = SequencesUtils.concatenate(middleLetter, rightPart);
            NucleotideSequence target = SequencesUtils.concatenate(leftPart, middleLetter, rightPart);
            NSequenceWithQuality targetQ = new NSequenceWithQuality(target,
                    SequenceQuality.getUniformQuality(SequenceQuality.GOOD_QUALITY_VALUE, target.getSequence().size()));
            FuzzyMatchPattern pattern1 = new FuzzyMatchPattern(getTestPatternAligner(), motif1);
            FuzzyMatchPattern pattern2 = new FuzzyMatchPattern(getTestPatternAligner(), motif2);
            PlusPattern plusPattern1 = new PlusPattern(getTestPatternAligner(Integer.MIN_VALUE, 0, 0,
                    errorScorePenalty), pattern1, pattern2);
            PlusPattern plusPattern2 = new PlusPattern(getTestPatternAligner(Integer.MIN_VALUE, 0, 0,
                    errorScorePenalty), pattern2, pattern1);
            PlusPattern plusPattern3 = new PlusPattern(getTestPatternAligner(Integer.MIN_VALUE, 0, 0,
                    errorScorePenalty), pattern1, pattern2);
            PlusPattern plusPattern4 = new PlusPattern(getTestPatternAligner(Integer.MIN_VALUE, 0, 0,
                    errorScorePenalty), pattern2, pattern1);
            assertNull(plusPattern1.match(targetQ).getBestMatch());
            assertNull(plusPattern2.match(targetQ).getBestMatch());
            assertEquals(pattern1.match(targetQ).getBestMatch().getScore()
                    + pattern2.match(targetQ).getBestMatch().getScore() + errorScorePenalty,
                    plusPattern3.match(targetQ).getBestMatch().getScore());
            if (!leftPart.toString().equals(rightPart.toString()))
                assertNull(plusPattern4.match(targetQ).getBestMatch());
        }
    }
}
