package com.milaboratory.mist.parser;

import com.milaboratory.mist.pattern.Pattern;

public final class Parser {
    private final int maxErrors;
    private final float errorScorePenalty;

    /**
     * Initialize parser.
     *
     * @param maxErrors maximum number of errors for FuzzyMatchPattern; maximum number of nucleotide intersections
     *                  for AndPattern and PlusPattern
     * @param errorScorePenalty score penalty for nucleotide intersections for AndPattern and PlusPattern
     */
    public Parser(int maxErrors, float errorScorePenalty) {
        this.maxErrors = maxErrors;
        this.errorScorePenalty = errorScorePenalty;
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
        return null;
    }
}
