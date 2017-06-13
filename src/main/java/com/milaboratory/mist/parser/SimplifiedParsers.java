package com.milaboratory.mist.parser;

import com.milaboratory.core.sequence.NucleotideSequence;
import com.milaboratory.mist.pattern.*;

import java.util.ArrayList;
import java.util.List;

import static com.milaboratory.mist.parser.BracketsDetector.*;

/**
 * Parsers for objects and their parameters for simplified syntax.
 */
final class SimplifiedParsers {
    /**
     * Parse FuzzyMatchPattern parameters; group edge positions must be already parsed in this stage.
     *
     * @param patternAligner pattern aligner
     * @param str string containing FuzzyMatchPattern and all arguments
     * @param groupEdgePositions parsed group edge positions
     * @return FuzzyMatchPattern
     */
    static FuzzyMatchPattern parseFuzzyMatchPattern(PatternAligner patternAligner, String str,
                                                    ArrayList<GroupEdgePosition> groupEdgePositions) throws ParserException {
        List<QuotesPair> quotesPairs = getAllQuotes(str);
        NucleotideSequence seq;

        int commaPosition = nonQuotedIndexOf(quotesPairs, str, ",", 0);
        if (commaPosition == -1)
            if (str.length() == 0)
                throw new ParserException("Missing nucleotide sequence, empty argument string for fuzzy match pattern!");
            else try {
                seq = new NucleotideSequence(str);
            } catch (IllegalArgumentException e) {
                throw new ParserException("Wrong nucleotide sequence in " + str + ": " + e);
            }
        else
            if (!str.substring(commaPosition, commaPosition + 3).equals(", ["))
                throw new ParserException("Error while parsing " + str + ": expected ', [', found '"
                        + str.substring(commaPosition, commaPosition + 3) + "'");
            else if (commaPosition == 0)
                throw new ParserException("Missing nucleotide sequence: " + str);
            else try {
                seq = new NucleotideSequence(str.substring(0, commaPosition));
            } catch (IllegalArgumentException e) {
                throw new ParserException("Wrong nucleotide sequence in " + str + ": " + e);
            }

        return new FuzzyMatchPattern(patternAligner, seq, groupEdgePositions);
    }

    /**
     * Parse AndPattern from tokenized substring returned by getTokens() function and already parsed operand patterns.
     *
     * @param patternAligner pattern aligner
     * @param tokenizedSubstring tokenized substring returned by getTokens() function
     * @param singlePatterns parsed operand patterns
     * @return AndPattern
     */
    static AndPattern parseAndPattern(PatternAligner patternAligner, ArrayList<Object> tokenizedSubstring,
                                      ArrayList<SinglePattern> singlePatterns) throws ParserException {
        checkOperandArraySpelling(tokenizedSubstring);
        return new AndPattern(patternAligner, singlePatterns.toArray(new SinglePattern[singlePatterns.size()]));
    }

    static PlusPattern parsePlusPattern(PatternAligner patternAligner, ArrayList<Object> tokenizedSubstring,
                                        ArrayList<SinglePattern> singlePatterns) throws ParserException {
        checkOperandArraySpelling(tokenizedSubstring);
        return new PlusPattern(patternAligner, singlePatterns.toArray(new SinglePattern[singlePatterns.size()]));
    }

    static OrPattern parseOrPattern(PatternAligner patternAligner, ArrayList<Object> tokenizedSubstring,
                                    ArrayList<SinglePattern> singlePatterns) throws ParserException {
        checkOperandArraySpelling(tokenizedSubstring);
        return new OrPattern(patternAligner, singlePatterns.toArray(new SinglePattern[singlePatterns.size()]));
    }

    static MultiPattern parseMultiPattern(PatternAligner patternAligner, ArrayList<Object> tokenizedSubstring,
                                          ArrayList<SinglePattern> singlePatterns) throws ParserException {
        checkOperandArraySpelling(tokenizedSubstring);
        return new MultiPattern(patternAligner, singlePatterns.toArray(new SinglePattern[singlePatterns.size()]));
    }

    static AndOperator parseAndOperator(PatternAligner patternAligner, ArrayList<Object> tokenizedSubstring,
                                        ArrayList<MultipleReadsOperator> operands) throws ParserException {
        checkOperandArraySpelling(tokenizedSubstring);
        return new AndOperator(patternAligner, operands.toArray(new MultipleReadsOperator[operands.size()]));
    }

    static OrOperator parseOrOperator(PatternAligner patternAligner, ArrayList<Object> tokenizedSubstring,
                                      ArrayList<MultipleReadsOperator> operands) throws ParserException {
        checkOperandArraySpelling(tokenizedSubstring);
        return new OrOperator(patternAligner, operands.toArray(new MultipleReadsOperator[operands.size()]));
    }

