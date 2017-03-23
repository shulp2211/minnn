package com.milaboratory.mist.pattern;

import com.milaboratory.core.Range;
import com.milaboratory.core.sequence.MultiNSequenceWithQuality;
import com.milaboratory.core.sequence.NSequenceWithQuality;

interface SinglePattern extends Pattern {
    @Override
    default MatchingResult match(MultiNSequenceWithQuality input) {
        if (!(input instanceof NSequenceWithQuality))
            throw new IllegalArgumentException("Supports only single NSequenceWithQuality.");
        return match((NSequenceWithQuality) input);
    }

    default MatchingResult match(NSequenceWithQuality input) {
        return match(input, 0, input.size(), (byte) 1);
    }

    default MatchingResult match(NSequenceWithQuality input, Range range) {
        if (range.isReverse())
            throw new IllegalArgumentException("Doesn't support reversed ranges.");
        return match(input, range.getFrom(), range.getTo());
    }

    default MatchingResult match(NSequenceWithQuality input, int from, int to) {
        return match(input, from, to, (byte) 1);
    }

    MatchingResult match(NSequenceWithQuality input, int from, int to, byte targetId);
}
