package com.milaboratory.mist.pattern;

import com.milaboratory.core.Range;
import com.milaboratory.core.sequence.NSequenceWithQuality;

public final class MatchedGroup extends MatchedRange {
    private final String groupName;

    public MatchedGroup(String groupName, NSequenceWithQuality target, byte targetId, int patternIndex, Range range) {
        super(target, targetId, patternIndex, range);
        this.groupName = groupName;
    }

    public String getGroupName() {
        return groupName;
    }
}
