package com.milaboratory.mist.parser;

import com.milaboratory.mist.pattern.GroupEdgePosition;

import java.util.*;
import java.util.stream.Collectors;

import static com.milaboratory.mist.parser.BracketsDetector.getAllBrackets;
import static com.milaboratory.mist.parser.BracketsType.*;
import static com.milaboratory.mist.parser.ParserUtils.getObjectName;
import static com.milaboratory.mist.parser.SimplifiedParsers.parseGroupEdgePosition;
import static com.milaboratory.mist.parser.SimplifiedSyntaxStrings.*;

final class GroupsChecker {
    /**
     * This function checks groups in query and throws ParserException if they are not correct.
     *
     * @param query query string
     * @param format parser format: NORMAL for end users or SIMPLIFIED as toString() output in inner classes
     */
    static void checkGroups(String query, ParserFormat format) throws ParserException {
        switch (format) {
            case NORMAL:
                throw new IllegalStateException("Not yet implemented");
            case SIMPLIFIED:
                List<BracketsPair> parenthesesPairs = getAllBrackets(PARENTHESES, query);
                parenthesesPairs.sort(Comparator.comparingInt(bp -> bp.start));
                List<GroupEdgesPair> groupEdgesPairs = getGroupEdgesPairsSimplified(parenthesesPairs, query);
                checkGroupsPlacement(parenthesesPairs, groupEdgesPairs, query);
                break;
            default:
                throw new IllegalArgumentException("Unknown parser format: " + format);
        }
    }

    /**
     * Get list of group edges pairs from query in SIMPLIFIED parser format.
     *
     * @param parenthesesPairs list of parentheses pairs
     * @param query query in SIMPLIFIED parser format
     * @return list of group edges pairs
     */
    private static List<GroupEdgesPair> getGroupEdgesPairsSimplified(List<BracketsPair> parenthesesPairs, String query)
            throws ParserException {
        ArrayList<GroupEdgeToken> groupEdgeTokens = new ArrayList<>();
        ArrayList<GroupEdgesPair> groupEdgesPairs = new ArrayList<>();
        for (int i = 0; i < parenthesesPairs.size(); i++) {
            BracketsPair parenthesesPair = parenthesesPairs.get(i);
            if (getObjectName(parenthesesPair.start, query).equals(GROUP_EDGE_POSITION_NAME)) {
                String groupEdgePositionString = query.substring(parenthesesPair.start - GROUP_EDGE_POSITION_NAME.length(),
                        parenthesesPair.end + 1);
                GroupEdgePosition groupEdgePosition = parseGroupEdgePosition(groupEdgePositionString);
                groupEdgeTokens.add(new GroupEdgeToken(groupEdgePosition, parenthesesPair, i));
            }
        }
        for (int i = 1; i < groupEdgeTokens.size(); i++)
            for (int j = i - 1; j >= 0; j--) {
                GroupEdgeToken start = groupEdgeTokens.get(j);
                GroupEdgeToken end = groupEdgeTokens.get(i);
                if (end.isPair(start)) {
                    groupEdgesPairs.add(new GroupEdgesPair(start.getGroupName(), start.getParenthesisIndex(),
                            end.getParenthesisIndex()));
                    start.use();
                    end.use();
                }
            }

        List<String> notParsedTokens = groupEdgeTokens.stream().filter(GroupEdgeToken::notUsed)
                .map(t -> t.getString(query)).collect(Collectors.toList());
        if (notParsedTokens.size() > 0)
            throw new ParserException("Some group edges don't have pairs, or have start after end: " + notParsedTokens);

        return groupEdgesPairs;
    }

    /**
     * Check groups in query in SIMPLIFIED parser format and throw ParserException if they are not correct.
     *
     * @param parenthesesPairs list of parentheses pairs
     * @param groupEdgesPairs list of group edges pairs
     * @param query query in SIMPLIFIED parser format
     */
    private static void checkGroupsPlacement(List<BracketsPair> parenthesesPairs, List<GroupEdgesPair> groupEdgesPairs,
                                             String query) throws ParserException {
        for (int i = 0; i < groupEdgesPairs.size(); i++) {
            GroupEdgesPair currentGroup = groupEdgesPairs.get(i);
            ArrayList<Integer> currentGroupStartAncestors = getAncestors(parenthesesPairs,
                    currentGroup.groupStartParenthesisIndex);
            ArrayList<Integer> currentGroupEndAncestors = getAncestors(parenthesesPairs,
                    currentGroup.groupEndParenthesisIndex);
            ArrayList<Integer> currentGroupNotCommonAncestors = getTreeToCommonAncestor(currentGroupStartAncestors,
                    currentGroupEndAncestors);
            checkOuterObjects(currentGroup.groupName, positionsToNames(currentGroupStartAncestors, query),
                    positionsToNames(currentGroupEndAncestors, query), positionsToNames(currentGroupNotCommonAncestors, query));
            for (int j = 0; j < i; j++) {
                GroupEdgesPair otherGroup = groupEdgesPairs.get(j);
                if (currentGroup.sameName(otherGroup)) {
                    ArrayList<Integer> otherGroupStartAncestors = getAncestors(parenthesesPairs,
                            otherGroup.groupStartParenthesisIndex);
                    ArrayList<Integer> otherGroupEndAncestors = getAncestors(parenthesesPairs,
                            otherGroup.groupEndParenthesisIndex);
                    int closestCommonAncestorPosition = getClosestCommonAncestor(currentGroupStartAncestors,
                            currentGroupEndAncestors, otherGroupStartAncestors, otherGroupEndAncestors);
                    String closestCommonAncestor = getObjectName(closestCommonAncestorPosition, query);
                    if (!validDuplicateGroupsCommonAncestors.contains(closestCommonAncestor))
                        throw new ParserException("Group with the same name " + currentGroup.groupName
                                + " found, and closest common ancestor " + closestCommonAncestor
                                + " is not allowed; only allowed " + validDuplicateGroupsCommonAncestors);
                }
            }
        }
    }

