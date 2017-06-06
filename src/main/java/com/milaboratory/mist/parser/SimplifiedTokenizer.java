package com.milaboratory.mist.parser;

import com.milaboratory.mist.pattern.*;

import java.util.*;
import java.util.stream.Collectors;

import static com.milaboratory.mist.parser.BracketsDetector.getAllBrackets;
import static com.milaboratory.mist.parser.BracketsType.*;
import static com.milaboratory.mist.parser.ParserFormat.*;
import static com.milaboratory.mist.parser.ParserUtils.getScoreThresholds;
import static com.milaboratory.mist.parser.SimplifiedParsers.*;

final class SimplifiedTokenizer {
    private final PatternAligner patternAligner;
    private final HashMap<String, String> ObjectStringStartingParts;

    SimplifiedTokenizer(PatternAligner patternAligner) {
        this.patternAligner = patternAligner;
        ObjectStringStartingParts = new HashMap<>();
        for (String name: new String[] {
                "FuzzyMatchPattern", "AndPattern", "PlusPattern", "OrPattern", "MultiPattern",
                "AndOperator", "OrOperator", "NotOperator", "MultipleReadsFilterPattern",
                "FilterPattern", "ScoreFilter", "BorderFilter"})
            ObjectStringStartingParts.put(name, name + "(");
        ObjectStringStartingParts.put("GroupEdgePosition", "GroupEdgePosition(GroupEdge('");
    }

