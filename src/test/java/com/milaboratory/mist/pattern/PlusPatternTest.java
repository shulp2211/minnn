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

import static org.junit.Assert.*;

public class PlusPatternTest {
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
        PlusPattern plusPattern1 = new PlusPattern(pattern1, pattern3);
        PlusPattern plusPattern2 = new PlusPattern(pattern2, pattern3);
        PlusPattern plusPattern3 = new PlusPattern(pattern2, pattern1, pattern3);
        PlusPattern plusPattern4 = new PlusPattern(pattern4);
        PlusPattern plusPattern5 = new PlusPattern();
        PlusPattern plusPattern6 = new PlusPattern(pattern1);
        PlusPattern plusPattern7 = new PlusPattern(pattern1, pattern3, pattern2);

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
            FuzzyMatchPattern patternMotif1 = new FuzzyMatchPattern(seqMotif1);
            FuzzyMatchPattern patternMotif2 = new FuzzyMatchPattern(seqMotif2);
            PlusPattern plusPattern = new PlusPattern(patternMotif1, patternMotif2);
            assertEquals(true, plusPattern.match(target).isFound());

            NSequenceWithQuality foundSequence = plusPattern.match(target).getBestMatch().getValue();
            assertEquals(true, patternMotif1.match(foundSequence).isFound());
            assertEquals(true, patternMotif2.match(foundSequence).isFound());
        }
    }

    @Test
    public void allMatchesTest() throws Exception {
        FuzzyMatchPattern pattern1 = new FuzzyMatchPattern(new NucleotideSequence("ATTA"));
        FuzzyMatchPattern pattern2 = new FuzzyMatchPattern(new NucleotideSequence("GACA"));
        NSequenceWithQuality nseq = new NSequenceWithQuality("GACATTATTATTAGACAGACATTAGACATTATTAGACAGACATTAATTA");
        PlusPattern plusPattern1 = new PlusPattern(pattern1, pattern2);
        PlusPattern plusPattern2 = new PlusPattern(pattern1, pattern1, pattern2);
        assertNotNull(plusPattern1.match(nseq).getBestMatch());
        assertNotNull(plusPattern2.match(nseq).getBestMatch());
        assertEquals(22, plusPattern1.match(nseq).getMatchesNumber());
        assertEquals(30, plusPattern2.match(nseq).getMatchesNumber());
        for (boolean byScore : new boolean[] {true, false}) {
            OutputPort<Match> matchesPattern1 = plusPattern1.match(nseq).getMatches(byScore);
            OutputPort<Match> matchesPattern2 = plusPattern2.match(nseq).getMatches(byScore);
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
        FuzzyMatchPattern pattern1 = new FuzzyMatchPattern(new NucleotideSequence("ATA"));
        FuzzyMatchPattern pattern2 = new FuzzyMatchPattern(new NucleotideSequence("TAT"));
        PlusPattern plusPattern = new PlusPattern(pattern1, pattern2);
        NSequenceWithQuality nseq = new NSequenceWithQuality("ATATATATTATA");
        OutputPort<Match> matches = plusPattern.match(nseq).getMatches(false);
        while (true) {
            Match match = matches.take();
            if (match == null) break;
            String seq = match.getValue().getSequence().toString();
            Range range = match.getRange();
            System.out.println(seq + " " + range.getLower() + " " + range.getUpper());
        }
        assertEquals(6, plusPattern.match(nseq).getMatchesNumber());
    }

    @Test
    public void quickSearchTest() throws Exception {
        FuzzyMatchPattern pattern1 = new FuzzyMatchPattern(new NucleotideSequence("ATA"));
        FuzzyMatchPattern pattern2 = new FuzzyMatchPattern(new NucleotideSequence("TAT"));
        PlusPattern plusPattern = new PlusPattern(pattern1, pattern2);
        NSequenceWithQuality nseq1 = new NSequenceWithQuality("ATATATATTATA");
        NSequenceWithQuality nseq2 = new NSequenceWithQuality("GCGGTGCGTATAGCG");
        MatchingResult match1 = plusPattern.match(nseq1);
        MatchingResult match2 = plusPattern.match(nseq2);
        assertEquals(true, match1.isFound());
        assertEquals(false, match2.isFound());
        assertEquals(6, match1.getMatchesNumber());
        assertEquals(0, match2.getMatchesNumber());
        match1 = plusPattern.match(nseq1);
        match2 = plusPattern.match(nseq2);
        assertEquals(true, match1.isFound());
        assertEquals(false, match2.isFound());
        assertNotNull(match1.getMatches(true).take());
        assertNotNull(match1.getMatches(false).take());
        assertNotNull(match1.getMatches().take());
        assertNotNull(match2.getMatches(true));
        assertNotNull(match2.getMatches(false));
        assertNotNull(match2.getMatches());
        assertNull(match2.getMatches().take());
        assertEquals(6, match1.getMatchesNumber());
        assertEquals(0, match2.getMatchesNumber());
    }

    @Test
    public void groupsTest() throws Exception {
        HashMap<GroupEdge, Integer> groupEdges1 = new HashMap<GroupEdge, Integer>() {{
            put(new GroupEdge("1", true), 0);
            put(new GroupEdge("1", false), 1);
            put(new GroupEdge("2", true), 1);
            put(new GroupEdge("2", false), 3);
            put(new GroupEdge("4", false), 5);
        }};

        HashMap<GroupEdge, Integer> groupEdges2 = new HashMap<GroupEdge, Integer>() {{
            put(new GroupEdge("3", true), 1);
            put(new GroupEdge("3", false), 3);
            put(new GroupEdge("4", true), 4);
            put(new GroupEdge("5", true), 5);
            put(new GroupEdge("5", false), 6);
        }};

        FuzzyMatchPattern pattern1 = new FuzzyMatchPattern(new NucleotideSequence("TAGCC"), groupEdges1);
        FuzzyMatchPattern pattern2 = new FuzzyMatchPattern(new NucleotideSequence("CAGATGCA"), groupEdges2);
        PlusPattern plusPattern = new PlusPattern(pattern2, pattern1);
        NSequenceWithQuality nseq = new NSequenceWithQuality("AAACAGATGCAGACATAGCC");
        MatchingResult result = plusPattern.match(nseq);
        Match match = result.getMatches(false).take();
        assertEquals(16, match.getMatchedGroupEdge("2", true)
                .getPosition());
        assertEquals(20, match.getMatchedGroupEdge("4", false)
                .getPosition());
        assertEquals("3", match.getMatchedGroupEdge("3", true)
                .getGroupName());
        assertNull(result.getMatches(false).take());
    }
}
