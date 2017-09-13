package com.milaboratory.mist.pattern;

import cc.redberry.pipe.OutputPort;
import com.milaboratory.core.sequence.NSequenceWithQuality;
import com.milaboratory.mist.util.*;

import java.util.Arrays;

import static com.milaboratory.mist.pattern.MatchValidationType.INTERSECTION;
import static com.milaboratory.mist.util.UnfairSorterConfiguration.unfairSorterPortLimits;

public final class AndPattern extends MultiplePatternsOperator {
    public AndPattern(PatternAligner patternAligner, SinglePattern... operandPatterns) {
        super(patternAligner, operandPatterns);
    }

    @Override
    public String toString() {
        return "AndPattern(" + Arrays.toString(operandPatterns) + ")";
    }

    @Override
    public MatchingResult match(NSequenceWithQuality target, int from, int to) {
        return new AndPatternMatchingResult(patternAligner, operandPatterns, target, from, to);
    }

    private static class AndPatternMatchingResult extends MatchingResult {
        private final PatternAligner patternAligner;
        private final SinglePattern[] operandPatterns;
        private final NSequenceWithQuality target;
        private final int from;
        private final int to;

        AndPatternMatchingResult(PatternAligner patternAligner, SinglePattern[] operandPatterns,
                                 NSequenceWithQuality target, int from, int to) {
            this.patternAligner = patternAligner;
            this.operandPatterns = operandPatterns;
            this.target = target;
            this.from = from;
            this.to = to;
        }

        @Override
        public OutputPort<Match> getMatches(boolean byScore, boolean fairSorting) {
            ApproximateSorterConfiguration conf = new ApproximateSorterConfiguration(target, from, to, patternAligner,
                    true, fairSorting, INTERSECTION, unfairSorterPortLimits.get(AndPattern.class),
                    operandPatterns);
            ApproximateSorter sorter;

            if (byScore)
                sorter = new SorterByScore(conf);
            else
                sorter = new SorterByCoordinate(conf);

            return sorter.getOutputPort();
        }
    }
}
