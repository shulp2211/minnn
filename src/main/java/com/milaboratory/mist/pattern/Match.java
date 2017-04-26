package com.milaboratory.mist.pattern;

import com.milaboratory.core.Range;
import com.milaboratory.core.sequence.NSequenceWithQuality;

import java.util.ArrayList;

public final class Match {
    private final int numberOfPatterns;
    private final float score;

    /**
     * This list contains both matched ranges and matched group edges.
     */
    private final ArrayList<MatchedItem> matchedItems;

    public Match(int numberOfPatterns, float score, ArrayList<MatchedItem> matchedItems) {
        this.numberOfPatterns = numberOfPatterns;
        this.score = score;
        this.matchedItems = matchedItems;
    }

    /**
     * Return MatchedRange by pattern index.
     *
     * @param patternIndex pattern index for multi-pattern matchers (number of patterns may be bigger than number of
     *                     targets in case of high level logic patterns); 0 - for single target matchers
     * @return MatchedRange for specified pattern
     */
    public MatchedRange getMatchedRange(int patternIndex) {
        for (MatchedItem item : matchedItems)
            if (MatchedRange.class.isAssignableFrom(item.getClass())
                    && (((MatchedRange)item).getPatternIndex() == patternIndex))
                return (MatchedRange)item;
        if (patternIndex >= numberOfPatterns)
            throw new IllegalStateException("Trying to get pattern index " + patternIndex + ", maximum allowed is "
                + (numberOfPatterns - 1));
        else
            throw new IllegalStateException("matchedItems doesn't contain item with index " + patternIndex
                + "; numberOfPatterns is " + numberOfPatterns);
    }

    /**
     * Return MatchedRange. Applicable only to single target matchers.
     *
     * @return MatchedRange with patternIndex 0
     */
    public MatchedRange getMatchedRange() {
        if (numberOfPatterns != 1)
            throw new IllegalStateException("Multiple pattern. Use getMatchedRange(int) instead.");
        return getMatchedRange(0);
    }

    public Range getRange() {
        return getMatchedRange().getRange();
    }

    public NSequenceWithQuality getValue() {
        return getMatchedRange().getValue();
    }

    /**
     * Return MatchedGroupEdge by name and isStart flag.
     *
     * @param groupName group name
     * @param isStart flag, true if it must be group start, false if must be group end
     * @return MatchedRange for specified pattern
     */
    public MatchedGroupEdge getMatchedGroupEdge(String groupName, boolean isStart) {
        for (MatchedItem item : matchedItems)
            if (MatchedGroupEdge.class.isAssignableFrom(item.getClass())
                    && (((MatchedGroupEdge)item).getGroupName().equals(groupName))
                    && (((MatchedGroupEdge)item).isStart() == isStart))
                return (MatchedGroupEdge)item;
        throw new IllegalStateException("Trying to get group " + (isStart ? "start" : "end") + " with name "
                + groupName + " and it doesn't exist");
    }

    /**
     * Get all matched items.
     *
     * @return ArrayList with all matched items.
     */
    public ArrayList<MatchedItem> getMatchedItems() {
        return matchedItems;
    }

    /**
     * Get all matched ranges.
     *
     * @return ArrayList with all matched ranges.
     */
    public ArrayList<MatchedRange> getMatchedRanges() {
        ArrayList<MatchedRange> matchedRanges = new ArrayList<>();
        for (MatchedItem item : matchedItems)
            if (MatchedRange.class.isAssignableFrom(item.getClass()))
                matchedRanges.add((MatchedRange)item);
        return matchedRanges;
    }

    /**
     * Get all matched group edges.
     *
     * @return ArrayList with all matched group edges.
     */
    public ArrayList<MatchedGroupEdge> getMatchedGroupEdges() {
        ArrayList<MatchedGroupEdge> matchedGroupEdges = new ArrayList<>();
        for (MatchedItem item : matchedItems)
            if (MatchedGroupEdge.class.isAssignableFrom(item.getClass()))
                matchedGroupEdges.add((MatchedGroupEdge)item);
        return matchedGroupEdges;
    }

    public int getNumberOfPatterns() {
        return numberOfPatterns;
    }

    public float getScore() {
        return score;
    }
}
