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
import java.util.stream.Collectors;

import static com.milaboratory.mist.pattern.PatternUtils.*;

public final class FuzzyMatchPattern extends SinglePattern {
    private final ArrayList<NucleotideSequence> sequences;
    private final ArrayList<Motif<NucleotideSequence>> motifs;
    private final int leftCut;
    private final int rightCut;
    private final int fixedLeftBorder;
    private final int fixedRightBorder;
    private final List<GroupEdgePosition> groupEdgePositions;
    private final ArrayList<Integer> groupMovements;

    public FuzzyMatchPattern(PatternAligner patternAligner, NucleotideSequence patternSeq) {
        this(patternAligner, patternSeq, new ArrayList<>());
    }

    public FuzzyMatchPattern(PatternAligner patternAligner, NucleotideSequence patternSeq, int leftCut, int rightCut,
                int fixedLeftBorder, int fixedRightBorder) {
        this(patternAligner, patternSeq, leftCut, rightCut, fixedLeftBorder, fixedRightBorder, new ArrayList<>());
    }

    public FuzzyMatchPattern(PatternAligner patternAligner, NucleotideSequence patternSeq,
                List<GroupEdgePosition> groupEdgePositions) {
        this(patternAligner, patternSeq, 0, 0, -1, -1, groupEdgePositions);
    }

    /**
     * Find match with possible insertions and deletions using bitap and aligner. If fixedLeftBorder or fixedRightBorder
     * is specified, find only matches near that border.
     *
     * @param patternAligner pattern aligner; it also provides information about maxErrors for bitap
     * @param patternSeq sequence to find in the target
     * @param leftCut number of nucleotides that can be cut on the left without penalty
     * @param rightCut number of nucleotides that can be cut on the right without penalty
     * @param fixedLeftBorder position in target where must be the left border; -1 if there is no fixed left border
     *                        -2 - x means coordinate from the end of target; fixedLeftBorder is inclusive
     * @param fixedRightBorder position in target where must be the right border; -1 if there is no fixed right border
     *                         -2 - x means coordinate from the end of target; fixedRightBorder is inclusive
     * @param groupEdgePositions list of group edges and their positions
     */
    public FuzzyMatchPattern(PatternAligner patternAligner, NucleotideSequence patternSeq, int leftCut, int rightCut,
                int fixedLeftBorder, int fixedRightBorder, List<GroupEdgePosition> groupEdgePositions) {
        super(patternAligner);
        int size = patternSeq.size();
        if (leftCut + rightCut >= size)
            throw new IllegalArgumentException("Wrong arguments: leftCut=" + leftCut + ", rightCut=" + rightCut
                    + ", patternSeq=" + patternSeq);
        else {
            this.leftCut = leftCut;
            this.rightCut = rightCut;
        }
        this.sequences = new ArrayList<>();
        this.motifs = new ArrayList<>();
        this.groupMovements = new ArrayList<>();
        if ((leftCut == 0) && (rightCut == 0)) {
            sequences.add(patternSeq);
            motifs.add(patternSeq.toMotif());
            groupMovements.add(0);
        } else
            for (int i = 0; i <= leftCut; i++)
                for (int j = 0; j <= rightCut; j++) {
                    NucleotideSequence seq = new NucleotideSequence(patternSeq.toString().substring(i, size - j));
                    sequences.add(seq);
                    motifs.add(seq.toMotif());
                    groupMovements.add(-i);
                }
        this.fixedLeftBorder = fixedLeftBorder;
        this.fixedRightBorder = fixedRightBorder;
        this.groupEdgePositions = groupEdgePositions;

        for (GroupEdgePosition groupEdgePosition : groupEdgePositions)
            if (groupEdgePosition.getPosition() > size)
                throw new IllegalArgumentException("Group edge " + groupEdgePosition.getGroupEdge().getGroupName()
                        + (groupEdgePosition.getGroupEdge().isStart() ? " start" : " end") + " ("
                        + groupEdgePosition.getPosition() + ") is outside of motif (motif size: " + size + ")");
    }

    @Override
    public String toString() {
        if (groupEdgePositions.size() > 0)
            return "FuzzyMatchPattern(" + sequences.get(0) + ", " + leftCut + ", " + rightCut + ", "
                    + fixedLeftBorder + ", " + fixedRightBorder + ", " + groupEdgePositions + ")";
        else
            return "FuzzyMatchPattern(" + sequences.get(0) + ", " + leftCut + ", " + rightCut + ", "
                    + fixedLeftBorder + ", " + fixedRightBorder + ")";
    }

    @Override
    public ArrayList<GroupEdge> getGroupEdges() {
        return groupEdgePositions.stream().map(GroupEdgePosition::getGroupEdge)
                .collect(Collectors.toCollection(ArrayList::new));
    }

