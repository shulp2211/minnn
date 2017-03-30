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

import java.util.ArrayList;
import java.util.Arrays;

import static com.milaboratory.mist.pattern.Match.COMMON_GROUP_NAME_PREFIX;
import static com.milaboratory.mist.pattern.Match.WHOLE_PATTERN_MATCH_GROUP_NAME_PREFIX;
import static org.junit.Assert.*;

public class PerfectMatchPatternTest {
    @Rule
    public final ExpectedException exception = ExpectedException.none();

    @Test
    public void bestMatchTest() throws Exception {
        PerfectMatchPattern pattern = new PerfectMatchPattern(new NucleotideSequence("ATTAGACA").toMotif());
        NSequenceWithQuality nseq = new NSequenceWithQuality("ACTGCGATAAATTAGACAGTACGTA");
        ArrayList<MatchingResult> results = new ArrayList<>(Arrays.asList(
                pattern.match(nseq, 1, 19, (byte)0, false),
                pattern.match(nseq, 10, 18, (byte)0, false),
                pattern.match(nseq, 10, 18, (byte)0),
                pattern.match(nseq, new Range(10, 18)),
                pattern.match(nseq)
        ));
        Range expectedRange = new Range(10, 18);
        for (MatchingResult result : results) {
            assertEquals(expectedRange.getLower(), result.getBestMatch().getWholePatternMatch().getRange().getLower());
            assertEquals(expectedRange.getUpper(), result.getBestMatch().getWholePatternMatch().getRange().getUpper());
            assertEquals(new NSequenceWithQuality("ATTAGACA"), result.getBestMatch().getWholePatternMatch().getValue());
            assertEquals(nseq, result.getBestMatch().getWholePatternMatch().getTarget());
            assertEquals(true, result.isFound());
            assertEquals(1, result.getMatchesNumber());
            assertEquals(true, result.getBestMatch().isFound());
            assertEquals(1, result.getBestMatch().getNumberOfPatterns());
            assertEquals(result.getBestMatch(), result.getMatches().take());
            Range rangeFromGroupMatches = result.getBestMatch().groupMatches.get(WHOLE_PATTERN_MATCH_GROUP_NAME_PREFIX + "0").getRange();
            assertEquals(expectedRange, rangeFromGroupMatches);
            assertEquals(null, result.getBestMatch().groupMatches.get(COMMON_GROUP_NAME_PREFIX + "0"));
        }
    }

    @Test
    public void noMatchesTest() throws Exception {
        PerfectMatchPattern pattern = new PerfectMatchPattern(new NucleotideSequence("ATTAGACA").toMotif());
        NSequenceWithQuality nseq1 = new NSequenceWithQuality("ACTGCGATAAATTAGACAGTACGTA");
        NSequenceWithQuality nseq2 = new NSequenceWithQuality("ACTGCGATAAATTACACAGTACGTA");
        ArrayList<MatchingResult> results = new ArrayList<>(Arrays.asList(
                pattern.match(nseq1, 11, 19, (byte)0, false),
                pattern.match(nseq1, 10, 17, (byte)0, false),
                pattern.match(nseq2)
        ));
        for (MatchingResult result : results) {
            assertEquals(null, result.getBestMatch());
            assertEquals(null, result.getMatches().take());
            assertEquals(false, result.isFound());
            assertEquals(0, result.getMatchesNumber());
        }
    }

    @Test
    public void quickMatchTest() throws Exception {
        PerfectMatchPattern pattern = new PerfectMatchPattern(new NucleotideSequence("ATTAGACA").toMotif());
        NSequenceWithQuality nseq = new NSequenceWithQuality("ACTGCGATAAATTAGACAGTACGTA");
        MatchingResult result = pattern.match(nseq, 1, 19, (byte)0, true);
        assertEquals(true, result.isFound());
        result = pattern.match(nseq, 1, 17, (byte)0, true);
        assertEquals(false, result.isFound());
        result = pattern.match(nseq, 11, 20, (byte)0, true);
        assertEquals(false, result.isFound());
        pattern = new PerfectMatchPattern(new NucleotideSequence("ATTTTACA").toMotif());
        result = pattern.match(nseq, 1, 19, (byte)0, true);
        assertEquals(false, result.isFound());
        exception.expect(IllegalStateException.class);
        result.getMatchesNumber();
    }

