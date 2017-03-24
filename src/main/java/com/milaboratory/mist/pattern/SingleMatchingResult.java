package com.milaboratory.mist.pattern;

import cc.redberry.pipe.OutputPort;

import java.util.PriorityQueue;

public class SingleMatchingResult implements MatchingResult {
    private final OutputPort<Match> matchOutputPort;
    private final Match bestMatch;

    public SingleMatchingResult(Match bestMatch) {
        this.matchOutputPort = new SingleMatchPort(bestMatch);
        this.bestMatch = bestMatch;
    }

    @Override
    public OutputPort<Match> getMatches(boolean byScore) {
        return matchOutputPort;
    }

    @Override
    public Match getBestMatch() {
        return bestMatch;
    }

    @Override
    public long getMatchesNumber() {
        if (bestMatch == null)
            return 0;
        else
            return 1;
    }

    private final class SingleMatchPort implements OutputPort<Match> {
        final PriorityQueue<Match> queue = new PriorityQueue<>();

        SingleMatchPort(Match bestMatch) {
            if (bestMatch != null)
                queue.add(bestMatch);
        }

        @Override
        public synchronized Match take() {
            if (queue.isEmpty())
                return null;

            return queue.poll();
        }
    }
}
