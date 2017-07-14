package com.milaboratory.mist.parser;

import com.milaboratory.core.sequence.NucleotideSequence;
import com.milaboratory.mist.pattern.*;

import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import static com.milaboratory.mist.parser.ParserUtils.*;

/**
 * Parsers for objects and their parameters for normal syntax.
 */
final class NormalParsers {
    private final PatternAligner patternAligner;
    private final String query;
    private final List<BracketsPair> parenthesesPairs;
    private final List<BracketsPair> squareBracketsPairs;
    private final List<BracketsPair> bracesPairs;
    private final List<QuotesPair> quotesPairs;
    private final ArrayList<Integer> startStickMarkers;
    private final ArrayList<Integer> endStickMarkers;
    private final ArrayList<ScoreThreshold> scoreThresholds;
    private final List<BorderFilterBracesPair> borderFilterBracesPairs;
    private final List<NormalSyntaxGroupName> groupNames;

    NormalParsers(PatternAligner patternAligner, String query, List<BracketsPair> parenthesesPairs,
                  List<BracketsPair> squareBracketsPairs, List<BracketsPair> bracesPairs,
                  List<QuotesPair> quotesPairs, ArrayList<Integer> startStickMarkers,
                  ArrayList<Integer> endStickMarkers, ArrayList<ScoreThreshold> scoreThresholds,
                  List<BorderFilterBracesPair> borderFilterBracesPairs, List<NormalSyntaxGroupName> groupNames) {
        this.patternAligner = patternAligner;
        this.query = query;
        this.parenthesesPairs = parenthesesPairs;
        this.squareBracketsPairs = squareBracketsPairs;
        this.bracesPairs = bracesPairs;
        this.quotesPairs = quotesPairs;
        this.startStickMarkers = startStickMarkers;
        this.endStickMarkers = endStickMarkers;
        this.scoreThresholds = scoreThresholds;
        this.borderFilterBracesPairs = borderFilterBracesPairs;
        this.groupNames = groupNames;
    }

    ArrayList<FoundToken> parseRepeatPatterns(List<BracketsPair> repeatPatternBracesPairs) throws ParserException {
        // must be changed to Integer.MAX_VALUE when bitap wrapper for longer sequences will be implemented
        final int MAX_REPEATS = 63;

        ArrayList<FoundToken> foundTokens = new ArrayList<>();
        for (BracketsPair bracesPair : repeatPatternBracesPairs) {
            if (bracesPair.start == 0)
                throw new ParserException("Found '{' without nucleotide in the start of query!");
            String arguments = query.substring(bracesPair.start + 1, bracesPair.end);
            NucleotideSequence patternSeq = toNSeq(query.substring(bracesPair.start - 1, bracesPair.start));
            int minRepeats = 1;
            int maxRepeats = MAX_REPEATS;
            int fixedLeftBorder = startStick(bracesPair.start - 1) ? 0 : -1;
            int fixedRightBorder = endStick(bracesPair.end) ? -2 : -1;
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
            String leftGroupName = getGroupName(bracesPair.start - 1, true);
            if (leftGroupName != null)
                groupEdgePositions.add(new GroupEdgePosition(new GroupEdge(leftGroupName, true), 0));
            String rightGroupName = getGroupName(bracesPair.end, false);
            if (rightGroupName != null)
                groupEdgePositions.add(new GroupEdgePosition(new GroupEdge(rightGroupName, false), maxRepeats));

            foundTokens.add(new FoundToken(new RepeatPattern(getPatternAligner(bracesPair.start - 1, bracesPair.end + 1),
                    patternSeq, minRepeats, maxRepeats, fixedLeftBorder, fixedRightBorder, groupEdgePositions),
                    bracesPair.start - 1, bracesPair.end + 1));
        }

        return foundTokens;
    }

    ArrayList<FoundToken> parseFuzzyMatchPatterns(TokenizedString tokenizedString) throws ParserException {
        ArrayList<FoundToken> foundTokens = new ArrayList<>();
        List<Token> stringTokens = tokenizedString.getTokens(0, tokenizedString.getFullLength()).stream()
                .filter(Token::isString).collect(Collectors.toList());
        for (Token currentStringToken : stringTokens) {
            Matcher regexMatcher = Pattern.compile("[a-zA-Z :()]*").matcher(currentStringToken.getString());
            while (regexMatcher.find()) {
                int start = regexMatcher.start() + currentStringToken.getStartCoordinate();
                int end = regexMatcher.end() + currentStringToken.getStartCoordinate();
                ArrayList<GroupEdgePosition> groupEdgePositions = getGroupEdgePositions(start, end);
                validateGroupEdgePositions(groupEdgePositions);
                int fixedLeftBorder = startStick(start) ? 0 : -1;
                int fixedRightBorder = endStick(end - 1) ? -2 : -1;
                NucleotideSequence patternSeq = toNSeq(cutGroupsAndSpaces(start, end));

                foundTokens.add(new FoundToken(new FuzzyMatchPattern(getPatternAligner(start, end), patternSeq,
                        fixedLeftBorder, fixedRightBorder, groupEdgePositions), start, end));
            }
        }

        return foundTokens;
    }

