package com.milaboratory.mist.parser;

import com.milaboratory.mist.pattern.Pattern;

public final class Parser {
    private Parser() {
    }

    public static Pattern parseQuery(String query, long scoreThreshold) throws ParserException {
        return parseQuery(query, scoreThreshold, ParserFormat.NORMAL);
    }

    /**
     * Main parser function that transforms query string to Pattern object. It will throw ParserException if something
     * is wrong in the query.
     *
     * @param query query string
     * @param scoreThreshold score threshold
     * @param format parser format: NORMAL for end users or SIMPLIFIED as toString() output in inner classes
     * @return Pattern object for specified query string
     */
    public static Pattern parseQuery(String query, long scoreThreshold, ParserFormat format) throws ParserException {
        if (query.equals("")) throw new ParserException("Query is empty!");
        TokenizedString tokenizedString = new TokenizedString(query);
        Tokenizer tokenizer = (format == ParserFormat.NORMAL) ? new NormalTokenizer(scoreThreshold)
                : new SimplifiedTokenizer(scoreThreshold);
        tokenizer.tokenize(tokenizedString);
        return tokenizedString.getFinalPattern();
    }
}
