package com.milaboratory.mist.parser;

import com.milaboratory.mist.pattern.Pattern;
import com.milaboratory.mist.pattern.PatternAligner;

public final class Parser {
    private final PatternAligner patternAligner;

    public Parser(PatternAligner patternAligner) {
        this.patternAligner = patternAligner;
    }

    public Pattern parseQuery(String query) throws ParserException {
        return parseQuery(query, ParserFormat.NORMAL);
    }

    /**
     * Main parser function that transforms query string to Pattern object. It will throw ParserException if something
     * is wrong in the query.
     *
     * @param query query string
     * @param format parser format: NORMAL for end users or SIMPLIFIED as toString() output in inner classes
     * @return Pattern object for specified query string
     */
    public Pattern parseQuery(String query, ParserFormat format) throws ParserException {
        if (query.equals("")) throw new IllegalArgumentException("Parser query is empty!");
        TokenizedString tokenizedString = new TokenizedString(query);
        if (format == ParserFormat.SIMPLIFIED) {
            SimplifiedTokenizer simplifiedTokenizer = new SimplifiedTokenizer(patternAligner);
            simplifiedTokenizer.tokenize(tokenizedString);
            return tokenizedString.getFinalPattern();
        } else {
            return null;
        }
    }
}
