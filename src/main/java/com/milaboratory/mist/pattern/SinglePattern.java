package com.milaboratory.mist.pattern;

import com.milaboratory.core.sequence.MultiNSequenceWithQuality;
import com.milaboratory.core.sequence.NSequenceWithQuality;

public interface SinglePattern extends Pattern {
    @Override
    default MatchingResult match(MultiNSequenceWithQuality input) {
        if (!(input instanceof NSequenceWithQuality))
            throw new IllegalArgumentException("Supports only single NSequenceWithQuality.");
        return match((NSequenceWithQuality) input);
    }

    default MatchingResult match(NSequenceWithQuality input) {
        return match(input, (byte)1);
    }

    MatchingResult match(NSequenceWithQuality input, byte targetId);
}
