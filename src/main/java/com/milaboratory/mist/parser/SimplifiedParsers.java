package com.milaboratory.mist.parser;

import com.milaboratory.mist.pattern.*;

import java.util.ArrayList;

/**
 * Parsers for objects and their parameters for simplified syntax.
 */
final class SimplifiedParsers {
    /**
     * Parse FuzzyMatchPattern parameters; group edge positions must be already parsed in this stage.
     *
     * @param patternAligner pattern aligner
     * @param str string containing FuzzyMatchPattern and all arguments
     * @param startingPart start of pattern string before 1st argument
     * @param groupEdgePositions parsed group edge positions
     * @return FuzzyMatchPattern
     */
    static FuzzyMatchPattern parseFuzzyMatchPattern(PatternAligner patternAligner, String str, String startingPart,
                                                    ArrayList<GroupEdgePosition> groupEdgePositions) throws ParserException {
        return null;
    }

    /**
     * Parse AndPattern from tokenized substring returned by getTokens() function and already parsed operand patterns.
     *
     * @param patternAligner pattern aligner
     * @param tokenizedSubstring tokenized substring returned by getTokens() function
     * @param startingPart start of pattern string before 1st argument
     * @param singlePatterns parsed operand patterns
     * @return AndPattern
     */
    static AndPattern parseAndPattern(PatternAligner patternAligner, ArrayList<Object> tokenizedSubstring,
                                      String startingPart, ArrayList<SinglePattern> singlePatterns)
            throws ParserException {
        checkOperandArraySpelling(tokenizedSubstring, startingPart);
        return new AndPattern(patternAligner, singlePatterns.toArray(new SinglePattern[singlePatterns.size()]));
    }

    static PlusPattern parsePlusPattern(PatternAligner patternAligner, ArrayList<Object> tokenizedSubstring,
                                        String startingPart, ArrayList<SinglePattern> singlePatterns)
            throws ParserException {
        checkOperandArraySpelling(tokenizedSubstring, startingPart);
        return new PlusPattern(patternAligner, singlePatterns.toArray(new SinglePattern[singlePatterns.size()]));
    }

    static OrPattern parseOrPattern(PatternAligner patternAligner, ArrayList<Object> tokenizedSubstring,
                                    String startingPart, ArrayList<SinglePattern> singlePatterns)
            throws ParserException {
        checkOperandArraySpelling(tokenizedSubstring, startingPart);
        return new OrPattern(patternAligner, singlePatterns.toArray(new SinglePattern[singlePatterns.size()]));
    }

    static MultiPattern parseMultiPattern(PatternAligner patternAligner, ArrayList<Object> tokenizedSubstring,
                                          String startingPart, ArrayList<SinglePattern> singlePatterns)
            throws ParserException {
        checkOperandArraySpelling(tokenizedSubstring, startingPart);
        return new MultiPattern(patternAligner, singlePatterns.toArray(new SinglePattern[singlePatterns.size()]));
    }

    static AndOperator parseAndOperator(PatternAligner patternAligner, ArrayList<Object> tokenizedSubstring,
                                        String startingPart, ArrayList<MultipleReadsOperator> operands)
            throws ParserException {
        checkOperandArraySpelling(tokenizedSubstring, startingPart);
        return new AndOperator(patternAligner, operands.toArray(new MultipleReadsOperator[operands.size()]));
    }

    static OrOperator parseOrOperator(PatternAligner patternAligner, ArrayList<Object> tokenizedSubstring,
                                      String startingPart, ArrayList<MultipleReadsOperator> operands)
            throws ParserException {
        checkOperandArraySpelling(tokenizedSubstring, startingPart);
        return new OrOperator(patternAligner, operands.toArray(new MultipleReadsOperator[operands.size()]));
    }

    /**
     * Parse group edge position from string that represents it.
     *
     * @param str string representing 1 group edge position
     * @param startingPart start of GroupEdgePosition string before group name
     * @return parsed GroupEdgePosition
     */
    static GroupEdgePosition parseGroupEdgePosition(String str, String startingPart) throws ParserException {
        if (!str.substring(0, startingPart.length()).equals(startingPart))
            throw new IllegalArgumentException("Incorrect string start in " + str + ", expected: " + startingPart);

        int firstCommaPosition = str.indexOf(",");
        if (firstCommaPosition == -1)
            throw new ParserException("Missing comma in " + str);
        String groupName = str.substring(startingPart.length(), firstCommaPosition - 1);
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

    private static void checkOperandArraySpelling(ArrayList<Object> tokenizedSubstring, String startingPart)
            throws ParserException {
        if (tokenizedSubstring.size() < 3)
            throw new IllegalArgumentException("Wrong tokenizedSubstring for array of patterns: " + tokenizedSubstring);
        if (!(tokenizedSubstring.get(0) instanceof String)
                || (tokenizedSubstring.get(0).equals(startingPart + "[")))
            throw new IllegalArgumentException("Incorrect string start in " + tokenizedSubstring
                    + ", expected: " + startingPart);
        if (!(tokenizedSubstring.get(tokenizedSubstring.size() - 1) instanceof String))
            throw new IllegalArgumentException("Wrong tokenizedSubstring for array of patterns: " + tokenizedSubstring);
        else if (!tokenizedSubstring.get(tokenizedSubstring.size() - 1).equals("])"))
            throw new ParserException("Found wrong end of operand array string: "
                    + tokenizedSubstring.get(tokenizedSubstring.size() - 1));
        if (!(Pattern.class.isAssignableFrom(tokenizedSubstring.get(1).getClass())))
            throw new IllegalArgumentException("Wrong tokenizedSubstring for array of patterns: " + tokenizedSubstring);
        for (int i = 2; i < tokenizedSubstring.size() - 1; i++) {
            if (i % 2 == 0)
                if (!(tokenizedSubstring.get(i) instanceof String))
                    throw new IllegalArgumentException("Wrong tokenizedSubstring for array of patterns: " + tokenizedSubstring);
                else if (!tokenizedSubstring.get(i).equals(", "))
                    throw new ParserException("Found wrong delimiter in array of patterns: " + tokenizedSubstring.get(i));
            else if (!(Pattern.class.isAssignableFrom(tokenizedSubstring.get(i).getClass())))
                throw new IllegalArgumentException("Wrong tokenizedSubstring for array of patterns: " + tokenizedSubstring);
        }
    }
}