    @Override
    public MatchingResult match(NSequenceWithQuality target, int from, int to, byte targetId) {
        int fixedLeftBorder = (this.fixedLeftBorder > -2) ? this.fixedLeftBorder
                : target.size() - 1 - invertCoordinate(this.fixedLeftBorder);
        int fixedRightBorder = (this.fixedRightBorder > -2) ? this.fixedRightBorder
                : target.size() - 1 - invertCoordinate(this.fixedRightBorder);
        int fromWithBorder = (fixedLeftBorder == -1) ? from : Math.max(from, fixedLeftBorder);
        // to is exclusive and fixedRightBorder is inclusive
        int toWithBorder = (fixedRightBorder == -1) ? to : Math.min(to, fixedRightBorder + 1);
        return new FuzzyMatchingResult(patternAligner, sequences, motifs, fixedLeftBorder, fixedRightBorder,
                groupEdgePositions, groupMovements, target, fromWithBorder, toWithBorder, targetId);
    }

    private static class FuzzyMatchingResult extends MatchingResult {
        private final PatternAligner patternAligner;
        private final ArrayList<NucleotideSequence> sequences;
        private final ArrayList<Motif<NucleotideSequence>> motifs;
        private final int fixedLeftBorder;
        private final int fixedRightBorder;
        private final List<GroupEdgePosition> groupEdgePositions;
        private final ArrayList<Integer> groupMovements;
        private final NSequenceWithQuality target;
        private final int from;
        private final int to;
        private final byte targetId;

        FuzzyMatchingResult(PatternAligner patternAligner, ArrayList<NucleotideSequence> sequences,
                            ArrayList<Motif<NucleotideSequence>> motifs, int fixedLeftBorder, int fixedRightBorder,
                            List<GroupEdgePosition> groupEdgePositions, ArrayList<Integer> groupMovements,
                            NSequenceWithQuality target, int from, int to, byte targetId) {
            this.patternAligner = patternAligner;
            this.sequences = sequences;
            this.motifs = motifs;
            this.fixedLeftBorder = fixedLeftBorder;
            this.fixedRightBorder = fixedRightBorder;
            this.groupEdgePositions = groupEdgePositions;
            this.groupMovements = groupMovements;
            this.target = target;
            this.from = from;
            this.to = to;
            this.targetId = targetId;
        }

        @Override
        public OutputPort<Match> getMatches(boolean byScore, boolean fairSorting) {
            return new FuzzyMatchOutputPort(patternAligner, sequences, motifs, fixedLeftBorder, fixedRightBorder,
                    groupEdgePositions, groupMovements, target, from, to, targetId, byScore, fairSorting);
        }

        private static class FuzzyMatchOutputPort implements OutputPort<Match> {
            private final PatternAligner patternAligner;
            private final ArrayList<NucleotideSequence> sequences;
            private final int fixedLeftBorder;
            private final int fixedRightBorder;
            private final boolean fixedBorder;
            private final List<GroupEdgePosition> groupEdgePositions;
            private final ArrayList<Integer> groupMovements;
            private final int maxErrors;
            private final NSequenceWithQuality target;
            private final int from;
            private final int to;
            private final byte targetId;
            private final boolean byScore;
            private final boolean fairSorting;
            private final List<BitapPattern> bitapPatterns;
            private final List<BitapMatcherFilter> bitapMatcherFilters;

            /* Current index in lists of sequences and bitap patterns.
             * Index represents combination of numbers of cut nucleotides on the left and right sides. */
            private int currentIndex = 0;

            // Data structures used for fair sorting and for matching in fixed position.
            private Match[] allMatches;
            private boolean sortingPerformed = false;
            private int takenValues = 0;

            // Used only in takeUnfairByScore(). Current number of bitap errors get matches with this number of errors.
            private int currentNumBitapErrors = 0;

            /* Used only in takeUnfairByScore(). Already returned positions saved to skip them when searching with
             * bigger number of errors. Separate HashSet for each pattern index. */
            private ArrayList<HashSet<Integer>> alreadyReturnedPositions;

