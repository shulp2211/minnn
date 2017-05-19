package com.milaboratory.mist.pattern;

import com.milaboratory.core.Range;

import java.util.ArrayList;

public final class GroupUtils {
    public static ArrayList<MatchedGroup> getGroupsFromMatch(Match match) {
        ArrayList<MatchedGroup> matchedGroups = new ArrayList<>();
        ArrayList<MatchedGroupEdge> matchedGroupEdges = match.getMatchedGroupEdges();
        MatchedGroupEdge endOfCurrentGroup;
        Range currentRange;

        for (MatchedGroupEdge matchedGroupEdge : matchedGroupEdges)
            if (matchedGroupEdge.isStart()) {
                endOfCurrentGroup = match.getMatchedGroupEdge(matchedGroupEdge.getGroupName(), false);
                if (matchedGroupEdge.getPosition() >= endOfCurrentGroup.getPosition())
                    throw new IllegalStateException("Group start must be lower than the end. Start: "
                        + matchedGroupEdge.getPosition() + ", end: " + endOfCurrentGroup.getPosition());
                if (matchedGroupEdge.getPatternIndex() != endOfCurrentGroup.getPatternIndex())
                    throw new IllegalStateException("Start and end of the group " + matchedGroupEdge.getGroupName()
                        + " have different pattern indexes (start: " + matchedGroupEdge.getPatternIndex() + ", end: "
                        + endOfCurrentGroup.getPatternIndex() + ")!");
                currentRange = new Range(matchedGroupEdge.getPosition(), endOfCurrentGroup.getPosition());
                matchedGroups.add(new MatchedGroup(matchedGroupEdge.getGroupName(), matchedGroupEdge.getTarget(),
                        matchedGroupEdge.getTargetId(), matchedGroupEdge.getPatternIndex(), currentRange));
            }

        return matchedGroups;
    }
}
