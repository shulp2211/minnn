package com.milaboratory.mist.pattern;

import cc.redberry.pipe.OutputPort;
import com.milaboratory.core.sequence.NSequenceWithQuality;
import com.milaboratory.mist.util.*;

import java.util.Arrays;
import java.util.stream.Collectors;

import static com.milaboratory.mist.pattern.MatchValidationType.FOLLOWING;
import static com.milaboratory.mist.util.UnfairSorterConfiguration.unfairSorterPortLimits;

public final class SequencePattern extends MultiplePatternsOperator {
    public SequencePattern(PatternAligner patternAligner, SinglePattern... operandPatterns) {
        super(patternAligner, operandPatterns);
    }

    @Override
    public String toString() {
        return "SequencePattern(" + Arrays.toString(operandPatterns) + ")";
    }

    @Override
    public MatchingResult match(NSequenceWithQuality target, int from, int to, byte targetId) {
        return new SequencePatternMatchingResult(patternAligner, operandPatterns, target, from, to, targetId);
    }

    private static class SequencePatternMatchingResult extends MatchingResult {
        private final PatternAligner patternAligner;
        private final SinglePattern[] operandPatterns;
        private final NSequenceWithQuality target;
        private final int from;
        private final int to;
        private final byte targetId;

        SequencePatternMatchingResult(PatternAligner patternAligner, SinglePattern[] operandPatterns,
                                      NSequenceWithQuality target, int from, int to, byte targetId) {
            this.patternAligner = patternAligner;
            this.operandPatterns = operandPatterns;
            this.target = target;
            this.from = from;
            this.to = to;
            this.targetId = targetId;
        }

        @Override
        public OutputPort<Match> getMatches(boolean byScore, boolean fairSorting) {
            ApproximateSorter sorter;

            if (byScore)
                sorter = new SorterByScore(patternAligner, false, true, fairSorting,
                        FOLLOWING, unfairSorterPortLimits.get(SequencePattern.class));
            else
                sorter = new SorterByCoordinate(patternAligner, false, true, fairSorting,
                        FOLLOWING, unfairSorterPortLimits.get(SequencePattern.class));

            return sorter.getOutputPort(Arrays.stream(operandPatterns).map(pattern -> new ApproximateSorterOperandPort(
                    pattern.match(target, from, to, targetId).getMatches(byScore, fairSorting),
                    unfairSorterPortLimits.get(pattern.getClass()))).collect(Collectors.toList()));
        }
    }
}
