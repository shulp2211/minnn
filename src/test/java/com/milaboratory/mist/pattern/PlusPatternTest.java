package com.milaboratory.mist.pattern;

import cc.redberry.pipe.OutputPort;
import com.milaboratory.core.Range;
import com.milaboratory.core.motif.Motif;
import com.milaboratory.core.sequence.NSequenceWithQuality;
import com.milaboratory.core.sequence.NucleotideSequence;
import com.milaboratory.core.sequence.SequenceQuality;
import com.milaboratory.core.sequence.SequencesUtils;
import com.milaboratory.test.TestUtil;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;

import static org.junit.Assert.*;

public class PlusPatternTest {
    @Rule
    public final ExpectedException exception = ExpectedException.none();

    @Test
    public void simpleTest() throws Exception {
        PerfectMatchPattern pattern1 = new PerfectMatchPattern(new NucleotideSequence("ATTAGACA").toMotif());
        PerfectMatchPattern pattern2 = new PerfectMatchPattern(new NucleotideSequence("TTAG").toMotif());
        PerfectMatchPattern pattern3 = new PerfectMatchPattern(new NucleotideSequence("AGTA").toMotif());
        PerfectMatchPattern pattern4 = new PerfectMatchPattern(new NucleotideSequence("AGTAG").toMotif());
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
        assertEquals(false, plusPattern1.match(nseq1, 0, 25, (byte)1, true).isFound());
        assertEquals(false, plusPattern1.match(nseq1, new Range(0, 25)).isFound());
        assertEquals(true, plusPattern2.match(nseq1).isFound());
        assertEquals(true, plusPattern2.match(nseq1, 0, 25, (byte)1, true).isFound());
        assertEquals(true, plusPattern2.match(nseq1, new Range(0, 25)).isFound());
        assertEquals(false, plusPattern4.match(nseq3, new Range(0, 24), (byte)-1, false).isFound());
        assertEquals(false, plusPattern3.match(nseq3, new Range(0, 24), (byte)-1, false).isFound());
        assertEquals(true, plusPattern7.match(nseq3, new Range(0, 24), (byte)-1, false).isFound());
        assertEquals(false, plusPattern3.match(nseq1).isFound());
        assertEquals(false, plusPattern6.match(nseq2).isFound());
        assertEquals(true, plusPattern6.match(nseq1).isFound());
        assertEquals(false, plusPattern2.match(nseq1, new Range(12, 21)).isFound());

        assertEquals(new Range(0, 17), plusPattern7.match(nseq3, new Range(0, 24), (byte)-1, false).getBestMatch().getWholePatternMatch().getRange());
        assertEquals(new Range(11, 21), plusPattern2.match(nseq1, new Range(1, 21)).getBestMatch().getWholePatternMatch().getRange());
        assertEquals(null, plusPattern2.match(nseq1, new Range(11, 20)).getBestMatch());

        exception.expect(IllegalStateException.class);
        plusPattern5.match(nseq1);
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
            Motif<NucleotideSequence> motif1 = new Motif<>(seqMotif1);
            Motif<NucleotideSequence> motif2 = new Motif<>(seqMotif2);
            NSequenceWithQuality target = new NSequenceWithQuality(fullSeq, SequenceQuality.getUniformQuality(SequenceQuality.GOOD_QUALITY_VALUE, fullSeq.getSequence().size()));
            PerfectMatchPattern patternMotif1 = new PerfectMatchPattern(motif1);
            PerfectMatchPattern patternMotif2 = new PerfectMatchPattern(motif2);
            PlusPattern plusPattern = new PlusPattern(patternMotif1, patternMotif2);
            assertEquals(true, plusPattern.match(target).isFound());

            NSequenceWithQuality foundSequence = plusPattern.match(target).getBestMatch().getWholePatternMatch().getValue();
            assertEquals(true, patternMotif1.match(foundSequence).isFound());
            assertEquals(true, patternMotif2.match(foundSequence).isFound());
        }
    }

    @Test
    public void allMatchesTest() throws Exception {
        PerfectMatchPattern pattern1 = new PerfectMatchPattern(new NucleotideSequence("ATTA").toMotif());
        PerfectMatchPattern pattern2 = new PerfectMatchPattern(new NucleotideSequence("GACA").toMotif());
        NSequenceWithQuality nseq = new NSequenceWithQuality("GACATTATTATTAGACAGACATTAGACATTATTAGACAGACATTAATTA");
        PlusPattern plusPattern1 = new PlusPattern(pattern1, pattern2);
        PlusPattern plusPattern2 = new PlusPattern(pattern1, pattern1, pattern2);
        assertNotNull(plusPattern1.match(nseq).getBestMatch());
        assertNotNull(plusPattern2.match(nseq).getBestMatch());
        assertEquals(22, plusPattern1.match(nseq).getMatchesNumber());
        assertEquals(13, plusPattern2.match(nseq).getMatchesNumber());
        for (boolean byScore : new boolean[] {true, false}) {
            OutputPort<Match> matchesPattern1 = plusPattern1.match(nseq).getMatches(byScore);
            OutputPort<Match> matchesPattern2 = plusPattern2.match(nseq).getMatches(byScore);
            for (int i = 0; i < 22; i++) {
                assertNotNull(matchesPattern1.take().getWholePatternMatch().getValue());
            }
            assertNull(matchesPattern1.take());
            for (int i = 0; i < 13; i++) {
                assertNotNull(matchesPattern2.take().getWholePatternMatch().getValue());
            }
            assertNull(matchesPattern2.take());
        }
    }

    @Test
    public void matchesIntersectionTest() throws Exception {
        PerfectMatchPattern pattern1 = new PerfectMatchPattern(new NucleotideSequence("ATA").toMotif());
        PerfectMatchPattern pattern2 = new PerfectMatchPattern(new NucleotideSequence("TAT").toMotif());
        PlusPattern plusPattern = new PlusPattern(pattern1, pattern2);
        NSequenceWithQuality nseq = new NSequenceWithQuality("ATATATATTATA");
        OutputPort<Match> matches = plusPattern.match(nseq).getMatches(false);
        while (true) {
            Match match = matches.take();
            if (match == null) break;
            String seq = match.getWholePatternMatch().getValue().getSequence().toString();
            Range range = match.getWholePatternMatch().getRange();
            System.out.println(seq + " " + range.getLower() + " " + range.getUpper());
        }
        assertEquals(6, plusPattern.match(nseq).getMatchesNumber());
    }
}
