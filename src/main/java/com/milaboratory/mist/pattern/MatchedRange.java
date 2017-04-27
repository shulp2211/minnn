package com.milaboratory.mist.pattern;

import com.milaboratory.core.Range;
import com.milaboratory.core.sequence.NSequenceWithQuality;

public class MatchedRange extends MatchedItem {
    private final Range range;

    public MatchedRange(NSequenceWithQuality target, byte targetId, int patternIndex, Range range) {
        super(target, targetId, patternIndex);
        this.range = range;
    }

    public Range getRange() {
        return range;
    }

    public NSequenceWithQuality getValue() {
        return target.getRange(range);
    }
}
