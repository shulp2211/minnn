package com.milaboratory.mist.pattern;

import cc.redberry.pipe.OutputPort;
import com.milaboratory.core.sequence.NSequenceWithQuality;
import com.milaboratory.mist.util.ApproximateSorter;
import com.milaboratory.mist.util.SorterByCoordinate;
import com.milaboratory.mist.util.SorterByScore;

import java.util.ArrayList;
import java.util.Arrays;

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
    public MatchingResult match(NSequenceWithQuality target, int from, int to, byte targetId) {
        return new OrPatternMatchingResult(patternAligner, operandPatterns, target, from, to, targetId);
    }

    private static class OrPatternMatchingResult extends MatchingResult {
        private final PatternAligner patternAligner;
        private final SinglePattern[] operandPatterns;
        private final NSequenceWithQuality target;
        private final int from;
        private final int to;
        private final byte targetId;

        OrPatternMatchingResult(PatternAligner patternAligner, SinglePattern[] operandPatterns,
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
            ArrayList<OutputPort<Match>> operandPorts = new ArrayList<>();
            ApproximateSorter sorter;

            for (SinglePattern operandPattern : operandPatterns)
                operandPorts.add(operandPattern.match(target, from, to, targetId).getMatches(byScore, fairSorting));

            if (byScore)
                sorter = new SorterByScore(patternAligner, false, false, fairSorting,
                        MatchValidationType.FIRST);
            else
                sorter = new SorterByCoordinate(patternAligner, false, false, fairSorting,
                        MatchValidationType.FIRST);

            return sorter.getOutputPort(operandPorts);
        }
    }
}