            FuzzyMatchOutputPort(PatternAligner patternAligner, ArrayList<NucleotideSequence> sequences,
                                 ArrayList<Motif<NucleotideSequence>> motifs, int fixedLeftBorder, int fixedRightBorder,
                                 List<GroupEdgePosition> groupEdgePositions, ArrayList<Integer> groupMovements,
                                 NSequenceWithQuality target, int from, int to, byte targetId,
                                 boolean byScore, boolean fairSorting) {
                this.patternAligner = patternAligner;
                this.sequences = sequences;
                this.fixedLeftBorder = fixedLeftBorder;
                this.fixedRightBorder = fixedRightBorder;
                this.fixedBorder = (fixedLeftBorder != -1) || (fixedRightBorder != -1);
                this.groupEdgePositions = groupEdgePositions;
                this.groupMovements = groupMovements;
                this.maxErrors = patternAligner.bitapMaxErrors();
                this.target = target;
                this.from = from;
                this.to = to;
                this.targetId = targetId;
                this.byScore = byScore;
                this.fairSorting = fairSorting;
                this.bitapPatterns = motifs.stream().map(Motif::getBitapPattern).collect(Collectors.toList());
                if (!fixedBorder && !fairSorting && byScore) {
                    this.bitapMatcherFilters = bitapPatterns.stream().map(bp -> new BitapMatcherFilter(
                            bp.substitutionAndIndelMatcherLast(0, target.getSequence(), from, to)))
                            .collect(Collectors.toList());
                    this.alreadyReturnedPositions = new ArrayList<>(Collections.nCopies(sequences.size(),
                            new HashSet<>()));
                } else
                    this.bitapMatcherFilters = bitapPatterns.stream().map(bp -> new BitapMatcherFilter(
                            bp.substitutionAndIndelMatcherLast(maxErrors, target.getSequence(), from, to)))
                            .collect(Collectors.toList());
            }

            @Override
            public Match take() {
                Match match;
                if (fixedBorder)
                    match = takeFromFixedPosition();
                else
                    if (fairSorting)
                        match = takeFair();
                    else
                        if (byScore) match = takeUnfairByScore();
                        else match = takeUnfairByCoordinate();

                return match;
            }

            private Match takeUnfairByScore() {
                while (currentNumBitapErrors <= maxErrors) {
                    while (currentIndex < sequences.size()) {
                        int position = bitapMatcherFilters.get(currentIndex).findNext();
                        if (position == -1) {
                            bitapMatcherFilters.set(currentIndex, new BitapMatcherFilter(bitapPatterns.get(currentIndex)
                                    .substitutionAndIndelMatcherLast(currentNumBitapErrors + 1,
                                            target.getSequence(), from, to)));
                            currentIndex++;
                        } else {
                            HashSet<Integer> currentReturnedPositions = alreadyReturnedPositions.get(currentIndex);
                            if (currentReturnedPositions.contains(position))
                                continue;
                            else
                                currentReturnedPositions.add(position);
                            Alignment<NucleotideSequence> alignment = patternAligner.align(sequences.get(currentIndex),
                                        target, position);
                            if (alignment.getScore() >= patternAligner.penaltyThreshold())
                                return generateMatch(alignment, target, targetId,
                                        fixGroupEdgePositions(groupEdgePositions, groupMovements.get(currentIndex),
                                                sequences.get(currentIndex).size()));
                        }
                    }
                    currentIndex = 0;
                    currentNumBitapErrors++;
                }
                return null;
            }

            private Match takeUnfairByCoordinate() {
                while (currentIndex < sequences.size()) {
                    int position = bitapMatcherFilters.get(currentIndex).findNext();
                    if (position == -1)
                        currentIndex++;
                    else {
                        Alignment<NucleotideSequence> alignment = patternAligner.align(sequences.get(currentIndex),
                                target, position);
                        if (alignment.getScore() >= patternAligner.penaltyThreshold())
                            return generateMatch(alignment, target, targetId,
                                    fixGroupEdgePositions(groupEdgePositions, groupMovements.get(currentIndex),
                                    sequences.get(currentIndex).size()));
                    }
                }
                return null;
            }

            private Match takeFair() {
                if (!sortingPerformed) {
                    fillAllMatchesForFairSorting();
                    if (byScore)
                        Arrays.sort(allMatches, Comparator.comparingLong(Match::getScore).reversed());
                    else
                        Arrays.sort(allMatches, Comparator.comparingInt(match -> match.getRange().getLower()));
                    sortingPerformed = true;
                }
                if (takenValues == allMatches.length) return null;
                return allMatches[takenValues++];
            }

            private Match takeFromFixedPosition() {
                // important: to is exclusive and fixedRightBorder is inclusive
                if (((fixedLeftBorder != -1) && (from > fixedLeftBorder))
                        || ((fixedRightBorder != -1) && (to <= fixedRightBorder)))
                    return null;
                if (!sortingPerformed) {
                    if (fixedRightBorder != -1)
                        fillAllMatchesForFixedRightBorder();
                    else if (fixedLeftBorder != -1)
                        fillAllMatchesForFixedLeftBorder();
                    else throw new IllegalArgumentException("Wrong call of takeFromFixedPosition: fixedLeftBorder="
                                + fixedLeftBorder + ", fixedRightBorder=" + fixedRightBorder);
                    Arrays.sort(allMatches, Comparator.comparingLong(Match::getScore).reversed());
                    sortingPerformed = true;
                }
                if (takenValues == allMatches.length) return null;
                return allMatches[takenValues++];
            }

