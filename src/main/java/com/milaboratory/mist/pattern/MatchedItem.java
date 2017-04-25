package com.milaboratory.mist.pattern;

import com.milaboratory.core.sequence.NSequenceWithQuality;

/**
 * Parent class for MatchedRange and MatchedGroupEdge.
 */
public abstract class MatchedItem {
    protected final NSequenceWithQuality target;
    /**
     * actualTargetId + 1 if matched on forward strand
     * -actualTargetId - 1 if matched on reverse strand
     * 0 used only in NullMatchedRange (when there is actually no match)
     */
    protected final byte targetId;

    public MatchedItem(NSequenceWithQuality target, byte targetId) {
        this.target = target;
        this.targetId = targetId;
    }

    public NSequenceWithQuality getTarget() {
        return target;
    }

    public byte getTargetId() {
        return targetId;
    }
}