    /**
     * Check that group edges are correct, otherwise throw ParserException.
     *
     * @param groupEdges list of group edges to check
     * @param pairsRequired group starts and ends must come in pairs
     * @param duplicatesAllowed duplicates of group edges are allowed in the list
     */
    void validateGroupEdges(ArrayList<GroupEdge> groupEdges, boolean pairsRequired, boolean duplicatesAllowed)
            throws ParserException {
        if (!duplicatesAllowed)
            for (int i = 1; i < groupEdges.size(); i++)
                for (int j = 0; j < i; j++) {
                    GroupEdge groupEdge1 = groupEdges.get(i);
                    GroupEdge groupEdge2 = groupEdges.get(j);
                    if (groupEdge1.getGroupName().equals(groupEdge2.getGroupName())
                            && (groupEdge1.isStart() == groupEdge2.isStart()))
                        throw new ParserException("Duplicate groups allowed only on different sides of || operator; "
                                + "found invalid duplicate of group " + groupEdge1.getGroupName());
                }
        if (pairsRequired) {
            HashSet<String> groupNames = groupEdges.stream().map(GroupEdge::getGroupName)
                    .collect(Collectors.toCollection(HashSet::new));
            for (String groupName : groupNames) {
                long starts = groupEdges.stream().filter(ge -> ge.getGroupName().equals(groupName))
                        .filter(GroupEdge::isStart).count();
                long ends = groupEdges.stream().filter(ge -> ge.getGroupName().equals(groupName))
                        .filter(ge -> !ge.isStart()).count();
                if (starts != ends)
                    throw new ParserException("Group " + groupName + " has " + starts + " start(s) and " + ends
                            + " end(s) where expected equal number of starts and ends!");
            }
        }
    }

    /**
     * Check that group edge positions are correct, otherwise throw ParserException.
     *
     * @param groupEdgePositions list of group edge positions to check
     */
    private void validateGroupEdgePositions(ArrayList<GroupEdgePosition> groupEdgePositions) throws ParserException {
        for (int i = 1; i < groupEdgePositions.size(); i++)
            for (int j = 0; j < i; j++) {
                GroupEdgePosition groupEdgePosition1 = groupEdgePositions.get(i);
                GroupEdgePosition groupEdgePosition2 = groupEdgePositions.get(j);
                if (groupEdgePosition1.getGroupEdge().getGroupName()
                        .equals(groupEdgePosition2.getGroupEdge().getGroupName())) {
                    if (groupEdgePosition1.getGroupEdge().isStart() == groupEdgePosition2.getGroupEdge().isStart())
                        throw new ParserException("Found duplicate" + (groupEdgePosition1.getGroupEdge().isStart()
                                ? "start" : "end") + " of group " + groupEdgePosition1.getGroupEdge().getGroupName()
                                + " in 1 sequence of nucleotides!");
                    if ((groupEdgePosition1.getGroupEdge().isStart()
                            && (groupEdgePosition1.getPosition() > groupEdgePosition2.getPosition()))
                            || (groupEdgePosition2.getGroupEdge().isStart()
                            && (groupEdgePosition2.getPosition() > groupEdgePosition1.getPosition())))
                        throw new ParserException("Found end of group "
                                + groupEdgePosition1.getGroupEdge().getGroupName()
                                + " before start of group with the same name in 1 sequence of nucleotides!");
                }
            }
    }