    static NotOperator parseNotOperator(PatternAligner patternAligner, ArrayList<Object> tokenizedSubstring)
            throws ParserException {
        if (tokenizedSubstring.size() != 1)
            throw new ParserException("Syntax not parsed correctly for Not operator; possibly missing operand: "
                    + tokenizedSubstring);
        if (!MultipleReadsOperator.class.isAssignableFrom(tokenizedSubstring.get(0).getClass()))
            throw new IllegalArgumentException("Wrong tokenizedSubstring for NotOperator: " + tokenizedSubstring);

        return new NotOperator(patternAligner, (MultipleReadsOperator)(tokenizedSubstring.get(0)));
    }

    static Pattern parseFilterPattern(PatternAligner patternAligner, ArrayList<Object> tokenizedSubstring,
                                      boolean multipleReads) throws ParserException {
        if (tokenizedSubstring.size() != 2)
            throw new ParserException("Syntax not parsed correctly for Filter pattern; possibly missing operand: "
                    + tokenizedSubstring);
        if (!(tokenizedSubstring.get(0) instanceof String))
            throw new IllegalArgumentException("Incorrect start in " + tokenizedSubstring + ", expected filter string!");
        String startingSubstring = (String)(tokenizedSubstring.get(0));
        if (!Pattern.class.isAssignableFrom(tokenizedSubstring.get(1).getClass()))
            throw new IllegalArgumentException("Wrong tokenizedSubstring for FilterPattern: " + tokenizedSubstring);

        if (!startingSubstring.substring(startingSubstring.length() - 2).equals(", "))
            throw new ParserException("Expected ', ' in FilterPattern starting substring, found '"
                    + startingSubstring.substring(startingSubstring.length() - 2) + "'");

        String filterString = startingSubstring.substring(0, startingSubstring.length() - 2);
        int parenthesisPosition = filterString.indexOf("(");
        if (parenthesisPosition == -1)
            throw new ParserException("Missing open parenthesis in filter string: " + filterString);
        String filterName = filterString.substring(0, parenthesisPosition);
        String filterStartingPart = filterName + "(";
        Filter filter;
        switch (filterName) {
            case "ScoreFilter":
                filter = parseScoreFilter(filterString, filterStartingPart);
                break;
            case "BorderFilter":
                if (multipleReads)
                    throw new ParserException("BorderFilter must not be used with multiple reads!");
                filter = parseBorderFilter(patternAligner, filterString, filterStartingPart);
                break;
            default:
                throw new ParserException("Wrong filter name: " + filterName);
        }

        if (multipleReads)
            return new MultipleReadsFilterPattern(patternAligner, filter, (MultipleReadsOperator)(tokenizedSubstring.get(1)));
        else
            return new FilterPattern(patternAligner, filter, (SinglePattern)(tokenizedSubstring.get(1)));
    }

    static ScoreFilter parseScoreFilter(String str, String startingPart) throws ParserException {
        if (!str.substring(0, startingPart.length()).equals(startingPart))
            throw new ParserException("Incorrect ScoreFilter start in " + str + ", expected: " + startingPart);
        if (!str.substring(str.length() - 1).equals(")"))
            throw new ParserException("Missing closing parenthesis in " + str);

        int scoreThreshold;
        try {
            scoreThreshold = Integer.parseInt(str.substring(startingPart.length(), str.length() - 1));
        } catch (NumberFormatException e) {
            throw new ParserException("Failed to parse score threshold ("
                    + str.substring(startingPart.length(), str.length() - 1) + ") in " + str);
        }

        return new ScoreFilter(scoreThreshold);
    }

