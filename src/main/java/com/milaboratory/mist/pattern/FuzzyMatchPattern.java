package com.milaboratory.mist.pattern;

import cc.redberry.pipe.OutputPort;
import com.milaboratory.core.Range;
import com.milaboratory.core.alignment.Alignment;
import com.milaboratory.core.motif.BitapMatcherFilter;
import com.milaboratory.core.motif.BitapPattern;
import com.milaboratory.core.motif.Motif;
import com.milaboratory.core.sequence.NSequenceWithQuality;
import com.milaboratory.core.sequence.NucleotideSequence;

import java.util.*;

public final class FuzzyMatchPattern extends SinglePattern {
    private final NucleotideSequence patternSeq;
    private final Motif<NucleotideSequence> motif;
    private final ArrayList<GroupEdgePosition> groupEdgePositions;

    public FuzzyMatchPattern(PatternAligner patternAligner, NucleotideSequence patternSeq) {
        this(patternAligner, patternSeq, new ArrayList<>());
    }

    /**
     * Find match with possible insertions and deletions using bitap and aligner.
     *
     * @param patternAligner pattern aligner; it also provides information about scoring and pattern overlap limits
     * @param patternSeq sequence to find in the target
     * @param groupEdgePositions list of group edges and their positions
     */
    public FuzzyMatchPattern(PatternAligner patternAligner, NucleotideSequence patternSeq, ArrayList<GroupEdgePosition> groupEdgePositions) {
        super(patternAligner);
        this.patternSeq = patternSeq;
        this.motif = patternSeq.toMotif();
        this.groupEdgePositions = groupEdgePositions;

        int size = patternSeq.size();

        for (GroupEdgePosition groupEdgePosition : groupEdgePositions)
            if (groupEdgePosition.getPosition() > size)
                throw new IllegalArgumentException("Group edge " + groupEdgePosition.getGroupEdge().getGroupName()
                        + (groupEdgePosition.getGroupEdge().isStart() ? " start" : " end") + " ("
                        + groupEdgePosition.getPosition() + ") is outside of motif (motif size: " + size + ")");
    }

    @Override
    public String toString() {
        if (groupEdgePositions.size() > 0)
            return "FuzzyMatchPattern(" + patternSeq + ", " + groupEdgePositions + ")";
        else
            return "FuzzyMatchPattern(" + patternSeq + ")";
    }

    @Override
    public ArrayList<GroupEdge> getGroupEdges() {
        ArrayList<GroupEdge> groupEdges = new ArrayList<>();
        for (GroupEdgePosition groupEdgePosition : groupEdgePositions)
            groupEdges.add(groupEdgePosition.getGroupEdge());
        return groupEdges;
    }

    @Override
    public MatchingResult match(NSequenceWithQuality target, int from, int to, byte targetId) {
        return new FuzzyMatchingResult(patternAligner, patternSeq, motif, groupEdgePositions, target, from, to, targetId);
    }

    private static class FuzzyMatchingResult extends MatchingResult {
        private final PatternAligner patternAligner;
        private final NucleotideSequence patternSeq;
        private final Motif<NucleotideSequence> motif;
        private final ArrayList<GroupEdgePosition> groupEdgePositions;
        private final NSequenceWithQuality target;
        private final int from;
        private final int to;
        private final byte targetId;

        FuzzyMatchingResult(PatternAligner patternAligner, NucleotideSequence patternSeq,
                            Motif<NucleotideSequence> motif, ArrayList<GroupEdgePosition> groupEdgePositions,
                            NSequenceWithQuality target, int from, int to, byte targetId) {
            this.patternAligner = patternAligner;
            this.patternSeq = patternSeq;
            this.motif = motif;
            this.groupEdgePositions = groupEdgePositions;
            this.target = target;
            this.from = from;
            this.to = to;
            this.targetId = targetId;
        }

        @Override
        public OutputPort<Match> getMatches(boolean byScore, boolean fairSorting) {
            return new FuzzyMatchOutputPort(patternAligner, patternSeq, motif, groupEdgePositions,
                    target, from, to, targetId, byScore, fairSorting);
        }

        private static class FuzzyMatchOutputPort implements OutputPort<Match> {
            private final PatternAligner patternAligner;
            private final NucleotideSequence patternSeq;
            private final ArrayList<GroupEdgePosition> groupEdgePositions;
            private final int maxErrors;
            private final NSequenceWithQuality target;
            private final int from;
            private final int to;
            private final byte targetId;
            private final boolean byScore;
            private final boolean fairSorting;
            private final BitapPattern bitapPattern;

            private BitapMatcherFilter bitapMatcherFilter;

            /**
             * Data structures used only for fair sorting.
             */
            private Match[] allMatches;
            private HashSet<Range> uniqueRanges;
            private boolean sortingPerformed = false;
            private int takenValues = 0;

            /**
             * Used only in takeUnfairByScore(). Current number of bitap errors get matches with this number of errors.
             */
            private int currentNumBitapErrors = 0;

            /**
             * Used only in takeUnfairByScore(). Already returned positions saved to skip them when searching with
             * bigger number of errors.
             */
            private HashSet<Integer> alreadyReturnedPositions;

