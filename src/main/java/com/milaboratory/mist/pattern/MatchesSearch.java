package com.milaboratory.mist.pattern;

import java.util.ArrayList;

public abstract class MatchesSearch {
    protected ArrayList<Match> allMatches = new ArrayList<>();
    protected Match bestMatch = null;
    protected boolean quickSearchPerformed = false;
    protected boolean matchFound = false;
    protected boolean fullSearchPerformed = false;

    public Match[] getAllMatches() {
        if (!fullSearchPerformed) performSearch(false);
        return allMatches.toArray(new Match[allMatches.size()]);
    }

    public Match getBestMatch() {
        if (!fullSearchPerformed) performSearch(false);
        return bestMatch;
    }

    public long getMatchesNumber() {
        if (!fullSearchPerformed) performSearch(false);
        return allMatches.size();
    }

    public boolean isFound() {
        if (!quickSearchPerformed) performSearch(true);
        return matchFound;
    }

    /**
     * Find all matches and best match, calculate matches number.
     */
    protected abstract void performSearch(boolean quickSearch);
}
