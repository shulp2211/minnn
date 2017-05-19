package com.milaboratory.mist.pattern;

public interface Filter {
    /**
     * Returns the same match if it passed through the filter, or null if the match was filtered out.
     *
     * @param match match to check
     * @return the same match, or null if the match was filtered out
     */
    Match checkMatch(Match match);
}
