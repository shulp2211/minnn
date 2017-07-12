package com.milaboratory.mist.parser;

import static com.milaboratory.mist.parser.BracketsType.PARENTHESES;

final class BorderFilterParenthesesPair extends CharPair {
    final BracketsPair bracketsPair;
    final boolean leftBorder;
    final int numberOfRepeats;

    BorderFilterParenthesesPair(BracketsPair bracketsPair, boolean leftBorder, int numberOfRepeats) {
        super(bracketsPair.start, bracketsPair.end);
        if (bracketsPair.bracketsType != PARENTHESES)
            throw new IllegalStateException("Trying to create BorderFilterParenthesesPair with brackets of type "
                    + bracketsPair.bracketsType);
        this.bracketsPair = bracketsPair;
        this.leftBorder = leftBorder;
        this.numberOfRepeats = numberOfRepeats;
    }
}
