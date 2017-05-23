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
import static com.milaboratory.mist.util.CommonTestUtils.makeRandomErrors;
import static org.junit.Assert.*;

public class OrPatternTest {
    @Rule
    public final ExpectedException exception = ExpectedException.none();

    @Test
    public void simpleTest() throws Exception {
        FuzzyMatchPattern pattern1 = new FuzzyMatchPattern(new NucleotideSequence("ATTAGACA"));
        FuzzyMatchPattern pattern2 = new FuzzyMatchPattern(new NucleotideSequence("TTTAG"));
        FuzzyMatchPattern pattern3 = new FuzzyMatchPattern(new NucleotideSequence("AGTA"));
        FuzzyMatchPattern pattern4 = new FuzzyMatchPattern(new NucleotideSequence("AGTAG"));
        NSequenceWithQuality nseq1 = new NSequenceWithQuality("ACTGCGATAAATTAGACGTACGTAA");
        NSequenceWithQuality nseq2 = new NSequenceWithQuality("TTAGTAGAGTATTTAGAGA");
        NSequenceWithQuality nseq3 = new NSequenceWithQuality("ATTAGACAAGTAATTAGACATTAG");
        OrPattern orPattern1 = new OrPattern(pattern1, pattern3);
        OrPattern orPattern2 = new OrPattern(pattern2, pattern3);
        OrPattern orPattern3 = new OrPattern(pattern2, pattern1, pattern3);
        OrPattern orPattern4 = new OrPattern(pattern4);
        OrPattern orPattern5 = new OrPattern();
        OrPattern orPattern6 = new OrPattern(pattern1);

        assertEquals(false, orPattern1.match(nseq1).isFound());
        assertEquals(false, orPattern1.match(nseq1, 0, 25, (byte)1).isFound());
        assertEquals(false, orPattern1.match(nseq1, new Range(0, 25)).isFound());
        assertEquals(true, orPattern2.match(nseq3).isFound());
        assertEquals(true, orPattern2.match(nseq3, 0, 24, (byte)1).isFound());
        assertEquals(true, orPattern2.match(nseq3, new Range(0, 24)).isFound());
        assertEquals(false, orPattern4.match(nseq3, new Range(0, 24), (byte)-1).isFound());
        assertEquals(true, orPattern3.match(nseq3, new Range(0, 24), (byte)-1).isFound());
        assertEquals(false, orPattern3.match(nseq1).isFound());
        assertEquals(false, orPattern6.match(nseq2).isFound());
        assertEquals(true, orPattern6.match(nseq3).isFound());
        assertEquals(false, orPattern2.match(nseq3, new Range(12, 21)).isFound());

        assertEquals(new Range(0, 8), orPattern3.match(nseq3, new Range(0, 24), (byte)-1).getBestMatch().getRange());
        assertEquals(new Range(2, 6), orPattern2.match(nseq2, new Range(1, 17)).getMatches(false, true).take().getRange());
        assertEquals(null, orPattern2.match(nseq3, new Range(12, 21)).getBestMatch());

        exception.expect(IllegalArgumentException.class);
        orPattern5.match(nseq1).getBestMatch();
    }

