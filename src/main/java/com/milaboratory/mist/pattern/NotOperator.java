package com.milaboratory.mist.pattern;

import com.milaboratory.core.sequence.MultiNSequenceWithQuality;

public class NotOperator implements Pattern {
    private final Pattern operandPattern;

    public NotOperator(Pattern operandPattern) {
        if (operandPattern.areGroupsInside())
            throw new IllegalStateException("Not operator must not contain groups inside!");
        this.operandPattern = operandPattern;
    }

    @Override
    public MatchingResult match(MultiNSequenceWithQuality input) {
        final NotOperatorMatchesSearch matchesSearch = new NotOperatorMatchesSearch(operandPattern, input);
        final MatchesOutputPort allMatchesByScore = new MatchesOutputPort(matchesSearch, true);
        final MatchesOutputPort allMatchesByCoordinate = new MatchesOutputPort(matchesSearch, false);

        return new SimpleMatchingResult(allMatchesByScore, allMatchesByCoordinate);
    }

    @Override
    public boolean areGroupsInside() {
        return false;
    }

    private final class NotOperatorMatchesSearch extends MatchesSearch {
        private final Pattern operandPattern;
        private final MultiNSequenceWithQuality input;

        public NotOperatorMatchesSearch(Pattern operandPattern, MultiNSequenceWithQuality input) {
            this.operandPattern = operandPattern;
            this.input = input;
        }

        @Override
        protected void performSearch(boolean quickSearch) {
            matchFound = !operandPattern.match(input).isFound();
            quickSearchPerformed = true;
            fullSearchPerformed = true;
        }
    }
}
