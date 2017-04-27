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

public class FuzzyMatchPattern extends SinglePattern {
    private final NucleotideSequence patternSeq;
    private final Motif<NucleotideSequence> motif;
    private final Map<GroupEdge, Integer> groupEdges;
    private final int maxErrors;

    public FuzzyMatchPattern(NucleotideSequence patternSeq) {
        this(patternSeq, new HashMap<>());
    }

    public FuzzyMatchPattern(NucleotideSequence patternSeq, Map<GroupEdge, Integer> groupEdges) {
        this(patternSeq, groupEdges, 0);
    }

    public FuzzyMatchPattern(NucleotideSequence patternSeq, int maxErrors) {
        this(patternSeq, new HashMap<>(), maxErrors);
    }

    /**
     * Find match with possible insertions and deletions using bitap and aligner.
     *
     * @param patternSeq sequence to find in the target
     * @param groupEdges map of group edges and their positions
     * @param maxErrors maximum allowed number of substitutions, insertions and deletions
     */
    public FuzzyMatchPattern(NucleotideSequence patternSeq, Map<GroupEdge, Integer> groupEdges, int maxErrors) {
        this.patternSeq = patternSeq;
        this.motif = patternSeq.toMotif();
        this.groupEdges = groupEdges;
        this.maxErrors = maxErrors;

        int size = patternSeq.size();

        for (Map.Entry<GroupEdge, Integer> groupEdge : groupEdges.entrySet())
            if (groupEdge.getValue() > size)
                throw new IllegalStateException("Group edge " + groupEdge.getKey().getGroupName()
                        + (groupEdge.getKey().isStart() ? " start" : " end") + " (" + groupEdge.getValue()
                        + ") is outside of motif (motif size: " + size + ")");
    }

    @Override
    public ArrayList<GroupEdge> getGroupEdges() {
        return new ArrayList<>(groupEdges.keySet());
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
        final FuzzyMatchesSearch matchesSearch = new FuzzyMatchesSearch(patternSeq, motif, groupEdges, maxErrors,
                input, from, to, targetId);
        final MatchesOutputPort allMatchesByScore = new MatchesOutputPort(matchesSearch, true);
        final MatchesOutputPort allMatchesByCoordinate = new MatchesOutputPort(matchesSearch, false);

        return new SimpleMatchingResult(allMatchesByScore, allMatchesByCoordinate);
    }

    private final static class FuzzyMatchesSearch extends MatchesSearch {
        private final NucleotideSequence patternSeq;
        private final Motif<NucleotideSequence> motif;
        private final Map<GroupEdge, Integer> groupEdges;
        private final int maxErrors;
        private final NSequenceWithQuality input;
        private final int from;
        private final int to;
        private final byte targetId;

        FuzzyMatchesSearch(NucleotideSequence patternSeq, Motif<NucleotideSequence> motif, Map<GroupEdge, Integer> groupEdges,
                           int maxErrors, NSequenceWithQuality input, int from, int to, byte targetId) {
            this.patternSeq = patternSeq;
            this.motif = motif;
            this.groupEdges = groupEdges;
            this.maxErrors = maxErrors;
            this.input = input;
            this.from = from;
            this.to = to;
            this.targetId = targetId;
        }

        @Override
        protected void performSearch(boolean quickSearch) {
            BitapMatcher matcher = motif.getBitapPattern().substitutionAndIndelMatcherLast(maxErrors, input.getSequence(), from, to);
            int matchLastPosition;
            float bestScore = Float.NEGATIVE_INFINITY;

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
                    MatchedRange matchedRange = new MatchedRange(input, targetId, 0, foundRange);
                    ArrayList<MatchedItem> matchedItems = new ArrayList<MatchedItem>() {{ add(matchedRange); }};

                    for (Map.Entry<GroupEdge, Integer> groupEdge : groupEdges.entrySet()) {
                        MatchedGroupEdge matchedGroupEdge = new MatchedGroupEdge(input, targetId, 0,
                                groupEdge.getKey(), groupEdge.getValue() + foundRange.getLower());
                        matchedItems.add(matchedGroupEdge);
                    }

                    Match currentMatch = new Match(1, foundScore, matchedItems);
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
                    patternSeq, input.getSequence(), 0, patternSeq.size(), 0,
                    firstPostition, patternSeq.size() + addedLength, addedLength * 2, maxErrors);
        }
    }
}
