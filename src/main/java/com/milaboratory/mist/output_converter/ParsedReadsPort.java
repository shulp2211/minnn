package com.milaboratory.mist.output_converter;

import cc.redberry.pipe.OutputPort;
import com.milaboratory.mist.pattern.MatchingResult;

public class ParsedReadsPort implements OutputPort<ParsedRead> {
    private final MatchingResult matchingResult;

    public ParsedReadsPort(MatchingResult matchingResult) {
        this.matchingResult = matchingResult;
    }

    @Override
    public ParsedRead take() {
        return null;
    }
}
