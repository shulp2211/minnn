package com.milaboratory.mist.output_converter;

import com.milaboratory.core.io.sequence.MultiRead;

import java.util.List;

public final class ParsedRead {
    private final MultiRead read;
    private final List<MatchedGroup> groups;

    public ParsedRead(MultiRead read, List<MatchedGroup> groups) {
        this.read = read;
        this.groups = groups;
    }

    public MultiRead read() {
        return read;
    }

    public List<MatchedGroup> groups() {
        return groups;
    }
}
