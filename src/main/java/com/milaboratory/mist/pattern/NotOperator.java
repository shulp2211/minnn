package com.milaboratory.mist.pattern;

import com.milaboratory.core.Range;
import com.milaboratory.core.sequence.MultiNSequenceWithQuality;

public class NotOperator extends MultipleReadsOperator {
    public NotOperator(MultipleReadsOperator... operandPatterns) {
        super(operandPatterns);
        if (operandPatterns.length != 1)
            throw new IllegalArgumentException("Not operator must take exactly 1 argument!");
        if (groupEdges.size() > 0)
            throw new IllegalStateException("Not operator must not contain group edges inside!");
    }

    @Override
    public MatchingResult match(MultiNSequenceWithQuality input, Range[] ranges, boolean[] reverseComplements) {
        final NotOperatorMatchesSearch matchesSearch = new NotOperatorMatchesSearch(operandPatterns, input, ranges, reverseComplements);
        final MatchesOutputPort allMatchesByScore = new MatchesOutputPort(matchesSearch, true);
        final MatchesOutputPort allMatchesByCoordinate = new MatchesOutputPort(matchesSearch, false);

        return new SimpleMatchingResult(allMatchesByScore, allMatchesByCoordinate);
    }

    private final class NotOperatorMatchesSearch extends MatchesSearch {
        private final MultipleReadsOperator[] operandPatterns;
        private final Range[] ranges;
        private final boolean[] reverseComplements;
        private final MultiNSequenceWithQuality input;

        NotOperatorMatchesSearch(MultipleReadsOperator[] operandPatterns, MultiNSequenceWithQuality input, Range[] ranges, boolean[] reverseComplements) {
            this.operandPatterns = operandPatterns;
            this.ranges = ranges;
            this.reverseComplements = reverseComplements;
            this.input = input;
        }

        @Override
        protected void performSearch(boolean quickSearch) {
            matchFound = !operandPatterns[0].match(input, ranges, reverseComplements).isFound();
            quickSearchPerformed = true;
            fullSearchPerformed = true;
        }
    }
}
