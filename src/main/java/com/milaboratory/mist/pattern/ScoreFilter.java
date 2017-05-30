package com.milaboratory.mist.pattern;

public final class ScoreFilter implements Filter {
    private final int scoreThreshold;

    public ScoreFilter(int scoreThreshold) {
        this.scoreThreshold = scoreThreshold;
    }

    @Override
    public Match checkMatch(Match match) {
        if (match.getScore() < scoreThreshold)
            return null;
        else return match;
    }
}
