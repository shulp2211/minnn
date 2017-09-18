package com.milaboratory.mist.pattern;

import cc.redberry.pipe.OutputPort;
import com.milaboratory.core.sequence.NSequenceWithQuality;
import com.milaboratory.mist.util.*;

import java.util.Arrays;

import static com.milaboratory.mist.pattern.MatchValidationType.FIRST;
import static com.milaboratory.mist.util.UnfairSorterConfiguration.unfairSorterPortLimits;

/**
 * This pattern takes multiple SinglePattern arguments and matches best of them that is found, or not matches
 * if all arguments didn't match.
 */
public final class OrPattern extends MultiplePatternsOperator {
    public OrPattern(PatternAligner patternAligner, SinglePattern... operandPatterns) {
        super(patternAligner, false, operandPatterns);
    }

    @Override
    public String toString() {
        return "OrPattern(" + Arrays.toString(operandPatterns) + ")";
    }

    @Override
    public MatchingResult match(NSequenceWithQuality target, int from, int to) {
        return new OrPatternMatchingResult(patternAligner, operandPatterns, target, from, to);
    }

    @Override
    public int estimateMaxLength() {
        int maxLength = -1;
        for (SinglePattern currentPattern : operandPatterns) {
            int currentPatternMaxLength = currentPattern.estimateMaxLength();
            if (currentPatternMaxLength == -1)
                return -1;
            else if (currentPatternMaxLength > maxLength)
                maxLength = currentPatternMaxLength;
        }
        return maxLength;
    }

    private static class OrPatternMatchingResult extends MatchingResult {
        private final PatternAligner patternAligner;
        private final SinglePattern[] operandPatterns;
        private final NSequenceWithQuality target;
        private final int from;
        private final int to;

        OrPatternMatchingResult(PatternAligner patternAligner, SinglePattern[] operandPatterns,
                                       NSequenceWithQuality target, int from, int to) {
            this.patternAligner = patternAligner;
            this.operandPatterns = operandPatterns;
            this.target = target;
            this.from = from;
            this.to = to;
        }

        @Override
        public OutputPort<Match> getMatches(boolean fairSorting) {
            ApproximateSorterConfiguration conf = new ApproximateSorterConfiguration(target, from, to, patternAligner,
                    false, fairSorting, FIRST, unfairSorterPortLimits.get(OrPattern.class),
                    operandPatterns);
            return new ApproximateSorter(conf).getOutputPort();
        }
    }
}
