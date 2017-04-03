package com.milaboratory.mist.pattern;

public interface MatchesSearch {
    Match[] getAllMatches();
    Match getBestMatch();
    long getMatchesNumber();
    boolean isFound();
}
