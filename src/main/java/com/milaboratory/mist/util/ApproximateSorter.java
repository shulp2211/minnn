package com.milaboratory.mist.util;

import cc.redberry.pipe.OutputPort;
import com.milaboratory.core.Range;
import com.milaboratory.core.sequence.NSequenceWithQuality;
import com.milaboratory.mist.pattern.*;

import java.util.*;
import java.util.stream.Collectors;

import static com.milaboratory.mist.pattern.MatchValidationType.*;
import static com.milaboratory.mist.util.RangeTools.*;

public abstract class ApproximateSorter {
    protected final PatternAligner patternAligner;
    protected final boolean multipleReads;
    protected final boolean combineScoresBySum;
    protected final boolean fairSorting;
    protected final MatchValidationType matchValidationType;

    /**
     * This sorter allows to get output port for approximately sorted matches by score or coordinate from
     * input ports. Specific sorters (by score, coordinate and with different rules) are extending this class.
     *
     * @param patternAligner pattern aligner that provides information about scoring and pattern overlap limits
     * @param multipleReads true if we combine matches from multiple reads; false if we combine matches
     *                      from single read
     * @param combineScoresBySum true if combined score must be equal to sum of match scores; false if combined
     *                           score must be the highest of match scores
     * @param fairSorting true if we need slow but fair sorting
     * @param matchValidationType type of validation used to determine that current matches combination is invalid
     */
    public ApproximateSorter(PatternAligner patternAligner, boolean multipleReads, boolean combineScoresBySum,
                             boolean fairSorting, MatchValidationType matchValidationType) {
        this.patternAligner = patternAligner;
        this.multipleReads = multipleReads;
        this.combineScoresBySum = combineScoresBySum;
        this.fairSorting = fairSorting;
        this.matchValidationType = matchValidationType;
        if ((multipleReads && ((matchValidationType == INTERSECTION)
                || (matchValidationType == ORDER) || (matchValidationType == FOLLOWING) || (matchValidationType == FIRST)))
                || (!multipleReads && ((matchValidationType == LOGICAL_AND) || (matchValidationType == LOGICAL_OR))))
            throw new IllegalArgumentException("Invalid combination of multipleReads and matchValidationType flags!");
    }

    /**
     * Get output port for sorted combined matches.
     *
     * @param inputPorts operand ports, with limits for unfair sorter
     * @return output port
     */
    public abstract OutputPort<Match> getOutputPort(List<ApproximateSorterOperandPort> inputPorts);

    /**
     * Get combined match from a group of input matches. It uses multipleReads flag to determine how to combine matches
     * (by combining ranges for single read or by numbering the matched ranges for multiple reads).
     *
     * @param sortingByScore true if we use sorting by score, false if we use sorting by coordinate;
     *                       it really used only for matchValidationType == FIRST
     * @param matches input matches
     * @return combined match
     */
    protected Match combineMatches(boolean sortingByScore, Match... matches) {
        ArrayList<MatchedItem> matchedItems = new ArrayList<>();

        if (multipleReads) {
            int patternIndex = 0;
            boolean allMatchesAreNull = true;
            for (Match match : matches) {
                if (match == null) {
                    if (matchValidationType == LOGICAL_OR) {
                        matchedItems.add(new NullMatchedRange(patternIndex++));
                        continue;
                    } else throw new IllegalStateException("Found null match when MatchValidationType doesn't allow them");
                } else allMatchesAreNull = false;
                for (int i = 0; i < match.getNumberOfPatterns(); i++) {
                    MatchedRange currentMatchedRange = match.getMatchedRange(i);
                    if (NullMatchedRange.class.isAssignableFrom(currentMatchedRange.getClass())) {
                        if (match.getMatchedGroupEdgesByPattern(i).size() > 0)
                            throw new IllegalStateException("Null pattern contains "
                                    + match.getMatchedGroupEdgesByPattern(i).size() + " group edges");
                        matchedItems.add(new NullMatchedRange(patternIndex++));
                    } else {
                        matchedItems.add(new MatchedRange(currentMatchedRange.getTarget(), currentMatchedRange.getTargetId(),
                                patternIndex, currentMatchedRange.getRange()));
                        for (MatchedGroupEdge matchedGroupEdge : match.getMatchedGroupEdgesByPattern(i))
                            matchedItems.add(new MatchedGroupEdge(matchedGroupEdge.getTarget(), matchedGroupEdge.getTargetId(),
                                    patternIndex, matchedGroupEdge.getGroupEdge(), matchedGroupEdge.getPosition()));
                        patternIndex++;
                    }
                }
            }
            if (allMatchesAreNull) return null;
            return new Match(patternIndex, combineMatchScores(matches), matchedItems);
        } else
            if (matchValidationType == FIRST) {
                boolean matchExist = false;
                int bestMatchPort = 0;
                int bestCoordinate = Integer.MAX_VALUE;
                long bestScore = Long.MIN_VALUE;
                for (int i = 0; i < matches.length; i++)
                    if ((matches[i] != null)
                        && ((sortingByScore && (matches[i].getScore() > bestScore))
                        || (!sortingByScore && (matches[i].getRange().getLower() < bestCoordinate)))) {
                        matchExist = true;
                        if (sortingByScore) bestScore = matches[i].getScore();
                        else bestCoordinate = matches[i].getRange().getLower();
                        bestMatchPort = i;
                    }
                if (matchExist)
                    return matches[bestMatchPort];
                else
                    return null;
            } else {
                NSequenceWithQuality target = matches[0].getMatchedRange().getTarget();
                byte targetId = matches[0].getMatchedRange().getTargetId();
                Range[] ranges = new Range[matches.length];
                ArrayList<ArrayList<MatchedGroupEdge>> matchedGroupEdgesFromOperands = new ArrayList<>();

                for (int i = 0; i < matches.length; i++) {
                    matchedGroupEdgesFromOperands.add(new ArrayList<>());
                    matchedGroupEdgesFromOperands.get(i).addAll(matches[i].getMatchedGroupEdges());
                    ranges[i] = matches[i].getRange();
                }

                Arrays.sort(ranges, Comparator.comparingInt(Range::getLower));
                CombinedRange combinedRange = combineRanges(patternAligner, matchedGroupEdgesFromOperands,
                        target, matchValidationType == FOLLOWING, ranges);
                matchedItems.addAll(combinedRange.getMatchedGroupEdges());
                matchedItems.add(new MatchedRange(target, targetId, 0, combinedRange.getRange()));
                return new Match(1, combineMatchScores(matches) + combinedRange.getScorePenalty(),
                        matchedItems);
            }
    }

