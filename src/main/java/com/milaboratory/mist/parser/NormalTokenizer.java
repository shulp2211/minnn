package com.milaboratory.mist.parser;

import com.milaboratory.mist.pattern.*;

import java.util.*;

import static com.milaboratory.mist.parser.BracketsDetector.*;
import static com.milaboratory.mist.parser.BracketsType.*;
import static com.milaboratory.mist.parser.ParserFormat.*;
import static com.milaboratory.mist.parser.ParserUtils.*;

final class NormalTokenizer extends Tokenizer {
    NormalTokenizer(PatternAligner patternAligner) {
        super(patternAligner);
    }

    @Override
    void tokenize(TokenizedString tokenizedString) throws ParserException {
        String fullString = tokenizedString.getOneString();
        List<BracketsPair> parenthesesPairs = getAllBrackets(PARENTHESES, fullString);
        List<BracketsPair> bracesPairs = getAllBrackets(BRACES, fullString);
        List<QuotesPair> quotesPairs = getAllQuotes(fullString);
        ArrayList<Integer> startStickMarkers = getTokenPositions(fullString, "^", quotesPairs);
        ArrayList<Integer> endStickMarkers = getTokenPositions(fullString, "$", quotesPairs);
        ArrayList<ScoreThreshold> scoreThresholds = getScoreThresholds(fullString, NORMAL);
        ArrayList<NormalSyntaxSquareBrackets> squareBracketsPairs = getSquareBrackets(fullString, scoreThresholds);
        List<BorderFilterBracesPair> borderFilterBracesPairs = getBorderFilterBraces(fullString,
                bracesPairs);
        List<NormalSyntaxGroupName> groupNames = getGroupNames(fullString, parenthesesPairs);
        groupNames.sort(Comparator.comparingInt(gn -> gn.start));

        NormalParsers normalParsers = new NormalParsers(patternAligner, fullString, parenthesesPairs,
                squareBracketsPairs, bracesPairs, startStickMarkers, endStickMarkers, scoreThresholds,
                borderFilterBracesPairs, groupNames);

        normalParsers.parseRepeatPatterns(getRepeatPatternBraces(bracesPairs, borderFilterBracesPairs))
                .forEach(tokenizedString::tokenizeSubstring);
        normalParsers.parseFuzzyMatchPatterns(tokenizedString).forEach(tokenizedString::tokenizeSubstring);
        normalParsers.parseAnyPatterns(tokenizedString).forEach(tokenizedString::tokenizeSubstring);
        for (boolean left : new boolean[] {true, false})
            normalParsers.parseBorderFilters(tokenizedString, left).forEach(tokenizedString::tokenizeSubstring);
        normalParsers.parseSequencePatterns(tokenizedString).forEach(tokenizedString::tokenizeSubstring);

        int maxBracketsNestedLevel = squareBracketsPairs.stream().mapToInt(bp -> bp.bracketsPair.nestedLevel)
                .max().orElse(0);
        for (int currentNestedLevel = maxBracketsNestedLevel; currentNestedLevel >= -1; currentNestedLevel--) {
            normalParsers.parseSingleReadOperators(tokenizedString, currentNestedLevel)
                    .forEach(tokenizedString::tokenizeSubstring);
            normalParsers.parseSequencePatterns(tokenizedString).forEach(tokenizedString::tokenizeSubstring);
        }

        normalParsers.parseMultiPatterns(tokenizedString).forEach(tokenizedString::tokenizeSubstring);
        for (int currentNestedLevel = maxBracketsNestedLevel; currentNestedLevel >= -1; currentNestedLevel--)
            normalParsers.parseMultiReadOperators(tokenizedString, currentNestedLevel)
                    .forEach(tokenizedString::tokenizeSubstring);

        Pattern finalPattern = tokenizedString.getFinalPattern();
        boolean duplicateGroupsAllowed = OrPattern.class.isAssignableFrom(finalPattern.getClass())
                || OrOperator.class.isAssignableFrom(finalPattern.getClass());
        validateGroupEdges(finalPattern.getGroupEdges(), true, duplicateGroupsAllowed);
    }

    /**
     * Get group names from group parentheses pairs.
     *
     * @param fullString full query string
     * @param parenthesesPairs parentheses pairs
     * @return group names
     */
    private static ArrayList<NormalSyntaxGroupName> getGroupNames(String fullString, List<BracketsPair> parenthesesPairs)
            throws ParserException {
        ArrayList<NormalSyntaxGroupName> groupNames = new ArrayList<>();
        for (BracketsPair parenthesesPair : parenthesesPairs) {
            int colonPosition = fullString.indexOf(":", parenthesesPair.start + 1);
            if (colonPosition == -1)
                throw new ParserException("Missing colon in parentheses pair: "
                        + fullString.substring(parenthesesPair.start, parenthesesPair.end + 1));
            groupNames.add(new NormalSyntaxGroupName(parenthesesPair, fullString.substring(parenthesesPair.start + 1,
                    colonPosition)));
        }
        return groupNames;
    }

    /**
     * Get square brackets and their score thresholds if they present.
     *
     * @param fullString full query string
     * @param scoreThresholds score thresholds returned by getScoreThresholds() function
     * @return square brackets with score thresholds if they present
     */
    private static ArrayList<NormalSyntaxSquareBrackets> getSquareBrackets(String fullString,
            ArrayList<ScoreThreshold> scoreThresholds) throws ParserException {
        ArrayList<NormalSyntaxSquareBrackets> bracketsList = new ArrayList<>();
        List<BracketsPair> squareBracketsPairs = getAllBrackets(SQUARE, fullString);
        for (BracketsPair bracketsPair : squareBracketsPairs) {
            ScoreThreshold scoreThreshold = scoreThresholds.stream()
                    .filter(st -> (st.start == bracketsPair.start) && (st.end == bracketsPair.end + 1))
                    .findFirst().orElse(null);
            if (scoreThreshold == null)
                bracketsList.add(new NormalSyntaxSquareBrackets(bracketsPair));
            else
                bracketsList.add(new NormalSyntaxSquareBrackets(bracketsPair, true,
                        fullString.indexOf(":", bracketsPair.start), scoreThreshold.threshold));
        }
        return bracketsList;
    }
}
