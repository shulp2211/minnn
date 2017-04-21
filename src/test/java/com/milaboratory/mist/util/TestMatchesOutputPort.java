package com.milaboratory.mist.util;

import cc.redberry.pipe.OutputPort;
import com.milaboratory.mist.pattern.Match;

public class TestMatchesOutputPort implements OutputPort<Match> {
    private final Match[] matches;
    private int matchIndex = 0;

    public TestMatchesOutputPort(Match... matches) {
        this.matches = matches;
    }

    @Override
    public Match take() {
        if (matchIndex >= matches.length)
            return null;
        else
            return matches[matchIndex++];
    }

    public TestMatchesOutputPort getCopy() {
        return new TestMatchesOutputPort(matches);
    }
}
