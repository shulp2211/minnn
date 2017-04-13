package com.milaboratory.mist.pattern;

import com.milaboratory.core.Range;
import com.milaboratory.core.alignment.Alignment;
import com.milaboratory.core.alignment.BandedLinearAligner;
import com.milaboratory.core.alignment.LinearGapAlignmentScoring;
import com.milaboratory.core.motif.BitapMatcher;
import com.milaboratory.core.motif.Motif;
import com.milaboratory.core.sequence.*;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import static com.milaboratory.mist.pattern.Match.WHOLE_PATTERN_MATCH_GROUP_NAME_PREFIX;
import static com.milaboratory.mist.pattern.Match.COMMON_GROUP_NAME_PREFIX;

public class FuzzyMatchPattern extends SinglePattern {
    private final NSequenceWithQuality patternSeq;
    private final Map<String, Range> groups;
    private final int maxErrors;

    public FuzzyMatchPattern(NSequenceWithQuality patternSeq) {
        this(patternSeq, new HashMap<>());
    }

    public FuzzyMatchPattern(NSequenceWithQuality patternSeq, Map<String, Range> groups) {
        this(patternSeq, groups, 0);
    }

    public FuzzyMatchPattern(NSequenceWithQuality patternSeq, int maxErrors) {
        this(patternSeq, new HashMap<>(), maxErrors);
    }

    /**
     * Find match with possible insertions and deletions using bitap and aligner.
     *
     * @param patternSeq sequence to find in the target
     * @param groups map of group names and their ranges
     * @param maxErrors maximum allowed number of substitutions, insertions and deletions
     */
    public FuzzyMatchPattern(NSequenceWithQuality patternSeq, Map<String, Range> groups, int maxErrors) {
        this.patternSeq = patternSeq;
        this.groups = groups;
        this.maxErrors = maxErrors;

        int size = patternSeq.getSequence().size();

        for (Map.Entry<String, Range> group : groups.entrySet())
            if (group.getValue().getUpper() > size)
                throw new IllegalStateException("Group " + group.getKey() + " (" + group.getValue()
                        + ") doesn't fit into motif length " + size);
    }

    @Override
    public ArrayList<String> getGroupNames() {
        return new ArrayList<>(groups.keySet());
    }

    /**
     * Get matching result.
     *
     * @param input    target sequence
     * @param from     starting point in target sequence (inclusive)
     * @param to       ending point in target sequence (exclusive)
     * @param targetId number of read where sequence is matched
     * @return matching result
     */
    @Override
    public MatchingResult match(NSequenceWithQuality input, int from, int to, byte targetId) {
        final FuzzyMatchesSearch matchesSearch = new FuzzyMatchesSearch(patternSeq, groups, maxErrors, input, from, to, targetId);
        final MatchesOutputPort allMatchesByScore = new MatchesOutputPort(matchesSearch, true);
        final MatchesOutputPort allMatchesByCoordinate = new MatchesOutputPort(matchesSearch, false);

        return new SimpleMatchingResult(allMatchesByScore, allMatchesByCoordinate);
    }

    private final class FuzzyMatchesSearch extends MatchesSearch {
        private final NSequenceWithQuality patternSeq;
        private final Map<String, Range> groups;
        private final int maxErrors;
        private final NSequenceWithQuality input;
        private final int from;
        private final int to;
        private final byte targetId;

        FuzzyMatchesSearch(NSequenceWithQuality patternSeq, Map<String, Range> groups, int maxErrors, NSequenceWithQuality input, int from, int to, byte targetId) {
            this.patternSeq = patternSeq;
            this.groups = groups;
            this.maxErrors = maxErrors;
            this.input = input;
            this.from = from;
            this.to = to;
            this.targetId = targetId;
        }

        @Override
        protected void performSearch(boolean quickSearch) {
            Motif<NucleotideSequence> motif = patternSeq.getSequence().toMotif();
            BitapMatcher matcher = motif.getBitapPattern().substitutionAndIndelMatcherLast(maxErrors, input.getSequence(), from, to);
            int matchLastPosition;
            float bestScore = -Float.MAX_VALUE;

            do {
                matchLastPosition = matcher.findNext();
                if (matchLastPosition != -1) {
                    matchFound = true;
                    // for quick search stop on first found valid match
                    if (quickSearch) {
                        quickSearchPerformed = true;
                        return;
                    }

                    Alignment<NucleotideSequence> alignment = getMatchWithAligner(matchLastPosition, maxErrors);
                    Range foundRange = alignment.getSequence2Range();
                    float foundScore = alignment.getScore();
                    CaptureGroupMatch wholePatternMatch = new CaptureGroupMatch(input, targetId, foundRange);
                    Map<String, CaptureGroupMatch> groupMatchMap = new HashMap<String, CaptureGroupMatch>() {{
                        put(WHOLE_PATTERN_MATCH_GROUP_NAME_PREFIX + "0", wholePatternMatch);
                    }};

                    for (Map.Entry<String, Range> group : groups.entrySet()) {
                        CaptureGroupMatch currentGroupMatch = new CaptureGroupMatch(input, targetId,
                                group.getValue().move(foundRange.getLower()));
                        groupMatchMap.put(COMMON_GROUP_NAME_PREFIX + group.getKey(), currentGroupMatch);
                    }

                    Match currentMatch = new Match(1, foundScore, groupMatchMap);
                    allMatches.add(currentMatch);
                    if (foundScore > bestScore) {
                        bestMatch = currentMatch;
                        bestScore = foundScore;
                    }
                }
            } while (matchLastPosition != -1);

            quickSearchPerformed = true;
            fullSearchPerformed = true;
        }

        /**
         * After searching with bitap, perform alignment for precise search.
         *
         * @param matchLastPosition match last letter position for aligner (inclusive)
         * @return best found alignment
         */
        private Alignment<NucleotideSequence> getMatchWithAligner(int matchLastPosition, int maxErrors) {
            int firstPostition = matchLastPosition + 1 - patternSeq.size() - maxErrors;
            int addedLength = maxErrors;    // number of nucleotides added to the left in target
            if (firstPostition < 0) {
                firstPostition = 0;
                addedLength = matchLastPosition - patternSeq.size() + 1;
            }
            return BandedLinearAligner.alignLeftAdded(LinearGapAlignmentScoring.getNucleotideBLASTScoring(),
                    patternSeq.getSequence(), input.getSequence(), 0, patternSeq.size(), 0,
                    firstPostition, patternSeq.size() + addedLength, addedLength * 2, maxErrors);
        }
    }
}