    /**
     * Combine match scores. It is used in combineMatches function. Different patterns may combine operand scores
     * by sum or by max value of operand scores.
     *
     * @param matches matches from which we will get the scores
     * @return combined score
     */
    protected long combineMatchScores(Match... matches) {
        long resultScore;
        if (combineScoresBySum) {
            resultScore = 0;
            for (Match match : matches)
                if (match != null)
                    resultScore += match.getScore();
        } else {
            resultScore = Long.MIN_VALUE;
            for (Match match : matches)
                if (match != null)
                    if (match.getScore() > resultScore)
                        resultScore = match.getScore();
        }
        return resultScore;
    }

    /**
     * Returns true if null match taken from operand does not guarantee that this operator will not match.
     *
     * @return true if null matches taken from operands must not automatically discard the current combination
     */
    protected boolean areNullMatchesAllowed() {
        return ((matchValidationType == LOGICAL_OR) || (matchValidationType == FIRST));
    }

    /**
     * Fills array for fair sorting. Array will be already filtered: match combinations that contain incompatible
     * ranges will not be saved to array.
     *
     * @param inputPorts ports for input matches
     * @param numberOfPorts number of ports for input matches
     * @param sortingByScore true if we use sorting by score, false if we use sorting by coordinate;
     *                       it used as argument for combineMatches
     * @return array for fair sorting
     */
    protected Match[] fillArrayForFairSorting(List<ApproximateSorterOperandPort> inputPorts, int numberOfPorts,
                                              boolean sortingByScore) {
        ArrayList<ArrayList<Match>> allMatches = new ArrayList<>();
        ArrayList<Match> allMatchesFiltered = new ArrayList<>();
        TableOfIterations tableOfIterations = new TableOfIterations(numberOfPorts);
        Match currentMatch;
        int totalNumberOfCombinations = 1;

        // get all matches from all operands
        for (int i = 0; i < numberOfPorts; i++) {
            allMatches.add(new ArrayList<>());
            do {
                currentMatch = inputPorts.get(i).outputPort.take();
                if ((currentMatch != null) || (areNullMatchesAllowed() && (allMatches.get(i).size() == 0)))
                    allMatches.get(i).add(currentMatch);
            } while (currentMatch != null);
            totalNumberOfCombinations *= allMatches.get(i).size();
        }

        int[] innerArrayIndexes = new int[numberOfPorts];
        Match[] currentMatches = new Match[numberOfPorts];
        for (int i = 0; i < totalNumberOfCombinations; i++) {
            if (tableOfIterations.isCompatible(false, innerArrayIndexes)) {
                for (int j = 0; j < numberOfPorts; j++)
                    currentMatches[j] = allMatches.get(j).get(innerArrayIndexes[j]);
                IncompatibleIndexes incompatibleIndexes = findIncompatibleIndexes(currentMatches, innerArrayIndexes);
                if (incompatibleIndexes != null)
                    tableOfIterations.addIncompatibleIndexes(incompatibleIndexes);
                else {
                    Match combinedMatch = combineMatches(sortingByScore, currentMatches);
                    if ((combinedMatch != null) && (combinedMatch.getScore() >= patternAligner.penaltyThreshold()))
                        allMatchesFiltered.add(combinedMatch);
                }
            }

            // Update innerArrayIndexes to switch to the next combination on next iteration of outer loop
            for (int j = 0; j < numberOfPorts; j++) {
                if (innerArrayIndexes[j] + 1 < allMatches.get(j).size()) {
                    innerArrayIndexes[j]++;
                    break;
                }
                // we need to update next index and reset current index to zero
                innerArrayIndexes[j] = 0;
            }
        }

        return allMatchesFiltered.toArray(new Match[allMatchesFiltered.size()]);
    }

