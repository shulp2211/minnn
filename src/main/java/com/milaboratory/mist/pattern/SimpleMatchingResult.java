package com.milaboratory.mist.pattern;

import cc.redberry.pipe.OutputPort;

public class SimpleMatchingResult implements MatchingResult {
    protected final MatchesOutputPort matchesByScore;
    protected final MatchesOutputPort matchesByCoordinate;

    public SimpleMatchingResult(MatchesOutputPort matchesByScore, MatchesOutputPort matchesByCoordinate) {
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
        return matchesByScore.getBestMatch();
    }

    @Override
    public long getMatchesNumber() {
        return matchesByScore.getMatchesNumber();
    }

    @Override
    public boolean isFound() {
        return matchesByScore.isFound();
    }
}
