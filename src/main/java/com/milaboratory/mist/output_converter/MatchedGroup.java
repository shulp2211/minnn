package com.milaboratory.mist.output_converter;

import com.milaboratory.core.Range;
import com.milaboratory.core.sequence.NSequenceWithQuality;
import com.milaboratory.mist.pattern.MatchedRange;

public final class MatchedGroup extends MatchedRange {
    private final String groupName;
    private final Range relativeRange;

    public MatchedGroup(String groupName, NSequenceWithQuality target, byte targetId, int patternIndex, Range range) {
        this(groupName, target, targetId, patternIndex, range, new Range(-1, -1));
    }

    public MatchedGroup(String groupName, NSequenceWithQuality target, byte targetId, int patternIndex, Range range,
                        Range relativeRange) {
        super(target, targetId, patternIndex, range);
        this.groupName = groupName;
        this.relativeRange = relativeRange;
    }

    public String getGroupName() {
        return groupName;
    }

    public Range getRelativeRange() {
        return relativeRange;
    }
}
