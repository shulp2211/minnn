package com.milaboratory.mist.pattern;

import com.milaboratory.core.Range;
import com.milaboratory.core.sequence.NSequenceWithQuality;

public class NullMatchedRange extends MatchedRange {
    public NullMatchedRange(int patternIndex) {
        super(null, (byte)0, patternIndex, null);
    }

    @Override
    public NSequenceWithQuality getTarget() {
        throw new IllegalStateException("Called getTarget() for NullMatchedRange object.");
    }

    @Override
    public byte getTargetId() {
        throw new IllegalStateException("Called getTargetId() for NullMatchedRange object.");
    }

    @Override
    public Range getRange() {
        throw new IllegalStateException("Called getRange() for NullMatchedRange object.");
    }
}
