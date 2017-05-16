package com.milaboratory.mist.pattern;

public final class ScoreFilter implements Filter {
    private final float scoreThreshold;

    public ScoreFilter(float scoreThreshold) {
        this.scoreThreshold = scoreThreshold;
    }

    @Override
    public Match checkMatch(Match match) {
        if (match.getScore() < scoreThreshold)
            return null;
        else return match;
    }
}
