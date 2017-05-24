package com.milaboratory.mist.pattern;

import cc.redberry.pipe.OutputPort;
import com.milaboratory.core.Range;
import com.milaboratory.core.sequence.MultiNSequenceWithQuality;

import java.util.ArrayList;

public final class NotOperator extends MultipleReadsOperator {
    public NotOperator(PatternAligner patternAligner, MultipleReadsOperator... operandPatterns) {
        super(patternAligner, operandPatterns);
        if (operandPatterns.length != 1)
            throw new IllegalArgumentException("Not operator must take exactly 1 operand!");
        if (groupEdges.size() > 0)
            throw new IllegalStateException("Not operator must not contain group edges inside!");
    }

    @Override
    public MatchingResult match(MultiNSequenceWithQuality target, Range[] ranges, boolean[] reverseComplements) {
        return new NotOperatorMatchingResult(patternAligner, operandPatterns[0], target, ranges, reverseComplements);
    }

    private static class NotOperatorMatchingResult extends MatchingResult {
        private final PatternAligner patternAligner;
        private final MultipleReadsOperator operandPattern;
        private final MultiNSequenceWithQuality target;
        private final Range[] ranges;
        private final boolean[] reverseComplements;

        NotOperatorMatchingResult(PatternAligner patternAligner, MultipleReadsOperator operandPattern,
                                 MultiNSequenceWithQuality target, Range[] ranges, boolean[] reverseComplements) {
            this.patternAligner = patternAligner;
            this.operandPattern = operandPattern;
            this.target = target;
            this.ranges = ranges;
            this.reverseComplements = reverseComplements;
        }

        @Override
        public OutputPort<Match> getMatches(boolean byScore, boolean fairSorting) {
            return new NotOperatorOutputPort(patternAligner, operandPattern.match(target, ranges, reverseComplements)
                    .getMatches(byScore, fairSorting));
        }

        private static class NotOperatorOutputPort implements OutputPort<Match> {
            private final PatternAligner patternAligner;
            private final OutputPort<Match> operandPort;
            private boolean firstCall = true;
            private boolean operandIsMatching;

            NotOperatorOutputPort(PatternAligner patternAligner, OutputPort<Match> operandPort) {
                this.patternAligner = patternAligner;
                this.operandPort = operandPort;
            }

            @Override
            public Match take() {
                if (!firstCall) return null;
                operandIsMatching = (operandPort.take() != null);
                firstCall = false;
                if (operandIsMatching)
                    return null;
                else {
                    ArrayList<MatchedItem> matchedItems = new ArrayList<>();
                    matchedItems.add(new NullMatchedRange(0));
                    return new Match(1, patternAligner.notResultScore(), matchedItems);
                }
            }
        }
    }
}
