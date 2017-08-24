package com.milaboratory.mist.output_converter;

import com.milaboratory.core.io.sequence.SequenceRead;

import java.util.ArrayList;
import java.util.List;

public final class ParsedRead {
    private final SequenceRead originalRead;
    private final SequenceRead parsedRead;
    private final List<MatchedGroup> groups;
    private final boolean reverseMatch;
    private final long bestMatchScore;

    public ParsedRead(SequenceRead originalRead) {
        this(originalRead, null, new ArrayList<>(), false, Long.MIN_VALUE);
    }

    public ParsedRead(SequenceRead originalRead, SequenceRead parsedRead, List<MatchedGroup> groups,
                      boolean reverseMatch, long bestMatchScore) {
        this.originalRead = originalRead;
        this.parsedRead = parsedRead;
        this.groups = groups;
        this.reverseMatch = reverseMatch;
        this.bestMatchScore = bestMatchScore;
    }

    public SequenceRead getOriginalRead() {
        return originalRead;
    }

    public SequenceRead getParsedRead() {
        return parsedRead;
    }

    public List<MatchedGroup> getGroups() {
        return groups;
    }

    public boolean isReverseMatch() {
        return reverseMatch;
    }

    public long getBestMatchScore() {
        return bestMatchScore;
    }
}
