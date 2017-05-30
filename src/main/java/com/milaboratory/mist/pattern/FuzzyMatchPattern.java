package com.milaboratory.mist.pattern;

import cc.redberry.pipe.OutputPort;
import com.milaboratory.core.Range;
import com.milaboratory.core.alignment.Alignment;
import com.milaboratory.core.motif.BitapMatcher;
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
            private final BitapMatcher[] bitapMatchersMain;

            /**
             * Secondary matcher is used in getNumberOfErrorsForPosition() function to avoid resetting main matchers.
             */
            private final BitapMatcher[] bitapMatchersSecondary;

            /**
             * Data structures used only for fair sorting.
             */
            private Match[] allMatches;
            private HashSet<Range> uniqueRanges;
            private boolean sortingPerformed = false;
            private int takenValues = 0;

            /**
             * Used only for unfair sorting. Saved last found position, so the next position must be bigger than this.
             */
            private int lastFoundPosition = Integer.MIN_VALUE;

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
                this.bitapMatchersMain = new BitapMatcher[maxErrors + 1];
                this.bitapMatchersSecondary = new BitapMatcher[maxErrors + 1];
                for (int bitapMaxErrors = 0; bitapMaxErrors <= maxErrors; bitapMaxErrors++) {
                    resetBitapMatcher(bitapMaxErrors, true);
                    resetBitapMatcher(bitapMaxErrors, false);
                }
                if (fairSorting)
                    uniqueRanges = new HashSet<>();
                else if (byScore)
                    alreadyReturnedPositions = new HashSet<>();
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
                    position = bitapMatchersMain[currentNumBitapErrors].findNext();
                    if (position == -1) {
                        if (currentNumBitapErrors == maxErrors)
                            return null;
                        currentNumBitapErrors++;
                        lastFoundPosition = Integer.MIN_VALUE;
                        continue;
                    }
                    if (isBadMatch(position)) {
                        if (lastFoundPosition < position) lastFoundPosition = position;
                        continue;
                    }

                    position = bitapMatchWithFewestErrors(position);

                    if (alreadyReturnedPositions.contains(position)) {
                        // this will also prevent the loop from exiting, so it will search for the next match
                        if (lastFoundPosition < position)
                            lastFoundPosition = position;
                    } else
                        alreadyReturnedPositions.add(position);
                } while ((position <= lastFoundPosition) || (position == -1));
                lastFoundPosition = position;

                return generateMatch(patternAligner.align(patternSeq, target, position));
            }

            private Match takeUnfairByCoordinate() {
                int position;

                do {
                    position = bitapMatchersMain[maxErrors].findNext();
                    if (position == -1)
                        return null;
                    if (isBadMatch(position)) {
                        if (lastFoundPosition < position) lastFoundPosition = position;
                        continue;
                    }
                    position = bitapMatchWithFewestErrors(position);
                } while (position <= lastFoundPosition);
                lastFoundPosition = position;

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
             * Find match with lowest number of errors from a group of consequent bitap matches.
             *
             * @param foundPosition first found position, which treated as worst match in a group of consequent
             *                      matches where error for next match must be smaller than for previous
             * @return position of best match in this group of consequent matches
             */
            private int bitapMatchWithFewestErrors(int foundPosition) {
                int currentPosition = foundPosition;

                while (true) {
                    currentPosition++;
                    if (getNumberOfErrorsForPosition(currentPosition) >= getNumberOfErrorsForPosition(currentPosition - 1))
                        return currentPosition - 1;
                }
            }

            /**
             * Returns true if match in found position has more errors than in previous position; otherwise false.
             *
             * @param position position of the match to test
             * @return true if this match is bad and should be skipped
             */
            private boolean isBadMatch(int position) {
                return (position != 0)
                        && (getNumberOfErrorsForPosition(position) - getNumberOfErrorsForPosition(position - 1) > 0);
            }

            /**
             * Returns minimal number of errors for specified position when bitap still finds this position.
             * If this number of errors is bigger than maxErrors, it returns Integer.MAX_VALUE.
             *
             * @param position position to test for number of errors
             * @return minimal number of errors for specified position when bitap still finds this position
             */
            private int getNumberOfErrorsForPosition(int position) {
                int currentPosition;

                for (int bitapMaxErrors = 0; bitapMaxErrors <= maxErrors; bitapMaxErrors++)
                    while (true) {
                        currentPosition = bitapMatchersSecondary[bitapMaxErrors].findNext();
                        if ((currentPosition == -1) || (currentPosition > position)) {
                            resetBitapMatcher(bitapMaxErrors, false);
                            break;
                        }
                        if (currentPosition == position) {
                            resetBitapMatcher(bitapMaxErrors, false);
                            return bitapMaxErrors;
                        }
                    }

                return Integer.MAX_VALUE;
            }

            /**
             * (Re)initialize bitap matcher for specified number of errors.
             *
             * @param bitapMaxErrors maximum number of errors for this matcher
             * @param main true to reinitialize main matcher, false for secondary matcher
             */
            private void resetBitapMatcher(int bitapMaxErrors, boolean main) {
                if (main)
                    bitapMatchersMain[bitapMaxErrors] = bitapPattern.substitutionAndIndelMatcherLast(bitapMaxErrors,
                            target.getSequence(), from, to);
                else
                    bitapMatchersSecondary[bitapMaxErrors] = bitapPattern.substitutionAndIndelMatcherLast(bitapMaxErrors,
                            target.getSequence(), from, to);
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
                    MatchedGroupEdge matchedGroupEdge = new MatchedGroupEdge(target, targetId, 0,
                            groupEdgePosition.getGroupEdge(), groupEdgePosition.getPosition() + foundRange.getLower());
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
                    matchLastPosition = bitapMatchersMain[maxErrors].findNext();
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
