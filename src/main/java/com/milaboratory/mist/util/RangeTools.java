package com.milaboratory.mist.util;

import com.milaboratory.core.Range;

import java.util.HashMap;

public class RangeTools {
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

    /**
     * Combine ranges and calculate score penalty for intersections.
     *
     * @param errorPenalty score penalty for each letter of intersection; in most cases it must be negative value
     * @param ranges ranges to combine
     * @return combined range and total score penalty
     */
    public static HashMap.SimpleEntry<Range, Float> combineRanges(float errorPenalty, Range... ranges) {
        if (ranges.length == 0)
            throw new IllegalArgumentException("Cannot combine 0 ranges.");

        float totalPenalty = 0;
        for (int i = 0; i < ranges.length; i++)
            for (int j = i + 1; j < ranges.length; j++)
                totalPenalty += getIntersectionLength(ranges[i], ranges[j]) * errorPenalty;

        return new HashMap.SimpleEntry<>(combineRanges(ranges), totalPenalty);
    }
}
