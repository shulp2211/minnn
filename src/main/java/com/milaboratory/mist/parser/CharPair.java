package com.milaboratory.mist.parser;

abstract class CharPair {
    final int start;
    final int end;

    CharPair(int start, int end) {
        if (start >= end)
            throw new IllegalArgumentException("CharPair: start=" + start + ", end=" + end);
        this.start = start;
        this.end = end;
    }

    /**
     * Returns true if this characters pair is inside the specified characters pair, otherwise false.
     *
     * @param outer other characters pair
     * @return is this characters pair inside specified characters pair
     */
    boolean inside(CharPair outer) {
        return (start > outer.start) && (end < outer.end);
    }

    /**
     * Returns true if this characters pair contains the specified characters pair inside, otherwise false.
     *
     * @param inner other characters pair
     * @return is this characters pair contain specified characters pair
     */
    boolean contains(CharPair inner) {
        return (start < inner.start) && (end > inner.end);
    }
}
