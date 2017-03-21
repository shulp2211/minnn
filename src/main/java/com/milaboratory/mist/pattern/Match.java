package com.milaboratory.mist.pattern;

import java.util.Map;

public final class Match {
    public static final String WHOLE_PATTERN_MATCH_GROUP_NAME_PREFIX = "WM";
    final int numberOfPatterns;
    final CaptureGroupMatch[] wholePatternMatch;
    final Map<String, CaptureGroupMatch> groupMatches;

    Match(int numberOfPatterns, Map<String, CaptureGroupMatch> groupMatches) {
        this.numberOfPatterns = numberOfPatterns;
        this.wholePatternMatch = new CaptureGroupMatch[numberOfPatterns];
        for (int i = 0; i < numberOfPatterns; i++)
            this.wholePatternMatch[i] = groupMatches.get(WHOLE_PATTERN_MATCH_GROUP_NAME_PREFIX + i);
        this.groupMatches = groupMatches;
    }

    public CaptureGroupMatch getWholePatternMatch(int id) {
        return wholePatternMatch[id];
    }

    public boolean isFound() {
        for (CaptureGroupMatch captureGroupMatch : wholePatternMatch)
            if (!captureGroupMatch.isFound())
                return false;
        return true;
    }
}
