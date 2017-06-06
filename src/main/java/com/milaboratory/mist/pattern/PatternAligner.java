package com.milaboratory.mist.pattern;

import com.milaboratory.core.alignment.Alignment;
import com.milaboratory.core.sequence.NSequenceWithQuality;
import com.milaboratory.core.sequence.NucleotideSequence;

public interface PatternAligner {
    /**
     * Returned alignment will have maximal score equals zero.
     *
     * @param pattern nucleotide sequence from pattern
     * @param target target nucleotide sequence with quality
     * @param rightMatchPosition right position of found bitap match, inclusive
     * @return alignment with score less or equals than zero
     */
    Alignment<NucleotideSequence> align(NucleotideSequence pattern, NSequenceWithQuality target, int rightMatchPosition);

    /**
     * Penalty threshold, negative value.
     *
     * @return penalty threshold
     */
    int penaltyThreshold();

    /**
     * Calculate penalty value for given overlap in the target.
     *
     * @param target        target
     * @param overlapOffset offset inclusive
     * @param overlapLength length
     * @return negative penalty value
     */
    int overlapPenalty(NSequenceWithQuality target, int overlapOffset, int overlapLength);

    /**
     * Max errors to use in bitap matcher.
     *
     * @return max errors for bitap
     */
    int bitapMaxErrors();

    /**
     * Score for result of Not operator.
     *
     * @return not result score
     */
    int notResultScore();

    /**
     * Checks whether two aligners are of the same type (may be with different penalty threshold).
     *
     * @param otherAligner other aligner
     * @return true if aligners are compatible
     */
    boolean compatible(PatternAligner otherAligner);

    /**
     * Return new pattern with more strict threshold.
     *
     * @return copy of this PatternAligner with more strict threshold
     */
    PatternAligner overridePenaltyThreshold(int newThresholdValue);
}
