package com.milaboratory.mist.pattern;

import cc.redberry.pipe.OutputPort;
import com.milaboratory.core.Range;
import com.milaboratory.core.sequence.*;

import java.util.*;
import java.util.stream.Collectors;

public final class RepeatPattern extends SinglePattern {
    private final NucleotideSequence patternSeq;
    private final int minRepeats;
    private final int maxRepeats;
    private final int fixedLeftBorder;
    private final int fixedRightBorder;
    private final List<GroupEdgePosition> groupEdgePositions;

    public RepeatPattern(PatternAligner patternAligner, NucleotideSequence patternSeq, int minRepeats, int maxRepeats) {
        this(patternAligner, patternSeq, minRepeats, maxRepeats, new ArrayList<>());
    }

    public RepeatPattern(PatternAligner patternAligner, NucleotideSequence patternSeq, int minRepeats, int maxRepeats,
                         int fixedLeftBorder, int fixedRightBorder) {
        this(patternAligner, patternSeq, minRepeats, maxRepeats, fixedLeftBorder, fixedRightBorder, new ArrayList<>());

    }

    public RepeatPattern(PatternAligner patternAligner, NucleotideSequence patternSeq, int minRepeats, int maxRepeats,
                         List<GroupEdgePosition> groupEdgePositions) {
        this(patternAligner, patternSeq, minRepeats, maxRepeats, -1, -1, groupEdgePositions);
    }

    /**
     * Match several repeats of specified sequence. Number of repeats specified as interval.
     * Calls FuzzyMatchPattern to find matches for each number of repeats.
     *
     * @param patternAligner pattern aligner, for FuzzyMatchPattern
     * @param patternSeq sequence to repeat
     * @param minRepeats minimum number of repeats; minimum allowed value is 1
     * @param maxRepeats maximum number of repeats; use Integer.MAX_VALUE to match without maximum limit of repeats
     * @param fixedLeftBorder position in target where must be the left border, for FuzzyMatchPattern
     * @param fixedRightBorder position in target where must be the right border, for FuzzyMatchPattern
     * @param groupEdgePositions list of group edges and their positions, for FuzzyMatchPattern.
     *                           Group edges beyond the right border of motif will be moved to the right border.
     */
    public RepeatPattern(PatternAligner patternAligner, NucleotideSequence patternSeq, int minRepeats, int maxRepeats,
                         int fixedLeftBorder, int fixedRightBorder, List<GroupEdgePosition> groupEdgePositions) {
        super(patternAligner);
        this.patternSeq = patternSeq;
        if ((minRepeats < 1) || (maxRepeats < minRepeats))
            throw new IllegalArgumentException("Wrong arguments: minRepeats=" + minRepeats + ", maxRepeats=" + maxRepeats);
        else {
            this.minRepeats = minRepeats;
            this.maxRepeats = maxRepeats;
        }
        if (patternSeq.size() * maxRepeats >= 64)
            throw new IllegalArgumentException("Motif length must be less than 64; patternSeq.size()="
                    + patternSeq.size() + ", maxRepeats=" + maxRepeats);
        this.fixedLeftBorder = fixedLeftBorder;
        this.fixedRightBorder = fixedRightBorder;
        this.groupEdgePositions = groupEdgePositions;
    }

    @Override
    public String toString() {
        if (groupEdgePositions.size() > 0)
            return "RepeatPattern(" + patternSeq + ", " + minRepeats + ", " + maxRepeats + ", "
                    + fixedLeftBorder + ", " + fixedRightBorder + ", " + groupEdgePositions + ")";
        else
            return "RepeatPattern(" + patternSeq + ", " + minRepeats + ", " + maxRepeats + ", "
                    + fixedLeftBorder + ", " + fixedRightBorder + ")";
    }

    @Override
    public ArrayList<GroupEdge> getGroupEdges() {
        return groupEdgePositions.stream().map(GroupEdgePosition::getGroupEdge)
                .collect(Collectors.toCollection(ArrayList::new));
    }

    @Override
    public MatchingResult match(NSequenceWithQuality target, int from, int to, byte targetId) {
        return new RepeatPatternMatchingResult(patternAligner, patternSeq, minRepeats, maxRepeats,
                fixedLeftBorder, fixedRightBorder, groupEdgePositions, target, from, to, targetId);
    }

    private static class RepeatPatternMatchingResult extends MatchingResult {
        private final PatternAligner patternAligner;
        private final NucleotideSequence patternSeq;
        private final int minRepeats;
        private final int maxRepeats;
        private final int fixedLeftBorder;
        private final int fixedRightBorder;
        private final List<GroupEdgePosition> groupEdgePositions;
        private final NSequenceWithQuality target;
        private final int from;
        private final int to;
        private final byte targetId;

        RepeatPatternMatchingResult(PatternAligner patternAligner, NucleotideSequence patternSeq,
                                    int minRepeats, int maxRepeats, int fixedLeftBorder, int fixedRightBorder,
                                    List<GroupEdgePosition> groupEdgePositions,
                                    NSequenceWithQuality target, int from, int to, byte targetId) {
            this.patternAligner = patternAligner;
            this.patternSeq = patternSeq;
            this.minRepeats = minRepeats;
            this.maxRepeats = maxRepeats;
            this.fixedLeftBorder = fixedLeftBorder;
            this.fixedRightBorder = fixedRightBorder;
            this.groupEdgePositions = groupEdgePositions;
            this.target = target;
            this.from = from;
            this.to = to;
            this.targetId = targetId;
        }

