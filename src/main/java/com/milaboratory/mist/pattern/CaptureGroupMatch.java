package com.milaboratory.mist.pattern;

import com.milaboratory.core.Range;
import com.milaboratory.core.sequence.NSequenceWithQuality;

public final class CaptureGroupMatch {
    private final NSequenceWithQuality target;
    /**
     * actualTargetId + 1 if matched on forward strand
     * -actualTargetId - 1 if matched on reverse strand
     * 0 if complex pattern uses multiple reads to match
     */
    private final byte targetId;
    private final Range range;

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

    public NSequenceWithQuality getTarget() {
        return target;
    }

    public byte getTargetId() {
        return targetId;
    }
}
