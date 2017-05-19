package com.milaboratory.mist.parser;

import java.util.HashMap;

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
}