    @Test
    public void randomMatchTest() throws Exception {
        int its = TestUtil.its(1000, 10000);
        for (int i = 0; i < its; ++i) {
            NucleotideSequence seqLeft = TestUtil.randomSequence(NucleotideSequence.ALPHABET, 0, 40);
            NucleotideSequence seqMotif1 = TestUtil.randomSequence(NucleotideSequence.ALPHABET, 1, 60);
            NucleotideSequence seqMotif2 = TestUtil.randomSequence(NucleotideSequence.ALPHABET, 1, 60);
            NucleotideSequence seqRight = TestUtil.randomSequence(NucleotideSequence.ALPHABET, 0, 40);
            NucleotideSequence fullSeq = SequencesUtils.concatenate(seqLeft, seqMotif1, seqRight);
            NSequenceWithQuality target = new NSequenceWithQuality(fullSeq,
                    SequenceQuality.getUniformQuality(SequenceQuality.GOOD_QUALITY_VALUE, fullSeq.getSequence().size()));
            FuzzyMatchPattern patternMotif1 = new FuzzyMatchPattern(seqMotif1);
            FuzzyMatchPattern patternMotif2 = new FuzzyMatchPattern(seqMotif2);
            OrPattern orPattern1 = new OrPattern(patternMotif2, patternMotif1);
            OrPattern orPattern2 = new OrPattern(patternMotif1, patternMotif2);
            OrPattern orPattern3 = new OrPattern(patternMotif1, patternMotif1);
            assertTrue(orPattern1.match(target).isFound());
            assertTrue(orPattern2.match(target).isFound());
            assertTrue(orPattern3.match(target).isFound());
            assertNotNull(orPattern1.match(target).getBestMatch(false));
            assertNotNull(orPattern1.match(target).getMatches(true, false).take());
            assertNotNull(orPattern1.match(target).getMatches(false, false).take());
            assertNotNull(orPattern1.match(target).getBestMatch(true));
            assertNotNull(orPattern1.match(target).getMatches(true, true).take());
            assertNotNull(orPattern1.match(target).getMatches(false, true).take());

            NSequenceWithQuality foundSequence = orPattern1.match(target).getBestMatch(true).getValue();
            assertTrue(foundSequence.getSequence().toString().equals(
                    patternMotif1.match(target).getBestMatch(true).getValue().getSequence().toString())
                    || foundSequence.getSequence().toString().equals(
                    patternMotif2.match(target).getBestMatch(true).getValue().getSequence().toString()));

            foundSequence = orPattern1.match(target).getBestMatch(false).getValue();
            assertTrue(foundSequence.getSequence().toString().equals(
                    patternMotif1.match(target).getBestMatch(false).getValue().getSequence().toString())
                    || foundSequence.getSequence().toString().equals(
                    patternMotif2.match(target).getBestMatch(false).getValue().getSequence().toString()));
        }
    }

    @Test
    public void allMatchesTest() throws Exception {
        FuzzyMatchPattern pattern1 = new FuzzyMatchPattern(new NucleotideSequence("ATTA"));
        FuzzyMatchPattern pattern2 = new FuzzyMatchPattern(new NucleotideSequence("GACA"));
        NSequenceWithQuality nseq = new NSequenceWithQuality("GACATTATTATTAGACAGACATTAGACATTATTAGACAGACATTAATTA");
        OrPattern orPattern1 = new OrPattern(pattern1, pattern2);
        OrPattern orPattern2 = new OrPattern(pattern1, pattern1, pattern2);
        assertNotNull(orPattern1.match(nseq).getBestMatch());
        assertNotNull(orPattern2.match(nseq).getBestMatch());
        assertEquals(48, countMatches(orPattern1.match(nseq), true));
        assertEquals(384, countMatches(orPattern2.match(nseq), true));
        for (boolean byScore : new boolean[] {true, false}) {
            OutputPort<Match> matchesPattern1 = orPattern1.match(nseq).getMatches(byScore, true);
            OutputPort<Match> matchesPattern2 = orPattern2.match(nseq).getMatches(byScore, true);
            for (int i = 0; i < 48; i++) {
                assertNotNull(matchesPattern1.take().getValue());
            }
            assertNull(matchesPattern1.take());
            for (int i = 0; i < 384; i++) {
                assertNotNull(matchesPattern2.take().getValue());
            }
            assertNull(matchesPattern2.take());
        }
    }

