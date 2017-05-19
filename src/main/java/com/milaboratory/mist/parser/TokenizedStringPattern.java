package com.milaboratory.mist.parser;

import com.milaboratory.mist.pattern.Pattern;

final class TokenizedStringPattern {
    final Pattern pattern;
    final int length;

    TokenizedStringPattern(Pattern pattern, int length) {
        this.pattern = pattern;
        this.length = length;
    }
}
