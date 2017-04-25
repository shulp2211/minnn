package com.milaboratory.mist.pattern;

import com.milaboratory.core.sequence.NSequenceWithQuality;

public class MatchedGroupEdge extends MatchedItem {
    private final GroupEdge groupEdge;
    private final int position;

    public MatchedGroupEdge(NSequenceWithQuality target, byte targetId, GroupEdge groupEdge, int position) {
        super(target, targetId);
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
