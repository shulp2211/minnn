package com.milaboratory.mist.pattern;

import com.milaboratory.core.Range;
import com.milaboratory.core.sequence.NSequenceWithQuality;

public class MatchedRange extends MatchedItem {
    private final int patternIndex;
    private final Range range;

    public MatchedRange(NSequenceWithQuality target, byte targetId, int patternIndex, Range range) {
        super(target, targetId);
        this.patternIndex = patternIndex;
        this.range = range;
    }

    public int getPatternIndex() {
        return patternIndex;
    }

    public Range getRange() {
        return range;
    }
}
