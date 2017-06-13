package com.milaboratory.mist.parser;

import com.milaboratory.mist.pattern.MultipleReadsOperator;
import com.milaboratory.mist.pattern.Pattern;
import com.milaboratory.mist.pattern.SinglePattern;

final class Token {
    private final boolean isString;
    private final String string;
    private final Pattern pattern;
    private final int length;

    Token(String string) {
        this.isString = true;
        this.string = string;
        this.pattern = null;
        this.length = string.length();
    }

    Token(Pattern pattern, int length) {
        this.isString = false;
        this.string = null;
        this.pattern = pattern;
        this.length = length;
    }

    boolean isString() {
        return isString;
    }

    String getString() {
        if (isString) return string;
        else throw new IllegalStateException("Trying to get string, but this is pattern token: " + pattern);
    }

    Pattern getPattern() {
        if (!isString) return pattern;
        else throw new IllegalStateException("Trying to get pattern, but this is string token: " + string);
    }

    int getLength() {
        return length;
    }

    String getSubstring(int start) {
        return getString().substring(start);
    }

    String getSubstring(int start, int end) {
        return getString().substring(start, end);
    }

    Token getSubstringToken(int start) {
        return new Token(getSubstring(start));
    }

    Token getSubstringToken(int start, int end) {
        return new Token(getSubstring(start, end));
    }

    SinglePattern getSinglePattern() throws ParserException {
        if (SinglePattern.class.isAssignableFrom(getPattern().getClass()))
            return (SinglePattern)pattern;
        else throw new ParserException("Expected SinglePattern, but got " + pattern);
    }

    MultipleReadsOperator getMultipleReadsOperator() throws ParserException {
        if (MultipleReadsOperator.class.isAssignableFrom(getPattern().getClass()))
            return (MultipleReadsOperator)pattern;
        else throw new ParserException("Expected MultipleReadsOperator, but got " + pattern);
    }

    <P extends Pattern> P getSpecificPattern() throws ParserException {
        try {
            return (P)getPattern();
        } catch (ClassCastException e) {
            throw new ParserException("Got pattern of unexpected class " + pattern + ": " + e);
        }
    }

    @Override
    public String toString() {
        return "Token{" + "isString=" + isString + ", string=" + string + ", pattern=" + pattern + ", length=" + length + "}";
    }
}
