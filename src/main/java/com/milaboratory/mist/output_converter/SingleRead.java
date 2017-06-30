package com.milaboratory.mist.output_converter;

import com.milaboratory.core.sequence.NSequenceWithQuality;
import com.milaboratory.mist.pattern.MatchedRange;

public class SingleRead {
    private final NSequenceWithQuality target;
    private final byte targetId;
    private final NSequenceWithQuality matchedSequence;

    public SingleRead(MatchedRange matchedRange) {
        target = matchedRange.getTarget();
        targetId = matchedRange.getTargetId();
        matchedSequence = matchedRange.getValue();
    }

    public NSequenceWithQuality getTarget() {
        return target;
    }

    public byte getTargetId() {
        return targetId;
    }

    public NSequenceWithQuality getMatchedSequence() {
        return matchedSequence;
    }
}
