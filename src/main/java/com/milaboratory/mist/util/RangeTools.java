package com.milaboratory.mist.util;

import com.milaboratory.core.Range;
import com.milaboratory.mist.pattern.Match;

public final class RangeTools {
    /**
     * Check if there are any intersections between ranges.
     *
     * @param ranges ranges
     * @return false if all ranges don't intersect, true if any 2 intersects
     */
    public static boolean checkRangesIntersection(Range... ranges) {
        for (int i = 0; i < ranges.length; i++)
            for (int j = i + 1; j < ranges.length; j++)
                if (ranges[i].intersectsWith(ranges[j]))
                    return true;
        return false;
    }

    /**
     * Check if there are any intersections between ranges; and intersections not counted while sum of lengths of intersected
     * parts is lower than maxErrors.
     *
     * @param maxErrors maximum allowed number of intersected positions
     * @param ranges ranges
     * @return true if ranges intersect more than on maxErrors, otherwise false
     */
    public static boolean checkRangesIntersection(int maxErrors, Range... ranges) {
        int sumIntersection = 0;

        for (int i = 0; i < ranges.length; i++)
            for (int j = i + 1; j < ranges.length; j++) {
                sumIntersection += getIntersectionLength(ranges[i], ranges[j]);
                if (sumIntersection > maxErrors)
                    return true;
            }
        return false;
    }

    /**
     * Calculate the length of intersection for 2 ranges.
     *
     * @param range0 first range
     * @param range1 second range
     * @return number of intersected positions in these ranges.
     */
    public static int getIntersectionLength(Range range0, Range range1) {
        Range intersection = range0.intersection(range1);
        if (intersection == null) return 0;
        return intersection.length();
    }

    /**
     * Returns true if one of 2 range contains another; otherwise false.
     */
    public static boolean checkFullIntersection(Range range0, Range range1) {
        return range0.contains(range1) || range1.contains(range0);
    }

    // For performance, to avoid allocation of new array
    public static Range combine2Ranges(Range range0, Range range1) {
        return new Range(Math.min(range0.getLower(), range1.getLower()),
                Math.max(range0.getUpper(), range1.getUpper()));
    }

    public static Range combineRanges(Range... ranges) {
        if (ranges.length == 0)
            throw new IllegalArgumentException("Cannot combine 0 ranges.");

        int lower = Integer.MAX_VALUE;
        int upper = Integer.MIN_VALUE;

        for (Range range : ranges) {
            lower = Math.min(lower, range.getLower());
            upper = Math.max(upper, range.getUpper());
        }

        return new Range(lower, upper, false);
    }

    public static Range combineRanges(Match... matches) {
        if (matches.length == 0)
            throw new IllegalArgumentException("Cannot combine ranges from 0 matches.");

        int lower = Integer.MAX_VALUE;
        int upper = Integer.MIN_VALUE;

        Range range;
        for (Match match : matches) {
            range = match.getRange();
            lower = Math.min(lower, range.getLower());
            upper = Math.max(upper, range.getUpper());
        }

        return new Range(lower, upper, false);
    }
}
