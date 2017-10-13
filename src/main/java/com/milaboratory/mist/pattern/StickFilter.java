package com.milaboratory.mist.pattern;

import com.milaboratory.core.Range;

import java.util.ArrayList;

public final class StickFilter implements Filter {
    private final boolean left;
    private final int position;

    public StickFilter(boolean left, int position) {
        this.left = left;
        this.position = position;
    }

    @Override
    public String toString() {
        return "StickFilter(" + left + ", " + position + ")";
    }

    @Override
    public Match checkMatch(Match match) {
        ArrayList<MatchedRange> matchedRanges = match.getMatchedRanges();
        if (matchedRanges.size() != 1)
            throw new IllegalArgumentException("Expected exactly 1 matched range in StickFilter; got "
                    + matchedRanges.size());
        else {
            Range range = match.getRange();
            // position is always inclusive; range.getFrom() is inclusive, range.getTo() is exclusive
            if ((left && (range.getFrom() != position)) || (!left && (range.getTo() - 1 != position)))
                return null;
            else
                return match;
        }
    }
}
