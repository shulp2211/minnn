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

import java.util.HashMap;

import static com.milaboratory.mist.pattern.MatchUtils.countMatches;
import static org.junit.Assert.*;

public class AndPatternTest {
    @Rule
    public final ExpectedException exception = ExpectedException.none();

    @Test
    public void simpleTest() throws Exception {
        FuzzyMatchPattern pattern1 = new FuzzyMatchPattern(new NucleotideSequence("ATTAGACA"));
        FuzzyMatchPattern pattern2 = new FuzzyMatchPattern(new NucleotideSequence("TTAG"));
        FuzzyMatchPattern pattern3 = new FuzzyMatchPattern(new NucleotideSequence("AGTA"));
        FuzzyMatchPattern pattern4 = new FuzzyMatchPattern(new NucleotideSequence("AGTAG"));
        NSequenceWithQuality nseq1 = new NSequenceWithQuality("ACTGCGATAAATTAGACAGTACGTA");
        NSequenceWithQuality nseq2 = new NSequenceWithQuality("TTAGTAGAGTATTTAGAGA");
        NSequenceWithQuality nseq3 = new NSequenceWithQuality("ATTAGACAAGTAATTAGACATTAG");
        AndPattern andPattern1 = new AndPattern(0, -1, pattern1, pattern3);
        AndPattern andPattern2 = new AndPattern(0, -1, pattern2, pattern3);
        AndPattern andPattern3 = new AndPattern(0, -1, pattern2, pattern1, pattern3);
        AndPattern andPattern4 = new AndPattern(0, -1, pattern4);
        AndPattern andPattern5 = new AndPattern(0, -1);
        AndPattern andPattern6 = new AndPattern(0, -1, pattern1);

        assertEquals(false, andPattern1.match(nseq1).isFound());
        assertEquals(false, andPattern1.match(nseq1, 0, 25, (byte)1).isFound());
        assertEquals(false, andPattern1.match(nseq1, new Range(0, 25)).isFound());
        assertEquals(true, andPattern2.match(nseq1).isFound());
        assertEquals(true, andPattern2.match(nseq1, 0, 25, (byte)1).isFound());
        assertEquals(true, andPattern2.match(nseq1, new Range(0, 25)).isFound());
        assertEquals(false, andPattern4.match(nseq3, new Range(0, 24), (byte)-1).isFound());
        assertEquals(true, andPattern3.match(nseq3, new Range(0, 24), (byte)-1).isFound());
        assertEquals(false, andPattern3.match(nseq1).isFound());
        assertEquals(false, andPattern6.match(nseq2).isFound());
        assertEquals(true, andPattern6.match(nseq1).isFound());
        assertEquals(false, andPattern2.match(nseq1, new Range(12, 21)).isFound());

        assertEquals(new Range(0, 17), andPattern3.match(nseq3, new Range(0, 24), (byte)-1).getBestMatch().getRange());
        assertEquals(new Range(11, 21), andPattern2.match(nseq1, new Range(1, 21)).getBestMatch().getRange());
        assertEquals(null, andPattern2.match(nseq1, new Range(11, 20)).getBestMatch());

        exception.expect(IllegalArgumentException.class);
        andPattern5.match(nseq1).getBestMatch();
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
            FuzzyMatchPattern patternMotif1 = new FuzzyMatchPattern(seqMotif1);
            FuzzyMatchPattern patternMotif2 = new FuzzyMatchPattern(seqMotif2);
            AndPattern andPattern1 = new AndPattern(0, -1, patternMotif1, patternMotif2);
            AndPattern andPattern2 = new AndPattern(0, -1, patternMotif2, patternMotif1);
            assertTrue(andPattern1.match(target).isFound());
            assertTrue(andPattern2.match(target).isFound());
            assertNotNull(andPattern1.match(target).getBestMatch(false));
            assertNotNull(andPattern1.match(target).getMatches(true, false).take());
            assertNotNull(andPattern1.match(target).getMatches(false, false).take());
            assertNotNull(andPattern1.match(target).getBestMatch(true));
            assertNotNull(andPattern1.match(target).getMatches(true, true).take());
            assertNotNull(andPattern1.match(target).getMatches(false, true).take());

            NSequenceWithQuality foundSequence = andPattern1.match(target).getBestMatch(true).getValue();
            assertTrue(patternMotif1.match(foundSequence).isFound());
            assertTrue(patternMotif2.match(foundSequence).isFound());

            foundSequence = andPattern1.match(target).getBestMatch(false).getValue();
            assertTrue(patternMotif1.match(foundSequence).isFound());
            assertTrue(patternMotif2.match(foundSequence).isFound());
        }
    }

    @Test
    public void allMatchesTest() throws Exception {
        FuzzyMatchPattern pattern1 = new FuzzyMatchPattern(new NucleotideSequence("ATTA"));
        FuzzyMatchPattern pattern2 = new FuzzyMatchPattern(new NucleotideSequence("GACA"));
        NSequenceWithQuality nseq = new NSequenceWithQuality("GACATTATTATTAGACAGACATTAGACATTATTAGACAGACATTAATTA");
        AndPattern andPattern1 = new AndPattern(0, -1, pattern1, pattern2);
        AndPattern andPattern2 = new AndPattern(0, -1, pattern1, pattern1, pattern2);
        assertNotNull(andPattern1.match(nseq).getBestMatch());
        assertNotNull(andPattern2.match(nseq).getBestMatch());
        assertEquals(44, countMatches(andPattern1.match(nseq), true));
        assertEquals(248, countMatches(andPattern2.match(nseq), true));
        for (boolean byScore : new boolean[] {true, false}) {
            OutputPort<Match> matchesPattern1 = andPattern1.match(nseq).getMatches(byScore, true);
            OutputPort<Match> matchesPattern2 = andPattern2.match(nseq).getMatches(byScore, true);
            for (int i = 0; i < 44; i++) {
                assertNotNull(matchesPattern1.take().getValue());
            }
            assertNull(matchesPattern1.take());
            for (int i = 0; i < 248; i++) {
                assertNotNull(matchesPattern2.take().getValue());
            }
            assertNull(matchesPattern2.take());
        }
    }

    @Test
    public void matchesIntersectionTest() throws Exception {
        FuzzyMatchPattern pattern1 = new FuzzyMatchPattern(new NucleotideSequence("ATA"));
        FuzzyMatchPattern pattern2 = new FuzzyMatchPattern(new NucleotideSequence("TAT"));
        AndPattern andPattern = new AndPattern(0, -1, pattern1, pattern2);
        NSequenceWithQuality nseq = new NSequenceWithQuality("ATATATATTATA");
        OutputPort<Match> matches = andPattern.match(nseq).getMatches(false, true);
        while (true) {
            Match match = matches.take();
            if (match == null) break;
            String seq = match.getValue().getSequence().toString();
            Range range = match.getRange();
            System.out.println(seq + " " + range.getLower() + " " + range.getUpper());
        }
        assertEquals(10, countMatches(andPattern.match(nseq), true));
    }

    @Test
    public void quickSearchTest() throws Exception {
        FuzzyMatchPattern pattern1 = new FuzzyMatchPattern(new NucleotideSequence("ATA"));
        FuzzyMatchPattern pattern2 = new FuzzyMatchPattern(new NucleotideSequence("TAT"));
        AndPattern andPattern = new AndPattern(0, -1, pattern1, pattern2);
        NSequenceWithQuality nseq1 = new NSequenceWithQuality("ATATATATTATA");
        NSequenceWithQuality nseq2 = new NSequenceWithQuality("GCGGTGCGTATAGCG");
        MatchingResult match1 = andPattern.match(nseq1);
        MatchingResult match2 = andPattern.match(nseq2);
        assertEquals(true, match1.isFound());
        assertEquals(false, match2.isFound());
        assertEquals(10, countMatches(match1, true));
        assertEquals(0, countMatches(match2, true));
        match1 = andPattern.match(nseq1);
        match2 = andPattern.match(nseq2);
        assertEquals(true, match1.isFound());
        assertEquals(false, match2.isFound());
        assertNotNull(match1.getMatches(true, true).take());
        assertNotNull(match1.getMatches(false, true).take());
        assertNotNull(match1.getMatches().take());
        assertNotNull(match2.getMatches(true, true));
        assertNotNull(match2.getMatches(false, true));
        assertNotNull(match2.getMatches());
        assertNull(match2.getMatches().take());
        assertEquals(10, countMatches(match1, true));
        assertEquals(0, countMatches(match2, true));
    }

    @Test
    public void groupsTest() throws Exception {
        HashMap<GroupEdge, Integer> groupEdges1 = new HashMap<GroupEdge, Integer>() {{
            put(new GroupEdge("1", true), 0);
            put(new GroupEdge("1", false), 1);
            put(new GroupEdge("2", true), 1);
            put(new GroupEdge("2", false), 3);
            put(new GroupEdge("4", true), 4);
            put(new GroupEdge("4", false), 5);
        }};

        HashMap<GroupEdge, Integer> groupEdges2 = new HashMap<GroupEdge, Integer>() {{
            put(new GroupEdge("3", true), 1);
            put(new GroupEdge("3", false), 3);
            put(new GroupEdge("5", true), 5);
            put(new GroupEdge("5", false), 6);
        }};

        FuzzyMatchPattern pattern1 = new FuzzyMatchPattern(new NucleotideSequence("TAGCC"), groupEdges1);
        FuzzyMatchPattern pattern2 = new FuzzyMatchPattern(new NucleotideSequence("CAGATGCA"), groupEdges2);
        AndPattern andPattern = new AndPattern(0, -1, pattern1, pattern2);
        NSequenceWithQuality nseq = new NSequenceWithQuality("AAACAGATGCAGACATAGCC");
        MatchingResult result = andPattern.match(nseq);
        OutputPort<Match> matchOutputPort = result.getMatches(false, true);
        Match match = matchOutputPort.take();
        assertEquals(16, match.getMatchedGroupEdge("2", true)
                .getPosition());
        assertEquals(9, match.getMatchedGroupEdge("5", false)
                .getPosition());
        assertEquals("3", match.getMatchedGroupEdge("3", true)
                .getGroupName());
        assertNull(matchOutputPort.take());
    }
}
