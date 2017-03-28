package com.milaboratory.mist.pattern;

import cc.redberry.pipe.OutputPort;

import java.util.*;

public class SimpleMatchingResult implements MatchingResult {
    private final Match[] matches;

    public SimpleMatchingResult(Match... matches) {
        this.matches = matches;
    }

    @Override
    public OutputPort<Match> getMatches(boolean byScore) {
        return new SimpleMatchingResult.MatchesPort(byScore, matches);
    }

    @Override
    public Match getBestMatch() {
        if (getMatchesNumber() == 0) return null;
        Match bestMatch = matches[0];
        int bestScore = matches[0].getScore();
        for (int i = 0; i < getMatchesNumber(); i++)
            if (matches[i].getScore() > bestScore) {
                bestMatch = matches[i];
                bestScore = bestMatch.getScore();
        }
        return bestMatch;
    }

    @Override
    public long getMatchesNumber() {
        return matches.length;
    }

    private final class MatchesPort implements OutputPort<Match> {
        final Queue<Match> queue = new LinkedList<>();

        MatchesPort(boolean byScore, Match... matches) {
            Match[] sortedMatches = matches.clone();
            if (byScore)
                Arrays.sort(sortedMatches, Comparator.comparingInt(Match::getScore).reversed());
            else
                Arrays.sort(sortedMatches, Comparator.comparingInt(match -> match.getWholePatternMatch().getRange().getLower()));
            queue.addAll(Arrays.asList(sortedMatches));
        }

        @Override
        public synchronized Match take() {
            if (queue.isEmpty())
                return null;

            return queue.poll();
        }
    }
}
