package com.milaboratory.mist.parser;

import com.milaboratory.mist.pattern.GroupEdge;

import java.util.*;
import java.util.stream.Collectors;

import static com.milaboratory.mist.parser.BracketsType.*;

final class BracketsDetector {
    /**
     * Get list of all bracket pairs of the specified type.
     *
     * @param bracketsType brackets type
     * @param str string to search: it may be entire parser query or substring from TokenizedString
     * @return list of all brackets pairs of the specified type
     */
    static List<BracketsPair> getAllBrackets(BracketsType bracketsType, String str) throws ParserException {
        ArrayList<BracketsPair> bracketsPairs = new ArrayList<>();
        Stack<OpenBracket> openBrackets = new Stack<>();
        int currentNestedLevel = 0;
        for (int currentPosition = 0; currentPosition < str.length(); currentPosition++) {
            char currentChar = str.charAt(currentPosition);
            switch (currentChar) {
                case '(':
                    openBrackets.push(new OpenBracket(PARENTHESES, currentPosition, currentNestedLevel++));
                    break;
                case '[':
                    openBrackets.push(new OpenBracket(SQUARE, currentPosition, currentNestedLevel++));
                    break;
                case '{':
                    openBrackets.push(new OpenBracket(BRACES, currentPosition, currentNestedLevel++));
                    break;
                case ')':
                    if (currentNestedLevel == 0)
                        throw new ParserException("Found ')' without '(' in " + str);
                    if (openBrackets.peek().bracketType != PARENTHESES)
                        throw new ParserException("Found ')' after opening bracket of type "
                                + openBrackets.peek().bracketType + " in " + str);
                    bracketsPairs.add(new BracketsPair(PARENTHESES, openBrackets.pop().position, currentPosition,
                            --currentNestedLevel));
                    break;
                case ']':
                    if (currentNestedLevel == 0)
                        throw new ParserException("Found ']' without '[' in " + str);
                    if (openBrackets.peek().bracketType != SQUARE)
                        throw new ParserException("Found ']' after opening bracket of type "
                                + openBrackets.peek().bracketType + " in " + str);
                    bracketsPairs.add(new BracketsPair(SQUARE, openBrackets.pop().position, currentPosition,
                            --currentNestedLevel));
                    break;
                case '}':
                    if (currentNestedLevel == 0)
                        throw new ParserException("Found '}' without '{' in " + str);
                    if (openBrackets.peek().bracketType != BRACES)
                        throw new ParserException("Found '}' after opening bracket of type "
                                + openBrackets.peek().bracketType + " in " + str);
                    bracketsPairs.add(new BracketsPair(BRACES, openBrackets.pop().position, currentPosition,
                            --currentNestedLevel));
                    break;
            }
        }
        if (currentNestedLevel > 0)
            throw new ParserException("Missing " + currentNestedLevel + " closing bracket(s) in " + str);
        return bracketsPairs.stream().filter(bp -> bp.bracketsType == bracketsType).collect(Collectors.toList());
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
    static List<BracketsPair> getCommonParentheses(String query) throws ParserException {
        return null;
    }

    private static class OpenBracket {
        final BracketsType bracketType;
        final int position;
        final int nestedLevel;

        OpenBracket(BracketsType bracketType, int position, int nestedLevel) {
            this.bracketType = bracketType;
            this.position = position;
            this.nestedLevel = nestedLevel;
        }
    }
}