    /**
     * Check that group is not inside objects where groups are not allowed, and that group start and end
     * don't have illegal not common or closest common ancestors.
     *
     * @param groupName group name, used only for error messages
     * @param groupStartOuterObjects list of object names of all objects that contain group start
     * @param groupEndOuterObjects list of object names of all objects that contain group end
     * @param notCommonOuterObjects list of object names of outer objects that are not common for group start and
     *                              group end, and name of closest common ancestor; if start and end are in the same
     *                              FuzzyMatchPattern, it will be the only element in this list
     */
    private static void checkOuterObjects(String groupName, ArrayList<String> groupStartOuterObjects,
            ArrayList<String> groupEndOuterObjects, ArrayList<String> notCommonOuterObjects) throws ParserException {
        List<String> invalidObjects = groupStartOuterObjects.stream()
                .filter(o -> !validGroupOuterObjectNames.contains(o)).collect(Collectors.toList());
        if (invalidObjects.size() > 0)
            throw new ParserException("Start of group " + groupName + " is inside not allowed object(s): "
                    + invalidObjects);
        invalidObjects = groupEndOuterObjects.stream()
                .filter(o -> !validGroupOuterObjectNames.contains(o)).collect(Collectors.toList());
        if (invalidObjects.size() > 0)
            throw new ParserException("End of group " + groupName + " is inside not allowed object(s): "
                    + invalidObjects);
        invalidObjects = notCommonOuterObjects.stream()
                .filter(o -> !validGroupPartNotCommonObjectNames.contains(o)).collect(Collectors.toList());
        if (invalidObjects.size() > 0)
            throw new ParserException("Start and end of group " + groupName
                    + " have illegal not common or closest common ancestor(s): " + invalidObjects);
    }

    /**
     * Transform list of parentheses positions into list of corresponding object names.
     *
     * @param positions list of positions of parentheses
     * @param query query in SIMPLIFIED parser format
     * @return list of names of objects that are before brackets from specified positions
     */
    private static ArrayList<String> positionsToNames(ArrayList<Integer> positions, String query) throws ParserException {
        ArrayList<String> names = new ArrayList<>();
        for (int position : positions)
            names.add(getObjectName(position, query));
        return names;
    }

    /**
     * Get positions for ancestor objects for coordinate in query string (for SIMPLIFIED parser format).
     * Positions are for open parentheses for ancestor objects. They are sorted from left to right.
     *
     * @param parenthesesPairs list of parentheses pairs; it must be initially sorted by open parentheses coordinates
     * @param index index of parentheses pair of this GroupEdgePosition object in parenthesesPairs list
     * @return list of open parentheses positions for ancestor objects
     */
    private static ArrayList<Integer> getAncestors(List<BracketsPair> parenthesesPairs, int index) throws ParserException {
        ArrayList<Integer> ancestors = new ArrayList<>();

        int currentNestedLevel = parenthesesPairs.get(index).nestedLevel;
        for (int i = index - 1; i >= 0; i--) {
            BracketsPair currentParentheses = parenthesesPairs.get(i);
            if (currentParentheses.nestedLevel < currentNestedLevel) {
                ancestors.add(currentParentheses.start);
                currentNestedLevel = currentParentheses.nestedLevel;
            }
        }
        if (ancestors.size() == 0)
            throw new ParserException("GroupEdgePosition objects must not be outside of patterns!");
        Collections.reverse(ancestors);
        return ancestors;
    }