    /**
     * Convert all tokenizedString contents into pattern. This class is for simplified parser syntax.
     *
     * @param tokenizedString TokenizedString object that was created from query string
     */
    void tokenize(TokenizedString tokenizedString) throws ParserException {
        String fullString = tokenizedString.getOneString();
        List<BracketsPair> parenthesesPairs = getAllBrackets(PARENTHESES, fullString);
        List<BracketsPair> squareBracketsPairs = getAllBrackets(SQUARE, fullString);
        ArrayList<ScoreThreshold> scoreThresholds = getScoreThresholds(fullString, SIMPLIFIED);
        ArrayList<ObjectString> objectStrings = new ArrayList<>();

        for (BracketsPair parenthesesPair : parenthesesPairs) {
            if (parenthesesPair.end == parenthesesPair.start + 1)
                throw new ParserException("Found empty parentheses: "
                        + parenthesesPair.start + ", " + parenthesesPair.end + "; argument list must not be empty!");
            objectStrings.add(new ObjectString(getObjectName(parenthesesPair.start, fullString), parenthesesPair));
        }
        objectStrings.sort(Comparator.comparingInt(ObjectString::getNestedLevel).reversed());

        for (ObjectString objectString : objectStrings) {
            String startingPart = ObjectStringStartingParts.get(objectString.getName());
            PatternAligner currentPatternAligner = getPatternAligner(scoreThresholds, objectString);
            switch (objectString.getName()) {
                case "GroupEdge":
                case "GroupEdgePosition":
                case "ScoreFilter":
                case "BorderFilter":
                    break;
                case "FuzzyMatchPattern":
                    ArrayList<GroupEdgePosition> groupEdgePositions;
                    List<BracketsPair> innerSquareBrackets = squareBracketsPairs.stream().
                            filter(bp -> objectString.getParenthesesPair().contains(bp)).collect(Collectors.toList());
                    switch (innerSquareBrackets.size()) {
                        case 0:
                            groupEdgePositions = new ArrayList<>();
                            break;
                        case 1:
                            groupEdgePositions = parseArrayOfGroupEdgePositions(tokenizedString, innerSquareBrackets.get(0));
                            break;
                        default:
                            throw new ParserException("Found multiple square bracket pairs in FuzzyMatchPattern!");
                    }

                    String fuzzyMatchPatternString = tokenizedString.getOneString(
                            objectString.getStart(), objectString.getEnd());
                    FuzzyMatchPattern fuzzyMatchPattern = parseFuzzyMatchPattern(currentPatternAligner,
                            fuzzyMatchPatternString, startingPart, groupEdgePositions);
                    tokenizedString.tokenizeSubstring(fuzzyMatchPattern, objectString.getStart(), objectString.getEnd());
                    break;
                case "AndPattern":
                    ArrayList<SinglePattern> andPatternOperands = getPatternOperands(
                            tokenizedString, squareBracketsPairs, objectString);
                    ArrayList<Object> andPatternTokenizedSubstring = tokenizedString.getTokens(
                            objectString.getStart(), objectString.getEnd());
                    AndPattern andPattern = parseAndPattern(currentPatternAligner, andPatternTokenizedSubstring,
                            startingPart, andPatternOperands);
                    tokenizedString.tokenizeSubstring(andPattern, objectString.getStart(), objectString.getEnd());
                    break;
                case "PlusPattern":
                    ArrayList<SinglePattern> plusPatternOperands = getPatternOperands(
                            tokenizedString, squareBracketsPairs, objectString);
                    ArrayList<Object> plusPatternTokenizedSubstring = tokenizedString.getTokens(
                            objectString.getStart(), objectString.getEnd());
                    PlusPattern plusPattern = parsePlusPattern(currentPatternAligner, plusPatternTokenizedSubstring,
                            startingPart, plusPatternOperands);
                    tokenizedString.tokenizeSubstring(plusPattern, objectString.getStart(), objectString.getEnd());
                    break;
                case "OrPattern":
                    ArrayList<SinglePattern> orPatternOperands = getPatternOperands(
                            tokenizedString, squareBracketsPairs, objectString);
                    ArrayList<Object> orPatternTokenizedSubstring = tokenizedString.getTokens(
                            objectString.getStart(), objectString.getEnd());
                    OrPattern orPattern = parseOrPattern(currentPatternAligner, orPatternTokenizedSubstring,
                            startingPart, orPatternOperands);
                    tokenizedString.tokenizeSubstring(orPattern, objectString.getStart(), objectString.getEnd());
                    break;
                case "MultiPattern":
                    ArrayList<SinglePattern> multiPatternOperands = getPatternOperands(
                            tokenizedString, squareBracketsPairs, objectString);
                    ArrayList<Object> multiPatternTokenizedSubstring = tokenizedString.getTokens(
                            objectString.getStart(), objectString.getEnd());
                    MultiPattern multiPattern = parseMultiPattern(currentPatternAligner, multiPatternTokenizedSubstring,
                            startingPart, multiPatternOperands);
                    tokenizedString.tokenizeSubstring(multiPattern, objectString.getStart(), objectString.getEnd());
                    break;
                case "AndOperator":
                    ArrayList<MultipleReadsOperator> andOperatorOperands = getPatternOperands(
                            tokenizedString, squareBracketsPairs, objectString);
                    ArrayList<Object> andOperatorTokenizedSubstring = tokenizedString.getTokens(
                            objectString.getStart(), objectString.getEnd());
                    AndOperator andOperator = parseAndOperator(currentPatternAligner, andOperatorTokenizedSubstring,
                            startingPart, andOperatorOperands);
                    tokenizedString.tokenizeSubstring(andOperator, objectString.getStart(), objectString.getEnd());
                    break;
                case "OrOperator":
                    ArrayList<MultipleReadsOperator> orOperatorOperands = getPatternOperands(
                            tokenizedString, squareBracketsPairs, objectString);
                    ArrayList<Object> orOperatorTokenizedSubstring = tokenizedString.getTokens(
                            objectString.getStart(), objectString.getEnd());
                    OrOperator orOperator = parseOrOperator(currentPatternAligner, orOperatorTokenizedSubstring,
                            startingPart, orOperatorOperands);
                    tokenizedString.tokenizeSubstring(orOperator, objectString.getStart(), objectString.getEnd());
                    break;
                case "NotOperator":
                    ArrayList<Object> notOperatorTokenizedSubstring = tokenizedString.getTokens(
                            objectString.getStart(), objectString.getEnd());
                    NotOperator notOperator = parseNotOperator(currentPatternAligner, notOperatorTokenizedSubstring,
                            startingPart);
                    tokenizedString.tokenizeSubstring(notOperator, objectString.getStart(), objectString.getEnd());
                    break;
                case "MultipleReadsFilterPattern":
                    // TODO
                    break;
                case "FilterPattern":
                    ArrayList<Object> filterPatternTokenizedSubstring = tokenizedString.getTokens(
                            objectString.getStart(), objectString.getEnd());
                    FilterPattern filterPattern = parseFilterPattern(currentPatternAligner, filterPatternTokenizedSubstring,
                            startingPart);
                    tokenizedString.tokenizeSubstring(filterPattern, objectString.getStart(), objectString.getEnd());
                    break;
                default:
                    throw new ParserException("Found wrong object name: " + objectString.getName());
            }
        }
    }

