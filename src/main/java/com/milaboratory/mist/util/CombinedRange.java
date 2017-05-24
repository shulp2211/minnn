package com.milaboratory.mist.util;

import com.milaboratory.core.Range;

class CombinedRange {
    private final Range range;
    private final float scorePenalty;

    /**
     * CombinedRange used to store result when combining multiple ranges and calculating score penalty
     * for intersections.
     *
     * @param range resulting range
     * @param scorePenalty score penalty for intersections (zero or negative)
     */
    CombinedRange(Range range, float scorePenalty) {
        this.range = range;
        this.scorePenalty = scorePenalty;
    }

    Range getRange() {
        return range;
    }

    float getScorePenalty() {
        return scorePenalty;
    }
}
