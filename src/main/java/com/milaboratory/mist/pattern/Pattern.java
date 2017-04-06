package com.milaboratory.mist.pattern;

import com.milaboratory.core.sequence.MultiNSequenceWithQuality;

public interface Pattern {
    MatchingResult match(MultiNSequenceWithQuality input);
    boolean areGroupsInside();
}
