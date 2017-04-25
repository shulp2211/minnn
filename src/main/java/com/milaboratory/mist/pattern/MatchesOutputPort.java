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
            if (sortedMatches[0].getNumberOfPatterns() == 1)
                Arrays.sort(sortedMatches, Comparator.comparingInt(match -> match.getRange().getLower()));
            else
                Arrays.sort(sortedMatches, Comparator.comparingInt(this::getMatchCoordinateWeight));
        }
        queue.addAll(Arrays.asList(sortedMatches));

        sortingPerformed = true;
    }

    /**
     * Get weight from match to sort multiple pattern matches by coordinate.
     * In multiple patterns, getMatchedRange() can return NullMatchedRange objects.
     *
     * @return weight: lower end of the range in the first non-null match
     */
    private int getMatchCoordinateWeight(Match match) {
        for (int i = 0; i < match.getNumberOfPatterns(); i++) {
            MatchedRange currentMatch = match.getMatchedRange(i);
            if (NullMatchedRange.class.isAssignableFrom(currentMatch.getClass())) continue;
            return currentMatch.getRange().getLower();
        }

        return 0;
    }
}
