package com.milaboratory.mist.util;

import cc.redberry.pipe.OutputPort;
import com.milaboratory.core.Range;
import com.milaboratory.core.sequence.NSequenceWithQuality;
import com.milaboratory.mist.pattern.CaptureGroupMatch;
import com.milaboratory.mist.pattern.Match;
import com.milaboratory.mist.pattern.MatchValidationType;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;

import static com.milaboratory.mist.pattern.Match.WHOLE_PATTERN_MATCH_GROUP_NAME_PREFIX;
import static com.milaboratory.mist.util.RangeTools.combineRanges;

public abstract class ApproximateSorter {
    protected final boolean multipleReads;
    protected final boolean combineScoresBySum;
    protected final boolean fairSorting;
    protected final MatchValidationType matchValidationType;
    protected final OutputPort<Match>[] inputPorts;
    protected final int numberOfPorts;

    /**
     * This sorter allows to get output port for approximately sorted matches by score or coordinate from
     * input ports. Specific sorters (by score, coordinate and with different rules) are extending this class.
     *
     * @param multipleReads true if we combine matches from multiple reads; false if we combine matches
     *                      from single read
     * @param combineScoresBySum true if combined score must be equal to sum of match scores; false if combined
     *                           score must be the highest of match scores
     * @param fairSorting true if we need slow but fair sorting
     * @param matchValidationType type of validation used to determine that current matches combination is invalid
     * @param inputPorts ports for input matches; we assume that they are already sorted, maybe approximately
     */
    public ApproximateSorter(boolean multipleReads, boolean combineScoresBySum, boolean fairSorting,
                             MatchValidationType matchValidationType, OutputPort<Match>[] inputPorts) {
        this.multipleReads = multipleReads;
        this.combineScoresBySum = combineScoresBySum;
        this.fairSorting = fairSorting;
        this.matchValidationType = matchValidationType;
        this.inputPorts = inputPorts;
        this.numberOfPorts = inputPorts.length;
    }

    /**
     * Get output port for sorted combined matches.
     *
     * @return output port
     */
    public abstract OutputPort<Match> getOutputPort();

    /**
     * Get combined match from a group of input matches. It uses multipleReads flag to determine how to combine matches
     * (by combining ranges for single read or by numbering the whole group matches for multiple reads).
     *
     * @param matches input matches
     * @return combined match
     */
    protected Match combineMatches(Match... matches) {
        Map<String, CaptureGroupMatch> groupMatches = new HashMap<>();

        if (multipleReads) {
            int wholeGroupIndex = 0;
            for (Match match : matches) {
                if (match == null) {
                    groupMatches.put(WHOLE_PATTERN_MATCH_GROUP_NAME_PREFIX + wholeGroupIndex++, null);
                    continue;
                }
                groupMatches.putAll(match.getGroupMatches(true));
                for (int i = 0; i < match.getNumberOfPatterns(); i++)
                    groupMatches.put(WHOLE_PATTERN_MATCH_GROUP_NAME_PREFIX + wholeGroupIndex++,
                            match.getWholePatternMatch(i));
            }
            return new Match(wholeGroupIndex, combineMatchScores(matches), groupMatches);
        } else {
            NSequenceWithQuality target = matches[0].getWholePatternMatch().getTarget();
            byte targetId = matches[0].getWholePatternMatch().getTargetId();
            Range[] ranges = new Range[matches.length];

            for (int i = 0; i < matches.length; i++) {
                groupMatches.putAll(matches[i].getGroupMatches(true));
                ranges[i] = matches[i].getWholePatternMatch().getRange();
            }

            CaptureGroupMatch wholePatternMatch = new CaptureGroupMatch(target, targetId, combineRanges(ranges));
            groupMatches.put(WHOLE_PATTERN_MATCH_GROUP_NAME_PREFIX + 0, wholePatternMatch);
            return new Match(1, combineMatchScores(matches), groupMatches);
        }
    }

    /**
     * Combine match scores. It is used in combineMatches function. Different patterns may combine operand scores
     * by sum or by max value of operand scores.
     *
     * @param matches matches from which we will get the scores
     * @return combined score
     */
    protected float combineMatchScores(Match... matches) {
        float resultScore;
        if (combineScoresBySum) {
            resultScore = 0;
            for (Match match : matches)
                if (match != null)
                    resultScore += match.getScore();
        } else {
            resultScore = Float.NEGATIVE_INFINITY;
            for (Match match : matches)
                if (match != null)
                    if (match.getScore() > resultScore)
                        resultScore = match.getScore();
        }
        return resultScore;
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
            throw new IllegalStateException("matches length is " + matches.length + ", indexes length is "
                + indexes.length + "; they must be equal!");

        IncompatibleIndexes result = null;
        switch (matchValidationType) {
            case ALWAYS:
                return null;
            case INTERSECTION:
                Range ranges[] = new Range[matches.length];

                OUTER:
                for (int i = 0; i < matches.length; i++) {
                    Range currentRange = matches[i].getWholePatternMatch().getRange();
                    ranges[i] = currentRange;
                    for (int j = 0; j < i; j++)  // Compare with all previously added matches
                        if (ranges[j].intersectsWith(currentRange)) {
                            result = new IncompatibleIndexes(j, indexes[j], i, indexes[i]);
                            break OUTER;
                        }
                }

                return result;
            case ORDER:
                Range currentRange;
                Range previousRange;

                for (int i = 1; i < matches.length; i++) {
                    currentRange = matches[i].getWholePatternMatch().getRange();
                    previousRange = matches[i - 1].getWholePatternMatch().getRange();
                    if (previousRange.getUpper() > currentRange.getLower()) {
                        result = new IncompatibleIndexes(i - 1, indexes[i - 1], i, indexes[i]);
                        break;
                    }
                }
                return result;
        }
        return null;
    }

    protected class IncompatibleIndexes {
        public int port1;
        public int index1;
        public int port2;
        public int index2;

        public IncompatibleIndexes(int port1, int index1, int port2, int index2) {
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
            if (indexes.length != numberOfPorts)
                throw new IllegalStateException("Number of indexes: " + indexes.length + ", number of ports: "
                    + numberOfPorts + "; they should be equal!");
            return returnedCombinations.contains(new ArrayList<Integer>() {{ for (int i : indexes) add(i); }});
        }

        void addReturnedCombination(int... indexes) {
            if (isCombinationReturned(indexes))
                throw new IllegalStateException("Trying to add already returned combination!");
            returnedCombinations.add(new ArrayList<Integer>() {{ for (int i : indexes) add(i); }});
        }

        /**
         * Check if this combination of indexes contains incompatible indexes. Incompatible means that we
         * already know that matches with that indexes have misplaced ranges. Also this function automatically
         * marks found incompatible combinations as already returned.
         *
         * @param indexes indexes of matches
         * @return true if there are no incompatible indexes found; false if they are found
         */
        boolean isCompatible(int... indexes) {
            for (IncompatibleIndexes currentIndexes : incompatibleIndexes)
                if (matchValidationType == MatchValidationType.ORDER)
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
