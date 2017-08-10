package com.milaboratory.mist.parser;

import static com.milaboratory.mist.parser.BracketsType.PARENTHESES;
import static com.milaboratory.mist.parser.ParserUtils.checkGroupName;

final class NormalSyntaxGroupName extends CharPair {
    final BracketsPair bracketsPair;
    final String name;

    /**
     * Group name in NORMAL parser syntax is bordered with open parenthesis and colon.
     * "start" position in superclass points to opening parenthesis, and "end" position points to the colon
     *
     * @param bracketsPair brackets pair corresponding to this group
     * @param name group name
     */
    NormalSyntaxGroupName(BracketsPair bracketsPair, String name) throws ParserException {
        super(bracketsPair.start, bracketsPair.start + name.length() + 1);
        if (bracketsPair.bracketsType != PARENTHESES)
            throw new IllegalStateException("Trying to create NormalSyntaxGroupName with brackets of type "
                    + bracketsPair.bracketsType);
        this.bracketsPair = bracketsPair;
        this.name = name.trim();
        checkGroupName(this.name);
    }
}
