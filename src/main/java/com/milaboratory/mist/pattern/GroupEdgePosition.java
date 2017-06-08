package com.milaboratory.mist.pattern;

public final class GroupEdgePosition {
    private final GroupEdge groupEdge;
    private final int position;

    public GroupEdgePosition(GroupEdge groupEdge, int position) {
        this.groupEdge = groupEdge;
        this.position = position;
    }

    @Override
    public String toString() {
        return "GroupEdgePosition(" + groupEdge + ", " + position + ")";
    }

    public GroupEdge getGroupEdge() {
        return groupEdge;
    }

    public int getPosition() {
        return position;
    }
}
