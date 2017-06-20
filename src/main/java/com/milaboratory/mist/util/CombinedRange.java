package com.milaboratory.mist.util;

import com.milaboratory.core.Range;
import com.milaboratory.mist.pattern.MatchedGroupEdge;

import java.util.ArrayList;

class CombinedRange {
    private final ArrayList<MatchedGroupEdge> matchedGroupEdges;
    private final Range range;
    private final long scorePenalty;

    /**
     * CombinedRange used to store result when combining multiple ranges and calculating score penalty
     * for intersections.
     *
     * @param range resulting range
     * @param scorePenalty score penalty for intersections (zero or negative)
     */
    CombinedRange(ArrayList<MatchedGroupEdge> matchedGroupEdges, Range range, long scorePenalty) {
        this.matchedGroupEdges = matchedGroupEdges;
        this.range = range;
        this.scorePenalty = scorePenalty;
    }

    ArrayList<MatchedGroupEdge> getMatchedGroupEdges() {
        return matchedGroupEdges;
    }

    Range getRange() {
        return range;
    }

    long getScorePenalty() {
        return scorePenalty;
    }
}
