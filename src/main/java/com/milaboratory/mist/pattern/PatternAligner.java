package com.milaboratory.mist.pattern;

import com.milaboratory.core.Range;
import com.milaboratory.core.alignment.*;
import com.milaboratory.core.sequence.NSequenceWithQuality;
import com.milaboratory.core.sequence.NucleotideSequenceCaseSensitive;

public class PatternAligner {
    private static PatternAndTargetAlignmentScoring scoring;
    private static long singleOverlapPenalty;
    private static int bitapMaxErrors;
    private static int maxOverlap;
    private static boolean initialized = false;

    private PatternAligner() {
    }

    /**
     * Initialize configuration for scoring and alignment for patterns.
     *
     * @param scoringArg scoring for pattern and target alignment
     * @param singleOverlapPenaltyArg 0 or negative; this is penalty for 1 nucleotide overlap between 2 patterns
     * @param bitapMaxErrorsArg 0 or positive; maximum allowed number of errors for bitap
     * @param maxOverlapArg 0 or positive; maximum allowed number of overlapped nucleotides between 2 patterns
     */
    public static void init(PatternAndTargetAlignmentScoring scoringArg, long singleOverlapPenaltyArg,
                            int bitapMaxErrorsArg, int maxOverlapArg) {
        if (initialized)
            throw new IllegalStateException("Repeated initialization of PatternAligner!");
        scoring = scoringArg;
        singleOverlapPenalty = singleOverlapPenaltyArg;
        bitapMaxErrors = bitapMaxErrorsArg;
        maxOverlap = maxOverlapArg;
        initialized = true;
    }

    public static Alignment<NucleotideSequenceCaseSensitive> align(NucleotideSequenceCaseSensitive pattern,
            NSequenceWithQuality target, int rightMatchPosition, int leftBorder) {
        if (leftBorder == -1) {
            return PatternAndTargetAligner.alignLeftAdded(scoring, pattern, target, rightMatchPosition, bitapMaxErrors);
        } else {
            Range targetRange = new Range(leftBorder, rightMatchPosition + 1);
            NSequenceWithQuality targetPart = new NSequenceWithQuality(target.getSequence().getRange(targetRange),
                    target.getQuality().getRange(targetRange));
            Alignment<NucleotideSequenceCaseSensitive> partAlignment = PatternAndTargetAligner.alignGlobal(scoring,
                    pattern, targetPart);
            return new Alignment<>(pattern, partAlignment.getAbsoluteMutations(),
                    partAlignment.getSequence1Range(), partAlignment.getSequence2Range().move(leftBorder),
                    partAlignment.getScore());
        }
    }

    public static long overlapPenalty(int overlapLength) {
        return singleOverlapPenalty * overlapLength;
    }

    public static long insertionPenalty(int insertionLength) {
        return singleOverlapPenalty * insertionLength;
    }

    public static int bitapMaxErrors() {
        return bitapMaxErrors;
    }

    public static int maxOverlap() {
        return maxOverlap;
    }
}