            FuzzyMatchOutputPort(PatternAligner patternAligner, NucleotideSequence patternSeq,
                                 Motif<NucleotideSequence> motif, ArrayList<GroupEdgePosition> groupEdgePositions,
                                 NSequenceWithQuality target, int from, int to, byte targetId,
                                 boolean byScore, boolean fairSorting) {
                this.patternAligner = patternAligner;
                this.patternSeq = patternSeq;
                this.groupEdgePositions = groupEdgePositions;
                this.maxErrors = patternAligner.bitapMaxErrors();
                this.target = target;
                this.from = from;
                this.to = to;
                this.targetId = targetId;
                this.byScore = byScore;
                this.fairSorting = fairSorting;
                this.bitapPattern = motif.getBitapPattern();
                if (!fairSorting && byScore) {
                    this.bitapMatcherFilter = new BitapMatcherFilter(bitapPattern.substitutionAndIndelMatcherLast(
                            0, target.getSequence(), from, to));
                    this.alreadyReturnedPositions = new HashSet<>();
                } else
                    this.bitapMatcherFilter = new BitapMatcherFilter(bitapPattern.substitutionAndIndelMatcherLast(
                            maxErrors, target.getSequence(), from, to));
                if (fairSorting)
                    uniqueRanges = new HashSet<>();
            }

            @Override
            public Match take() {
                Match match;
                if (fairSorting)
                    if (byScore) match = takeFairByScore();
                    else match = takeFairByCoordinate();
                else
                    if (byScore) match = takeUnfairByScore();
                    else match = takeUnfairByCoordinate();

                return match;
            }

            private Match takeUnfairByScore() {
                int position;

                do {
                    position = bitapMatcherFilter.findNext();
                    if (position == -1) {
                        if (currentNumBitapErrors == maxErrors)
                            return null;
                        currentNumBitapErrors++;
                        bitapMatcherFilter = new BitapMatcherFilter(bitapPattern.substitutionAndIndelMatcherLast(
                                currentNumBitapErrors, target.getSequence(), from, to));
                    } else {
                        if (alreadyReturnedPositions.contains(position))
                            position = -1;  // prevent loop from exiting
                        else
                            alreadyReturnedPositions.add(position);
                    }
                } while (position == -1);

                return generateMatch(patternAligner.align(patternSeq, target, position));
            }

            private Match takeUnfairByCoordinate() {
                int position = bitapMatcherFilter.findNext();
                if (position == -1)
                    return null;
                else
                    return generateMatch(patternAligner.align(patternSeq, target, position));
            }

            private Match takeFairByScore() {
                if (!sortingPerformed) {
                    fillAllMatchesForFairSorting();
                    Arrays.sort(allMatches, Comparator.comparingInt(Match::getScore).reversed());
                    sortingPerformed = true;
                }

                if (takenValues == allMatches.length) return null;
                return allMatches[takenValues++];
            }

            private Match takeFairByCoordinate() {
                if (!sortingPerformed) {
                    fillAllMatchesForFairSorting();
                    Arrays.sort(allMatches, Comparator.comparingInt(match -> match.getRange().getLower()));
                    sortingPerformed = true;
                }

                if (takenValues == allMatches.length) return null;
                return allMatches[takenValues++];
            }

            /**
             * Generate match from alignment.
             *
             * @param alignment alignment returned from getAlignment function
             * @return generated match
             */
            private Match generateMatch(Alignment<NucleotideSequence> alignment) {
                Range foundRange = alignment.getSequence2Range();
                int foundScore = (int)alignment.getScore();
                MatchedRange matchedRange = new MatchedRange(target, targetId, 0, foundRange);
                ArrayList<MatchedItem> matchedItems = new ArrayList<>();
                matchedItems.add(matchedRange);

                for (GroupEdgePosition groupEdgePosition : groupEdgePositions) {
                    int foundGroupEdgePosition = alignment.convertToSeq2Position(groupEdgePosition.getPosition());
                    if (foundGroupEdgePosition == -1)
                        if (groupEdgePosition.getPosition() < alignment.getSequence1Range().getLower())
                            foundGroupEdgePosition = foundRange.getLower();
                        else if (groupEdgePosition.getPosition() > alignment.getSequence1Range().getUpper())
                            foundGroupEdgePosition = foundRange.getUpper();
                        else
                            throw new IllegalStateException("Unexpected state when converting group edge positions: "
                                    + "Sequence1Range=" + alignment.getSequence1Range()
                                    + ", Sequence2Range=" + alignment.getSequence2Range()
                                    + ", GroupEdgePosition=" + groupEdgePosition.getPosition());
                    else if (foundGroupEdgePosition < 0)
                        foundGroupEdgePosition = -2 - foundGroupEdgePosition;
                    MatchedGroupEdge matchedGroupEdge = new MatchedGroupEdge(target, targetId, 0,
                            groupEdgePosition.getGroupEdge(), foundGroupEdgePosition);
                    matchedItems.add(matchedGroupEdge);
                }

                return new Match(1, foundScore, matchedItems);
            }

            /**
             * Fill allMatches array with all existing matches for fair sorting.
             */
            private void fillAllMatchesForFairSorting() {
                ArrayList<Match> allMatchesList = new ArrayList<>();
                Alignment<NucleotideSequence> alignment;
                int matchLastPosition;

                do {
                    matchLastPosition = bitapMatcherFilter.findNext();
                    if (matchLastPosition != -1) {
                        alignment = patternAligner.align(patternSeq, target, matchLastPosition);
                        if (!uniqueRanges.contains(alignment.getSequence2Range())) {
                            uniqueRanges.add(alignment.getSequence2Range());
                            allMatchesList.add(generateMatch(alignment));
                        }
                    }
                } while (matchLastPosition != -1);

                allMatches = new Match[allMatchesList.size()];
                allMatchesList.toArray(allMatches);
            }
        }
    }
}
