package com.milaboratory.mist.pattern;

import cc.redberry.pipe.OutputPort;
import com.milaboratory.core.sequence.NSequenceWithQuality;
import com.milaboratory.mist.util.ApproximateSorter;
import com.milaboratory.mist.util.SorterByCoordinate;
import com.milaboratory.mist.util.SorterByScore;

import java.util.ArrayList;
import java.util.Arrays;

import static com.milaboratory.mist.pattern.MatchValidationType.ORDER;

public final class PlusPattern extends MultiplePatternsOperator {
    public PlusPattern(PatternAligner patternAligner, SinglePattern... operandPatterns) {
        super(patternAligner, operandPatterns);
    }

    @Override
    public String toString() {
        return "PlusPattern(" + Arrays.toString(operandPatterns) + ")";
    }

    @Override
    public MatchingResult match(NSequenceWithQuality target, int from, int to, byte targetId) {
        return new PlusPatternMatchingResult(patternAligner, operandPatterns, target, from, to, targetId);
    }

    private static class PlusPatternMatchingResult extends MatchingResult {
        private final PatternAligner patternAligner;
        private final SinglePattern[] operandPatterns;
        private final NSequenceWithQuality target;
        private final int from;
        private final int to;
        private final byte targetId;

        PlusPatternMatchingResult(PatternAligner patternAligner, SinglePattern[] operandPatterns,
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
                sorter = new SorterByScore(patternAligner, false, true, fairSorting,
                        ORDER);
            else
                sorter = new SorterByCoordinate(patternAligner, false, true, fairSorting,
                        ORDER);

            return sorter.getOutputPort(operandPorts);
        }
    }
}
