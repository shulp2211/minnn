package com.milaboratory.mist.pattern;

import com.milaboratory.core.sequence.MultiNSequenceWithQuality;

/**
 * Adapter for FilterPattern to pass as argument to operators that require MultipleReadsOperator argument.
 */
public final class MultipleReadsFilterPattern extends MultipleReadsOperator {
    private final FilterPattern filterPattern;
    private final Filter filter;

    public MultipleReadsFilterPattern(long scoreThreshold, Filter filter,
                                      MultipleReadsOperator... operandPatterns) {
        super(scoreThreshold, operandPatterns);
        if (operandPatterns.length != 1)
            throw new IllegalArgumentException("Filter pattern must take exactly 1 operand pattern!");
        this.filterPattern = new FilterPattern(scoreThreshold, filter, operandPatterns[0]);
        this.filter = filter;
    }

    @Override
    public String toString() {
        return "MultipleReadsFilterPattern(" + filter + ", " + operandPatterns[0] + ")";
    }

    @Override
    public MatchingResult match(MultiNSequenceWithQuality target) {
        return filterPattern.match(target);
    }

    @Override
    public long estimateComplexity() {
        return filterPattern.estimateComplexity();
    }
}
