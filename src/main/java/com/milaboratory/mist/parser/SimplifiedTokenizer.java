package com.milaboratory.mist.parser;

import com.milaboratory.mist.pattern.GroupEdge;
import com.milaboratory.mist.pattern.GroupEdgePosition;
import com.milaboratory.mist.pattern.Pattern;

import java.util.*;

import static com.milaboratory.mist.parser.BracketsDetector.getAllBrackets;
import static com.milaboratory.mist.parser.BracketsType.*;

class SimplifiedTokenizer {
    /**
     * Convert all tokenizedString contents into pattern. This class is for simplified parser syntax.
     *
     * @param tokenizedString TokenizedString object that was created from query string
     */
    static void tokenize(TokenizedString tokenizedString) throws ParserException {
        if (tokenizedString.getNumberOfStrings() != 1)
            throw new IllegalArgumentException("TokenizedString must contain exactly 1 string to use in this function!");
        String fullString = tokenizedString.getStrings().get(0);
        HashSet<BracketsPair> parenthesesPairs = getAllBrackets(PARENTHESES, fullString);
        HashSet<BracketsPair> squareBracketsPairs = getAllBrackets(SQUARE, fullString);
        ArrayList<ObjectString> objectStrings = new ArrayList<>();

        for (BracketsPair parenthesesPair : parenthesesPairs) {
            if (parenthesesPair.end == parenthesesPair.start + 1)
                throw new ParserException("Found empty parentheses: "
                        + parenthesesPair.start + ", " + parenthesesPair.end + "; argument list must not be empty!");
            objectStrings.add(new ObjectString(getObjectName(parenthesesPair.start, fullString),
                    parenthesesPair.start + 1, parenthesesPair.end, parenthesesPair.nestedLevel));
        }

        ObjectString[] objectStringsSorted = objectStrings.toArray(new ObjectString[objectStrings.size()]);
        Arrays.sort(objectStringsSorted, Comparator.comparingInt((ObjectString s) -> s.parenthesesNestedLevel).reversed());

        for (ObjectString objectString : objectStringsSorted) {
            switch (objectString.name) {
                case "GroupEdge":
                case "GroupEdgePosition":
                    break;
                case "FuzzyMatchPattern":
                    // TODO
                    break;
                case "AndPattern":
                    // TODO
                    break;
                case "PlusPattern":
                    // TODO
                    break;
                case "OrPattern":
                    // TODO
                    break;
                case "MultiPattern":
                    // TODO
                    break;
                case "AndOperator":
                    // TODO
                    break;
                case "OrOperator":
                    // TODO
                    break;
                case "NotOperator":
                    // TODO
                    break;
                case "FilterPattern":
                    // TODO
                    break;
                case "ScoreFilter":
                    // TODO
                    break;
                case "BorderFilter":
                    // TODO
                    break;
                default:
                    throw new ParserException("Found wrong object name: " + objectString.name);
            }
        }
    }

    private static String getObjectName(int leftParenthesisPosition, String fullString) {
        final String STOP_CHARACTERS = " ([";
        ArrayList<Character> reversedNameCharacters = new ArrayList<>();
        int currentPosition = leftParenthesisPosition - 1;

        while ((currentPosition >= 0)
                && (!STOP_CHARACTERS.contains(fullString.substring(currentPosition, currentPosition + 1))))
            reversedNameCharacters.add(fullString.charAt(currentPosition));
        StringBuilder builder = new StringBuilder(reversedNameCharacters.size());
        for (int i = reversedNameCharacters.size() - 1; i >= 0; i--)
            builder.append(reversedNameCharacters.get(i));
        return builder.toString();
    }

    private static <P extends Pattern> ArrayList<P> parseArrayOfPatterns(
            TokenizedString tokenizedString, BracketsPair bracketsPair) throws ParserException {
        if (bracketsPair.bracketsType != SQUARE)
            throw new IllegalArgumentException("Array of patterns must be in square brackets; got brackets pair of type "
                    + bracketsPair);
        if (bracketsPair.end == bracketsPair.start + 1)
            return new ArrayList<>();
        else {
            ArrayList<P> patterns = new ArrayList<>();
            for (Object token : tokenizedString.getTokens(bracketsPair.start + 1, bracketsPair.end)) {
                if (token instanceof String)
                    if (!token.equals(", "))
                        throw new ParserException("Found not parsed token in array of patterns: " + token);
                else try {
                        patterns.add((P)token);
                } catch (ClassCastException e) {
                    throw new IllegalStateException("Found token of invalid class in array of patterns: " + e);
                }
            }
            return patterns;
        }
    }

