package com.milaboratory.mist.pattern;

import com.milaboratory.core.sequence.NSequenceWithQuality;

public final class MatchedGroupEdge extends MatchedItem {
    private final GroupEdge groupEdge;
    private final int position;

    public MatchedGroupEdge(NSequenceWithQuality target, byte targetId, int patternIndex, GroupEdge groupEdge, int position) {
        super(target, targetId, patternIndex);
        this.groupEdge = groupEdge;
        this.position = position;
    }

    public GroupEdge getGroupEdge() {
        return groupEdge;
    }

    public String getGroupName() {
        return groupEdge.getGroupName();
    }

    public boolean isStart() {
        return groupEdge.isStart();
    }

    public int getPosition() {
        return position;
    }
}