    @Test
    public void randomMatchTest() throws Exception {
        int its = TestUtil.its(1000, 100000);
        for (int i = 0; i < its; ++i) {
            NucleotideSequence seqM = TestUtil.randomSequence(NucleotideSequence.ALPHABET, 10, 60);
            NucleotideSequence seqL = TestUtil.randomSequence(NucleotideSequence.ALPHABET, 0, 40);
            NucleotideSequence seqR = TestUtil.randomSequence(NucleotideSequence.ALPHABET, 0, 40);
            NucleotideSequence fullSeq = SequencesUtils.concatenate(seqL, seqM, seqR);
            Motif<NucleotideSequence> motif = new Motif<>(seqM);
            NSequenceWithQuality target = new NSequenceWithQuality(fullSeq, SequenceQuality.getUniformQuality(SequenceQuality.GOOD_QUALITY_VALUE, fullSeq.getSequence().size()));
            PerfectMatchPattern pattern = new PerfectMatchPattern(motif);
            assertEquals(true, pattern.match(target).isFound());
        }
    }

    @Test
    public void randomTest() throws Exception {
        int its = TestUtil.its(1000, 10000);
        for (int i = 0; i < its; ++i) {
            NucleotideSequence seq = TestUtil.randomSequence(NucleotideSequence.ALPHABET, 1, 1000);
            NucleotideSequence motifSeq = TestUtil.randomSequence(NucleotideSequence.ALPHABET, 1, 50);
            Motif<NucleotideSequence> motif = new Motif<>(motifSeq);
            NSequenceWithQuality target = new NSequenceWithQuality(seq, SequenceQuality.getUniformQuality(SequenceQuality.GOOD_QUALITY_VALUE, seq.getSequence().size()));
            PerfectMatchPattern pattern = new PerfectMatchPattern(motif);
            boolean isMatching = seq.toString().contains(motifSeq.toString());
            assertEquals(isMatching, pattern.match(target).isFound());
        }
    }

    @Test
    public void multipleMatchesTest() throws Exception {
        PerfectMatchPattern pattern = new PerfectMatchPattern(new NucleotideSequence("ATTAGACA").toMotif());
        NSequenceWithQuality nseq = new NSequenceWithQuality("ACTGCGATAAATTAGACATTAGACATTAGACAGTACGTATTAGACA");
        MatchingResult result = pattern.match(nseq);
        Match bestMatch1 = result.getBestMatch();
        Match firstMatchByScore = result.getMatches(true).take();
        Match bestMatch2 = result.getBestMatch();
        Match firstMatchByCoordinate = result.getMatches(false).take();
        Match bestMatch3 = result.getBestMatch();
        assertEquals(bestMatch1, bestMatch2);
        assertEquals(bestMatch1, bestMatch3);
        assertEquals(bestMatch1, firstMatchByScore);
        assertEquals(bestMatch1, firstMatchByCoordinate);
        assertEquals(true, result.isFound());
        assertEquals(4, result.getMatchesNumber());
        OutputPort<Match> matches = result.getMatches();
        assertEquals(10, matches.take().getWholePatternMatch().getRange().getLower());
        assertEquals("ATTAGACA", matches.take().getWholePatternMatch().getValue().getSequence().toString());
        assertEquals(24, matches.take().groupMatches.get(WHOLE_PATTERN_MATCH_GROUP_NAME_PREFIX + "0").getRange().getLower());
        assertEquals(46, matches.take().getWholePatternMatch(0).getRange().getUpper());
    }

    @Test
    public void matchesIntersectionTest() throws Exception {
        PerfectMatchPattern pattern = new PerfectMatchPattern(new NucleotideSequence("ATA").toMotif());
        NSequenceWithQuality nseq = new NSequenceWithQuality("ATATATTAAATATATATTTAATATATAAT");
        OutputPort<Match> matches = pattern.match(nseq).getMatches();
        assertEquals(new Range(0, 3), matches.take().getWholePatternMatch().getRange());
        assertEquals(new Range(2, 5), matches.take().getWholePatternMatch().getRange());
        assertEquals(new Range(9, 12), matches.take().getWholePatternMatch().getRange());
        assertEquals(new Range(11, 14), matches.take().getWholePatternMatch().getRange());
        assertEquals(new Range(13, 16), matches.take().getWholePatternMatch().getRange());
        assertEquals(new Range(20, 23), matches.take().getWholePatternMatch().getRange());
        assertEquals(new Range(22, 25), matches.take().getWholePatternMatch().getRange());
        assertEquals(new Range(24, 27), matches.take().getWholePatternMatch().getRange());
        assertEquals(null, matches.take());
    }
}
