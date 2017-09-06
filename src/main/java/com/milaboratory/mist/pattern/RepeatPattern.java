package com.milaboratory.mist.pattern;

import cc.redberry.pipe.OutputPort;
import com.milaboratory.core.Range;
import com.milaboratory.core.alignment.Alignment;
import com.milaboratory.core.sequence.*;

import java.util.*;
import java.util.stream.Collectors;

import static com.milaboratory.mist.pattern.PatternUtils.*;

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
     * Match several repeats of specified nucleotide or wildcard. Number of repeats specified as interval.
     * Calls FuzzyMatchPattern to find matches for each number of repeats.
     *
     * @param patternAligner pattern aligner, for FuzzyMatchPattern
     * @param patternSeq 1 character nucleotide sequence to repeat
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
            throw new IllegalArgumentException("Wrong arguments: minRepeats=" + minRepeats
                    + ", maxRepeats=" + maxRepeats);
        else {
            this.minRepeats = minRepeats;
            this.maxRepeats = maxRepeats;
        }
        if (patternSeq.size() != 1)
            throw new IllegalArgumentException("patternSeq length must be 1 for RepeatPattern, found " + patternSeq);
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
            private final boolean fixedBorder;
            private final List<GroupEdgePosition> groupEdgePositions;
            private final NSequenceWithQuality target;
            private final int from;
            private final int to;
            private final byte targetId;
            private final boolean byScore;
            private final boolean fairSorting;
            private final TargetSections targetSections;

            /* Length of longest valid section starting from this position (index1) in target inside (from->to) range.
             * Section is valid when number of errors (index2) isn't bigger than bitapMaxErrors in PatternAligner. */
            private final int[][] longestValidSections;

            private final HashMap<Integer, NucleotideSequence> sequences = new HashMap<>();

            // Data structures used for unfair sorting.
            private final HashSet<Range> uniqueRangesUnfairNotAligned = new HashSet<>();
            private final HashSet<Range> uniqueRangesUnfairAligned = new HashSet<>();
            private int currentRepeats;
            private int currentMaxErrors = 0;
            private int currentPosition = 0;
            private boolean noMoreMatches = false;

            // Data structures used for fair sorting and for matching in fixed position.
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
                this.minRepeats = minRepeats;
                this.maxRepeats = maxRepeats;
                this.fixedLeftBorder = fixedLeftBorder;
                this.fixedRightBorder = fixedRightBorder;
                this.fixedBorder = (fixedLeftBorder != -1) || (fixedRightBorder != -1);
                this.groupEdgePositions = groupEdgePositions;
                this.target = target;
                this.from = from;
                this.to = to;
                this.targetId = targetId;
                this.byScore = byScore;
                this.fairSorting = fairSorting;
                this.currentRepeats = maxRepeats;
                this.targetSections = new TargetSections(target.getSequence().toString().substring(from, to),
                        patternSeq);
                int maxErrors = patternAligner.bitapMaxErrors();
                this.longestValidSections = new int[to - from][maxErrors + 1];
                for (int i = 0; i < to - from; i++) {
                    longestValidSections[i] = new int[maxErrors + 1];
                    for (int j = 0; j <= maxErrors; j++)
                        longestValidSections[i][j] = calculateLongestValidSection(i, j);
                }
                for (int i = Math.max(1, minRepeats - maxErrors); i <= maxRepeats; i++) {
                    NucleotideSequence[] sequencesToConcatenate = new NucleotideSequence[i];
                    Arrays.fill(sequencesToConcatenate, patternSeq);
                    NucleotideSequence currentSequence = SequencesUtils.concatenate(sequencesToConcatenate);
                    this.sequences.put(i, currentSequence);
                }
            }

            @Override
            public Match take() {
                Match match;
                if (fixedBorder)
                    match = takeFromFixedPosition();
                else if (fairSorting)
                    match = takeFair();
                else
                    match = takeUnfair();

                return match;
            }

            private Match takeUnfair() {
                while (!noMoreMatches) {
                    int currentLongestSection = longestValidSections[currentPosition][currentMaxErrors];
                    if (currentRepeats <= currentLongestSection) {
                        Range currentRange = new Range(currentPosition + from,
                                Math.min(to, currentPosition + currentRepeats + from));
                        if (!uniqueRangesUnfairNotAligned.contains(currentRange)) {
                            uniqueRangesUnfairNotAligned.add(currentRange);
                            Alignment<NucleotideSequence> alignment = patternAligner.align(
                                    sequences.get(currentRepeats), target, currentRange.getUpper() - 1);
                            if ((alignment.getScore() >= patternAligner.penaltyThreshold())
                                    && !uniqueRangesUnfairAligned.contains(alignment.getSequence2Range())) {
                                uniqueRangesUnfairAligned.add(alignment.getSequence2Range());
                                pointToNextUnfairMatch();
                                return overrideMatchScore(generateMatch(alignment, target, targetId,
                                        fixGroupEdgePositions(groupEdgePositions, 0, currentRange.length())),
                                        currentRange.length());
                            }
                        }
                    }
                    pointToNextUnfairMatch();
                }
                return null;
            }

            private void pointToNextUnfairMatch() {
                if (byScore) {
                    currentPosition++;
                    if (currentPosition > to - from - Math.max(1, currentRepeats - currentMaxErrors)) {
                        currentPosition = 0;
                        currentMaxErrors++;
                        if (currentMaxErrors > patternAligner.bitapMaxErrors()) {
                            currentMaxErrors = 0;
                            currentRepeats--;
                            if (currentRepeats < minRepeats)
                                noMoreMatches = true;
                        }
                    }
                } else {
                    currentMaxErrors++;
                    int maxErrors = patternAligner.bitapMaxErrors();
                    if (currentMaxErrors > maxErrors) {
                        currentMaxErrors = 0;
                        currentRepeats--;
                        if (currentRepeats < minRepeats) {
                            currentPosition++;
                            currentRepeats = Math.min(maxRepeats, to - from - Math.max(1, currentPosition - maxErrors));
                            if (currentPosition > to - from - Math.max(1, minRepeats - maxErrors))
                                noMoreMatches = true;
                        }
                    }
                }
            }

            private Match takeFair() {
                if (!sortingPerformed) {
                    fillAllMatchesForFairSorting();
                    Arrays.sort(allMatches, Comparator.comparingInt((Match match) -> match.getRange().length())
                            .reversed());
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
                    Arrays.sort(allMatches, Comparator.comparingInt((Match match) -> match.getRange().length())
                            .reversed());
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
                HashSet<Range> uniqueRanges = new HashSet<>();
                int maxErrors = patternAligner.bitapMaxErrors();

                for (int repeats = maxRepeats; repeats >= minRepeats; repeats--)
                    for (int i = 0; i <= to - from - Math.max(1, repeats - maxErrors); i++) {
                        int currentLongestSection = longestValidSections[i][maxErrors];
                        if (repeats <= currentLongestSection)
                            uniqueRanges.add(new Range(i + from, Math.min(to, i + repeats + from)));
                    }

                ArrayList<Match> allMatchesList = getMatchesList(uniqueRanges, patternAligner);
                allMatches = new Match[allMatchesList.size()];
                allMatchesList.toArray(allMatches);
            }

            /**
             * Fill allMatches array with all possible alignments for fixed left border.
             */
            private void fillAllMatchesForFixedLeftBorder() {
                HashSet<Range> uniqueRanges = new HashSet<>();
                int maxErrors = patternAligner.bitapMaxErrors();

                for (int repeats = maxRepeats; repeats >= minRepeats; repeats--)
                    for (int i = Math.max(0, fixedLeftBorder - from);
                         i <= Math.min(to - from - Math.max(1, repeats - maxErrors),
                                 fixedLeftBorder + maxErrors - from); i++) {
                        int currentLongestSection = longestValidSections[i][maxErrors];
                        if (repeats <= currentLongestSection)
                            uniqueRanges.add(new Range(i + from, Math.min(to, i + repeats + from)));
                    }

                ArrayList<Match> allMatchesList = getMatchesList(uniqueRanges,
                        patternAligner.setLeftBorder(fixedLeftBorder));
                allMatches = new Match[allMatchesList.size()];
                allMatchesList.toArray(allMatches);
            }

            /**
             * Fill allMatches array with all possible alignments for fixed right border.
             */
            private void fillAllMatchesForFixedRightBorder() {
                HashSet<Range> uniqueRanges = new HashSet<>();
                PatternAligner aligner = (fixedLeftBorder == -1) ? patternAligner
                        : patternAligner.setLeftBorder(fixedLeftBorder);
                int maxErrors = aligner.bitapMaxErrors();

                for (int repeats = maxRepeats; repeats >= minRepeats; repeats--) {
                    int minIndex = (fixedLeftBorder == -1)
                            ? Math.max(0, fixedRightBorder - repeats - maxErrors - from + 1)
                            : Math.max(0, fixedLeftBorder - from);
                    int maxIndex = (fixedLeftBorder == -1)
                            ? Math.min(to - from - Math.max(1, repeats - maxErrors),
                                fixedRightBorder - repeats - from + 1)
                            : Math.min(to - from - Math.max(1, repeats - maxErrors),
                                Math.min(fixedLeftBorder + maxErrors - from, fixedRightBorder - repeats - from + 1));
                    for (int i = minIndex; i <= maxIndex; i++) {
                        int currentLongestSection = longestValidSections[i][maxErrors];
                        if (repeats <= currentLongestSection)
                            uniqueRanges.add(new Range(i + from, Math.min(to, i + repeats + from)));
                    }
                }

                ArrayList<Match> allMatchesList = getMatchesList(uniqueRanges, aligner);
                allMatches = new Match[allMatchesList.size()];
                allMatchesList.toArray(allMatches);
            }

            /**
             * Get list of aligned matches from specified set of ranges. Used for fair sorting and
             * for matching with fixed border.
             *
             * @param uniqueRanges set of ranges where to use aligner
             * @param aligner pattern aligner, maybe configured for matching with fixed border
             * @return list of aligned matches
             */
            private ArrayList<Match> getMatchesList(HashSet<Range> uniqueRanges, PatternAligner aligner) {
                ArrayList<Match> allMatchesList = new ArrayList<>();
                Alignment<NucleotideSequence> alignment;
                HashSet<Range> uniqueAlignedRanges = new HashSet<>();

                for (Range range : uniqueRanges) {
                    alignment = aligner.align(sequences.get(range.length()), target,
                            range.getUpper() - 1);
                    if ((alignment.getScore() >= aligner.penaltyThreshold())
                            && !uniqueAlignedRanges.contains(alignment.getSequence2Range())) {
                        uniqueAlignedRanges.add(alignment.getSequence2Range());
                        allMatchesList.add(overrideMatchScore(generateMatch(alignment, target, targetId,
                                fixGroupEdgePositions(groupEdgePositions, 0, range.length())),
                                range.length()));
                    }
                }

                return allMatchesList;
            }

            private Match overrideMatchScore(Match match, int repeats) {
                return new Match(match.getNumberOfPatterns(), match.getScore() + patternAligner.repeatsPenalty(
                        patternSeq, repeats, maxRepeats), match.getMatchedItems());
            }

            /**
             * Calculate length of longest valid section starting from specified position inside (from->to) range.
             * Section is considered valid if number of errors in it is not bigger than numberOfErrors.
             * Longest valid section length can end out of target bounds if numberOfErrors is not 0.
             *
             * @param position section starting position inside (from->to) range, inclusive
             * @param numberOfErrors maximum number of errors, inclusive
             * @return length of longest valid section that starts from this position
             */
            private int calculateLongestValidSection(int position, int numberOfErrors) {
                int[] sections = targetSections.sections;
                boolean currentSectionIsMatching = targetSections.firstMatching;
                int currentLength = 0;
                int currentPosition = 0;
                int currentErrors = 0;
                for (int i = 0; i < sections.length; i++, currentSectionIsMatching = !currentSectionIsMatching) {
                    int currentSectionValue = sections[i];
                    if (currentPosition + currentSectionValue < position) {
                        currentPosition += currentSectionValue;
                        continue;
                    } else if (currentPosition <= position) {
                        if (currentSectionIsMatching) {
                            currentLength = currentPosition + currentSectionValue - position;
                        } else if (currentPosition + currentSectionValue - position <= numberOfErrors) {
                            currentErrors = currentPosition + currentSectionValue - position;
                            currentLength = currentErrors;
                        } else {
                            currentLength = numberOfErrors;
                            currentErrors = numberOfErrors;
                            break;
                        }
                    } else {
                        if (currentSectionIsMatching) {
                            currentLength += currentSectionValue;
                        } else if (currentErrors + currentSectionValue <= numberOfErrors) {
                            currentErrors += currentSectionValue;
                            currentLength += currentSectionValue;
                        } else {
                            currentLength += numberOfErrors - currentErrors;
                            currentErrors = numberOfErrors;
                            break;
                        }
                    }
                    currentPosition += currentSectionValue;
                }
                return currentLength + (numberOfErrors - currentErrors);
            }

            /**
             * This class represents sections in the target substring (from "from" to "to" coordinates) with array of
             * integers. Each integer is length of section that consists of only matching or only non-matching letters.
             * firstMatching is true if first section is matching, otherwise false.
             */
            private static class TargetSections {
                final int[] sections;
                final boolean firstMatching;

                private static HashMap<Character, String> allMatchingLetters = new HashMap<>();
                static {
                    allMatchingLetters.put('A', "Aa");
                    allMatchingLetters.put('T', "Tt");
                    allMatchingLetters.put('G', "Gg");
                    allMatchingLetters.put('C', "Cc");
                    allMatchingLetters.put('W', "AaTt");
                    allMatchingLetters.put('S', "GgCc");
                    allMatchingLetters.put('M', "AaCc");
                    allMatchingLetters.put('K', "GgTt");
                    allMatchingLetters.put('R', "AaGg");
                    allMatchingLetters.put('Y', "CcTt");
                    allMatchingLetters.put('B', "TtGgCc");
                    allMatchingLetters.put('V', "AaGgCc");
                    allMatchingLetters.put('H', "AaTtCc");
                    allMatchingLetters.put('D', "AaTtGg");
                    allMatchingLetters.put('N', "AaTtGgCc");
                    new HashSet<>(allMatchingLetters.keySet())
                            .forEach(l -> allMatchingLetters.put(Character.toLowerCase(l), allMatchingLetters.get(l)));
                }

                TargetSections(String targetSubstring, NucleotideSequence patternSeq) {
                    String matchingLetters = allMatchingLetters.get(patternSeq.toString().charAt(0));
                    if (matchingLetters == null)
                        throw new IllegalArgumentException("Wrong patternSeq for RepeatPattern: "
                                + patternSeq);
                    if (targetSubstring.length() < 1)
                        throw new IllegalArgumentException("Wrong targetSubstring for RepeatPattern: "
                                + targetSubstring);
                    else {
                        ArrayList<Integer> sectionsList = new ArrayList<>();
                        boolean currentSectionMatching = matchingLetters.contains(targetSubstring.substring(0, 1));
                        int currentSectionLength = 1;
                        this.firstMatching = currentSectionMatching;
                        for (int i = 1; i < targetSubstring.length(); i++) {
                            String currentLetter = targetSubstring.substring(i, i + 1);
                            boolean currentLetterMatching = matchingLetters.contains(currentLetter);
                            if (currentLetterMatching != currentSectionMatching) {
                                sectionsList.add(currentSectionLength);
                                currentSectionLength = 1;
                                currentSectionMatching = currentLetterMatching;
                            } else
                                currentSectionLength++;
                        }
                        sectionsList.add(currentSectionLength);
                        this.sections = sectionsList.stream().mapToInt(i -> i).toArray();
                    }
                }
            }
        }
    }
}
