package com.milaboratory.mist.pattern;

import cc.redberry.pipe.OutputPort;

public class MatchUtils {
    public static long countMatches(MatchingResult matchingResult) {
        return countMatches(matchingResult, false);
    }

    public static long countMatches(MatchingResult matchingResult, boolean fair) {
        OutputPort<Match> port = matchingResult.getMatches(false, fair);
        long counter = 0;
        while (port.take() != null)
            counter++;
        return counter;
    }
}
