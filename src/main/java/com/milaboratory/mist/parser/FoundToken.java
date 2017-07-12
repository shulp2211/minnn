package com.milaboratory.mist.parser;

import com.milaboratory.mist.pattern.Pattern;

final class FoundToken {
    final Pattern pattern;
    final int from;
    final int to;

    /**
     * FoundToken is found pattern to pass to tokenizeSubstring() function.
     *
     * @param pattern found pattern
     * @param from start of found pattern in query string, inclusive
     * @param to end of found pattern in query, exclusive
     */
    FoundToken(Pattern pattern, int from, int to) {
        this.pattern = pattern;
        this.from = from;
        this.to = to;
    }
}
