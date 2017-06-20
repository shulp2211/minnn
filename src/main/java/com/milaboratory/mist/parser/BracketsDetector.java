package com.milaboratory.mist.parser;

import java.util.*;
import java.util.stream.Collectors;

import static com.milaboratory.mist.parser.BracketsType.*;
import static com.milaboratory.mist.parser.QuotesType.*;

final class BracketsDetector {
    /**
     * Get list of all quotes pairs of all types.
     *
     * @param str string to search: it may be entire parser query or substring from TokenizedString
     * @return list of all quotes pairs of all types
     */
    static List<QuotesPair> getAllQuotes(String str) throws ParserException {
        List<QuotesPair> quotesPairs = new ArrayList<>();
        OpenQuote lastOpenQuote = null;
        for (int currentPosition = 0; currentPosition < str.length(); currentPosition++) {
            if (lastOpenQuote != null) {
                if (currentPosition < str.length() - 1) {
                    switch (str.substring(currentPosition, currentPosition + 2)) {
                        case "\\\\":
                        case "\\\"":
                        case "\\\'":
                            // skip escaped backslash or escaped quotes inside quotes
                            currentPosition++;
                            continue;
                    }
                }
                switch (str.charAt(currentPosition)) {
                    case '\"':
                        if (lastOpenQuote.quotesType == DOUBLE) {
                            quotesPairs.add(new QuotesPair(DOUBLE, lastOpenQuote.position, currentPosition));
                            lastOpenQuote = null;
                        }
                        break;
                    case '\'':
                        if (lastOpenQuote.quotesType == SINGLE) {
                            quotesPairs.add(new QuotesPair(SINGLE, lastOpenQuote.position, currentPosition));
                            lastOpenQuote = null;
                        }
                }
            } else {
                switch (str.charAt(currentPosition)) {
                    case '\"':
                        lastOpenQuote = new OpenQuote(DOUBLE, currentPosition);
                        break;
                    case '\'':
                        lastOpenQuote = new OpenQuote(SINGLE, currentPosition);
                }
            }
        }
        if (lastOpenQuote != null)
            throw new ParserException("Missing closing " + (lastOpenQuote.quotesType == SINGLE ? "\'" : "\"")
                    + " in " + str);
        return quotesPairs;
    }

    /**
     * Checks is this position inside quotes (inclusive on left and right sides).
     *
     * @param quotesPairs all quotes pairs detected by getAllQuotes function
     * @param position position in string
     * @return is this position inside quotes (inclusive on left and right sides)
     */
    static boolean isInQuotes(List<QuotesPair> quotesPairs, int position) {
        for (QuotesPair quotesPair : quotesPairs)
            if (quotesPair.contains(position))
                return true;
        return false;
    }

    /**
     * String.indexOf function that searches only non-quoted tokens. It will skip tokens for which start position
     * is inside quotes.
     *
     * @param quotesPairs all quotes pairs detected by getAllQuotes function
     * @param str string in which we do the search
     * @param token token to search
     * @param currentPosition current position in string where we start the search
     * @return found position of next token; -1 if not found
     */
    static int nonQuotedIndexOf(List<QuotesPair> quotesPairs, String str, String token, int currentPosition) {
        int foundPosition;
        do {
            if (currentPosition == str.length()) return -1;
            foundPosition = str.indexOf(token, currentPosition);
            currentPosition = foundPosition + 1;
        } while (isInQuotes(quotesPairs, foundPosition));
        return foundPosition;
    }

    /**
     * Returns next position in query that is not inside quotes; used for searching non-quoted tokens in string.
     *
     * @param quotesPairs all quotes pairs detected by getAllQuotes function
     * @param currentPosition current position in string
     * @return next non-quoted position in string; string length is not checked
     */
    static int nextNonQuotedPosition(List<QuotesPair> quotesPairs, int currentPosition) {
        for (QuotesPair quotesPair : quotesPairs)
            if (quotesPair.contains(currentPosition + 1))
                if (!isInQuotes(quotesPairs, quotesPair.end + 1))
                    return quotesPair.end + 1;
                else
                    currentPosition = quotesPair.end;
        return currentPosition + 1;
    }

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
        List<QuotesPair> quotesPairs = getAllQuotes(str);
        int currentNestedLevel = 0;
        for (int currentPosition = 0; currentPosition < str.length(); currentPosition = nextNonQuotedPosition(
                quotesPairs, currentPosition)) {
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
    static void getGroupEdgePositions(String query) throws ParserException {
    }

    /**
     * Get all reads that specified in the string and their square brackets.
     *
     * @param str string to search: it may be entire parser query or substring from TokenizedString
     * @return map of bracket pairs and corresponding read numbers
     */
    static void getReads(String str) throws ParserException {
    }

    /**
     * Get all score limits that specified in the query.
     *
     * @param query query string as it came to the parser
     * @return map of curly braces pairs and corresponding score limits
     */
    static void getScoreLimits(String query) throws ParserException {
    }

    /**
     * Get parentheses that used for setting number for BorderFilter.
     *
     * @param query query string as it came to the parser
     * @return map of parentheses and their numbers of nucleotides for BorderFilter
     */
    static void getBorderFilterParentheses(String query) throws ParserException {
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

    /**
     * Get closing bracket coordinate by opening bracket coordinate from a list of bracket pairs.
     *
     * @param bracketsPairs list of bracket pairs
     * @param start opening bracket coordinate
     * @return closing bracket coordinate
     */
    static int getEndByStart(List<BracketsPair> bracketsPairs, int start) {
        for (BracketsPair bracketsPair : bracketsPairs)
            if (bracketsPair.start == start)
                return bracketsPair.end;
        throw new IllegalArgumentException("List of bracket pairs " + bracketsPairs
                + " doesn't contain bracket with start " + start);
    }

    private static class OpenQuote {
        final QuotesType quotesType;
        final int position;

        OpenQuote(QuotesType quotesType, int position) {
            this.quotesType = quotesType;
            this.position = position;
        }
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
