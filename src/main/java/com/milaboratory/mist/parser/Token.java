package com.milaboratory.mist.parser;

import com.milaboratory.mist.pattern.BorderFilterOperand;
import com.milaboratory.mist.pattern.MultipleReadsOperator;
import com.milaboratory.mist.pattern.Pattern;
import com.milaboratory.mist.pattern.SinglePattern;

final class Token {
    private final boolean isString;
    private final String string;
    private final Pattern pattern;
    private final int length;
    private final int startCoordinate;

    Token(String string, int startCoordinate) {
        this.isString = true;
        this.string = string;
        this.pattern = null;
        this.length = string.length();
        this.startCoordinate = startCoordinate;
    }

    Token(Pattern pattern, int length, int startCoordinate) {
        this.isString = false;
        this.string = null;
        this.pattern = pattern;
        this.length = length;
        this.startCoordinate = startCoordinate;
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

    int getStartCoordinate() {
        return startCoordinate;
    }

    String getSubstring(int start) {
        return getString().substring(start);
    }

    String getSubstring(int start, int end) {
        return getString().substring(start, end);
    }

    Token getSubstringToken(int start) {
        return new Token(getSubstring(start), startCoordinate + start);
    }

    Token getSubstringToken(int start, int end) {
        return new Token(getSubstring(start, end), startCoordinate + start);
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

    <P> P getSpecificPattern(Class<P> patternClass) throws ParserException {
        try {
            if (patternClass.isAssignableFrom(getPattern().getClass()))
                return (P)pattern;
            else throw new ParserException("Expected " + patternClass.getName() + ", but got " + pattern);
        } catch (ClassCastException e) {
            throw new IllegalStateException("Unexpected class cast exception with " + pattern + ": " + e);
        }
    }

    @Override
    public String toString() {
        return "Token{" + "isString=" + isString + ", string=" + string + ", pattern=" + pattern + ", length=" + length
                + ", startCoordinate=" + startCoordinate + "}";
    }
}
