package com.milaboratory.mist.parser;

abstract class Tokenizer {
    protected final long finalScoreThreshold;

    Tokenizer(long finalScoreThreshold) {
        this.finalScoreThreshold = finalScoreThreshold;
    }

    abstract void tokenize(TokenizedString tokenizedString) throws ParserException;
}
