package com.milaboratory.mist.parser;

import static com.milaboratory.mist.parser.BracketsType.SQUARE;

final class NormalSyntaxSquareBrackets extends CharPair {
    final BracketsPair bracketsPair;
    final boolean isScoreFilter;
    final long filterScore;

    NormalSyntaxSquareBrackets(BracketsPair bracketsPair) {
        this(bracketsPair, false, 0, 0);
    }

    /**
     * Square brackets in NORMAL parser syntax may contain score filter with colon, or not contain.
     * "start" position in superclass points to opening bracket if there is no score filter, or to colon if there is
     * score filter; "end" position points to closed square bracket.
     *
     * @param bracketsPair square brackets pair corresponding
     * @param isScoreFilter is there is score filter in the beginning
     * @param colonPosition score filter colon position; if isScoreFilter is false, value is ignored
     * @param filterScore score for score filter; if isScoreFilter is false, value is ignored
     */
    NormalSyntaxSquareBrackets(BracketsPair bracketsPair, boolean isScoreFilter, int colonPosition, long filterScore) {
        super(isScoreFilter ? colonPosition : bracketsPair.start, bracketsPair.end);
        if (bracketsPair.bracketsType != SQUARE)
            throw new IllegalStateException("Trying to create NormalSyntaxSquareBrackets with brackets of type "
                    + bracketsPair.bracketsType);
        this.bracketsPair = bracketsPair;
        this.isScoreFilter = isScoreFilter;
        this.filterScore = isScoreFilter ? filterScore : 0;
    }
}
