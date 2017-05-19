package com.milaboratory.mist.parser;

import com.milaboratory.mist.pattern.GroupEdge;

import java.util.HashMap;
import java.util.HashSet;

final class BracketsDetector {
    /**
     * Get non-filtered list of all bracket pairs of the specified type.
     *
     * @param bracketsType brackets type
     * @param str string to search: it may be entire parser query or substring from TokenizedString
     * @return list of all brackets pairs
     */
    private static HashSet<BracketsPair> getAllBrackets(BracketsType bracketsType, String str) throws ParserException {
        return null;
    }

    /**
     * Get all group edges and their positions.
     *
     * @param query query string as it came to the parser
     * @return map of group edges and their positions in the query string
     */
    static HashMap<GroupEdge, Integer> getGroupEdgePositions(String query) throws ParserException {
        return null;
    }

    /**
     * Get all reads that specified in the string and their square brackets.
     *
     * @param str string to search: it may be entire parser query or substring from TokenizedString
     * @return map of bracket pairs and corresponding read numbers
     */
    static HashMap<BracketsPair, Integer> getReads(String str) throws ParserException {
        return null;
    }

    /**
     * Get all score limits that specified in the query.
     *
     * @param query query string as it came to the parser
     * @return map of curly braces pairs and corresponding score limits
     */
    static HashMap<BracketsPair, Float> getScoreLimits(String query) throws ParserException {
        return null;
    }

    /**
     * Get parentheses that used for setting number for BorderFilter.
     *
     * @param query query string as it came to the parser
     * @return map of parentheses and their numbers of nucleotides for BorderFilter
     */
    static HashMap<BracketsPair, Integer> getBorderFilterParentheses(String query) throws ParserException {
        return null;
    }

    /**
     * Get list of parentheses, only ones that used for setting operations priority.
     *
     * @param query query string as it came to the parser
     * @return
     */
    static HashSet<BracketsPair> getCommonParentheses(String query) throws ParserException {
        return null;
    }
}
