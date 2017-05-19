package com.milaboratory.mist.parser;

final class BracketsPair {
    final BracketsType bracketsType;
    final int start;
    final int end;
    final int nestedLevel;

    /**
     * Pair of brackets.
     *
     * @param bracketsType parentheses, square brackets or braces
     * @param start opening bracket position in query
     * @param end closing bracket position in query
     * @param nestedLevel number of brackets inside which this one is placed; 0 is the top level
     */
    BracketsPair(BracketsType bracketsType, int start, int end, int nestedLevel) {
        this.bracketsType = bracketsType;
        this.start = start;
        this.end = end;
        this.nestedLevel = nestedLevel;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        BracketsPair that = (BracketsPair)o;

        return (start == that.start) && (end == that.end) && (nestedLevel == that.nestedLevel)
                && (bracketsType == that.bracketsType);
    }

    @Override
    public int hashCode() {
        int result = bracketsType.hashCode();
        result = 31 * result + start;
        result = 31 * result + end;
        result = 31 * result + nestedLevel;
        return result;
    }
}