    private static BorderFilter parseBorderFilter(PatternAligner patternAligner, String str, String startingPart)
            throws ParserException {
        if (!str.substring(0, startingPart.length()).equals(startingPart))
            throw new ParserException("Incorrect BorderFilter start in " + str + ", expected: " + startingPart);
        if (!str.substring(str.length() - 1).equals(")"))
            throw new ParserException("Missing closing parenthesis in " + str);

        boolean leftSide;
        NucleotideSequence seq;
        int minNucleotides;
        boolean useTarget;

        int firstCommaPosition = str.indexOf(", ");
        if (firstCommaPosition == -1)
            throw new ParserException("Missing ', ' in " + str);
        if (str.substring(startingPart.length(), firstCommaPosition).equals("true"))
            leftSide = true;
        else if (str.substring(startingPart.length(), firstCommaPosition).equals("false"))
            leftSide = false;
        else
            throw new ParserException("Failed to parse left/right side flag from " + str);

        int secondCommaPosition = str.substring(firstCommaPosition + 1).indexOf(", ") + firstCommaPosition + 1;
        if (secondCommaPosition == -1) {
            try {
                seq = new NucleotideSequence(str.substring(firstCommaPosition + 2, str.length() - 1));
            } catch (IllegalArgumentException e) {
                throw new ParserException("Wrong nucleotide sequence in " + str + ": " + e);
            }
            return new BorderFilter(patternAligner, leftSide, seq);
        } else try {
            seq = new NucleotideSequence(str.substring(firstCommaPosition + 2, secondCommaPosition));
        } catch (IllegalArgumentException e) {
            throw new ParserException("Wrong nucleotide sequence in " + str + ": " + e);
        }

        int thirdCommaPosition = str.substring(secondCommaPosition + 1).indexOf(", ") + secondCommaPosition + 1;
        if (thirdCommaPosition == -1) {
            if (str.substring(secondCommaPosition + 2, str.length() - 1).equals("true"))
                return new BorderFilter(patternAligner, leftSide, seq, true);
            else if (str.substring(secondCommaPosition + 2, str.length() - 1).equals("false"))
                return new BorderFilter(patternAligner, leftSide, seq, false);
            else try {
                minNucleotides = Integer.parseInt(str.substring(secondCommaPosition + 2, str.length() - 1));
            } catch (NumberFormatException e) {
                throw new ParserException("Failed to parse minimum number of nucleotides ("
                        + str.substring(secondCommaPosition + 2, str.length() - 1) + ") in " + str);
            }
            return new BorderFilter(patternAligner, leftSide, seq, minNucleotides);
        } else {
            try {
                minNucleotides = Integer.parseInt(str.substring(secondCommaPosition + 2, thirdCommaPosition));
            } catch (NumberFormatException e) {
                throw new ParserException("Failed to parse minimum number of nucleotides ("
                        + str.substring(secondCommaPosition + 2, thirdCommaPosition) + ") in " + str);
            }
            if (str.substring(thirdCommaPosition + 2, str.length() - 1).equals("true"))
                useTarget = true;
            else if (str.substring(thirdCommaPosition + 2, str.length() - 1).equals("false"))
                useTarget = false;
            else
                throw new ParserException("Failed to parse use motif/target flag from "
                        + str.substring(thirdCommaPosition + 2, str.length() - 1) + " in " + str);
        }

        return new BorderFilter(patternAligner, leftSide, seq, minNucleotides, useTarget);
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
        List<QuotesPair> quotesPairs = getAllQuotes(str);

        int firstCommaPosition = nonQuotedIndexOf(quotesPairs, str, ", ", 0);
        if (firstCommaPosition == -1)
            throw new ParserException("Missing ', ' in " + str);
        String groupName = str.substring(startingPart.length(), firstCommaPosition - 1);
        if (groupName.length() == 0)
            throw new ParserException("Found empty group name in " + str);

        int secondCommaPosition = nonQuotedIndexOf(quotesPairs, str, ", ", firstCommaPosition + 1);
        if (secondCommaPosition == -1)
            throw new ParserException("Missing second ', ' in " + str);
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

    private static void checkOperandArraySpelling(ArrayList<Object> tokenizedSubstring)
            throws ParserException {
        if (tokenizedSubstring.size() < 3)
            throw new IllegalArgumentException("Wrong tokenizedSubstring for array of patterns: " + tokenizedSubstring);
        if (!(tokenizedSubstring.get(0) instanceof String))
            throw new IllegalArgumentException("Incorrect string start in " + tokenizedSubstring + ", expected '['");
        else if (!tokenizedSubstring.get(0).equals("["))
            throw new ParserException("Incorrect operand string start: " + tokenizedSubstring.get(0) + ", expected '['");
        if (!(tokenizedSubstring.get(tokenizedSubstring.size() - 1) instanceof String))
            throw new IllegalArgumentException("Wrong tokenizedSubstring for array of patterns: " + tokenizedSubstring);
        else if (!tokenizedSubstring.get(tokenizedSubstring.size() - 1).equals("]"))
            throw new ParserException("Found wrong end of operand array string: "
                    + tokenizedSubstring.get(tokenizedSubstring.size() - 1));
        if (!Pattern.class.isAssignableFrom(tokenizedSubstring.get(1).getClass()))
            throw new IllegalArgumentException("Wrong tokenizedSubstring for array of patterns: " + tokenizedSubstring);
        for (int i = 2; i < tokenizedSubstring.size() - 1; i++) {
            if (i % 2 == 0) {
                if (!(tokenizedSubstring.get(i) instanceof String))
                    throw new IllegalArgumentException("Wrong tokenizedSubstring for array of patterns: " + tokenizedSubstring);
                else if (!tokenizedSubstring.get(i).equals(", "))
                    throw new ParserException("Found wrong delimiter in array of patterns: " + tokenizedSubstring.get(i));
            } else if (!(Pattern.class.isAssignableFrom(tokenizedSubstring.get(i).getClass())))
                throw new IllegalArgumentException("Wrong tokenizedSubstring for array of patterns: " + tokenizedSubstring);
        }
    }
}
