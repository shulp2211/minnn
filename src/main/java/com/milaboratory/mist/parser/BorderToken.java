package com.milaboratory.mist.parser;

final class BorderToken {
    final boolean leftBorder;
    final int numberOfRepeats;
    final int start;
    final int end;

    /**
     * Token for item like '<<<' or '>{8}' that represents cutting nucleotides on border of FuzzyMatchPattern.
     *
     * @param leftBorder true if this is left border ('<<<')
     * @param numberOfRepeats number of repeats of '<'/'>' or number in braces
     * @param start start of this token, inclusive
     * @param end end of this token, exclusive
     */
    BorderToken(boolean leftBorder, int numberOfRepeats, int start, int end) {
        this.leftBorder = leftBorder;
        this.numberOfRepeats = numberOfRepeats;
        this.start = start;
        this.end = end;
    }
}
