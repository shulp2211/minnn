package com.milaboratory.mist.parser;

import com.milaboratory.mist.pattern.Pattern;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.stream.Collectors;

final class TokenizedString {
    private final LinkedList<Token> tokenizedString;
    private final int length;

    /**
     * Tokenized string that allows to replace parts of string with patterns.
     *
     * @param query initial string
     */
    TokenizedString(String query) {
        this.tokenizedString = new LinkedList<>();
        this.tokenizedString.add(new Token(query));
        this.length = query.length();
    }

    /**
     * Wrapper for tokenizeSubstring that gets coordinates in string as parameters.
     *
     * @param pattern pattern to put into tokenized string
     * @param from start position to delete, inclusive
     * @param to end position to delete, exclusive
     */
    void tokenizeSubstring(Pattern pattern, int from, int to) {
        int startIndex = getIndexByPosition(from);
        int endIndex = getIndexByPosition(to - 1);
        int startIndexLastPosition;
        int endIndexFirstPosition;
        if ((from == getLeftByIndex(startIndex)) && !tokenizedString.get(startIndex).isString())
            startIndexLastPosition = -1;
        else
            if (tokenizedString.get(startIndex).isString())
                startIndexLastPosition = from - getLeftByIndex(startIndex);
            else throw new IllegalArgumentException("Trying to tokenize in the middle of "
                    + tokenizedString.get(startIndex));
        if ((to == getRightByIndex(endIndex)) && !tokenizedString.get(endIndex).isString())
            endIndexFirstPosition = -1;
        else
            if (tokenizedString.get(endIndex).isString())
                endIndexFirstPosition = to - getLeftByIndex(endIndex);
            else throw new IllegalArgumentException("Trying to tokenize in the middle of "
                    + tokenizedString.get(endIndex));

        tokenizeSubstring(pattern, startIndex, startIndexLastPosition, endIndex, endIndexFirstPosition);
    }

