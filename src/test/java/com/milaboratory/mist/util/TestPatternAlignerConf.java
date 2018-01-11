package com.milaboratory.mist.util;

import com.milaboratory.core.alignment.PatternAndTargetAlignmentScoring;
import com.milaboratory.mist.pattern.PatternAligner;

public final class TestPatternAlignerConf {
    public final long scoreThreshold;
    private final PatternAndTargetAlignmentScoring scoring;
    private final long singleOverlapPenalty;
    private final int bitapMaxErrors;
    private final int maxOverlap;

    public TestPatternAlignerConf(long scoreThreshold, PatternAndTargetAlignmentScoring scoring,
                                  long singleOverlapPenalty, int bitapMaxErrors, int maxOverlap) {
        this.scoreThreshold = scoreThreshold;
        this.scoring = scoring;
        this.singleOverlapPenalty = singleOverlapPenalty;
        this.bitapMaxErrors = bitapMaxErrors;
        this.maxOverlap = maxOverlap;
    }

    public void apply() {
        PatternAligner.allowValuesOverride();
        PatternAligner.init(scoring, singleOverlapPenalty, bitapMaxErrors, maxOverlap);
    }
}
