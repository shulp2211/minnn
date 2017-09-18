package com.milaboratory.mist.pattern;

import cc.redberry.pipe.OutputPort;

public abstract class MatchingResult {
    /**
     * Get iterator for all match results
     *
     * @param fairSorting true if we need fair sorting, otherwise false
     * @return OutputPort iterator for all match results
     */
    public abstract OutputPort<Match> getMatches(boolean fairSorting);

    /**
     * Get iterator for all match results
     *
     * @return OutputPort iterator for all match results
     */
    public OutputPort<Match> getMatches() {
        return getMatches(false);
    }

    /**
     * Get best matching result
     *
     * @param fairSorting true if we need fair sorting, otherwise false
     * @return best matching result
     */
    public Match getBestMatch(boolean fairSorting) {
        return getMatches(fairSorting).take();
    }

    /**
     * Get best matching result
     *
     * @return best matching result
     */
    public Match getBestMatch() {
        return getBestMatch(false);
    }

    /**
     * Check is pattern matched or not
     *
     * @return true if pattern matched
     */
    public boolean isFound() {
        return getMatches(false).take() != null;
    }
}
