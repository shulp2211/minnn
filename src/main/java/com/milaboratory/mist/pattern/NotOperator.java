package com.milaboratory.mist.pattern;

import cc.redberry.pipe.OutputPort;
import com.milaboratory.core.Range;
import com.milaboratory.core.sequence.MultiNSequenceWithQuality;

import java.util.ArrayList;

public class NotOperator extends MultipleReadsOperator {
    public static final float NOT_RESULT_SCORE = 0;

    public NotOperator(MultipleReadsOperator... operandPatterns) {
        super(operandPatterns);
        if (operandPatterns.length != 1)
            throw new IllegalArgumentException("Not operator must take exactly 1 argument!");
        if (groupEdges.size() > 0)
            throw new IllegalStateException("Not operator must not contain group edges inside!");
    }

    @Override
    public MatchingResult match(MultiNSequenceWithQuality target, Range[] ranges, boolean[] reverseComplements) {
        return new NotOperatorMatchingResult(operandPatterns[0], target, ranges, reverseComplements);
    }

    private static class NotOperatorMatchingResult extends MatchingResult {
        private final MultipleReadsOperator operandPattern;
        private final MultiNSequenceWithQuality target;
        private final Range[] ranges;
        private final boolean[] reverseComplements;

        NotOperatorMatchingResult(MultipleReadsOperator operandPattern,
                                 MultiNSequenceWithQuality target, Range[] ranges, boolean[] reverseComplements) {
            this.operandPattern = operandPattern;
            this.target = target;
            this.ranges = ranges;
            this.reverseComplements = reverseComplements;
        }

        @Override
        public OutputPort<Match> getMatches(boolean byScore, boolean fairSorting) {
            return new NotOperatorOutputPort(operandPattern.match(target, ranges, reverseComplements).getMatches(byScore, fairSorting));
        }

        private static class NotOperatorOutputPort implements OutputPort<Match> {
            private final OutputPort<Match> operandPort;
            private boolean operandChecked = false;
            private boolean operandIsMatching;

            NotOperatorOutputPort(OutputPort<Match> operandPort) {
                this.operandPort = operandPort;
            }

            @Override
            public Match take() {
                if (!operandChecked) {
                    operandIsMatching = (operandPort.take() != null);
                    operandChecked = true;
                }
                if (operandIsMatching)
                    return null;
                else
                    return new Match(1, NOT_RESULT_SCORE, new ArrayList<MatchedItem>() {{
                        add(new NullMatchedRange(0)); }});
            }
        }
    }
}
