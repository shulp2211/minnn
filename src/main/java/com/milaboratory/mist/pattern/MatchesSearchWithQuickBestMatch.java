package com.milaboratory.mist.pattern;

public abstract class MatchesSearchWithQuickBestMatch extends MatchesSearch {
    protected boolean quickBestMatchSearchPerformed = false;
    protected boolean quickBestMatchFound = false;

    @Override
    public Match getBestMatch() {
        if (!quickBestMatchSearchPerformed) performQuickBestMatchSearch();
        if (!quickBestMatchFound && !fullSearchPerformed) performSearch(false);
        return bestMatch;
    }

    /**
     * Find quick best match.
     */
    protected abstract void performQuickBestMatchSearch();
}
