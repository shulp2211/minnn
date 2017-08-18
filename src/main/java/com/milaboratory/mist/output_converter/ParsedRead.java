package com.milaboratory.mist.output_converter;

import com.milaboratory.core.io.sequence.SequenceRead;

import java.util.List;

public final class ParsedRead {
    private final SequenceRead originalRead;
    private final SequenceRead parsedRead;
    private final List<MatchedGroup> groups;

    public ParsedRead(SequenceRead originalRead, SequenceRead parsedRead, List<MatchedGroup> groups) {
        this.originalRead = originalRead;
        this.parsedRead = parsedRead;
        this.groups = groups;
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
}