    /**
     * Return the name of this group that has left edge on the left of this pattern (if onLeft == true)
     * or has right edge on the right of this pattern (if onLeft == false), without any patterns between this pattern
     * and group edge, otherwise return null.
     *
     * @param position position where to start the search; exclusive
     * @param onLeft true to search group name on the left from this pattern, false to search group closing parenthesis
     *               on the right from this pattern
     * @return group name or null if there is no group edge
     */
    private String getGroupName(int position, boolean onLeft) {
        int closestGroupIndex = getClosestGroupByPosition(position, onLeft);
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
     * @param position position in query string, inclusive (if index is on this position, it will be returned)
     * @param toLeft true: search group name to the left from position,
     *               false: search group closing parenthesis to the right
     * @return found index in list of group names, or -1 if not found
     */
    private int getClosestGroupByPosition(int position, boolean toLeft) {
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
     * Get group edge positions for FuzzyMatchPattern from specified range in query string, including groups on left
     * and right edges.
     *
     * @param start start of FuzzyMatchPattern, inclusive
     * @param end end of FuzzyMatchPattern, exclusive
     * @return list of group edge positions
     */
    private ArrayList<GroupEdgePosition> getGroupEdgePositions(int start, int end) {
        ArrayList<GroupEdgePosition> groupEdgePositions = new ArrayList<>();
        int ignoredCharactersCount = query.substring(start, end + 1).length()
                - query.substring(start, end + 1).replace(" ", "").length();
        for (NormalSyntaxGroupName groupName : groupNames) {
            int groupStart = groupName.start;
            int groupNameEnd = groupName.end;
            int groupEnd = groupName.bracketsPair.end;
            if (groupStart > start) {
                if (groupNameEnd < end - 1) {
                    groupEdgePositions.add(new GroupEdgePosition(new GroupEdge(groupName.name, true),
                            groupStart - start - ignoredCharactersCount));
                    ignoredCharactersCount += groupNameEnd - groupStart + 1;
                } else
                    throw new IllegalStateException("FuzzyMatchPattern: start=" + start + ", end=" + end
                            + ", group name: start=" + groupStart + ", end=" + groupNameEnd);
            }
            if ((groupEnd > start) && (groupEnd < end - 1)) {
                groupEdgePositions.add(new GroupEdgePosition(new GroupEdge(groupName.name, false),
                        groupEnd - start - ignoredCharactersCount));
                ignoredCharactersCount++;
            }
        }

        String leftGroupName = getGroupName(start, true);
        if (leftGroupName != null)
            groupEdgePositions.add(new GroupEdgePosition(new GroupEdge(leftGroupName, true), 0));
        String rightGroupName = getGroupName(end, false);
        if (rightGroupName != null)
            groupEdgePositions.add(new GroupEdgePosition(new GroupEdge(rightGroupName, false),
                    end - start + 1 - ignoredCharactersCount));

        return groupEdgePositions;
    }

    /**
     * Check is this pattern should stick to start (check "$" before the pattern).
     *
     * @param position pattern start position, inclusive
     * @return true if this pattern should stick to target start
     */
    private boolean startStick(int position) {
        int markerPosition = -1;
        for (int i = 0; i < startStickMarkers.size(); i++) {
            if ((position > startStickMarkers.get(i)) && ((i == startStickMarkers.size() - 1)
                    || (position < startStickMarkers.get(i + 1))))
                markerPosition = startStickMarkers.get(i);
        }

        return (markerPosition != -1) && !query.substring(markerPosition + 1, position).matches(".*\\\\.*")
                && !isAnyNucleotide(markerPosition + 1, position);
    }

    /**
     * Check is this pattern should stick to end (check "^" after the pattern).
     *
     * @param position pattern end position, inclusive
     * @return true if this pattern should stick to target end
     */
    private boolean endStick(int position) {
        int markerPosition = -1;
        for (int i = 0; i < endStickMarkers.size(); i++) {
            if ((position < endStickMarkers.get(i)) && ((i == 0) || (position > endStickMarkers.get(i - 1)))
                    && ((i != 0) || (endStickMarkers.size() == 1)))
                markerPosition = endStickMarkers.get(i);
        }

        return (markerPosition != -1) && !query.substring(position + 1, markerPosition).matches(".*\\\\.*")
                && !isAnyNucleotide(position + 1, markerPosition);
    }

    /**
     * Returns true if there is any nucleotide in query substring, otherwise false.
     *
     * @param start substring start position, inclusive
     * @param end substring end position, exclusive
     * @return true if there is any nucleotide in query substring, otherwise false
     */
    private boolean isAnyNucleotide(int start, int end) {
        for (int currentPosition = start; currentPosition < end; currentPosition++) {
            final int position = currentPosition;
            if (query.substring(position, position + 1).matches("[a-zA-Z]")
                    && groupNames.stream().noneMatch(gn -> (gn.start < position) && (gn.end > position)))
                return true;
        }
        return false;
    }

    /**
     * Cuts group start and end tokens and spaces from FuzzyMatchPattern substring.
     *
     * @param start FuzzyMatchPattern start, inclusive
     * @param end FuzzyMatchPattern end, exclusive
     * @return cutted string that contains only nucleotide sequence for FuzzyMatchPattern
     */
    private String cutGroupsAndSpaces(int start, int end) {
        StringBuilder result = new StringBuilder();
        for (int currentPosition = start; currentPosition < end; currentPosition++) {
            final int position = currentPosition;
            if ((query.charAt(position) != ' ')
                    && groupNames.stream().noneMatch(gn -> (gn.start <= position) && (gn.end >= position)))
                result.append(query.charAt(position));
        }
        return result.toString();
    }

    /**
     * Find score threshold for specified range and return PatternAligner with this threshold. If there is no score
     * threshold, return pattern aligner without changing its threshold.
     *
     * @param start start of range, inclusive
     * @param end end of range, exclusive
     * @return PatternAligner with updated score threshold for the specified range
     */
    private PatternAligner getPatternAligner(int start, int end) {
        int currentNestedLevel = -1;
        long currentThreshold = 0;
        for (ScoreThreshold scoreThreshold : scoreThresholds)
            if (scoreThreshold.contains(start, end) && (scoreThreshold.nestedLevel > currentNestedLevel)) {
                currentNestedLevel = scoreThreshold.nestedLevel;
                currentThreshold = scoreThreshold.threshold;
            }
        if (currentNestedLevel == -1)
            return patternAligner;
        else
            return patternAligner.overridePenaltyThreshold(currentThreshold);
    }
}
