package com.milaboratory.mist.pattern;

public class MultiplePatternsMatchingResult extends SimpleMatchingResult {
    private boolean quickBestMatchFound = false;
    private Match quickBestMatch;

    public MultiplePatternsMatchingResult() {
        super();
    }

    public MultiplePatternsMatchingResult(MatchesOutputPort matchesByScore, MatchesOutputPort matchesByCoordinate) {
        super(matchesByScore, matchesByCoordinate);
    }

    public MultiplePatternsMatchingResult(MatchesOutputPort matchesByScore, MatchesOutputPort matchesByCoordinate, Match quickBestMatch) {
        super(matchesByScore, matchesByCoordinate);
        this.quickBestMatch = quickBestMatch;
        this.quickBestMatchFound = true;
    }

    @Override
    public Match getBestMatch() {
        if (quickBestMatchFound)
            return quickBestMatch;
        else
            return matchesByScore.getBestMatch();
    }

    @Override
    public boolean isFound() {
        return quickBestMatchFound || matchesByScore.isFound();
    }
}
