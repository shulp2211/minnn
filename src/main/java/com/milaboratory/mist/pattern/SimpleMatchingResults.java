package com.milaboratory.mist.pattern;

import cc.redberry.pipe.OutputPort;

import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.PriorityQueue;

public class SimpleMatchingResults implements MatchingResult {
    private final Match[] matches;

    public SimpleMatchingResults(Match... matches) {
        this.matches = matches;
    }

    @Override
    public OutputPort<Match> getMatches(boolean byScore) {
        return new SimpleMatchingResults.MatchesPort(byScore, matches);
    }

    @Override
    public Match getBestMatch() {
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
        final PriorityQueue<Match> queue = new PriorityQueue<>();

        MatchesPort(boolean byScore, Match... matches) {
            if (byScore)
                Arrays.sort(matches, Comparator.comparingInt(Match::getScore).reversed());
            else {
                final Comparator<Match> MatchCoordinateComparator = new Comparator<Match>() {
                    public int compare(Match match1, Match match2) {
                        Integer coordinate1 = match1.getWholePatternMatch().getRange().getLower();
                        Integer coordinate2 = match2.getWholePatternMatch().getRange().getLower();
                        return coordinate1.compareTo(coordinate2);
                    }
                };
                Arrays.sort(matches, MatchCoordinateComparator);
            }
            Collections.addAll(queue, matches);
        }

        @Override
        public synchronized Match take() {
            if (queue.isEmpty())
                return null;

            return queue.poll();
        }
    }
}
