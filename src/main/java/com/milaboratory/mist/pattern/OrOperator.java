package com.milaboratory.mist.pattern;

import cc.redberry.pipe.OutputPort;
import com.milaboratory.core.sequence.MultiNSequenceWithQuality;
import com.milaboratory.mist.util.*;

import java.util.Arrays;

import static com.milaboratory.mist.pattern.MatchValidationType.LOGICAL_OR;
import static com.milaboratory.mist.util.UnfairSorterConfiguration.unfairSorterPortLimits;

public final class OrOperator extends MultipleReadsOperator {
    public OrOperator(PatternAligner patternAligner, MultipleReadsOperator... operandPatterns) {
        super(patternAligner, false, operandPatterns);
    }

    @Override
    public String toString() {
        return "OrOperator(" + Arrays.toString(operandPatterns) + ")";
    }

    @Override
    public MatchingResult match(MultiNSequenceWithQuality target) {
        return new OrOperatorMatchingResult(patternAligner, operandPatterns, target);
    }

    private static class OrOperatorMatchingResult extends MatchingResult {
        private final PatternAligner patternAligner;
        private final MultipleReadsOperator[] operandPatterns;
        private final MultiNSequenceWithQuality target;

        OrOperatorMatchingResult(PatternAligner patternAligner, MultipleReadsOperator[] operandPatterns,
                                  MultiNSequenceWithQuality target) {
            this.patternAligner = patternAligner;
            this.operandPatterns = operandPatterns;
            this.target = target;
        }

        @Override
        public OutputPort<Match> getMatches(boolean fairSorting) {
            ApproximateSorterConfiguration conf = new ApproximateSorterConfiguration(target, patternAligner,
                    false, false, fairSorting, LOGICAL_OR,
                    unfairSorterPortLimits.get(OrOperator.class), operandPatterns);
            return new ApproximateSorter(conf).getOutputPort();
        }
    }
}
