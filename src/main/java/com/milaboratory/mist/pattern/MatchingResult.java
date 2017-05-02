package com.milaboratory.mist.pattern;

import cc.redberry.pipe.OutputPort;

public abstract class MatchingResult {
    /**
     * Get iterator for all match results
     *
     * @param byScore Order by score if true, by coordinate if false
     * @param fairSorting true if we need fair sorting, otherwise false
     * @return OutputPort iterator for all match results
     */
    public abstract OutputPort<Match> getMatches(boolean byScore, boolean fairSorting);

    /**
     * Get iterator for all match results
     *
     * @return OutputPort iterator for all match results
     */
    public OutputPort<Match> getMatches() {
        return getMatches(true, false);
    }

    /**
     * Get best matching result
     *
     * @param fairSorting true if we need fair sorting, otherwise false
     * @return best matching result
     */
    public Match getBestMatch(boolean fairSorting) {
        return getMatches(true, fairSorting).take();
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
        return getMatches(false, false).take() != null;
    }
}
