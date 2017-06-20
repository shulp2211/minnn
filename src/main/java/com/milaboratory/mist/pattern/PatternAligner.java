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
    long penaltyThreshold();

    /**
     * Calculate penalty value for given overlap in the target.
     *
     * @param target        target
     * @param overlapOffset offset inclusive
     * @param overlapLength length
     * @return negative penalty value
     */
    long overlapPenalty(NSequenceWithQuality target, int overlapOffset, int overlapLength);

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
    default long notResultScore() {
        return 0;
    }

    /**
     * Maximal allowed overlap for two intersecting sub-patterns
     *
     * @return max overlap; or -1 if no limit on overlap size
     */
    default int maxOverlap() {
        return -1;
    }

    /**
     * Checks whether two aligners are of the same type (may be with different penalty threshold).
     *
     * @param otherAligner other aligner
     * @return true if aligners are compatible
     */
    boolean compatible(PatternAligner otherAligner);

    /**
     * Fixed left border position for alignment if it is specified; -1 if not specified.
     * If left border is specified, global aligner is used.
     *
     * @return left border position for alignment if it is specified; -1 if not specified
     */
    default int leftBorder() { return -1; }

    /**
     * Return new pattern with more strict penalty threshold.
     *
     * @return copy of this PatternAligner with more strict penalty threshold
     */
    PatternAligner overridePenaltyThreshold(long newThresholdValue);

    /**
     * Return new pattern with more strict maxOverlap threshold.
     *
     * @return copy of this PatternAligner with more strict maxOverlap threshold
     */
    PatternAligner overrideMaxOverlap(int newMaxOverlap);

    /**
     * Set left border for alignment. When it is set, global aligner is used.
     *
     * @param leftBorder left border for alignment
     * @return copy of this PatternAligner with fixed left border
     */
    PatternAligner setLeftBorder(int leftBorder);
}
