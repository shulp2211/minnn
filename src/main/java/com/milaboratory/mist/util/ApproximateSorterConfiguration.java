package com.milaboratory.mist.util;

import com.milaboratory.core.sequence.MultiNSequenceWithQuality;
import com.milaboratory.mist.pattern.*;

import static com.milaboratory.mist.pattern.MatchValidationType.*;

public final class ApproximateSorterConfiguration {
    final Pattern[] operandPatterns;
    final MultiNSequenceWithQuality target;
    private final int from;
    private final int to;
    final PatternAligner patternAligner;
    final boolean multipleReads;
    final boolean combineScoresBySum;
    final boolean fairSorting;
    final MatchValidationType matchValidationType;
    final int unfairSorterLimit;
    final boolean specificOutputPorts;

    /**
     * Configuration for MultipleReadsOperator patterns.
     *
     * @param target target nucleotide sequence (or multiple sequences)
     * @param patternAligner pattern aligner that provides information about scoring and pattern overlap limits
     * @param combineScoresBySum true if combined score must be equal to sum of match scores; false if combined
     *                           score must be the highest of match scores
     * @param fairSorting true if we need slow but fair sorting
     * @param matchValidationType type of validation used to determine that current matches combination is invalid
     * @param unfairSorterLimit maximum number of output values for this port for unfair sorter
     * @param operandPatterns operand patterns
     */
    public ApproximateSorterConfiguration(MultiNSequenceWithQuality target, PatternAligner patternAligner,
            boolean combineScoresBySum, boolean fairSorting, MatchValidationType matchValidationType,
            int unfairSorterLimit, Pattern... operandPatterns) {
        this.operandPatterns = operandPatterns;
        this.target = target;
        this.from = -1;
        this.to = -1;
        this.patternAligner = patternAligner;
        this.multipleReads = true;
        this.combineScoresBySum = combineScoresBySum;
        this.fairSorting = fairSorting;
        this.matchValidationType = matchValidationType;
        this.unfairSorterLimit = unfairSorterLimit;
        this.specificOutputPorts = false;
        if (((matchValidationType == INTERSECTION) || (matchValidationType == ORDER)
                || (matchValidationType == FOLLOWING) || (matchValidationType == FIRST)))
            throw new IllegalArgumentException("Invalid combination of multipleReads and matchValidationType flags: " +
                    "multipleReads = true, matchValidationType = " + matchValidationType);
        if (operandPatterns.length == 0)
            throw new IllegalArgumentException("Operand patterns array is empty!");
        for (Pattern operandPattern : operandPatterns) {
            if (!(operandPattern instanceof MultipleReadsOperator))
                throw new IllegalArgumentException("Invalid combination of multipleReads and operand pattern class: "
                        + "multipleReads = true, operand class: " + operandPattern.getClass());
        }
        if (target.numberOfSequences() == 1)
            throw new IllegalArgumentException("Invalid combination of multipleReads and target number of sequences: "
                    + "multipleReads = true, target number of sequences: 1");
    }

    /**
     * Configuration for SinglePattern patterns.
     *
     * @param target target nucleotide sequence (or multiple sequences)
     * @param from left border in target for range where to search matches, inclusive
     * @param to right border in target for range where to search matches, exclusive
     * @param patternAligner pattern aligner that provides information about scoring and pattern overlap limits
     * @param combineScoresBySum true if combined score must be equal to sum of match scores; false if combined
     *                           score must be the highest of match scores
     * @param fairSorting true if we need slow but fair sorting
     * @param matchValidationType type of validation used to determine that current matches combination is invalid
     * @param unfairSorterLimit maximum number of output values for this port for unfair sorter
     * @param operandPatterns operand patterns
     */
    public ApproximateSorterConfiguration(MultiNSequenceWithQuality target, int from, int to,
            PatternAligner patternAligner, boolean combineScoresBySum, boolean fairSorting,
            MatchValidationType matchValidationType, int unfairSorterLimit, SinglePattern... operandPatterns) {
        this.operandPatterns = operandPatterns;
        this.target = target;
        this.from = from;
        this.to = to;
        this.patternAligner = patternAligner;
        this.multipleReads = false;
        this.combineScoresBySum = combineScoresBySum;
        this.fairSorting = fairSorting;
        this.matchValidationType = matchValidationType;
        this.unfairSorterLimit = unfairSorterLimit;
        this.specificOutputPorts = !fairSorting
                && ((matchValidationType == ORDER) || (matchValidationType == FOLLOWING));
        if ((from < 0) || (to < 0))
            throw new IllegalArgumentException("Invalid from and to arguments: from = " + from + ", to = " + to);
        if ((matchValidationType == LOGICAL_AND) || (matchValidationType == LOGICAL_OR))
            throw new IllegalArgumentException("Invalid combination of multipleReads and matchValidationType flags: " +
                    "multipleReads = false, matchValidationType = " + matchValidationType);
        if (operandPatterns.length == 0)
            throw new IllegalArgumentException("Operand patterns array is empty!");
        for (Pattern operandPattern : operandPatterns) {
            if (!(operandPattern instanceof SinglePattern))
                throw new IllegalArgumentException("Invalid combination of multipleReads and operand pattern class: "
                        + "multipleReads = false, operand class: " + operandPattern.getClass());
        }
        if (target.numberOfSequences() != 1)
            throw new IllegalArgumentException("Invalid combination of multipleReads and target number of sequences: "
                    + "multipleReads = false, target number of sequences: " + target.numberOfSequences());
    }

    int from() {
        if (multipleReads)
            throw new IllegalStateException("Trying to get \"from\" when multipleReads is true!");
        return from;
    }

    int to() {
        if (multipleReads)
            throw new IllegalStateException("Trying to get \"to\" when multipleReads is true!");
        return to;
    }
}
