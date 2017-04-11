package com.milaboratory.mist.pattern;

import cc.redberry.pipe.OutputPort;

import java.util.Arrays;
import java.util.Comparator;
import java.util.LinkedList;
import java.util.Queue;

public class MatchesOutputPort implements OutputPort<Match> {
    private final Queue<Match> queue = new LinkedList<>();
    private final MatchesSearch matchesSearch;
    private final boolean byScore;
    private boolean sortingPerformed = false;

    /**
     * Constructor for port with empty queue, used if quick check found that there are no matches.
     */
    public MatchesOutputPort() {
        this(null, true);
        sortingPerformed = true;
    }

    public MatchesOutputPort(MatchesSearch matchesSearch, boolean byScore) {
        this.matchesSearch = matchesSearch;
        this.byScore = byScore;
    }

    @Override
    public synchronized Match take() {
        if (!sortingPerformed) performSorting();
        if (queue.isEmpty()) return null;

        return queue.poll();
    }

    public Match getBestMatch() {
        if (matchesSearch == null) return null;
        return matchesSearch.getBestMatch();
    }

    public long getMatchesNumber() {
        if (matchesSearch == null) return 0;
        return matchesSearch.getMatchesNumber();
    }

    public boolean isFound() {
        if (matchesSearch == null) return false;
        return matchesSearch.isFound();
    }

    /**
     * Sort matching results and fill queue.
     */
    private void performSorting() {
        Match[] sortedMatches = matchesSearch.getAllMatches().clone();
        if (byScore)
            Arrays.sort(sortedMatches, Comparator.comparingDouble(Match::getScore).reversed());
        else {
            /* sorting by coordinate is only applicable for single pattern matches because
               in multiple pattern matches getWholePatternMatch(x) can be null */
            if (sortedMatches[0].getNumberOfPatterns() == 1)
                Arrays.sort(sortedMatches, Comparator.comparingInt(match -> match.getWholePatternMatch().getRange().getLower()));
        }
        queue.addAll(Arrays.asList(sortedMatches));

        sortingPerformed = true;
    }
}
