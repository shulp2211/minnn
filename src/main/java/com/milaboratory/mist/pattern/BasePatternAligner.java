package com.milaboratory.mist.pattern;

import com.milaboratory.core.alignment.Aligner;
import com.milaboratory.core.alignment.Alignment;
import com.milaboratory.core.alignment.BandedLinearAligner;
import com.milaboratory.core.alignment.LinearGapAlignmentScoring;
import com.milaboratory.core.sequence.NSequenceWithQuality;
import com.milaboratory.core.sequence.NucleotideSequence;

public class BasePatternAligner implements PatternAligner {
    private final LinearGapAlignmentScoring<NucleotideSequence> scoring;
    private final long penaltyThreshold;
    private final long singleOverlapPenalty;
    private final int bitapMaxErrors;
    private final int maxOverlap;
    private final int leftBorder;

    public BasePatternAligner(LinearGapAlignmentScoring<NucleotideSequence> scoring, long penaltyThreshold,
                              long singleOverlapPenalty, int bitapMaxErrors, int maxOverlap) {
        this(scoring, penaltyThreshold, singleOverlapPenalty, bitapMaxErrors, maxOverlap, -1);
    }

    private BasePatternAligner(LinearGapAlignmentScoring<NucleotideSequence> scoring, long penaltyThreshold,
                              long singleOverlapPenalty, int bitapMaxErrors, int maxOverlap, int leftBorder) {
        this.scoring = scoring;
        this.penaltyThreshold = penaltyThreshold;
        this.singleOverlapPenalty = singleOverlapPenalty;
        this.bitapMaxErrors = bitapMaxErrors;
        this.maxOverlap = maxOverlap;
        this.leftBorder = leftBorder;
    }

    @Override
    public Alignment<NucleotideSequence> align(NucleotideSequence pattern, NSequenceWithQuality target,
                                               int rightMatchPosition) {
        if (leftBorder == -1) {
            int leftMatchPosition = rightMatchPosition + 1 - pattern.size() - bitapMaxErrors;
            if (leftMatchPosition < 0) leftMatchPosition = 0;
            return BandedLinearAligner.alignLeftAdded(scoring, pattern, target.getSequence(),
                    0, pattern.size(), 0, leftMatchPosition,
                    rightMatchPosition - leftMatchPosition + 1, bitapMaxErrors, bitapMaxErrors);
        } else {
            NucleotideSequence targetPart = target.getSubSequence(leftBorder, rightMatchPosition + 1)
                    .getSequence();
            Alignment<NucleotideSequence> partAlignment = Aligner.alignGlobal(scoring, pattern, targetPart);
            return new Alignment<>(pattern, partAlignment.getAbsoluteMutations(),
                    partAlignment.getSequence1Range(), partAlignment.getSequence2Range().move(leftBorder),
                    partAlignment.getScore());
        }
    }

    @Override
    public long penaltyThreshold() {
        return penaltyThreshold;
    }

    @Override
    public long overlapPenalty(NSequenceWithQuality target, int overlapOffset, int overlapLength) {
        return singleOverlapPenalty * overlapLength;
    }

    @Override
    public long insertionPenalty(NSequenceWithQuality target, int insertionOffset, int insertionLength) {
        return singleOverlapPenalty * insertionLength;
    }

    @Override
    public int bitapMaxErrors() {
        return bitapMaxErrors;
    }

    @Override
    public int maxOverlap() {
        return maxOverlap;
    }

    @Override
    public boolean compatible(PatternAligner otherAligner) {
        return otherAligner.getClass() == BasePatternAligner.class;
    }

    @Override
    public int leftBorder() {
        return leftBorder;
    }

    @Override
    public PatternAligner overridePenaltyThreshold(long newThresholdValue) {
        return new BasePatternAligner(scoring, newThresholdValue, singleOverlapPenalty, bitapMaxErrors, maxOverlap,
                leftBorder);
    }

    @Override
    public PatternAligner setLeftBorder(int newLeftBorder) {
        return new BasePatternAligner(scoring, penaltyThreshold, singleOverlapPenalty, bitapMaxErrors, maxOverlap,
                newLeftBorder);
    }
}
