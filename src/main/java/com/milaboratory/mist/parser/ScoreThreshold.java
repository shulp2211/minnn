package com.milaboratory.mist.parser;

final class ScoreThreshold {
    private final int start;
    private final int end;
    final int threshold;
    final int nestedLevel;

    /**
     * Score threshold; used to detect that patterns inside it must use more strict score threshold than final pattern.
     *
     * @param threshold score threshold value
     * @param start start position in TokenizedString, inclusive
     * @param end end position in TokenizedString, exclusive
     * @param nestedLevel number of score thresholds outside of this threshold;
     *                    threshold with higher nestedLevel has higher priority
     */
    ScoreThreshold(int threshold, int start, int end, int nestedLevel) {
        this.threshold = threshold;
        this.start = start;
        this.end = end;
        this.nestedLevel = nestedLevel;
    }

    /**
     * Returns true if the specified range is inside this score threshold.
     *
     * @param start range start, inclusive
     * @param end range end, exclusive
     * @return true if the specified range is inside this score threshold
     */
    boolean contains(int start, int end) {
        if ((start > this.start) && (end < this.end))
            return true;
        else if (((start < this.start) && (end < this.start)) || ((start > this.end) && (end > this.end))
                || ((start < this.start) && (end >= this.end)))
            return false;
        else throw new IllegalStateException("Intersection of specified range and this ScoreThreshold: start="
                + start + ", this.start=" + this.start + ", end=" + end + ", this.end=" + this.end);
    }
}
