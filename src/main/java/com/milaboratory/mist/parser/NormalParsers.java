package com.milaboratory.mist.parser;

import com.milaboratory.core.sequence.NucleotideSequence;
import com.milaboratory.mist.pattern.GroupEdge;
import com.milaboratory.mist.pattern.GroupEdgePosition;
import com.milaboratory.mist.pattern.PatternAligner;
import com.milaboratory.mist.pattern.RepeatPattern;

import java.util.*;

import static com.milaboratory.mist.parser.ParserUtils.*;

/**
 * Parsers for objects and their parameters for normal syntax.
 */
final class NormalParsers {
    static ArrayList<FoundToken> parseRepeatPatterns(PatternAligner patternAligner, String query,
            List<BracketsPair> bracesPairs, ArrayList<Integer> startStickMarkers, ArrayList<Integer> endStickMarkers,
            List<NormalSyntaxGroupName> groupNames) throws ParserException {
        // must be changed to Integer.MAX_VALUE when bitap wrapper for longer sequences will be implemented
        final int MAX_REPEATS = 63;

        ArrayList<FoundToken> foundTokens = new ArrayList<>();
        for (BracketsPair bracesPair : bracesPairs) {
            if (bracesPair.start == 0)
                throw new ParserException("Found '{' without nucleotide in the start of query!");
            String arguments = query.substring(bracesPair.start + 1, bracesPair.end);
            NucleotideSequence patternSeq = toNSeq(query.substring(bracesPair.start - 1, bracesPair.start));
            int minRepeats = 1;
            int maxRepeats = MAX_REPEATS;
            int fixedLeftBorder = startStick(query, groupNames, startStickMarkers, bracesPair.start - 1) ? 0 : -1;
            int fixedRightBorder = endStick(query, groupNames, endStickMarkers, bracesPair.end) ? -2 : -1;
            if (arguments.length() == 0)
                throw new ParserException("Missing number of repeats in " + query.substring(bracesPair.start - 1,
                        bracesPair.end + 1));
            else if (arguments.equals("*"))
                minRepeats = MAX_REPEATS;
            else if (!arguments.contains(":"))
                minRepeats = toInt(arguments, "number of repeats");
            else {
                if (arguments.indexOf(":") != 0)
                    minRepeats = toInt(arguments.substring(0, arguments.indexOf(":")),
                            "minimum number of repeats");
                if (arguments.indexOf(":") != arguments.length() - 1)
                    maxRepeats = toInt(arguments.substring(arguments.indexOf(":") + 1),
                            "maximum number of repeats");
            }
            ArrayList<GroupEdgePosition> groupEdgePositions = new ArrayList<>();
            String leftGroupName = getGroupName(query, groupNames, bracesPair.start - 1, true);
            if (leftGroupName != null)
                groupEdgePositions.add(new GroupEdgePosition(new GroupEdge(leftGroupName, true), 0));
            String rightGroupName = getGroupName(query, groupNames, bracesPair.end, false);
            if (rightGroupName != null)
                groupEdgePositions.add(new GroupEdgePosition(new GroupEdge(rightGroupName, false), maxRepeats));

            foundTokens.add(new FoundToken(new RepeatPattern(patternAligner, patternSeq, minRepeats, maxRepeats,
                    fixedLeftBorder, fixedRightBorder, groupEdgePositions),
                    bracesPair.start - 1, bracesPair.end + 1));
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
        if (intermediateSubstring.matches(".*[a-zA-Z()].*"))
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
                    && ((i != 0) || (groupNames.size() == 1)))
                return i;
        }
        return -1;
    }

    /**
     * Check is this pattern should stick to start (check "$" before the pattern).
     *
     * @param query full query string
     * @param groupNames group names, sorted by group open parenthesis coordinate
     * @param startStickMarkers positions of non-quoted "$" characters in query
     * @param position pattern start position, inclusive
     * @return true if this pattern should stick to target start
     */
    private static boolean startStick(String query, List<NormalSyntaxGroupName> groupNames,
                                      ArrayList<Integer> startStickMarkers, int position) {
        int markerPosition = -1;
        for (int i = 0; i < startStickMarkers.size(); i++) {
            if ((position > startStickMarkers.get(i)) && ((i == startStickMarkers.size() - 1)
                    || (position < startStickMarkers.get(i + 1))))
                markerPosition = startStickMarkers.get(i);
        }

        return (markerPosition != -1) && !query.substring(markerPosition + 1, position).matches(".*\\\\.*")
                && !isAnyNucleotide(query, groupNames, markerPosition + 1, position);
    }

    /**
     * Check is this pattern should stick to end (check "^" after the pattern).
     *
     * @param query full query string
     * @param groupNames group names, sorted by group open parenthesis coordinate
     * @param endStickMarkers positions of non-quoted "^" characters in query
     * @param position pattern end position, inclusive
     * @return true if this pattern should stick to target end
     */
    private static boolean endStick(String query, List<NormalSyntaxGroupName> groupNames,
                                    ArrayList<Integer> endStickMarkers, int position) {
        int markerPosition = -1;
        for (int i = 0; i < endStickMarkers.size(); i++) {
            if ((position < endStickMarkers.get(i)) && ((i == 0) || (position > endStickMarkers.get(i - 1)))
                    && ((i != 0) || (endStickMarkers.size() == 1)))
                markerPosition = endStickMarkers.get(i);
        }

        return (markerPosition != -1) && !query.substring(position + 1, markerPosition).matches(".*\\\\.*")
                && !isAnyNucleotide(query, groupNames, position + 1, markerPosition);
    }

    /**
     * Returns true if there is any nucleotide in query substring, otherwise false.
     *
     * @param query full query string
     * @param groupNames group names, sorted by group open parenthesis coordinate
     * @param start substring start position, inclusive
     * @param end substring end position, exclusive
     * @return true if there is any nucleotide in query substring, otherwise false
     */
    private static boolean isAnyNucleotide(String query, List<NormalSyntaxGroupName> groupNames, int start, int end) {
        for (int currentPosition = start; currentPosition < end; currentPosition++) {
            final int position = currentPosition;
            if (query.substring(position, position + 1).matches("[a-zA-Z]")
                    && groupNames.stream().noneMatch(gn -> (gn.start < position) && (gn.end > position)))
                return true;
        }
        return false;
    }
}