    @Test
    public void quickSearchTest() throws Exception {
        FuzzyMatchPattern pattern1 = new FuzzyMatchPattern(new NucleotideSequence("ATA"));
        FuzzyMatchPattern pattern2 = new FuzzyMatchPattern(new NucleotideSequence("GAT"));
        OrPattern orPattern = new OrPattern(pattern1, pattern2);
        NSequenceWithQuality nseq1 = new NSequenceWithQuality("ATATATATTATA");
        NSequenceWithQuality nseq2 = new NSequenceWithQuality("GCGGTGCGTAGCG");
        MatchingResult match1 = orPattern.match(nseq1);
        MatchingResult match2 = orPattern.match(nseq2);
        assertEquals(true, match1.isFound());
        assertEquals(false, match2.isFound());
        assertEquals(4, countMatches(match1, true));
        assertEquals(0, countMatches(match2, true));
        match1 = orPattern.match(nseq1);
        match2 = orPattern.match(nseq2);
        assertEquals(true, match1.isFound());
        assertEquals(false, match2.isFound());
        assertNotNull(match1.getMatches(true, true).take());
        assertNotNull(match1.getMatches(false, true).take());
        assertNotNull(match1.getMatches().take());
        assertNotNull(match2.getMatches(true, true));
        assertNotNull(match2.getMatches(false, true));
        assertNotNull(match2.getMatches());
        assertNull(match2.getMatches().take());
        assertEquals(4, countMatches(match1, true));
        assertEquals(0, countMatches(match2, true));
    }

    @Test
    public void groupsTest() throws Exception {
        ArrayList<GroupEdgePosition> groupEdgePositions = new ArrayList<GroupEdgePosition>() {{
            add(new GroupEdgePosition(new GroupEdge("1", true), 0));
            add(new GroupEdgePosition(new GroupEdge("1", false), 1));
            add(new GroupEdgePosition(new GroupEdge("2", true), 1));
            add(new GroupEdgePosition(new GroupEdge("2", false), 3));
            add(new GroupEdgePosition(new GroupEdge("4", true), 4));
            add(new GroupEdgePosition(new GroupEdge("4", false), 5));
        }};

        FuzzyMatchPattern pattern1 = new FuzzyMatchPattern(new NucleotideSequence("TAGCC"), groupEdgePositions);
        FuzzyMatchPattern pattern2 = new FuzzyMatchPattern(new NucleotideSequence("CAGATGCA"), groupEdgePositions);
        OrPattern orPattern = new OrPattern(pattern1, pattern2);
        NSequenceWithQuality nseq = new NSequenceWithQuality("AAACAGATGCAGACATAGCC");
        MatchingResult result = orPattern.match(nseq);
        OutputPort<Match> matchOutputPort = result.getMatches(false, true);
        Match match = matchOutputPort.take();
        assertEquals(4, match.getMatchedGroupEdge("2", true)
                .getPosition());
        assertEquals(4, match.getMatchedGroupEdge("1", false)
                .getPosition());
        assertEquals("4", match.getMatchedGroupEdge("4", true)
                .getGroupName());
        assertNull(matchOutputPort.take());
    }