    /**
     * Replace a part of tokenized string with a TokenizedStringPattern that contains pattern and length of string
     * that it replaced.
     *
     * @param pattern pattern to put into tokenized string
     * @param startIndex start index to delete (fully or partially), inclusive
     * @param startIndexLastPosition -1 to delete fully; must be always -1 if startIndex points to Pattern;
     *                               if not -1, it is last position to preserve inside string, exclusive
     * @param endIndex end index to delete (fully or partially), inclusive
     * @param endIndexFirstPosition -1 to delete fully; must be always -1 if endIndex points to Pattern;
     *                              if not -1, it is first position to preserve inside string, inclusive
     */
    private void tokenizeSubstring(Pattern pattern, int startIndex, int startIndexLastPosition,
                           int endIndex, int endIndexFirstPosition) {
        if ((startIndex >= tokenizedString.size()) || (startIndex < 0))
            throw new IndexOutOfBoundsException("startIndex = " + startIndex + ", tokenizedString size = " + tokenizedString.size());
        if ((endIndex >= tokenizedString.size()) || (endIndex < 0))
            throw new IndexOutOfBoundsException("endIndex = " + endIndex + ", tokenizedString size = " + tokenizedString.size());
        if (startIndex > endIndex)
            throw new IllegalArgumentException("startIndex = " + startIndex + ", endIndex = " + endIndex);

        if (startIndex == endIndex) {
            Token specifiedToken = tokenizedString.get(startIndex);
            if (!specifiedToken.isString())
                throw new IllegalArgumentException("startIndex = endIndex = " + startIndex + " and this is not a string!");
            String specifiedString = specifiedToken.getString();

            if ((startIndexLastPosition == -1) && (endIndexFirstPosition == -1)) {
                Token patternToken = new Token(pattern, specifiedString.length());
                tokenizedString.set(startIndex, patternToken);
            } else if ((startIndexLastPosition >= 0) && (endIndexFirstPosition > startIndexLastPosition)) {
                Token patternToken = new Token(pattern, endIndexFirstPosition - startIndexLastPosition);

                String leftPart = specifiedString.substring(0, startIndexLastPosition);
                String rightPart = specifiedString.substring(endIndexFirstPosition);
                tokenizedString.remove(startIndex);
                if (rightPart.length() > 0)
                    tokenizedString.add(startIndex, new Token(rightPart));
                tokenizedString.add(startIndex, patternToken);
                if (leftPart.length() > 0)
                    tokenizedString.add(startIndex, new Token(leftPart));
            } else throw new IllegalArgumentException("startIndex = endIndex = " + startIndex
                    + ", startLast: " + startIndexLastPosition + ", endFirst: " + endIndexFirstPosition);
        } else {
            Token startToken = tokenizedString.get(startIndex);
            Token endToken = tokenizedString.get(endIndex);
            int deleteIndexFirst = startIndex + 1;
            int deleteIndexLast = endIndex;
            int patternLength = calculateLength(startIndex + 1, endIndex);

            if (startIndexLastPosition == -1)
                patternLength += calculateLength(startIndex, startIndex + 1);
            else {
                if (!startToken.isString())
                    throw new IllegalArgumentException("startLast is not -1 and startIndex doesn't point to String!");
                if ((startIndexLastPosition > startToken.getLength()) || (startIndexLastPosition < 0))
                    throw new IndexOutOfBoundsException("startLast: " + startIndexLastPosition
                            + ", startString: " + startToken.getString());
                patternLength += startToken.getLength() - startIndexLastPosition;
            }

            if (endIndexFirstPosition == -1)
                patternLength += calculateLength(endIndex, endIndex + 1);
            else {
                if (!endToken.isString())
                    throw new IllegalArgumentException("endFirst is not -1 and endIndex doesn't point to String!");
                if ((endIndexFirstPosition > endToken.getLength()) || (endIndexFirstPosition < 0))
                    throw new IndexOutOfBoundsException("endFirst: " + endIndexFirstPosition
                            + ", endString: " + endToken.getString());
                patternLength += endIndexFirstPosition;
            }

            Token patternToken = new Token(pattern, patternLength);
            tokenizedString.add(startIndex + 1, patternToken);

            if (startIndexLastPosition > 0) {
                tokenizedString.set(startIndex, startToken.getSubstringToken(0, startIndexLastPosition));
                deleteIndexFirst++;
                deleteIndexLast++;
            } else tokenizedString.remove(startIndex);

            if ((endIndexFirstPosition != -1) && (endIndexFirstPosition != endToken.getLength())) {
                tokenizedString.set(deleteIndexLast, endToken.getSubstringToken(endIndexFirstPosition));
                deleteIndexLast--;
            }

            for (int i = deleteIndexLast; i >= deleteIndexFirst; --i)
                tokenizedString.remove(i);
        }

        assertLengthNotChanged();
    }

    /**
     * Calculate summary string length of the sequence of tokens.
     *
     * @param start starting element, inclusive
     * @param end ending element, exclusive
     * @return string length
     */
    int calculateLength(int start, int end) {
        if (start > end) throw new IllegalArgumentException("start = " + start + ", end = " + end);
        if (start == end) return 0;

        return tokenizedString.subList(start, end).stream().mapToInt(Token::getLength).sum();
    }

    /**
     * Get left coordinate of the specified token.
     *
     * @param index index of token in the list
     * @return left coordinate of the specified token, inclusive
     */
    int getLeftByIndex(int index) {
        return calculateLength(0, index);
    }

    /**
     * Get right coordinate of the specified token.
     *
     * @param index index of token in the list
     * @return right coordinate of the specified token, exclusive
     */
    int getRightByIndex(int index) {
        return calculateLength(0, index + 1);
    }

