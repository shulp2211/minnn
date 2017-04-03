package com.milaboratory.mist.pattern;

import com.milaboratory.core.Range;
import com.milaboratory.core.motif.BitapMatcher;
import com.milaboratory.core.motif.Motif;
import com.milaboratory.core.sequence.NSequenceWithQuality;
import com.milaboratory.core.sequence.NucleotideSequence;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import static com.milaboratory.mist.pattern.Match.WHOLE_PATTERN_MATCH_GROUP_NAME_PREFIX;

public class PerfectMatchPattern implements SinglePattern {
    private final Motif<NucleotideSequence> motif;

    public PerfectMatchPattern(Motif<NucleotideSequence> motif) {
        this.motif = motif;
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
        final PerfectMatchesSearch matchesSearch = new PerfectMatchesSearch(motif, input, from, to, targetId);
        final MatchesOutputPort allMatchesByScore = new MatchesOutputPort(matchesSearch, true);
        final MatchesOutputPort allMatchesByCoordinate = new MatchesOutputPort(matchesSearch, false);

        return new SimpleMatchingResult(allMatchesByScore, allMatchesByCoordinate);
    }

    private final class PerfectMatchesSearch implements MatchesSearch {
        private final Motif<NucleotideSequence> motif;
        private final NSequenceWithQuality input;
        private final int from;
        private final int to;
        private final byte targetId;
        ArrayList<Match> allMatches = new ArrayList<>();
        private Match bestMatch = null;
        private boolean quickSearchPerformed = false;
        private boolean matchFound = false;
        private boolean fullSearchPerformed = false;

        public PerfectMatchesSearch(Motif<NucleotideSequence> motif, NSequenceWithQuality input, int from, int to, byte targetId) {
            this.motif = motif;
            this.input = input;
            this.from = from;
            this.to = to;
            this.targetId = targetId;
        }

        @Override
        public Match[] getAllMatches() {
            if (!fullSearchPerformed) performSearch(false);
            return allMatches.toArray(new Match[allMatches.size()]);
        }

        @Override
        public Match getBestMatch() {
            if (!fullSearchPerformed) performSearch(false);
            return bestMatch;
        }

        @Override
        public long getMatchesNumber() {
            if (!fullSearchPerformed) performSearch(false);
            return allMatches.size();
        }

        @Override
        public boolean isFound() {
            if (!quickSearchPerformed) performSearch(true);
            return matchFound;
        }

        /**
         * Find all matches and best match, calculate matches number.
         */
        private void performSearch(boolean quickSearch) {
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
