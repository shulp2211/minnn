package com.milaboratory.mist.parser;

import com.milaboratory.mist.pattern.PatternAligner;

abstract class Tokenizer {
    protected final PatternAligner patternAligner;

    Tokenizer(PatternAligner patternAligner) {
        this.patternAligner = patternAligner;
    }

    abstract void tokenize(TokenizedString tokenizedString) throws ParserException;
}
