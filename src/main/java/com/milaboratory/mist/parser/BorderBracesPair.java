package com.milaboratory.mist.parser;

import static com.milaboratory.mist.parser.BracketsType.BRACES;

final class BorderBracesPair extends CharPair {
    final boolean leftBorder;
    final int numberOfRepeats;

    BorderBracesPair(BracketsPair bracesPair, boolean leftBorder, int numberOfRepeats) {
        super(bracesPair.start, bracesPair.end);
        if (bracesPair.bracketsType != BRACES)
            throw new IllegalStateException("Trying to create BorderBracesPair with brackets of type "
                    + bracesPair.bracketsType);
        this.leftBorder = leftBorder;
        this.numberOfRepeats = numberOfRepeats;
    }
}
