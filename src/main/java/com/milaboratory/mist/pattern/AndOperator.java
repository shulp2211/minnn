package com.milaboratory.mist.pattern;

import cc.redberry.pipe.OutputPort;
import com.milaboratory.core.sequence.MultiNSequenceWithQuality;
import com.milaboratory.mist.util.*;

import java.util.Arrays;

import static com.milaboratory.mist.pattern.MatchValidationType.LOGICAL_AND;
import static com.milaboratory.mist.util.UnfairSorterConfiguration.unfairSorterPortLimits;

public final class AndOperator extends MultipleReadsOperator {
    public AndOperator(PatternAligner patternAligner, MultipleReadsOperator... operandPatterns) {
        super(patternAligner, operandPatterns);
    }

    @Override
    public String toString() {
        return "AndOperator(" + Arrays.toString(operandPatterns) + ")";
    }

    @Override
    public MatchingResult match(MultiNSequenceWithQuality target) {
        return new AndOperatorMatchingResult(patternAligner, operandPatterns, target);
    }

    private static class AndOperatorMatchingResult extends MatchingResult {
        private final PatternAligner patternAligner;
        private final MultipleReadsOperator[] operandPatterns;
        private final MultiNSequenceWithQuality target;

        AndOperatorMatchingResult(PatternAligner patternAligner, MultipleReadsOperator[] operandPatterns,
                                  MultiNSequenceWithQuality target) {
            this.patternAligner = patternAligner;
            this.operandPatterns = operandPatterns;
            this.target = target;
        }

        @Override
        public OutputPort<Match> getMatches(boolean fairSorting) {
            ApproximateSorterConfiguration conf = new ApproximateSorterConfiguration(target, patternAligner,
                    false, true, fairSorting, LOGICAL_AND,
                    unfairSorterPortLimits.get(AndOperator.class), operandPatterns);
            return new ApproximateSorter(conf).getOutputPort();
        }
    }
}
