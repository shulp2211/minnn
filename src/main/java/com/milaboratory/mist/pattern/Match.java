package com.milaboratory.mist.pattern;

import java.util.Map;

public final class Match {
    public static final String WHOLE_PATTERN_MATCH_GROUP_NAME_PREFIX = "WM";
    public static final String COMMON_GROUP_NAME_PREFIX = "G";
    private final int numberOfPatterns;
    private final int score;
    private final CaptureGroupMatch[] wholePatternMatch;
    final Map<String, CaptureGroupMatch> groupMatches;

    Match(int numberOfPatterns, int score, Map<String, CaptureGroupMatch> groupMatches) {
        this.numberOfPatterns = numberOfPatterns;
        this.score = score;
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

    public int getNumberOfPatterns() {
        return numberOfPatterns;
    }

    public int getScore() {
        return score;
    }
}
