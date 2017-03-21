package com.milaboratory.mist.pattern;

import com.milaboratory.core.sequence.MultiNSequenceWithQuality;
import com.milaboratory.core.sequence.NSequenceWithQuality;

public interface Pattern {
    MatchingResult match(MultiNSequenceWithQuality input);
}
