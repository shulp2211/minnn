package com.milaboratory.mist.util;

import com.milaboratory.mist.pattern.*;

public final class TestConfiguredPattern {
    private final TestPatternAlignerConf testPatternAlignerConf;
    private final Pattern pattern;

    public TestConfiguredPattern(TestPatternAlignerConf testPatternAlignerConf, Pattern pattern) {
        this.testPatternAlignerConf = testPatternAlignerConf;
        this.pattern = pattern;
    }

    public void applyConf() {
        testPatternAlignerConf.apply();
    }

    public Pattern get() {
        return pattern;
    }

    public SinglePattern getSinglePattern() {
        return (SinglePattern)pattern;
    }
}