    /**
     * Calculate token index in tokenizedString that contains the specified position in query string.
     *
     * @param position position in query string
     * @return index of token in tokenizedString
     */
    int getIndexByPosition(int position) {
        int currentPosition = 0;

        for (int currentIndex = 0; currentIndex < tokenizedString.size(); currentIndex++) {
            currentPosition += calculateLength(currentIndex, currentIndex + 1);
            if (currentPosition > position)
                return currentIndex;
        }

        throw new IllegalStateException("Reached the end of tokenizedString and didn't get index for position " + position);
    }

    /**
     * Get a part of TokenizedString by specifying the coordinates interval. If a token is partially in the interval,
     * Pattern will not be included, and String will be cut and returned partially.
     *
     * @param from left side, inclusive
     * @param to right side, exclusive
     * @return list of tokens that are inside the interval
     */
    ArrayList<Token> getTokens(int from, int to) {
        ArrayList<Token> tokens = new ArrayList<>();
        int lastIndex = getIndexByPosition(to - 1);
        int currentIndex = getIndexByPosition(from);
        int currentLeft = getLeftByIndex(currentIndex);
        Token currentToken = tokenizedString.get(currentIndex);

        if (currentLeft == from)
            tokens.add(currentToken);
        else if (currentToken.isString())
            if (currentIndex < lastIndex)
                tokens.add(currentToken.getSubstringToken(from - currentLeft));
            else
                tokens.add(currentToken.getSubstringToken(from - currentLeft, to - currentLeft));

        if (currentIndex < lastIndex) {
            currentIndex++;
            while (currentIndex < lastIndex)
                tokens.add(tokenizedString.get(currentIndex++));

            int lastRight = getRightByIndex(lastIndex);
            Token lastToken = tokenizedString.get(lastIndex);
            if (lastRight == to)
                tokens.add(lastToken);
            else if (lastToken.isString())
                tokens.add(lastToken.getSubstringToken(0, to - getLeftByIndex(lastIndex)));
        }
        return tokens;
    }

    /**
     * Convert this TokenizedString to String; must be used only before tokenizing anything.
     *
     * @return full query string
     */
    String getOneString() {
        assertNotTokenized();
        return tokenizedString.get(0).getString();
    }

    /**
     * Return a part of TokenizedString that must be one String, or throw exception if this is not one String.
     *
     * @param from left coordinate, inclusive
     * @param to right coordinate, exclusive
     * @return part of TokenizedString as String
     */
    String getOneString(int from, int to) {
        ArrayList<Token> tokenizedStringPart = getTokens(from, to);
        if ((tokenizedStringPart.size() != 1) || !tokenizedStringPart.get(0).isString())
            throw new IllegalStateException("Expected only 1 string in tokenizedStringPart, got this: "
                    + tokenizedStringPart);
        return tokenizedStringPart.get(0).getString();
    }

    /**
     * Get final pattern when string is fully tokenized.
     *
     * @return final pattern from fully tokenized string
     * @throws ParserException if string was not fully tokenized
     */
    Pattern getFinalPattern() throws ParserException {
        assertFullyTokenized();
        return tokenizedString.get(0).getPattern();
    }

    private void assertNotTokenized() {
        if ((tokenizedString.size() != 1) || !tokenizedString.get(0).isString())
            throw new IllegalStateException("Expected to find one string, found: " + tokenizedString);
    }

    private void assertFullyTokenized() throws ParserException {
        List<Token> strings = tokenizedString.stream().filter(Token::isString).collect(Collectors.toList());
        if (strings.size() > 0)
            throw new ParserException("Some tokens not parsed: " + strings);
        if (tokenizedString.size() > 1)
            throw new ParserException("After parsing, string contains separate patterns instead of 1 final pattern!");
        if (tokenizedString.size() < 1)
            throw new IllegalStateException("After parsing, tokenizedString size is " + tokenizedString.size());
    }

    private void assertLengthNotChanged() {
        if (calculateLength(0, tokenizedString.size()) != length)
            throw new IllegalStateException("Changed length: old " + length + ", new "
                    + calculateLength(0, tokenizedString.size()));
    }
}
