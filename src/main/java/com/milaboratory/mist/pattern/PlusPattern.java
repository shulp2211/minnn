package com.milaboratory.mist.pattern;

import cc.redberry.pipe.OutputPort;
import com.milaboratory.core.sequence.NSequenceWithQuality;
import com.milaboratory.mist.util.ApproximateSorter;
import com.milaboratory.mist.util.SorterByCoordinate;
import com.milaboratory.mist.util.SorterByScore;

import java.util.ArrayList;

public class PlusPattern extends MultiplePatternsOperator {
    public PlusPattern(SinglePattern... operandPatterns) {
        super(operandPatterns);
    }

    @Override
    public MatchingResult match(NSequenceWithQuality target, int from, int to, byte targetId) {
        return new PlusPatternMatchingResult(operandPatterns, target, from, to, targetId);
    }

    private static class PlusPatternMatchingResult extends MatchingResult {
        private final SinglePattern[] operandPatterns;
        private final NSequenceWithQuality target;
        private final int from;
        private final int to;
        private final byte targetId;

        PlusPatternMatchingResult(SinglePattern[] operandPatterns, NSequenceWithQuality target, int from, int to, byte targetId) {
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
                sorter = new SorterByScore(false, false, true,
                        fairSorting, MatchValidationType.ORDER);
            else
                sorter = new SorterByCoordinate(false, false, true,
                        fairSorting, MatchValidationType.ORDER);

            return sorter.getOutputPort(operandPorts);
        }
    }
}
