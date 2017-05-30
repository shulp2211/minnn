package com.milaboratory.mist.pattern;

final class GroupEdgePosition {
    private final GroupEdge groupEdge;
    private final int position;

    GroupEdgePosition(GroupEdge groupEdge, int position) {
        this.groupEdge = groupEdge;
        this.position = position;
    }

    @Override
    public String toString() {
        return "GroupEdgePosition(" + groupEdge + ", " + position + ")";
    }

    GroupEdge getGroupEdge() {
        return groupEdge;
    }

    int getPosition() {
        return position;
    }
}