    /**
     * Returns null if this match combination is valid or IncompatibleIndexes structure if it finds
     * 2 matches that have incompatible ranges.
     *
     * @param matches group of matches to check
     * @param indexes indexes of all provided matches for writing to IncompatibleIndexes structure
     * @return IncompatibleIndexes structure
     */
    protected IncompatibleIndexes findIncompatibleIndexes(Match[] matches, int[] indexes) {
        if (matches.length != indexes.length)
            throw new IllegalArgumentException("matches length is " + matches.length + ", indexes length is "
                + indexes.length + "; they must be equal!");

        IncompatibleIndexes result = null;
        switch (matchValidationType) {
            case LOGICAL_OR:
            case LOGICAL_AND:
            case FIRST:
                return null;
            case INTERSECTION:
                Range ranges[] = new Range[matches.length];

                OUTER:
                for (int i = 0; i < matches.length; i++) {
                    if (matches[i] == null) continue;
                    Range currentRange = matches[i].getRange();
                    ranges[i] = currentRange;
                    for (int j = 0; j < i; j++)     // Compare with all previously added matches
                        if (checkFullIntersection(ranges[i], ranges[j])
                                || checkOverlap(matches[0].getMatchedRange().getTarget(), ranges[i], ranges[j])) {
                            result = new IncompatibleIndexes(j, indexes[j], i, indexes[i]);
                            break OUTER;
                        }
                }

                return result;
            case ORDER:
            case FOLLOWING:
                Range currentRange;
                Range previousRange;

                for (int i = 1; i < matches.length; i++) {
                    if (matches[i] == null) continue;
                    NSequenceWithQuality target = matches[0].getMatchedRange().getTarget();
                    currentRange = matches[i].getRange();
                    previousRange = matches[i - 1].getRange();
                    if ((previousRange.getLower() >= currentRange.getLower())
                            || checkFullIntersection(previousRange, currentRange)
                            || checkOverlap(target, previousRange, currentRange)
                            || checkInsertionPenalty(target, previousRange, currentRange)) {
                        result = new IncompatibleIndexes(i - 1, indexes[i - 1], i, indexes[i]);
                        break;
                    }
                }
                return result;
        }
        return null;
    }

    /**
     * Check is overlap too big to invalidate this combination of ranges.
     *
     * @return true if overlap is too big and this combination of ranges is invalid
     */
    private boolean checkOverlap(NSequenceWithQuality target, Range range0, Range range1) {
        Range intersection = range0.intersection(range1);
        return (intersection != null) && (((patternAligner.maxOverlap() != -1) && (patternAligner.maxOverlap()
                < intersection.length())) || (patternAligner.overlapPenalty(target, intersection.getLower(),
                intersection.length()) < patternAligner.penaltyThreshold()));
    }

    /**
     * Check is insertion penalty between ranges too big to invalidate this combination of ranges.
     *
     * @return true if insertion penalty is too big and this combination of ranges is invalid
     */
    private boolean checkInsertionPenalty(NSequenceWithQuality target, Range range0, Range range1) {
        if (matchValidationType == FOLLOWING) {
            int insertionLength = range1.getLower() - range0.getUpper();
            return (insertionLength > 0) && (patternAligner.insertionPenalty(target, range0.getUpper(), insertionLength)
                    < patternAligner.penaltyThreshold());
        } else return false;
    }

    protected class IncompatibleIndexes {
        int port1;
        int index1;
        int port2;
        int index2;

        IncompatibleIndexes(int port1, int index1, int port2, int index2) {
            this.port1 = port1;
            this.index1 = index1;
            this.port2 = port2;
            this.index2 = index2;
        }

        @Override
        public boolean equals(Object other) {
            if (!(other instanceof IncompatibleIndexes))
                return false;

            IncompatibleIndexes that = (IncompatibleIndexes) other;

            return (this.port1 == that.port1) && (this.port2 == that.port2)
                    && (this.index1 == that.index1) && (this.index2 == that.index2);
        }

