package com.milaboratory.mist.parser;

final class QuotesPair extends CharPair {
    final QuotesType quotesType;

    QuotesPair(QuotesType quotesType, int start, int end) {
        super(start, end);
        this.quotesType = quotesType;
    }

    /**
     * Returns true if this quotes pair contains the specified position (inclusive on left and right), otherwise false.
     *
     * @param position position to test
     * @return is this quotes pair contain specified position (inclusive on left and right)
     */
    boolean contains(int position) {
        return (start <= position) && (end >= position);
    }
}
