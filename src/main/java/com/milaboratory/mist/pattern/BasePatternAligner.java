package com.milaboratory.mist.pattern;

import com.milaboratory.core.Range;
import com.milaboratory.core.alignment.*;
import com.milaboratory.core.sequence.NSequenceWithQuality;
import com.milaboratory.core.sequence.NucleotideSequenceCaseSensitive;

public class BasePatternAligner implements PatternAligner {
    private final PatternAndTargetAlignmentScoring scoring;
    private final long penaltyThreshold;
    private final long singleOverlapPenalty;
    private final int bitapMaxErrors;
    private final int maxOverlap;
    private final int leftBorder;

    public BasePatternAligner(PatternAndTargetAlignmentScoring scoring, long penaltyThreshold,
            long singleOverlapPenalty, int bitapMaxErrors, int maxOverlap) {
        this(scoring, penaltyThreshold, singleOverlapPenalty, bitapMaxErrors, maxOverlap, -1);
    }

    private BasePatternAligner(PatternAndTargetAlignmentScoring scoring, long penaltyThreshold,
            long singleOverlapPenalty, int bitapMaxErrors, int maxOverlap, int leftBorder) {
        this.scoring = scoring;
        this.penaltyThreshold = penaltyThreshold;
        this.singleOverlapPenalty = singleOverlapPenalty;
        this.bitapMaxErrors = bitapMaxErrors;
        this.maxOverlap = maxOverlap;
        this.leftBorder = leftBorder;
    }

    @Override
    public Alignment<NucleotideSequenceCaseSensitive> align(NucleotideSequenceCaseSensitive pattern,
            NSequenceWithQuality target, int rightMatchPosition) {
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
