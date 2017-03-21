package com.milaboratory.mist.pattern;

import cc.redberry.pipe.OutputPort;

public interface MatchingResult {
    OutputPort<Match> getMatches();

    CaptureGroupMatch getBestMatch();

    long getMatchesNumber();
}
