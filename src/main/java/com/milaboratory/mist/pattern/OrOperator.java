package com.milaboratory.mist.pattern;

import cc.redberry.pipe.OutputPort;
import com.milaboratory.core.Range;
import com.milaboratory.core.sequence.MultiNSequenceWithQuality;
import com.milaboratory.mist.util.*;

import java.util.Arrays;
import java.util.stream.Collectors;

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
    public MatchingResult match(MultiNSequenceWithQuality target, Range[] ranges, boolean[] reverseComplements) {
        return new OrOperatorMatchingResult(patternAligner, operandPatterns, target, ranges, reverseComplements);
    }

    private static class OrOperatorMatchingResult extends MatchingResult {
        private final PatternAligner patternAligner;
        private final MultipleReadsOperator[] operandPatterns;
        private final MultiNSequenceWithQuality target;
        private final Range[] ranges;
        private final boolean[] reverseComplements;

        OrOperatorMatchingResult(PatternAligner patternAligner, MultipleReadsOperator[] operandPatterns,
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
                sorter = new SorterByScore(patternAligner, true, false, fairSorting,
                        LOGICAL_OR, unfairSorterPortLimits.get(OrOperator.class));
            else
                sorter = new SorterByCoordinate(patternAligner, true, false, fairSorting,
                        LOGICAL_OR, unfairSorterPortLimits.get(OrOperator.class));

            return sorter.getOutputPort(Arrays.stream(operandPatterns).map(pattern -> new ApproximateSorterOperandPort(
                    pattern.match(target, ranges, reverseComplements).getMatches(byScore, fairSorting),
                    unfairSorterPortLimits.get(pattern.getClass()))).collect(Collectors.toList()));
        }
    }
}
