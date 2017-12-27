package com.milaboratory.mist.pattern;

import com.milaboratory.mist.io.IO;
import com.milaboratory.primitivio.PrimitivI;
import com.milaboratory.primitivio.PrimitivO;
import com.milaboratory.primitivio.annotations.Serializable;

@Serializable(by = IO.GroupEdgeSerializer.class)
public final class GroupEdge {
    private final String groupName;
    /**
     * true if this is group start, false if this is group end
     */
    private final boolean isStart;

    public GroupEdge(String groupName, boolean isStart) {
        this.groupName = groupName;
        this.isStart = isStart;
    }

    public String getGroupName() {
        return groupName;
    }

    public boolean isStart() {
        return isStart;
    }

    @Override
    public String toString() {
        return "GroupEdge('" + groupName + "', " + isStart + ")";
    }

    @Override
    public boolean equals(Object other) {
        if (!(other instanceof GroupEdge))
            return false;

        GroupEdge that = (GroupEdge) other;

        return (this.groupName.equals(that.getGroupName())) && (this.isStart == that.isStart());
    }

    @Override
    public int hashCode() {
        int hashCode = 1;

        hashCode = hashCode * 37 + this.groupName.hashCode();
        hashCode = hashCode * 37 + (this.isStart ? 1 : 0);

        return hashCode;
    }

    public static GroupEdge read(PrimitivI input) {
        String groupName = input.readUTF();
        boolean isStart = input.readBoolean();
        return new GroupEdge(groupName, isStart);
    }

    public static void write(PrimitivO output, GroupEdge object) {
        output.writeUTF(object.getGroupName());
        output.writeBoolean(object.isStart());
    }
}
