package com.milaboratory.mist.outputconverter;

import com.milaboratory.core.Range;
import com.milaboratory.mist.pattern.*;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.stream.Collectors;

public final class GroupUtils {
    /**
     * Return only groups that are fully inside main group, or groups that are not fully inside main group, depending
     * on insideMain flag. For groups that are inside main, relative ranges will be calculated.
     *
     * @param groups groups list to filter and calculate relative ranges
     * @param mainRange range of main group
     * @param insideMain true if we need to get only groups that are inside main and calculate relative ranges,
     *                   false if we need to get only groups that are not inside main
     * @return filtered list of groups
     */
    public static ArrayList<MatchedGroup> getGroupsInsideMain(ArrayList<MatchedGroup> groups, Range mainRange,
                                                              boolean insideMain) {
        if (insideMain)
            return groups.stream()
                    .filter(g -> mainRange.contains(g.getRange()))
                    .map(g -> new MatchedGroup(g.getGroupName(), g.getTarget(), g.getTargetId(),
                            g.getPatternIndex(), g.getRange(), g.getRange().move(-mainRange.getLower())))
                    .collect(Collectors.toCollection(ArrayList::new));
        else
            return groups.stream()
                    .filter(g -> !mainRange.contains(g.getRange()))
                    .collect(Collectors.toCollection(ArrayList::new));
    }

    public static String groupsToReadDescription(ArrayList<MatchedGroup> groups, String mainGroupName,
                                                 boolean insideMain) {
        StringBuilder descriptionBuilder = new StringBuilder();
        for (int i = 0; i < groups.size(); i++) {
            MatchedGroup currentGroup = groups.get(i);
            descriptionBuilder.append(currentGroup.getGroupName());
            descriptionBuilder.append('~');
            descriptionBuilder.append(currentGroup.getValue().getSequence().toString());
            descriptionBuilder.append('~');
            descriptionBuilder.append(currentGroup.getValue().getQuality().toString().replaceAll("[{}~|]", "z"));
            if (insideMain) {
                descriptionBuilder.append('{');
                descriptionBuilder.append(mainGroupName);
                descriptionBuilder.append('~');
                descriptionBuilder.append(Integer.toString(currentGroup.getRelativeRange().getLower()));
                descriptionBuilder.append('~');
                descriptionBuilder.append(Integer.toString(currentGroup.getRelativeRange().getUpper()));
                descriptionBuilder.append('}');
            }
            if (i < groups.size() - 1)
                descriptionBuilder.append('|');
        }
        return descriptionBuilder.toString();
    }

    public static String descriptionForNotMatchedGroups(Pattern topLevelPattern, int patternIndex,
                                                        ArrayList<MatchedGroup> matchedGroups) {
        HashSet<String> matchedGroupNames = matchedGroups.stream().map(MatchedGroup::getGroupName)
                .collect(Collectors.toCollection(HashSet::new));
        HashSet<String> notMatchedGroupNames = topLevelPattern.getGroupEdges(patternIndex).stream()
                .map(GroupEdge::getGroupName).filter(gn -> !matchedGroupNames.contains(gn))
                .collect(Collectors.toCollection(HashSet::new));
        StringBuilder descriptionBuilder = new StringBuilder();
        boolean firstGroupName = true;
        for (String notMatchedGroupName : notMatchedGroupNames) {
            if (firstGroupName)
                firstGroupName = false;
            else
                descriptionBuilder.append("|");
            descriptionBuilder.append(notMatchedGroupName);
            descriptionBuilder.append("~");
        }
        return descriptionBuilder.toString();
    }
}
