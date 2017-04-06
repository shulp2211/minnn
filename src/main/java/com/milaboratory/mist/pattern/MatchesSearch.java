package com.milaboratory.mist.pattern;

import java.util.ArrayList;

public abstract class MatchesSearch {
    protected ArrayList<Match> allMatches = new ArrayList<>();
    protected Match bestMatch = null;
    protected boolean quickSearchPerformed = false;
    protected boolean matchFound = false;
    protected boolean fullSearchPerformed = false;

    public Match[] getAllMatches() {
        if (!(quickSearchPerformed && !matchFound) && !fullSearchPerformed)
            performSearch(false);
        return allMatches.toArray(new Match[allMatches.size()]);
    }

    public Match getBestMatch() {
        if (!(quickSearchPerformed && !matchFound) && !fullSearchPerformed)
            performSearch(false);
        return bestMatch;
    }

    public long getMatchesNumber() {
        if (!(quickSearchPerformed && !matchFound) && !fullSearchPerformed)
            performSearch(false);
        return allMatches.size();
    }

    public boolean isFound() {
        if (!quickSearchPerformed) performSearch(true);
        return matchFound;
    }

    /**
     * Find all matches and best match, calculate matches number.
     *
     * @param quickSearch true if searching only for isFound(), false for full search
     */
    protected abstract void performSearch(boolean quickSearch);
}
