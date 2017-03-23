package com.milaboratory.mist.pattern;

import com.milaboratory.core.Range;
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

    default MatchingResult match(NSequenceWithQuality input, int from, int to, byte targetId) {
        return match(input, from, to, targetId, false);
    }

    /**
     * Search this pattern in target sequence
     *
     * @param input target sequence
     * @param from starting point in target sequence (inclusive)
     * @param to ending point in target sequence (exclusive)
     * @param targetId number of current block in multiple pattern match
     * @param quickMatch if true, match.isFound() returns true or false, other methods throw exception;
     *                   used for quick checking is pattern matching or not
     * @return matching result
     */
    MatchingResult match(NSequenceWithQuality input, int from, int to, byte targetId, boolean quickMatch);
}