            /**
             * Fill allMatches array with all existing matches for fair sorting.
             */
            private void fillAllMatchesForFairSorting() {
                ArrayList<Match> allMatchesList = new ArrayList<>();
                Alignment<NucleotideSequence> alignment;
                int matchLastPosition;

                for (currentIndex = 0; currentIndex < sequences.size(); currentIndex++) {
                    BitapMatcherFilter currentBitapFilter = bitapMatcherFilters.get(currentIndex);
                    NucleotideSequence currentSeq = sequences.get(currentIndex);
                    HashSet<Range> uniqueRanges = new HashSet<>();
                    do {
                        matchLastPosition = currentBitapFilter.findNext();
                        if (matchLastPosition != -1) {
                            alignment = patternAligner.align(currentSeq, target, matchLastPosition);
                            if ((alignment.getScore() >= patternAligner.penaltyThreshold())
                                    && !uniqueRanges.contains(alignment.getSequence2Range())) {
                                uniqueRanges.add(alignment.getSequence2Range());
                                allMatchesList.add(generateMatch(alignment, target, targetId,
                                        fixGroupEdgePositions(groupEdgePositions, groupMovements.get(currentIndex),
                                        sequences.get(currentIndex).size())));
                            }
                        }
                    } while (matchLastPosition != -1);
                }

                allMatches = new Match[allMatchesList.size()];
                allMatchesList.toArray(allMatches);
            }

            /**
             * Fill allMatches array with all possible alignments for fixed left border.
             */
            private void fillAllMatchesForFixedLeftBorder() {
                ArrayList<Match> allMatchesList = new ArrayList<>();
                PatternAligner patternAligner = this.patternAligner.setLeftBorder(fixedLeftBorder);
                Alignment<NucleotideSequence> alignment;

                for (currentIndex = 0; currentIndex < sequences.size(); currentIndex++) {
                    if (bitapMatcherFilters.get(currentIndex).findNext() == -1)
                        continue;
                    NucleotideSequence currentSeq = sequences.get(currentIndex);
                    HashSet<Range> uniqueRanges = new HashSet<>();
                    for (int rightBorder = Math.max(fixedLeftBorder, fixedLeftBorder + currentSeq.size()
                            - patternAligner.bitapMaxErrors() - 1);
                         rightBorder <= Math.min(to - 1, fixedLeftBorder + currentSeq.size()
                                 + patternAligner.bitapMaxErrors() - 1);
                         rightBorder++)
                        if ((rightBorder >= fixedLeftBorder) && (rightBorder < target.size())) {
                            alignment = patternAligner.align(currentSeq, target, rightBorder);
                            if ((alignment.getScore() >= patternAligner.penaltyThreshold())
                                    && !uniqueRanges.contains(alignment.getSequence2Range())) {
                                uniqueRanges.add(alignment.getSequence2Range());
                                allMatchesList.add(generateMatch(alignment, target, targetId,
                                        fixGroupEdgePositions(groupEdgePositions, groupMovements.get(currentIndex),
                                        sequences.get(currentIndex).size())));
                            }
                        }
                }

                allMatches = new Match[allMatchesList.size()];
                allMatchesList.toArray(allMatches);
            }

            /**
             * Fill allMatches array with all possible alignments for fixed right border.
             */
            private void fillAllMatchesForFixedRightBorder() {
                ArrayList<Match> allMatchesList = new ArrayList<>();
                PatternAligner patternAligner = (fixedLeftBorder == -1) ? this.patternAligner
                        : this.patternAligner.setLeftBorder(fixedLeftBorder);
                Alignment<NucleotideSequence> alignment;

                for (currentIndex = 0; currentIndex < sequences.size(); currentIndex++) {
                    if (bitapMatcherFilters.get(currentIndex).findNext() == -1)
                        continue;
                    NucleotideSequence currentSeq = sequences.get(currentIndex);
                    alignment = patternAligner.align(currentSeq, target, fixedRightBorder);
                    if (alignment.getScore() >= patternAligner.penaltyThreshold())
                        allMatchesList.add(generateMatch(alignment, target, targetId,
                                fixGroupEdgePositions(groupEdgePositions, groupMovements.get(currentIndex),
                                sequences.get(currentIndex).size())));
                }

                allMatches = new Match[allMatchesList.size()];
                allMatchesList.toArray(allMatches);
            }
        }
    }
}
