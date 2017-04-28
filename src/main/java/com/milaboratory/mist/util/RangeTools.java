package com.milaboratory.mist.util;

import com.milaboratory.core.Range;

public class RangeTools {
    /**
     * Check if there are any intersections between ranges.
     *
     * @param ranges ranges
     * @return false if all ranges don't intersect, true if any 2 intersects
     */
    public static boolean checkRangesIntersection(Range... ranges) {
        for (int i = 0; i < ranges.length; i++) {
            for (int j = i + 1; j < ranges.length; j++) {
                if (ranges[i].intersectsWith(ranges[j]))
                    return true;
            }
        }
        return false;
    }

    // For performance, to avoid allocation of new array
    public static Range combine2Ranges(Range range0, Range range1) {
        return new Range(Math.min(range0.getLower(), range1.getLower()),
                Math.max(range0.getUpper(), range1.getUpper()));
    }

    public static Range combineRanges(Range... ranges) {
        if (ranges.length == 0)
            throw new IllegalArgumentException("Cannot combine 0 ranges.");

        int lower = ranges[0].getLower();
        int upper = ranges[0].getUpper();

        for (int i = 1; i < ranges.length; i++) {
            Range range = ranges[i];
            lower = Math.min(lower, range.getLower());
            upper = Math.max(upper, range.getUpper());
        }

        return new Range(lower, upper, false);
    }
}