    @Test
    public void maxErrorsRandomTest() throws Exception {
        int its = TestUtil.its(1000, 2000);
        Random randomGenerator = new Random();
        for (int i = 0; i < its; ++i) {
            int targetLength = randomGenerator.nextInt(100) + 1;
            NucleotideSequence target = TestUtil.randomSequence(NucleotideSequence.ALPHABET, targetLength, targetLength);
            NucleotideSequence motif1 = TestUtil.randomSequence(NucleotideSequence.ALPHABET, 1, 50);
            NucleotideSequence motif2 = TestUtil.randomSequence(NucleotideSequence.ALPHABET, 1, 50);
            NSequenceWithQuality targetQ = new NSequenceWithQuality(target,
                    SequenceQuality.getUniformQuality(SequenceQuality.GOOD_QUALITY_VALUE, target.getSequence().size()));
            int maxErrors = randomGenerator.nextInt(10);
            NucleotideSequence motif1WithErrors = makeRandomErrors(motif1, maxErrors);
            NucleotideSequence motif2WithErrors = makeRandomErrors(motif2, maxErrors);
            FuzzyMatchPattern pattern1 = new FuzzyMatchPattern(motif1WithErrors, maxErrors);
            FuzzyMatchPattern pattern2 = new FuzzyMatchPattern(motif2WithErrors, maxErrors);
            boolean targetContainsPattern1 = target.toString().contains(motif1.toString());
            boolean targetContainsPattern2 = target.toString().contains(motif2.toString());
            boolean isMatchingPattern1 = pattern1.match(targetQ).isFound();
            boolean isMatchingPattern2 = pattern2.match(targetQ).isFound();

            if (targetContainsPattern1) {
                assertTrue(pattern1.match(targetQ).isFound());
                assertTrue(pattern1.match(targetQ).getBestMatch(false) != null);
                assertTrue(pattern1.match(targetQ).getMatches(true, false).take() != null);
                assertTrue(pattern1.match(targetQ).getMatches(false, false).take() != null);
                assertTrue(pattern1.match(targetQ).getBestMatch(true) != null);
                assertTrue(pattern1.match(targetQ).getMatches(true, true).take() != null);
                assertTrue(pattern1.match(targetQ).getMatches(false, true).take() != null);
            }

            if (targetContainsPattern2) {
                assertTrue(pattern2.match(targetQ).isFound());
                assertTrue(pattern2.match(targetQ).getBestMatch(false) != null);
                assertTrue(pattern2.match(targetQ).getMatches(true, false).take() != null);
                assertTrue(pattern2.match(targetQ).getMatches(false, false).take() != null);
                assertTrue(pattern2.match(targetQ).getBestMatch(true) != null);
                assertTrue(pattern2.match(targetQ).getMatches(true, true).take() != null);
                assertTrue(pattern2.match(targetQ).getMatches(false, true).take() != null);
            }

            OrPattern orPattern = new OrPattern(pattern1, pattern2);
            boolean orMustBeMatching = isMatchingPattern1 || isMatchingPattern2;

            assertEquals(orMustBeMatching, orPattern.match(targetQ).isFound());
            assertEquals(orMustBeMatching, orPattern.match(targetQ).getBestMatch(false) != null);
            assertEquals(orMustBeMatching, orPattern.match(targetQ).getMatches(true, false).take() != null);
            assertEquals(orMustBeMatching, orPattern.match(targetQ).getMatches(false, false).take() != null);
            assertEquals(orMustBeMatching, orPattern.match(targetQ).getBestMatch(true) != null);
            assertEquals(orMustBeMatching, orPattern.match(targetQ).getMatches(true, true).take() != null);
            assertEquals(orMustBeMatching, orPattern.match(targetQ).getMatches(false, true).take() != null);
        }
    }

    @Test
    public void scoringRandomTest() throws Exception {
        int its = TestUtil.its(100, 200);
        for (int i = 0; i < its; ++i) {
            NucleotideSequence motif1 = TestUtil.randomSequence(NucleotideSequence.ALPHABET, 5, 50);
            NucleotideSequence motif2 = TestUtil.randomSequence(NucleotideSequence.ALPHABET, 5, 50);
            NucleotideSequence target = SequencesUtils.concatenate(motif1, motif2);
            NSequenceWithQuality targetQ = new NSequenceWithQuality(target,
                    SequenceQuality.getUniformQuality(SequenceQuality.GOOD_QUALITY_VALUE, target.getSequence().size()));
            FuzzyMatchPattern pattern1 = new FuzzyMatchPattern(motif1, 0);
            FuzzyMatchPattern pattern2 = new FuzzyMatchPattern(motif2, 0);
            OrPattern orPattern1 = new OrPattern(pattern1, pattern2);
            OrPattern orPattern2 = new OrPattern(pattern2, pattern1);
            assertEquals(Math.max(pattern1.match(targetQ).getBestMatch().getScore(),
                    pattern2.match(targetQ).getBestMatch().getScore()),
                    orPattern1.match(targetQ).getBestMatch().getScore(), 0.0001);
            assertEquals(orPattern1.match(targetQ).getBestMatch().getScore(),
                    orPattern2.match(targetQ).getBestMatch().getScore(), 0.0001);
        }
    }
}
