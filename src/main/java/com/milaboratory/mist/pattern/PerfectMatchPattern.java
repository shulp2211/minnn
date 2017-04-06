package com.milaboratory.mist.pattern;

import com.milaboratory.core.Range;
import com.milaboratory.core.motif.BitapMatcher;
import com.milaboratory.core.motif.Motif;
import com.milaboratory.core.sequence.NSequenceWithQuality;
import com.milaboratory.core.sequence.NucleotideSequence;

import java.util.HashMap;
import java.util.Map;

import static com.milaboratory.mist.pattern.Match.WHOLE_PATTERN_MATCH_GROUP_NAME_PREFIX;
import static com.milaboratory.mist.pattern.Match.COMMON_GROUP_NAME_PREFIX;

public class PerfectMatchPattern implements SinglePattern {
    private final Motif<NucleotideSequence> motif;
    private final Map<String, Range> groups;

    public PerfectMatchPattern(Motif<NucleotideSequence> motif) {
        this(motif, new HashMap<>());
    }

    /**
     * Find match without insertions or deletions using bitap algorithm.
     *
     * @param motif motif to find
     * @param groups map of group names and their ranges
     */
    public PerfectMatchPattern(Motif<NucleotideSequence> motif, Map<String, Range> groups) {
        this.motif = motif;
        this.groups = groups;

        for (Map.Entry<String, Range> group : groups.entrySet())
            if (group.getValue().getUpper() > motif.size())
                throw new IllegalStateException("Group " + group.getKey() + " (" + group.getValue()
                        + ") doesn't fit into motif length " + motif.size());
    }

    /**
     * Find perfect match with bitap.
     *
     * @param input    target sequence
     * @param from     starting point in target sequence (inclusive)
     * @param to       ending point in target sequence (exclusive)
     * @param targetId number of read where sequence is matched
     * @return matching result
     */
    @Override
    public MatchingResult match(NSequenceWithQuality input, int from, int to, byte targetId) {
        final PerfectMatchesSearch matchesSearch = new PerfectMatchesSearch(motif, groups, input, from, to, targetId);
        final MatchesOutputPort allMatchesByScore = new MatchesOutputPort(matchesSearch, true);
        final MatchesOutputPort allMatchesByCoordinate = new MatchesOutputPort(matchesSearch, false);

        return new SimpleMatchingResult(allMatchesByScore, allMatchesByCoordinate);
    }

    @Override
    public boolean areGroupsInside() {
        return groups.size() > 0;
    }

    private final class PerfectMatchesSearch extends MatchesSearch {
        private final Motif<NucleotideSequence> motif;
        private final Map<String, Range> groups;
        private final NSequenceWithQuality input;
        private final int from;
        private final int to;
        private final byte targetId;

        public PerfectMatchesSearch(Motif<NucleotideSequence> motif, Map<String, Range> groups, NSequenceWithQuality input, int from, int to, byte targetId) {
            this.motif = motif;
            this.groups = groups;
            this.input = input;
            this.from = from;
            this.to = to;
            this.targetId = targetId;
        }

        @Override
        protected void performSearch(boolean quickSearch) {
            BitapMatcher matcher = motif.getBitapPattern().exactMatcher(input.getSequence(), from, to);
            int currentMatchPosition;
            int bestScore = 0;

            do {
                currentMatchPosition = matcher.findNext();
                if (currentMatchPosition != -1) {
                    matchFound = true;
                    // for quick search stop on first found valid match
                    if (quickSearch) {
                        quickSearchPerformed = true;
                        return;
                    }
                    Range foundRange = new Range(currentMatchPosition, currentMatchPosition + motif.size(), false);
                    CaptureGroupMatch wholePatternMatch = new CaptureGroupMatch(input, targetId, foundRange);
                    Map<String, CaptureGroupMatch> groupMatchMap = new HashMap<String, CaptureGroupMatch>() {{
                        put(WHOLE_PATTERN_MATCH_GROUP_NAME_PREFIX + "0", wholePatternMatch);
                    }};

                    for (Map.Entry<String, Range> group : groups.entrySet()) {
                        CaptureGroupMatch currentGroupMatch = new CaptureGroupMatch(input, targetId, group.getValue().move(currentMatchPosition));
                        groupMatchMap.put(COMMON_GROUP_NAME_PREFIX + group.getKey(), currentGroupMatch);
                    }

                    // TODO: create scoring rules
                    int currentScore = 1;
                    Match currentMatch = new Match(1, currentScore, groupMatchMap);
                    allMatches.add(currentMatch);
                    if (currentScore > bestScore) {
                        bestMatch = currentMatch;
                        bestScore = currentScore;
                    }
                }
            } while (currentMatchPosition != -1);

            quickSearchPerformed = true;
            fullSearchPerformed = true;
        }
    }
}
