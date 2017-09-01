package com.milaboratory.mist.pattern;

import cc.redberry.pipe.OutputPort;
import com.milaboratory.core.Range;
import com.milaboratory.core.sequence.MultiNSequenceWithQuality;
import com.milaboratory.mist.util.*;

import java.util.Arrays;
import java.util.stream.Collectors;

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
    public MatchingResult match(MultiNSequenceWithQuality target, Range[] ranges, boolean[] reverseComplements) {
        return new AndOperatorMatchingResult(patternAligner, operandPatterns, target, ranges, reverseComplements);
    }

    private static class AndOperatorMatchingResult extends MatchingResult {
        private final PatternAligner patternAligner;
        private final MultipleReadsOperator[] operandPatterns;
        private final MultiNSequenceWithQuality target;
        private final Range[] ranges;
        private final boolean[] reverseComplements;

        AndOperatorMatchingResult(PatternAligner patternAligner, MultipleReadsOperator[] operandPatterns,
                                  MultiNSequenceWithQuality target, Range[] ranges, boolean[] reverseComplements) {
            this.patternAligner = patternAligner;
            this.operandPatterns = operandPatterns;
            this.target = target;
            this.ranges = ranges;
            this.reverseComplements = reverseComplements;
        }

        @Override
        public OutputPort<Match> getMatches(boolean byScore, boolean fairSorting) {
            ApproximateSorter sorter;

            if (byScore)
                sorter = new SorterByScore(patternAligner, true, true, fairSorting,
                        LOGICAL_AND, unfairSorterPortLimits.get(AndOperator.class));
            else
                sorter = new SorterByCoordinate(patternAligner, true, true, fairSorting,
                        LOGICAL_AND, unfairSorterPortLimits.get(AndOperator.class));

            return sorter.getOutputPort(Arrays.stream(operandPatterns).map(pattern -> new ApproximateSorterOperandPort(
                    pattern.match(target, ranges, reverseComplements).getMatches(byScore, fairSorting),
                    unfairSorterPortLimits.get(pattern.getClass()))).collect(Collectors.toList()));
        }
    }
}
