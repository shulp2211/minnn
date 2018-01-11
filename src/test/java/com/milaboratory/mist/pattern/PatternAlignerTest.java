package com.milaboratory.mist.pattern;

import com.milaboratory.core.Range;
import com.milaboratory.core.alignment.Alignment;
import com.milaboratory.core.alignment.PatternAndTargetAlignmentScoring;
import com.milaboratory.core.sequence.NSequenceWithQuality;
import com.milaboratory.core.sequence.NucleotideSequenceCaseSensitive;
import com.milaboratory.test.TestUtil;
import org.junit.*;

import static com.milaboratory.core.sequence.NucleotideSequenceCaseSensitive.fromNucleotideSequence;
import static com.milaboratory.mist.cli.Defaults.*;
import static com.milaboratory.mist.util.CommonTestUtils.*;
import static org.junit.Assert.*;

public class PatternAlignerTest {
    @BeforeClass
    public static void init() throws Exception {
        PatternAligner.allowValuesOverride();
    }

    private void configureRandomAligner() {
        PatternAligner.init(getRandomScoring(), -rg.nextInt(10), rg.nextInt(4), rg.nextInt(4));
    }

    @Test
    public void randomAlignerTest() throws Exception {
        for (int i = 0; i < 1000; i++) {
            configureRandomAligner();
            int seqLength = rg.nextInt(40) + 1;
            NucleotideSequenceCaseSensitive[] sequences = new NucleotideSequenceCaseSensitive[3];
            sequences[0] = TestUtil.randomSequence(NucleotideSequenceCaseSensitive.ALPHABET, seqLength, seqLength);
            sequences[1] = fromNucleotideSequence(sequences[0].toNucleotideSequence(), false);
            sequences[2] = fromNucleotideSequence(sequences[0].toNucleotideSequence(), true);
            for (int leftBorder : new int[] {-1, 0})
                for (NucleotideSequenceCaseSensitive sequence : sequences) {
                    NSequenceWithQuality target = setRandomQuality(sequence.toString());
                    Alignment<NucleotideSequenceCaseSensitive> alignment = PatternAligner.align(sequence, target,
                            seqLength - 1, leftBorder);
                    assertEquals(new Range(0, seqLength), alignment.getSequence1Range());
                    assertEquals(alignment.getSequence1Range(), alignment.getSequence2Range());
                }
        }
    }

    @Test
    public void alignmentTest() throws Exception {
        PatternAndTargetAlignmentScoring scoring = new PatternAndTargetAlignmentScoring(0,
                -9, -10, DEFAULT_GOOD_QUALITY, DEFAULT_BAD_QUALITY, -3);
        PatternAligner.init(scoring, -10, 2, 1);
        NucleotideSequenceCaseSensitive pattern = new NucleotideSequenceCaseSensitive("aTTAgaca");
        NSequenceWithQuality target = new NSequenceWithQuality("CCTTATTC");
        Alignment<NucleotideSequenceCaseSensitive> alignment = PatternAligner.align(pattern, target, 7,
                -1);
        assertEquals(new Range(1, 8), alignment.getSequence2Range());
        assertEquals(new Range(0, 8), alignment.getSequence1Range());

        pattern = new NucleotideSequenceCaseSensitive("ATTAgaCA");
        target = new NSequenceWithQuality("ATTTAGACA");
        alignment = PatternAligner.align(pattern, target, 8, -1);
        assertEquals(new Range(1, 9), alignment.getSequence2Range());
        assertEquals(-9, (int)alignment.getScore());

        alignment = PatternAligner.align(pattern, target, 8, 0);
        assertEquals(new Range(0, 9), alignment.getSequence2Range());
        assertEquals(-28, (int)alignment.getScore());
    }
}
