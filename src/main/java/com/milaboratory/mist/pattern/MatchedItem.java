package com.milaboratory.mist.pattern;

import com.milaboratory.core.sequence.NSequenceWithQuality;

/**
 * Parent class for MatchedRange and MatchedGroupEdge.
 */
public abstract class MatchedItem {
    protected final NSequenceWithQuality target;
    /**
     * IDs start from 1.
     * 0 used only in NullMatchedRange (when there is actually no match).
     */
    protected final byte targetId;
    protected final int patternIndex;

    public MatchedItem(NSequenceWithQuality target, byte targetId, int patternIndex) {
        this.target = target;
        this.targetId = targetId;
        this.patternIndex = patternIndex;
    }

    public NSequenceWithQuality getTarget() {
        return target;
    }

    public byte getTargetId() {
        return targetId;
    }

    public int getPatternIndex() {
        return patternIndex;
    }
}