        @Override
        public OutputPort<Match> getMatches(boolean byScore, boolean fairSorting) {
            return new RepeatPatternOutputPort(patternAligner, patternSeq, minRepeats, maxRepeats, fixedLeftBorder,
                    fixedRightBorder, groupEdgePositions, target, from, to, targetId, byScore, fairSorting);
        }

        private static class RepeatPatternOutputPort implements OutputPort<Match> {
            private final PatternAligner patternAligner;
            private final NucleotideSequence patternSeq;
            private final int minRepeats;
            private final int maxRepeats;
            private final int fixedLeftBorder;
            private final int fixedRightBorder;
            private final List<GroupEdgePosition> groupEdgePositions;
            private final NSequenceWithQuality target;
            private final int from;
            private final int to;
            private final byte targetId;
            private final boolean byScore;
            private final boolean fairSorting;
            private final int patternSeqLength;

            private OutputPort<Match> currentPort;

            // for unfair sorting
            private boolean currentPortEmpty = true;
            private int currentRepeats;

            // for fair sorting
            private Match[] allMatches;
            private boolean sortingPerformed = false;
            private int takenValues = 0;

            RepeatPatternOutputPort(PatternAligner patternAligner, NucleotideSequence patternSeq,
                                    int minRepeats, int maxRepeats, int fixedLeftBorder, int fixedRightBorder,
                                    List<GroupEdgePosition> groupEdgePositions,
                                    NSequenceWithQuality target, int from, int to, byte targetId,
                                    boolean byScore, boolean fairSorting) {
                this.patternAligner = patternAligner;
                this.patternSeq = patternSeq;
                this.patternSeqLength = patternSeq.size();
                this.minRepeats = minRepeats;
                this.maxRepeats = Math.min(maxRepeats, (to - from + patternAligner.bitapMaxErrors()) / patternSeqLength);
                this.fixedLeftBorder = fixedLeftBorder;
                this.fixedRightBorder = fixedRightBorder;
                this.groupEdgePositions = groupEdgePositions;
                this.target = target;
                this.from = from;
                this.to = to;
                this.targetId = targetId;
                this.byScore = byScore;
                this.fairSorting = fairSorting;
                this.currentRepeats = maxRepeats;
            }

            @Override
            public Match take() {
                return fairSorting ? takeFair() : takeUnfair();
            }

            private Match takeUnfair() {
                while ((currentRepeats >= minRepeats) || !currentPortEmpty) {
                    if (currentPortEmpty) {
                        NucleotideSequence[] sequencesToConcatenate = new NucleotideSequence[currentRepeats];
                        Arrays.fill(sequencesToConcatenate, patternSeq);
                        NucleotideSequence currentSequence = SequencesUtils.concatenate(sequencesToConcatenate);
                        currentPort = new FuzzyMatchPattern(patternAligner, currentSequence,
                                0, 0, fixedLeftBorder, fixedRightBorder, fixGroupEdgePositions(
                                        groupEdgePositions, 0, patternSeqLength * currentRepeats))
                                .match(target, from, to, targetId)
                                .getMatches(byScore, false);
                        currentPortEmpty = false;
                        currentRepeats--;
                    }
                    Match currentMatch = currentPort.take();
                    if (currentMatch != null)
                        return overrideMatchScore(currentMatch, currentRepeats + 1);
                    else
                        currentPortEmpty = true;
                }
                return null;
            }

            private Match takeFair() {
                if (!sortingPerformed) {
                    HashMap<Range, Match> matchesWithUniqueRanges = new HashMap<>();
                    for (int repeats = maxRepeats; repeats >= minRepeats; repeats--) {
                        NucleotideSequence[] sequencesToConcatenate = new NucleotideSequence[repeats];
                        Arrays.fill(sequencesToConcatenate, patternSeq);
                        NucleotideSequence currentSequence = SequencesUtils.concatenate(sequencesToConcatenate);
                        currentPort = new FuzzyMatchPattern(patternAligner, currentSequence,
                                0, 0, fixedLeftBorder, fixedRightBorder, fixGroupEdgePositions(
                                        groupEdgePositions, 0, patternSeqLength * repeats))
                                .match(target, from, to, targetId)
                                .getMatches(byScore, true);
                        Match currentMatch;
                        do {
                            currentMatch = currentPort.take();
                            if (currentMatch != null) {
                                currentMatch = overrideMatchScore(currentMatch, repeats);
                                Range currentRange = currentMatch.getRange();
                                if ((matchesWithUniqueRanges.get(currentRange) == null)
                                        || (matchesWithUniqueRanges.get(currentRange).getScore() < currentMatch.getScore()))
                                    matchesWithUniqueRanges.put(currentRange, currentMatch);
                            }
                        } while (currentMatch != null);
                    }
                    allMatches = new Match[matchesWithUniqueRanges.size()];
                    matchesWithUniqueRanges.values().toArray(allMatches);
                    if (byScore)
                        Arrays.sort(allMatches, Comparator.comparingLong(Match::getScore).reversed());
                    else
                        Arrays.sort(allMatches, Comparator.comparingInt(match -> match.getRange().getLower()));
                    sortingPerformed = true;
                }

                if (takenValues == allMatches.length) return null;
                return allMatches[takenValues++];
            }

            private Match overrideMatchScore(Match match, int repeats) {
                return new Match(match.getNumberOfPatterns(), match.getScore() + patternAligner.repeatsPenalty(
                        patternSeq, repeats, maxRepeats), match.getMatchedItems());
            }
        }
    }
}
