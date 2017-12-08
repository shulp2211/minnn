package com.milaboratory.mist.pattern;

import cc.redberry.pipe.OutputPort;

public interface MatchingResult {
    /**
     * Get iterator for all match results
     *
     * @param fairSorting true if we need fair sorting, otherwise false
     * @return OutputPort iterator for all match results
     */
    OutputPort<Match> getMatches(boolean fairSorting);

    /**
     * Get iterator for all match results
     *
     * @return OutputPort iterator for all match results
     */
    default OutputPort<Match> getMatches() {
        return getMatches(false);
    }

    /**
     * Get best matching result
     *
     * @param fairSorting true if we need fair sorting, otherwise false
     * @return best matching result
     */
    default Match getBestMatch(boolean fairSorting) {
        return getMatches(fairSorting).take();
    }

    /**
     * Get best matching result
     *
     * @return best matching result
     */
    default Match getBestMatch() {
        return getBestMatch(false);
    }

    /**
     * Check is pattern matched or not
     *
     * @return true if pattern matched
     */
    default boolean isFound() {
        return getMatches(false).take() != null;
    }
}
