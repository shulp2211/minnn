package com.milaboratory.mist.parser;

import static com.milaboratory.mist.parser.BracketsType.BRACES;

final class BorderFilterBracesPair extends CharPair {
    final BracketsPair bracesPair;
    final boolean leftBorder;
    final int numberOfRepeats;

    BorderFilterBracesPair(BracketsPair bracesPair, boolean leftBorder, int numberOfRepeats) {
        super(bracesPair.start, bracesPair.end);
        if (bracesPair.bracketsType != BRACES)
            throw new IllegalStateException("Trying to create BorderFilterBracesPair with brackets of type "
                    + bracesPair.bracketsType);
        this.bracesPair = bracesPair;
        this.leftBorder = leftBorder;
        this.numberOfRepeats = numberOfRepeats;
    }
}