    /**
     * Get list of outer objects that are not common for group start and group end; and closest common
     * object for group start and group end. If start and end are in the same FuzzyMatchPattern, it will be the only
     * element in this list. List elements are open parentheses coordinates for objects.
     *
     * @param startAncestorPositions positions of all ancestors for group start
     * @param endAncestorPositions positions of all ancestors for group end
     * @return positions of closest common ancestor and of all not common ancestors for group start and end
     */
    private static ArrayList<Integer> getTreeToCommonAncestor(ArrayList<Integer> startAncestorPositions,
            ArrayList<Integer> endAncestorPositions) throws ParserException {
        ArrayList<Integer> tree = new ArrayList<>();
        int treeStart = startAncestorPositions.size() - 1;
        for (int i = 0; i < startAncestorPositions.size(); i++)
            if ((i == endAncestorPositions.size()) || !startAncestorPositions.get(i).equals(endAncestorPositions.get(i))) {
                if (i == 0) throw new ParserException(
                        "Group start and end don't have common ancestors; probably, query contains multiple final patterns!");
                treeStart = i - 1;
                break;
            }
        tree.add(startAncestorPositions.get(treeStart));
        for (int i = treeStart + 1; i < startAncestorPositions.size() - 1; i++)
            tree.add(startAncestorPositions.get(i));
        for (int i = treeStart + 1; i < endAncestorPositions.size() - 1; i++)
            tree.add(endAncestorPositions.get(i));
        return tree;
    }

    /**
     * Get position of open parenthesis for closest common ancestor for 2 groups.
     *
     * @param group1StartAncestors positions of ancestors for start of group 1
     * @param group1EndAncestors positions of ancestors for end of group 1
     * @param group2StartAncestors positions of ancestors for start of group 2
     * @param group2EndAncestors positions of ancestors for end of group 2
     * @return position of open parenthesis for closest common ancestor for 2 groups
     */
    private static int getClosestCommonAncestor(ArrayList<Integer> group1StartAncestors,
            ArrayList<Integer> group1EndAncestors, ArrayList<Integer> group2StartAncestors,
            ArrayList<Integer> group2EndAncestors) throws ParserException {
        for (int i = 0; i < group1StartAncestors.size(); i++)
            if ((i == group1EndAncestors.size()) || (i == group2StartAncestors.size()) || (i == group2EndAncestors.size())
                    || !group1StartAncestors.get(i).equals(group1EndAncestors.get(i))
                    || !group1StartAncestors.get(i).equals(group2StartAncestors.get(i))
                    || !group1StartAncestors.get(i).equals(group2EndAncestors.get(i))) {
                if (i == 0) throw new ParserException(
                        "Group edges don't have common ancestors; probably, query contains multiple final patterns!");
                return group1StartAncestors.get(i - 1);
            }
        return group1StartAncestors.get(group1StartAncestors.size() - 1);
    }

    private static class GroupEdgeToken {
        private final GroupEdgePosition groupEdgePosition;
        private final BracketsPair parenthesesPair;
        private final int parenthesesPairIndex;
        private boolean notUsed = true;

        GroupEdgeToken(GroupEdgePosition groupEdgePosition, BracketsPair parenthesesPair, int parenthesesPairIndex) {
            this.groupEdgePosition = groupEdgePosition;
            this.parenthesesPair = parenthesesPair;
            this.parenthesesPairIndex = parenthesesPairIndex;
        }

        private int getTokenStart() {
            return parenthesesPair.start - GROUP_EDGE_POSITION_NAME.length();
        }

        private int getTokenEnd() {
            return parenthesesPair.end + 1;
        }

        String getGroupName() {
            return groupEdgePosition.getGroupEdge().getGroupName();
        }

        boolean isGroupStart() {
            return groupEdgePosition.getGroupEdge().isStart();
        }

        int getParenthesisIndex() {
            return parenthesesPairIndex;
        }

        String getString(String query) {
            return query.substring(getTokenStart(), getTokenEnd());
        }

        /**
         * Returns true only if this token is group end and other token is start of the same group.
         *
         * @param other other token
         * @return true only if this token is group end and other token is start of the same group
         */
        boolean isPair(GroupEdgeToken other) {
            return notUsed && other.notUsed() && !isGroupStart() && other.isGroupStart()
                    && getGroupName().equals(other.getGroupName());
        }

        void use() {
            notUsed = false;
        }

        boolean notUsed() {
            return notUsed;
        }
    }

    private static class GroupEdgesPair {
        final String groupName;
        final int groupStartParenthesisIndex;
        final int groupEndParenthesisIndex;

        GroupEdgesPair(String groupName, int groupStartParenthesisIndex, int groupEndParenthesisIndex) {
            this.groupName = groupName;
            this.groupStartParenthesisIndex = groupStartParenthesisIndex;
            this.groupEndParenthesisIndex = groupEndParenthesisIndex;
        }

        boolean sameName(GroupEdgesPair other) {
            return groupName.equals(other.groupName);
        }
    }
}
