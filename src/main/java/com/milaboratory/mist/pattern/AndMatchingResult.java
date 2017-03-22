package com.milaboratory.mist.pattern;

import cc.redberry.pipe.OutputPort;

public class AndMatchingResult implements MatchingResult {
    private final Match bestMatch;
    private final long matchesNumber;

    public AndMatchingResult(Match bestMatch, long matchesNumber) {
        this.bestMatch = bestMatch;
        this.matchesNumber = matchesNumber;
    }

    @Override
    public OutputPort<Match> getMatches() {
        return null;
    }

    @Override
    public Match getBestMatch() {
        return bestMatch;
    }

    @Override
    public long getMatchesNumber() {
        return matchesNumber;
    }
}
