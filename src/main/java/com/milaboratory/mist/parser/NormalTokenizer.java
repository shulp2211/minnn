package com.milaboratory.mist.parser;

import com.milaboratory.mist.pattern.PatternAligner;

import java.util.*;

import static com.milaboratory.mist.parser.BracketsDetector.*;
import static com.milaboratory.mist.parser.BracketsType.*;
import static com.milaboratory.mist.parser.NormalParsers.*;
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
        List<BracketsPair> squareBracketsPairs = getAllBrackets(SQUARE, fullString);
        List<BracketsPair> bracesPairs = getAllBrackets(BRACES, fullString);
        List<QuotesPair> quotesPairs = getAllQuotes(fullString);
        ArrayList<Integer> startStickMarkers = getTokenPositions(fullString, "$", quotesPairs);
        ArrayList<Integer> endStickMarkers = getTokenPositions(fullString, "^", quotesPairs);
        ArrayList<ScoreThreshold> scoreThresholds = getScoreThresholds(fullString, NORMAL);
        List<BorderFilterBracesPair> borderFilterBracesPairs = getBorderFilterBraces(fullString,
                bracesPairs);
        List<NormalSyntaxGroupName> groupNames = getGroupNames(fullString, parenthesesPairs);
        groupNames.sort(Comparator.comparingInt(gn -> gn.start));

        parseRepeatPatterns(patternAligner, fullString, getRepeatPatternBraces(bracesPairs, borderFilterBracesPairs),
                startStickMarkers, endStickMarkers, groupNames).forEach(tokenizedString::tokenizeSubstring);
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
}
