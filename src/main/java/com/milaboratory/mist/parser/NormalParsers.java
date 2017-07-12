package com.milaboratory.mist.parser;

import com.milaboratory.mist.pattern.GroupEdge;
import com.milaboratory.mist.pattern.GroupEdgePosition;
import com.milaboratory.mist.pattern.PatternAligner;

import java.util.*;

/**
 * Parsers for objects and their parameters for normal syntax.
 */
final class NormalParsers {
    static ArrayList<FoundToken> parseRepeatPatterns(PatternAligner patternAligner, String query,
            List<BracketsPair> bracesPairs, List<NormalSyntaxGroupName> groupNames) throws ParserException {
        ArrayList<FoundToken> foundTokens = new ArrayList<>();
        for (BracketsPair bracesPair : bracesPairs) {
            if (bracesPair.start == 0)
                throw new ParserException("Found '{' without nucleotide in the start of query!");

            ArrayList<GroupEdgePosition> groupEdgePositions = new ArrayList<>();
            String leftGroupName = getGroupName(query, groupNames, bracesPair.start - 1, true);
            if (leftGroupName != null)
                groupEdgePositions.add(new GroupEdgePosition(new GroupEdge(leftGroupName, true), 0));
            String rightGroupName = getGroupName(query, groupNames, bracesPair.end, false);
            if (rightGroupName != null)
                groupEdgePositions.add(new GroupEdgePosition(new GroupEdge(rightGroupName, false),
                        Integer.MAX_VALUE));


        }

        return foundTokens;
    }

    /**
     * Return the name of this group that has left edge on the left of this pattern (if onLeft == true)
     * or has right edge on the right of this pattern (if onLeft == false), without any patterns between this pattern
     * and group edge, otherwise return null.
     *
     * @param query full query string
     * @param groupNames group names, sorted by group open parenthesis coordinate
     * @param position position where to start the search; exclusive
     * @param onLeft true to search group name on the left from this pattern, false to search group closing parenthesis
     *               on the right from this pattern
     * @return group name or null if there is no group edge
     */
    private static String getGroupName(String query, List<NormalSyntaxGroupName> groupNames,
                                       int position, boolean onLeft) throws ParserException {
        int closestGroupIndex = getClosestGroupByPosition(groupNames, position, onLeft);
        if (closestGroupIndex == -1)
            return null;
        String intermediateSubstring = onLeft ? query.substring(groupNames.get(closestGroupIndex).end + 1, position)
                : query.substring(position + 1, groupNames.get(closestGroupIndex).bracketsPair.end);
        if (intermediateSubstring.matches("[a-zA-Z()]"))
            return null;
        return groupNames.get(closestGroupIndex).name;
    }

    /**
     * Find closest index in group names list by position in query string; return -1 if index not found.
     *
     * @param groupNames list of group names, sorted by group open parenthesis coordinate
     * @param position position in query string, inclusive (if index is on this position, it will be returned)
     * @param toLeft true: search group name to the left from position,
     *               false: search group closing parenthesis to the right
     * @return found index in list of group names, or -1 if not found
     */
    private static int getClosestGroupByPosition(List<NormalSyntaxGroupName> groupNames, int position, boolean toLeft) {
        for (int i = 0; i < groupNames.size(); i++) {
            if (toLeft && (position >= groupNames.get(i).end)
                    && ((i == groupNames.size() - 1) || (position < groupNames.get(i + 1).start)))
                return i;
            if (!toLeft && (position <= groupNames.get(i).bracketsPair.end)
                    && ((i == 0) || (position > groupNames.get(i - 1).end))
                    && ((i != 0) || (groupNames.size() > 1)))
                return i;
        }
        return -1;
    }
}
