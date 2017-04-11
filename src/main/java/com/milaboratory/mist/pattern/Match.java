package com.milaboratory.mist.pattern;

import java.util.Map;

public final class Match {
    public static final String WHOLE_PATTERN_MATCH_GROUP_NAME_PREFIX = "WM_";
    public static final String COMMON_GROUP_NAME_PREFIX = "G_";
    private final int numberOfPatterns;
    private final float score;
    private final CaptureGroupMatch[] wholePatternMatch;
    final Map<String, CaptureGroupMatch> groupMatches;

    Match(int numberOfPatterns, float score, Map<String, CaptureGroupMatch> groupMatches) {
        this.numberOfPatterns = numberOfPatterns;
        this.score = score;
        this.wholePatternMatch = new CaptureGroupMatch[numberOfPatterns];
        for (int i = 0; i < numberOfPatterns; i++)
            this.wholePatternMatch[i] = groupMatches.get(WHOLE_PATTERN_MATCH_GROUP_NAME_PREFIX + i);
        this.groupMatches = groupMatches;
    }

    /**
     * Return capturing group for the whole i-th pattern.
     *
     * @param patternIndex pattern index for multi-pattern matchers (e.g. paired-end read matchers), 0 - for single
     *                     target matchers
     * @return capturing group for the whole i-th pattern
     */
    public CaptureGroupMatch getWholePatternMatch(int patternIndex) {
        return wholePatternMatch[patternIndex];
    }

    /**
     * Return capturing group for the whole pattern. Applicable only to single pattern matchers.
     *
     * @return capturing group for the whole pattern; single pattern matchers only
     */
    public CaptureGroupMatch getWholePatternMatch() {
        if (numberOfPatterns != 1)
            throw new IllegalStateException("Multiple pattern. Use getWholePatternMatch(int) instead.");
        return getWholePatternMatch(0);
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

    public float getScore() {
        return score;
    }
}
