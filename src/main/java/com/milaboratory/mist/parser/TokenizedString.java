package com.milaboratory.mist.parser;

import com.milaboratory.mist.pattern.Pattern;

import java.util.ArrayList;
import java.util.LinkedList;

final class TokenizedString {
    private final LinkedList<Object> tokenizedString;

    /**
     * Tokenized string that allows to replace parts of string with patterns.
     *
     * @param query initial string
     */
    TokenizedString(String query) {
        this.tokenizedString = new LinkedList<>();
        this.tokenizedString.add(query);
    }

    LinkedList<Object> getTokenizedString() {
        return tokenizedString;
    }

    int getNumberOfStrings() {
        int numberOfStrings = 0;
        for (Object token : tokenizedString)
            if (token instanceof String) numberOfStrings++;
        return numberOfStrings;
    }

    ArrayList<String> getStrings() {
        ArrayList<String> strings = new ArrayList<>();
        for (Object token : tokenizedString)
            if (token instanceof String) strings.add((String)token);
        return strings;
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
    void tokenizeSubstring(Pattern pattern, int startIndex, int startIndexLastPosition,
                           int endIndex, int endIndexFirstPosition) {
        if ((startIndex >= tokenizedString.size()) || (startIndex < 0))
            throw new IndexOutOfBoundsException("startIndex = " + startIndex + ", tokenizedString size = " + tokenizedString.size());
        if ((endIndex >= tokenizedString.size()) || (endIndex < 0))
            throw new IndexOutOfBoundsException("endIndex = " + endIndex + ", tokenizedString size = " + tokenizedString.size());
        if (startIndex > endIndex)
            throw new IllegalArgumentException("startIndex = " + startIndex + ", endIndex = " + endIndex);

        if (startIndex == endIndex) {
            Object specifiedObject = tokenizedString.get(startIndex);
            if (!(specifiedObject instanceof String))
                throw new IllegalArgumentException("startIndex = endIndex = " + startIndex + " and this is not a string!");
            String specifiedString = (String)specifiedObject;

            if ((startIndexLastPosition == -1) && (endIndexFirstPosition == -1)) {
                TokenizedStringPattern tokenizedStringPattern = new TokenizedStringPattern(pattern, specifiedString.length());
                tokenizedString.set(startIndex, tokenizedStringPattern);
            } else if (endIndexFirstPosition > startIndexLastPosition) {
                TokenizedStringPattern tokenizedStringPattern  = new TokenizedStringPattern(pattern,
                        endIndexFirstPosition - startIndexLastPosition);

                String leftPart = specifiedString.substring(0, startIndexLastPosition);
                String rightPart = specifiedString.substring(endIndexFirstPosition);
                tokenizedString.remove(startIndex);
                if (rightPart.length() > 0)
                    tokenizedString.add(startIndex, rightPart);
                tokenizedString.add(startIndex, tokenizedStringPattern);
                if (leftPart.length() > 0)
                    tokenizedString.add(startIndex, leftPart);
            } else throw new IllegalArgumentException("startIndex = endIndex = " + startIndex
                    + ", startLast: " + startIndexLastPosition + ", endFirst: " + endIndexFirstPosition);
        } else {
            Object startObject = tokenizedString.get(startIndex);
            Object endObject = tokenizedString.get(endIndex);
            int deleteIndexFirst = startIndex + 1;
            int deleteIndexLast = endIndex;
            int patternLength = calculateLength(startIndex + 1, endIndex);

            if (startIndexLastPosition == -1)
                patternLength += calculateLength(startIndex, startIndex + 1);
            else {
                if (!(startObject instanceof String))
                    throw new IllegalArgumentException("startLast is not -1 and startIndex doesn't point to String!");
                if ((startIndexLastPosition > ((String)startObject).length()) || (startIndexLastPosition < 0))
                    throw new IndexOutOfBoundsException("startLast: " + startIndexLastPosition
                            + ", startString: " + startObject);
                patternLength += startIndexLastPosition;
            }

            if (endIndexFirstPosition == -1)
                patternLength += calculateLength(endIndex, endIndex + 1);
            else {
                if (!(endObject instanceof String))
                    throw new IllegalArgumentException("endFirst is not -1 and endIndex doesn't point to String!");
                if ((endIndexFirstPosition > ((String)endObject).length()) || (endIndexFirstPosition < 0))
                    throw new IndexOutOfBoundsException("endFirst: " + endIndexFirstPosition
                            + ", endString: " + endObject);
                patternLength += ((String)endObject).length() - endIndexFirstPosition;
            }

            TokenizedStringPattern tokenizedStringPattern = new TokenizedStringPattern(pattern, patternLength);
            tokenizedString.add(startIndex + 1, tokenizedStringPattern);

            if (startIndexLastPosition > 0) {
                tokenizedString.set(startIndex, ((String)startObject).substring(0, startIndexLastPosition));
                deleteIndexFirst++;
                deleteIndexLast++;
            } else tokenizedString.remove(startIndex);

            if ((endIndexFirstPosition != -1) && (endIndexFirstPosition != ((String)endObject).length())) {
                tokenizedString.set(endIndex, ((String)endObject).substring(endIndexFirstPosition));
                deleteIndexLast--;
            }

            for (int i = deleteIndexLast; i >= deleteIndexFirst; --i)
                tokenizedString.remove(i);
        }
    }

    /**
     * Calculate summary string length of the sequence of tokenizedString elements.
     *
     * @param start starting element, inclusive
     * @param end ending element, exclusive
     * @return string length
     */
    int calculateLength(int start, int end) {
        if (start > end) throw new IllegalArgumentException("start = " + start + ", end = " + end);
        if (start == end) return 0;
        int length = 0;

        for (Object currentObject : tokenizedString.subList(start, end)) {
            if (currentObject instanceof String)
                length += ((String)currentObject).length();
            else if (currentObject instanceof TokenizedStringPattern)
                length += ((TokenizedStringPattern)currentObject).length;
            else throw new IllegalStateException("TokenizedString contains object of class " + currentObject.getClass());
        }

        return length;
    }

    /**
     * Get left coordinate of the specified element of TokenizedString.
     *
     * @param index index of element in the list
     * @return left coordinate of the specified element, inclusive
     */
    int getLeftByIndex(int index) {
        return calculateLength(0, index);
    }

    /**
     * Get right coordinate of the specified element of TokenizedString.
     *
     * @param index index of element in the list
     * @return right coordinate of the specified element, exclusive
     */
    int getRightByIndex(int index) {
        return calculateLength(0, index + 1);
    }

    /**
     * Calculate element index in tokenizedString that contains the specified position in query string.
     *
     * @param position position in query string
     * @return index of element in tokenizedString
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
     * @return list of tokens that are inside the interval; types of tokens are String and Pattern
     */
    ArrayList<Object> getTokens(int from, int to) {
        ArrayList<Object> tokens = new ArrayList<>();
        int lastIndex = getIndexByPosition(to - 1);
        int currentIndex = getIndexByPosition(from);
        int currentLeft = getLeftByIndex(currentIndex);
        Object currentObject = tokenizedString.get(currentIndex);

        if (currentLeft == from)
            tokens.add(getStringOrPattern(currentObject));
        else if (currentObject instanceof String)
            if (currentIndex < lastIndex)
                tokens.add(((String)currentObject).substring(from - currentLeft));
            else
                tokens.add(((String)currentObject).substring(from - currentLeft, to - currentLeft));

        if (currentIndex < lastIndex) {
            currentIndex++;
            while (currentIndex < lastIndex)
                tokens.add(getStringOrPattern(tokenizedString.get(currentIndex++)));

            int lastRight = getRightByIndex(lastIndex);
            Object lastObject = tokenizedString.get(lastIndex);
            if (lastRight == to)
                tokens.add(getStringOrPattern(lastObject));
            else if (lastObject instanceof String)
                tokens.add(((String)lastObject).substring(0, to - getLeftByIndex(lastIndex)));
        }
        return tokens;
    }

    /**
     * Convert token from TokenizedString that is String or TokenizedStringPattern to String or Pattern.
     *
     * @param object token from TokenizedString
     * @return unchanged String or Pattern from TokenizedStringPattern
     */
    private Object getStringOrPattern(Object object) {
        if (object instanceof String)
            return object;
        else if (object instanceof TokenizedStringPattern)
            return ((TokenizedStringPattern)object).pattern;
        else throw new IllegalArgumentException("Called getStringOrPattern with object of class " + object.getClass());
    }
}
