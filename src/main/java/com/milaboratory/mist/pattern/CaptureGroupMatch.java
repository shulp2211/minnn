package com.milaboratory.mist.pattern;

import com.milaboratory.core.Range;
import com.milaboratory.core.sequence.NSequenceWithQuality;

public final class CaptureGroupMatch {
    final NSequenceWithQuality target;
    /**
     * actualTargetId + 1 if matched on forward strand
     * -actualTargetId - 1 if matched on reverse strand
     */
    final byte targetId;
    final Range range;

    public CaptureGroupMatch(NSequenceWithQuality target, byte targetId, Range range) {
        this.target = target;
        this.targetId = targetId;
        this.range = range;
    }

    public boolean isFound() {
        return range != null;
    }

    public Range getRange() {
        return range;
    }

    public NSequenceWithQuality getValue() {
        if (!isFound())
            throw new IllegalStateException("Pattern not found.");
        return target.getRange(range);
    }
}
