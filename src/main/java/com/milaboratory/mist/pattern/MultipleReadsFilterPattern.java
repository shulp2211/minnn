package com.milaboratory.mist.pattern;

import com.milaboratory.core.Range;
import com.milaboratory.core.sequence.MultiNSequenceWithQuality;

/**
 * Adapter for FilterPattern to pass as argument to operators that require MultipleReadsOperator argument.
 */
public final class MultipleReadsFilterPattern extends MultipleReadsOperator {
    private final FilterPattern filterPattern;
    private final Filter filter;

    public MultipleReadsFilterPattern(PatternAligner patternAligner, Filter filter, MultipleReadsOperator... operandPatterns) {
        super(patternAligner, operandPatterns);
        if (operandPatterns.length != 1)
            throw new IllegalArgumentException("Filter pattern must take exactly 1 operand pattern!");
        this.filterPattern = new FilterPattern(patternAligner, filter, operandPatterns[0]);
        this.filter = filter;
    }

    @Override
    public String toString() {
        return "MultipleReadsFilterPattern(" + filter + ", " + operandPatterns[0] + ")";
    }

    @Override
    public MatchingResult match(MultiNSequenceWithQuality target, Range[] ranges, boolean[] reverseComplements) {
        return filterPattern.match(target, ranges, reverseComplements);
    }
}
