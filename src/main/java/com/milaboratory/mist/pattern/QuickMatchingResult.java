package com.milaboratory.mist.pattern;

import cc.redberry.pipe.OutputPort;

public final class QuickMatchingResult implements MatchingResult {
    private final boolean found;

    QuickMatchingResult(boolean found) {
        this.found = found;
    }

    public OutputPort<Match> getMatches(boolean byScore) {
        throw new IllegalStateException("getMatches should not be called for QuickMatchingResult.");
    }

    public Match getBestMatch() {
        throw new IllegalStateException("getBestMatch should not be called for QuickMatchingResult.");
    }

    public long getMatchesNumber() {
        throw new IllegalStateException("getMatchesNumber should not be called for QuickMatchingResult.");
    }

    /**
     * Check is pattern matched or not
     *
     * @return true if pattern matched
     */
    public boolean isFound() {
        return found;
    }
}
