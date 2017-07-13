package com.milaboratory.mist.parser;

import java.util.*;

import static com.milaboratory.mist.parser.BracketsDetector.getAllBrackets;
import static com.milaboratory.mist.parser.BracketsDetector.getEndByStart;
import static com.milaboratory.mist.parser.BracketsType.*;
import static com.milaboratory.mist.parser.SimplifiedParsers.parseScoreFilter;
import static com.milaboratory.mist.parser.SimplifiedSyntaxStrings.*;

final class ParserUtils {
    /**
     * Get object name at left of open parenthesis for simplified syntax.
     *
     * @param leftParenthesisPosition position of open parenthesis in string
     * @param fullString string where to search
     * @return object name
     */
    static String getObjectName(int leftParenthesisPosition, String fullString) throws ParserException {
        ArrayList<Character> reversedNameCharacters = new ArrayList<>();
        int currentPosition = leftParenthesisPosition - 1;

        while ((currentPosition >= 0)
                && (!NAME_STOP_CHARACTERS.contains(fullString.substring(currentPosition, currentPosition + 1))))
            reversedNameCharacters.add(fullString.charAt(currentPosition--));
        StringBuilder builder = new StringBuilder(reversedNameCharacters.size());
        for (int i = reversedNameCharacters.size() - 1; i >= 0; i--)
            builder.append(reversedNameCharacters.get(i));
        return builder.toString();
    }

    /**
     * Detect all score thresholds in query string.
     *
     * @param query query string
     * @param format parser format: NORMAL for end users or SIMPLIFIED as toString() output in inner classes
     * @return list of score thresholds
     */
    static ArrayList<ScoreThreshold> getScoreThresholds(String query, ParserFormat format) throws ParserException {
        ArrayList<ScoreThreshold> scoreThresholds = new ArrayList<>();
        switch (format) {
            case NORMAL:
                List<BracketsPair> squareBrackets = getAllBrackets(SQUARE, query);
                squareBrackets.sort(Comparator.comparingInt(bp -> bp.start));
                for (BracketsPair bracketsPair : squareBrackets) {
                    boolean isScoreLimit = false;
                    int colonPosition = 0;
                    for (int i = bracketsPair.start + 1; i < bracketsPair.end; i++) {
                        if ("([{\"'".contains(query.substring(i, i + 1)))
                            break;
                        if (query.charAt(i) == ':') {
                            isScoreLimit = true;
                            colonPosition = i;
                            break;
                        }
                    }
                    if (isScoreLimit) {
                        long scoreThreshold;
                        try {
                            scoreThreshold = Long.parseLong(query.substring(bracketsPair.start + 1, colonPosition));
                        } catch (NumberFormatException e) {
                            throw new ParserException("Failed to parse score threshold ("
                                    + query.substring(bracketsPair.start + 1, colonPosition) + ") in " + query);
                        }
                        int currentNestedLevel = 0;
                        for (ScoreThreshold currentScoreThreshold : scoreThresholds)
                            if (currentScoreThreshold.contains(bracketsPair.start, bracketsPair.end))
                                currentNestedLevel++;
                        scoreThresholds.add(new ScoreThreshold(scoreThreshold,
                                bracketsPair.start, bracketsPair.end + 1, currentNestedLevel));
                    }
                }
                break;
            case SIMPLIFIED:
                int minFilterLength = SCORE_FILTER_START.length() + 8;
                List<BracketsPair> parentheses = getAllBrackets(PARENTHESES, query);
                for (int currentPosition = 0; currentPosition < query.length() - minFilterLength; currentPosition++)
                    if (query.substring(currentPosition, currentPosition + SCORE_FILTER_START.length())
                            .equals(SCORE_FILTER_START)) {
                        int startCoordinate = currentPosition + FILTER_PATTERN_NAME.length();
                        int endCoordinate = getEndByStart(parentheses, startCoordinate);
                        int filterStartCoordinate = startCoordinate + 1;
                        int filterEndCoordinate = getEndByStart(parentheses,
                                currentPosition + SCORE_FILTER_START.length() - 1);
                        String scoreFilterSubstring = query.substring(filterStartCoordinate, filterEndCoordinate + 1);
                        long scoreThreshold = parseScoreFilter(scoreFilterSubstring, SCORE_FILTER_NAME + "(")
                                .getScoreThreshold();
                        int currentNestedLevel = 0;
                        for (ScoreThreshold currentScoreThreshold : scoreThresholds)
                            if (currentScoreThreshold.contains(startCoordinate, endCoordinate))
                                currentNestedLevel++;
                        scoreThresholds.add(new ScoreThreshold(scoreThreshold, startCoordinate, endCoordinate + 1,
                                currentNestedLevel));
                    }
        }
        return scoreThresholds;
    }

    /**
     * Test group name for validity, throw ParserException if it is invalid. Special characters are not allowed
     * in group names because they may lead to bad Fastq output files.
     *
     * @param groupName group name
     */
    static void checkGroupName(String groupName) throws ParserException {
        if (groupName.length() == 0)
            throw new ParserException("Group name must not be empty!");
        if (!groupName.matches("[a-zA-Z0-9]*"))
            throw new ParserException("Group names must contain only letters and digits; invalid group name: " + groupName);
    }

    /**
     * Get nucleotide sequence from string or throw ParserException if failed.
     *
     * @param str string containing nucleotide sequence
     * @return NucleotideSequence object
     * @throws ParserException if NucleotideSequence constructor throws IllegalArgumentException
     */
    static NucleotideSequence toNSeq(String str) throws ParserException {
        try {
            return new NucleotideSequence(str);
        } catch (IllegalArgumentException e) {
            throw new ParserException("Failed to parse nucleotide sequence from " + str + ": " + e.getMessage());
        }
    }

    /**
     * Get integer from string or throw ParserException if failed.
     *
     * @param str string
     * @param paramName parameter name for error message
     * @return int value
     */
    static int toInt(String str, String paramName) throws ParserException {
        try {
            return Integer.parseInt(str);
        } catch (NumberFormatException e) {
            throw new ParserException("Failed to parse " + paramName + " from " + str + ": " + e.getMessage());
        }
    }

    /**
     * Get long from string or throw ParserException if failed.
     *
     * @param str string
     * @param paramName parameter name for error message
     * @return long value
     */
    static long toLong(String str, String paramName) throws ParserException {
        try {
            return Long.parseLong(str);
        } catch (NumberFormatException e) {
            throw new ParserException("Failed to parse " + paramName + " from " + str + ": " + e.getMessage());
        }
    }
}
