package com.milaboratory.mist.pattern;

import com.milaboratory.core.Range;
import com.milaboratory.core.sequence.NSequenceWithQuality;

import java.util.HashMap;
import java.util.Map;

import static com.milaboratory.mist.pattern.Match.WHOLE_PATTERN_MATCH_GROUP_NAME_PREFIX;

public abstract class MultiplePatternsOperator implements SinglePattern {
    protected final Pattern[] operandPatterns;

    MultiplePatternsOperator(Pattern... operandPatterns) {
        this.operandPatterns = operandPatterns;
    }

    /**
     * Check if there are any intersections between ranges
     * @param ranges ranges
     * @return false if all ranges don't intersect, true if any 2 intersects
     */
    static boolean checkRangesIntersection(Range... ranges) {
        for (int i = 0; i < ranges.length; i++) {
            for (int j = i + 1; j < ranges.length; j++) {
                if (ranges[i].intersectsWith(ranges[j]))
                    return true;
            }
        }
        return false;
    }

    static Range combineRanges(Range... ranges) {
        if (ranges.length == 0)
            throw new IllegalStateException("Cannot combine 0 ranges.");
        int lower = ranges[0].getLower();
        int upper = ranges[0].getUpper();

        for (Range range: ranges) {
            if (range == ranges[0]) continue;
            lower = Math.min(lower, range.getLower());
            upper = Math.max(upper, range.getUpper());
        }

        return new Range(lower, upper, false);
    }

    static Match combineMatches(NSequenceWithQuality target, byte targetId, Match... matches) {
        Map<String, CaptureGroupMatch> groupMatches = new HashMap<>();
        Range[] ranges = new Range[matches.length];

        for (int i = 0; i < matches.length; i++) {
            groupMatches.putAll(matches[i].groupMatches);
            ranges[i] = matches[i].getWholePatternMatch(0).getRange();
        }

        CaptureGroupMatch wholePatternMatch = new CaptureGroupMatch(target, targetId, combineRanges(ranges));
        groupMatches.put(WHOLE_PATTERN_MATCH_GROUP_NAME_PREFIX + 0, wholePatternMatch);
        return new Match(1, sumMatchesScore(matches), groupMatches);
}

    static int sumMatchesScore(Match... matches) {
        int score = 0;
        for (Match match: matches) {
            score += match.getScore();
        }
        return score;
    }
}
