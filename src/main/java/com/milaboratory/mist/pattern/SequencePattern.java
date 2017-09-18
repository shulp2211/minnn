package com.milaboratory.mist.pattern;

import cc.redberry.pipe.OutputPort;
import com.milaboratory.core.sequence.NSequenceWithQuality;
import com.milaboratory.mist.util.*;

import java.util.Arrays;

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
    public MatchingResult match(NSequenceWithQuality target, int from, int to) {
        return new SequencePatternMatchingResult(patternAligner, operandPatterns, target, from, to);
    }

    @Override
    public int estimateMaxLength() {
        int maxGap = Math.max(patternAligner.maxOverlap(), patternAligner.bitapMaxErrors());
        int summaryLength = maxGap * (operandPatterns.length - 1);
        for (SinglePattern currentPattern : operandPatterns) {
            int currentPatternMaxLength = currentPattern.estimateMaxLength();
            if (currentPatternMaxLength == -1)
                return -1;
            else
                summaryLength += currentPatternMaxLength;
        }
        return summaryLength;
    }

    private static class SequencePatternMatchingResult extends MatchingResult {
        private final PatternAligner patternAligner;
        private final SinglePattern[] operandPatterns;
        private final NSequenceWithQuality target;
        private final int from;
        private final int to;

        SequencePatternMatchingResult(PatternAligner patternAligner, SinglePattern[] operandPatterns,
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
                    true, fairSorting, FOLLOWING, unfairSorterPortLimits.get(SequencePattern.class),
                    operandPatterns);
            return new ApproximateSorter(conf).getOutputPort();
        }
    }
}
