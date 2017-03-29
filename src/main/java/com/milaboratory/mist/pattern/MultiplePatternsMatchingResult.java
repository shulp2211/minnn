package com.milaboratory.mist.pattern;

import cc.redberry.pipe.OutputPort;

public class MultiplePatternsMatchingResult implements MatchingResult {
    private final Match bestMatch;
    private final OperatorOutputPort matchesByScore;
    private final OperatorOutputPort matchesByCoordinate;

    public MultiplePatternsMatchingResult(Match bestMatch, OperatorOutputPort matchesByScore, OperatorOutputPort matchesByCoordinate) {
        this.bestMatch = bestMatch;
        this.matchesByScore = matchesByScore;
        this.matchesByCoordinate = matchesByCoordinate;
    }

    @Override
    public OutputPort<Match> getMatches(boolean byScore) {
        if (byScore)
            return matchesByScore;
        else
            return matchesByCoordinate;
    }

    @Override
    public Match getBestMatch() {
        return bestMatch;
    }

    @Override
    public long getMatchesNumber() {
        return matchesByScore.getMatchesNumber();
    }

    /**
     * Overriding isFound() because calling getMatchesNumber() may lead to complex calculations.
     *
     * @return true if pattern matched
     */
    @Override
    public boolean isFound() {
        return bestMatch != null;
    }
}
