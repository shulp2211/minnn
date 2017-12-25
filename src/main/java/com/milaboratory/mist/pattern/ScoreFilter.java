package com.milaboratory.mist.pattern;

public final class ScoreFilter implements Filter {
    private final long scoreThreshold;

    public ScoreFilter(long scoreThreshold) {
        this.scoreThreshold = scoreThreshold;
    }

    @Override
    public String toString() {
        return "ScoreFilter(" + scoreThreshold + ")";
    }

    @Override
    public MatchIntermediate checkMatch(MatchIntermediate match) {
        if (match.getScore() < scoreThreshold)
            return null;
        else return match;
    }
}
