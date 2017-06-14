package com.milaboratory.mist.parser;

import java.util.*;

import static com.milaboratory.mist.parser.BracketsDetector.getAllBrackets;
import static com.milaboratory.mist.parser.BracketsDetector.getEndByStart;
import static com.milaboratory.mist.parser.BracketsType.*;
import static com.milaboratory.mist.parser.SimplifiedParsers.parseScoreFilter;
import static com.milaboratory.mist.parser.SimplifiedSyntaxStrings.*;

final class ParserUtils {
    /**
     * Get position in string right after next semicolon.
     *
     * @param str string to search
     * @param currentPosition current position in str
     * @return position after next semicolon that is after currentPosition
     * @throws ParserException if semicolon not found after currentPosition
     */
    static int getPositionAfterSemicolon(String str, int currentPosition) throws ParserException {
        return 0;
    }

    /**
     * Find areas for fuzzy match patterns in the query. Found areas may contain group edges.
     *
     * @param query query string as it came to the parser
     * @return map of start (inclusive) and end (exclusive) positions for fuzzy match pattern areas
     */
    static HashMap<Integer, Integer> findFuzzyMatchPatterns(String query) throws ParserException {
        return null;
    }

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
                throw new IllegalStateException("Not yet implemented");
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
                        int scoreThreshold = parseScoreFilter(scoreFilterSubstring, SCORE_FILTER_NAME + "(")
                                .getScoreThreshold();
                        int currentNestedLevel = 0;
                        for (ScoreThreshold currentScoreThreshold : scoreThresholds)
                            if (currentScoreThreshold.contains(startCoordinate, endCoordinate))
                                currentNestedLevel++;
                        scoreThresholds.add(new ScoreThreshold(scoreThreshold, startCoordinate, endCoordinate + 1,
                                currentNestedLevel));
                    }
                return scoreThresholds;
            default:
                throw new IllegalArgumentException("Unknown parser format: " + format);
        }
    }
}