    private <P extends Pattern> ArrayList<P> getPatternOperands(
            TokenizedString tokenizedString, List<BracketsPair> squareBracketsPairs, ObjectString objectString)
            throws ParserException {
        List<BracketsPair> innerSquareBrackets = squareBracketsPairs.stream().
                filter(bp -> objectString.getParenthesesPair().contains(bp)).collect(Collectors.toList());
        switch (innerSquareBrackets.size()) {
            case 0:
                throw new ParserException("Missing square bracket pair in " + objectString.getName());
            case 1:
                return parseArrayOfPatterns(tokenizedString, innerSquareBrackets.get(0));
            default:
                throw new ParserException("Found multiple square bracket pairs in " + objectString.getName());
        }
    }

    private String getObjectName(int leftParenthesisPosition, String fullString) {
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

    private <P extends Pattern> ArrayList<P> parseArrayOfPatterns(
            TokenizedString tokenizedString, BracketsPair bracketsPair) throws ParserException {
        if (bracketsPair.bracketsType != SQUARE)
            throw new IllegalArgumentException("Array of patterns must be in square brackets; got brackets pair: "
                    + bracketsPair);
        if (bracketsPair.end == bracketsPair.start + 1)
            throw new ParserException("Expected array of patterns, found empty square brackets pair: " + bracketsPair);
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

    private ArrayList<GroupEdgePosition> parseArrayOfGroupEdgePositions(
            TokenizedString tokenizedString, BracketsPair bracketsPair) throws ParserException {
        if (bracketsPair.bracketsType != SQUARE)
            throw new IllegalArgumentException(
                    "Array of group edge positions must be in square brackets; got brackets pair of type "
                    + bracketsPair);
        if (bracketsPair.end == bracketsPair.start + 1)
            return new ArrayList<>();
        else {
            ArrayList<GroupEdgePosition> groupEdgePositions = new ArrayList<>();
            String stringStart = ObjectStringStartingParts.get("GroupEdgePosition");
            String arrayString = tokenizedString.getOneString(bracketsPair.start + 1, bracketsPair.end);
            int currentPosition = 0;
            int currentIndex = 0;
            int foundTokenPosition;
            do {
                foundTokenPosition = arrayString.indexOf(", " + stringStart, currentIndex);
                if (foundTokenPosition != -1) {
                    String currentToken = arrayString.substring(currentPosition, foundTokenPosition);
                    groupEdgePositions.add(parseGroupEdgePosition(currentToken, stringStart));
                    currentPosition = foundTokenPosition + 2;
                    currentIndex++;
                }
            } while (foundTokenPosition != -1);
            String lastToken = arrayString.substring(currentPosition);
            groupEdgePositions.add(parseGroupEdgePosition(lastToken, stringStart));

            return groupEdgePositions;
        }
    }

    /**
     * Find score threshold for specified object and return PatternAligner with this threshold. If there is no score
     * threshold, return pattern aligner without changing its threshold.
     *
     * @param scoreThresholds score thresholds list
     * @param objectString string of the object for which we calculate score threshold
     * @return PatternAligner with updated score threshold for the specified object
     */
    private PatternAligner getPatternAligner(ArrayList<ScoreThreshold> scoreThresholds, ObjectString objectString)
            throws ParserException {
        int currentNestedLevel = -1;
        int currentThreshold = 0;
        for (ScoreThreshold scoreThreshold : scoreThresholds)
            if (scoreThreshold.contains(objectString.getStart(), objectString.getEnd())
                    && (scoreThreshold.nestedLevel > currentNestedLevel)) {
                currentNestedLevel = scoreThreshold.nestedLevel;
                currentThreshold = scoreThreshold.threshold;
            }
        if (currentNestedLevel == -1)
            return patternAligner;
        else
            return patternAligner.overridePenaltyThreshold(currentThreshold);
    }

    private static class ObjectString {
        private String name;
        private BracketsPair parenthesesPair;

        ObjectString(String name, BracketsPair parenthesesPair) {
            this.name = name;
            this.parenthesesPair = parenthesesPair;
        }

        String getName() {
            return name;
        }

        BracketsPair getParenthesesPair() {
            return parenthesesPair;
        }

        int getStart() {
            return parenthesesPair.start + 1;
        }

        int getEnd() {
            return parenthesesPair.end;
        }

        int getNestedLevel() {
            return parenthesesPair.nestedLevel;
        }

        @Override
        public String toString() {
            return "ObjectString{" + "name='" + name + "', parenthesesPair=" + parenthesesPair + "}";
        }
    }
}