    private static ArrayList<GroupEdgePosition> parseArrayOfGroupEdgePositions(
            TokenizedString tokenizedString, BracketsPair bracketsPair) throws ParserException {
        if (bracketsPair.bracketsType != SQUARE)
            throw new IllegalArgumentException(
                    "Array of group edge positions must be in square brackets; got brackets pair of type "
                    + bracketsPair);
        if (bracketsPair.end == bracketsPair.start + 1)
            return new ArrayList<>();
        else {
            ArrayList<GroupEdgePosition> groupEdgePositions = new ArrayList<>();
            ArrayList<Object> tokenizedStringPart = tokenizedString.getTokens(bracketsPair.start + 1, bracketsPair.end);
            if ((tokenizedStringPart.size() != 1) || !(tokenizedStringPart.get(0) instanceof String))
                throw new IllegalStateException("Expected only 1 string in tokenizedStringPart, got this: "
                        + tokenizedStringPart);

            final String STRING_START = "GroupEdgePosition(GroupEdge('";
            String arrayString = (String)(tokenizedStringPart.get(0));
            int currentPosition = 0;
            int currentIndex = 0;
            int foundTokenPosition;
            do {
                foundTokenPosition = arrayString.indexOf(", " + STRING_START, currentIndex);
                if (foundTokenPosition != -1) {
                    String currentToken = arrayString.substring(currentPosition, foundTokenPosition);
                    groupEdgePositions.add(parseGroupEdgePosition(currentToken, STRING_START));
                    currentPosition = foundTokenPosition + 2;
                    currentIndex++;
                }
            } while (foundTokenPosition != -1);
            String lastToken = arrayString.substring(currentPosition);
            groupEdgePositions.add(parseGroupEdgePosition(lastToken, STRING_START));

            return groupEdgePositions;
        }
    }

    /**
     * Parse group edge position from string that represents it.
     *
     * @param str string representing 1 group edge position
     * @param STRING_START constant that contains start of GroupEdgePosition string before group name
     * @return parsed GroupEdgePosition
     */
    private static GroupEdgePosition parseGroupEdgePosition(
            String str, final String STRING_START) throws ParserException {
        if (!str.substring(0, STRING_START.length()).equals(STRING_START))
            throw new IllegalArgumentException("Incorrect string start in " + str + ", expected: " + STRING_START);

        int firstCommaPosition = str.indexOf(",");
        if (firstCommaPosition == -1)
            throw new ParserException("Missing comma in " + str);
        String groupName = str.substring(STRING_START.length(), firstCommaPosition - 1);
        if (groupName.length() == 0)
            throw new ParserException("Found empty group name in " + str);

        int secondCommaPosition = str.indexOf(",", 1);
        if (secondCommaPosition == -1)
            throw new ParserException("Missing second comma in " + str);
        boolean isStart;
        if (str.substring(firstCommaPosition - 1, secondCommaPosition).equals("', true)"))
            isStart = true;
        else if (str.substring(firstCommaPosition - 1, secondCommaPosition).equals("', false)"))
            isStart = false;
        else
            throw new ParserException("Failed to parse group edge position from " + str);

        if (!str.substring(str.length() - 1).equals(")"))
            throw new ParserException("Missing closing parenthesis in " + str);
        int position;
        try {
            position = Integer.parseInt(str.substring(secondCommaPosition + 2, str.length() - 1));
        } catch (NumberFormatException e) {
            throw new ParserException("Failed to parse position ("
                    + str.substring(secondCommaPosition + 2, str.length() - 1) + ") in " + str);
        }
        if (position < 0)
            throw new ParserException("Position is negative in " + str);

        return new GroupEdgePosition(new GroupEdge(groupName, isStart), position);
    }

    private static class ObjectString {
        String name;
        int argumentsFrom;
        int argumentsTo;
        int parenthesesNestedLevel;

        ObjectString(String name, int argumentsFrom, int argumentsTo, int parenthesesNestedLevel) {
            this.name = name;
            this.argumentsFrom = argumentsFrom;
            this.argumentsTo = argumentsTo;
            this.parenthesesNestedLevel = parenthesesNestedLevel;
        }
    }
}
