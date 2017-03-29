package com.milaboratory.mist.pattern;

import cc.redberry.pipe.OutputPort;

import java.util.Arrays;
import java.util.Comparator;
import java.util.LinkedList;
import java.util.Queue;

public class OperatorOutputPort implements OutputPort<Match> {
    private final Queue<Match> queue = new LinkedList<>();
    private final MatchesSearch matchesSearch;
    private final boolean byScore;
    private boolean searchPerformed = false;

    /**
     * Constructor for port with empty queue, used if quick check found that there are no matches.
     */
    public OperatorOutputPort() {
        this(null, true);
        searchPerformed = true;
    }

    public OperatorOutputPort(MatchesSearch matchesSearch, boolean byScore) {
        this.matchesSearch = matchesSearch;
        this.byScore = byScore;
    }

    @Override
    public synchronized Match take() {
        if (!searchPerformed) performSearch();
        if (queue.isEmpty()) return null;

        return queue.poll();
    }

    public long getMatchesNumber() {
        return matchesSearch.getMatchesNumber();
    }

    /**
     * Sort matching results and fill queue.
     */
    private void performSearch() {
        Match[] sortedMatches = matchesSearch.getAllMatches().clone();
        if (byScore)
            Arrays.sort(sortedMatches, Comparator.comparingInt(Match::getScore).reversed());
        else
            Arrays.sort(sortedMatches, Comparator.comparingInt(match -> match.getWholePatternMatch().getRange().getLower()));
        queue.addAll(Arrays.asList(sortedMatches));

        searchPerformed = true;
    }
}
