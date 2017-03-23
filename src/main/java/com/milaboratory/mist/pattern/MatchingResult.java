package com.milaboratory.mist.pattern;

import cc.redberry.pipe.OutputPort;

public interface MatchingResult {
    /**
     * Get iterator for all match results
     *
     * @param byScore Order by score if true, by coordinate if false
     * @return OutputPort iterator for all match results
     */
    OutputPort<Match> getMatches(boolean byScore);

    /**
     * Get iterator for all match results
     *
     * @return OutputPort iterator for all match results
     */
    default OutputPort<Match> getMatches() {
        return getMatches(true);
    }

    /**
     * Get best matching result
     *
     * @return best matching result
     */
    Match getBestMatch();

    /**
     * Get number of matching results
     *
     * @return number of matching results
     */
    long getMatchesNumber();

    /**
     * Check is pattern matched or not
     *
     * @return true if pattern matched
     */
    default boolean isFound() {
        return getMatchesNumber() > 0;
    }
}
