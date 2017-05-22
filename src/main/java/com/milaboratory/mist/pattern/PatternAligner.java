package com.milaboratory.mist.pattern;

import com.milaboratory.core.alignment.Alignment;
import com.milaboratory.core.sequence.NSequenceWithQuality;
import com.milaboratory.core.sequence.NucleotideSequence;

public interface PatternAligner {
    /**
     * Returned alignment will have maximal score equal zero.
     *
     * @param pattern
     * @param target
     * @param rightMatchPosition inclusive
     * @return alignment with score less or equal than zero
     */
    Alignment<NucleotideSequence> align(NucleotideSequence pattern, NSequenceWithQuality target, int rightMatchPosition);

    /**
     * Negative value
     *
     * @return
     */
    int penaltyThreshold();

    /**
     * @param target        target
     * @param overlapOffset offset inclusive
     * @param overlapLength length
     * @return negative penalty value
     */
    int overlapPenalty(NSequenceWithQuality target, int overlapOffset, int overlapLength);

    /**
     * Checks whether two aligners are of the same type (may be with different penalty threshold).
     *
     * @param otherAligner other aligner
     * @return true if aligners are compatible
     */
    boolean compatible(PatternAligner otherAligner);

    /**
     * Return new patter with more strict threshold.
     *
     * @return
     */
    PatternAligner overridePenaltyThreshold(int newThresholdValue);
}