        @Override
        public int hashCode() {
            int hashCode = 1;

            hashCode = hashCode * 37 + this.port1;
            hashCode = hashCode * 37 + this.port2;
            hashCode = hashCode * 37 + this.index1;
            hashCode = hashCode * 37 + this.index2;

            return hashCode;
        }
    }

    protected class TableOfIterations {
        private final HashSet<ArrayList<Integer>> returnedCombinations;
        private final HashSet<IncompatibleIndexes> incompatibleIndexes;
        private final int numberOfPorts;
        private final boolean portEndReached[];
        private final int portMatchesQuantities[];
        private int totalCombinationsCount = -1;

        TableOfIterations(int numberOfPorts) {
            returnedCombinations = new HashSet<>();
            incompatibleIndexes = new HashSet<>();
            this.numberOfPorts = numberOfPorts;
            this.portEndReached = new boolean[numberOfPorts];   // boolean initialize value is false
            this.portMatchesQuantities = new int[numberOfPorts];
        }

        boolean isPortEndReached(int portNumber) {
            return portEndReached[portNumber];
        }

        int getNumberOfEndedPorts() {
            int endedPorts = 0;
            for (int i = 0; i < numberOfPorts; i++)
                if (isPortEndReached(i)) endedPorts++;
            return endedPorts;
        }

        int getPortMatchesQuantity(int portNumber) {
            return portMatchesQuantities[portNumber];
        }

        void setPortEndReached(int portNumber, int matchesQuantity) {
            portEndReached[portNumber] = true;
            portMatchesQuantities[portNumber] = matchesQuantity;

            if (getNumberOfEndedPorts() == numberOfPorts) {
                totalCombinationsCount = 1;
                for (int currentPortMatchesQuantity : portMatchesQuantities)
                    totalCombinationsCount *= currentPortMatchesQuantity;
            }
        }

        /**
         * If all ports ended and total combinations count is calculated, returns total combinations count,
         * otherwise -1.
         *
         * @return total combinations count
         */
        int getTotalCombinationsCount() {
            return totalCombinationsCount;
        }

        /**
         * This is not a number of actually returned combinations, but number of combinations that are marked
         * as returned. It may be higher because found invalid combinations are marked as returned.
         *
         * @return number of combinations that marked as returned
         */
        int getNumberOfReturnedCombinations() {
            return returnedCombinations.size();
        }

        boolean isCombinationReturned(int... indexes) {
            ArrayList<Integer> indexesList = new ArrayList<>();
            for (int i : indexes)
                indexesList.add(i);
            return isCombinationReturned(indexesList);
        }

        boolean isCombinationReturned(ArrayList<Integer> indexes) {
            if (indexes.size() != numberOfPorts)
                throw new IllegalArgumentException("Number of indexes: " + indexes.size() + ", number of ports: "
                        + numberOfPorts + "; they should be equal!");
            return returnedCombinations.contains(indexes);
        }

        /**
         * Register combination as already returned.
         *
         * @param indexes indexes of matches to register as returned
         */
        void addReturnedCombination(int... indexes) {
            ArrayList<Integer> indexesList = new ArrayList<>();
            for (int i : indexes)
                indexesList.add(i);
            if (isCombinationReturned(indexesList))
                throw new IllegalStateException("Trying to add already returned combination!");
            returnedCombinations.add(indexesList);
        }

        /**
         * Check if this combination of indexes contains incompatible indexes. Incompatible means that we
         * already know that matches with that indexes have misplaced ranges. Also this function automatically
         * marks found incompatible combinations as already returned.
         *
         * @param allNextIncompatible true if operand matches are sorted by coordinate and matchValidationType == ORDER;
         *                            otherwise false. In fair sorting it must be always false. This flag marks that
         *                            next indexes can be considered incompatible to speed up search.
         * @param indexes indexes of matches
         * @return true if there are no incompatible indexes found; false if they are found
         */
        boolean isCompatible(boolean allNextIncompatible, int... indexes) {
            for (IncompatibleIndexes currentIndexes : incompatibleIndexes)
                if (allNextIncompatible)
                    if ((indexes[currentIndexes.port1] >= currentIndexes.index1)
                            && (indexes[currentIndexes.port2] <= currentIndexes.index2)) {
                        // if we find incompatible combination, mark it as already returned
                        addReturnedCombination(indexes);
                        return false;
                    }
                else
                    if ((indexes[currentIndexes.port1] == currentIndexes.index1)
                            && (indexes[currentIndexes.port2] == currentIndexes.index2)) {
                        addReturnedCombination(indexes);
                        return false;
                    }
            return true;
        }

        void addIncompatibleIndexes(IncompatibleIndexes foundIncompatibleIndexes) {
            incompatibleIndexes.add(foundIncompatibleIndexes);
        }
    }
}
