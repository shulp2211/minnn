package com.milaboratory.mist.util;

import cc.redberry.pipe.OutputPort;
import com.milaboratory.mist.pattern.*;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;

public class SorterByCoordinate extends ApproximateSorter {
    /**
     * Sorter by coordinate.
     *
     * @param multipleReads true if we combine matches from multiple reads; false if we combine matches
     *                      from single read
     * @param allowOneNull If true, if operand return null from first take(), it is considered as valid value,
     *                     otherwise null never considered as a match. High level logic operators must set this
     *                     to true, other operators - to false.
     * @param combineScoresBySum true if combined score must be equal to sum of match scores; false if combined
     *                           score must be the highest of match scores
     * @param fairSorting true if we need slow but fair sorting
     * @param matchValidationType type of validation used to determine that current matches combination is invalid
     */
    public SorterByCoordinate(boolean multipleReads, boolean allowOneNull, boolean combineScoresBySum, boolean fairSorting,
                              MatchValidationType matchValidationType) {
        super(multipleReads, allowOneNull, combineScoresBySum, fairSorting, matchValidationType);
    }

    @Override
    public OutputPort<Match> getOutputPort(ArrayList<OutputPort<Match>> inputPorts) {
        int numberOfPorts = inputPorts.size();
        if (numberOfPorts == 0)
            throw new IllegalArgumentException("List of input ports is empty!");
        return new MatchesOutputPort(inputPorts, numberOfPorts);
    }

    private class MatchesOutputPort implements OutputPort<Match> {
        private final ArrayList<ArrayList<Match>> takenMatches;
        private final ArrayList<OutputPort<Match>> inputPorts;
        private final int numberOfPorts;
        private final int[] currentIndexes;
        private final Match[] currentMatches;
        private final TableOfIterations tableOfIterations;
        private boolean alwaysReturnNull = false;

        // data structures used for fair sorting
        private Match[] allMatchesFiltered;
        private int filteredMatchesCount = 0;
        private int nextFairSortedMatch = 0;
        private boolean sortingPerformed = false;

        public MatchesOutputPort(ArrayList<OutputPort<Match>> inputPorts, int numberOfPorts) {
            this.takenMatches = new ArrayList<>();
            for (int i = 0; i < numberOfPorts; i++)
                this.takenMatches.add(new ArrayList<>());
            this.inputPorts = inputPorts;
            this.numberOfPorts = numberOfPorts;
            this.currentIndexes = new int[numberOfPorts];
            this.currentMatches = new Match[numberOfPorts];
            this.tableOfIterations = new TableOfIterations(numberOfPorts);
        }

        @Override
        public Match take() {
            if (alwaysReturnNull) return null;
            if (fairSorting) return takeFairSorted();

            boolean combinationFound = false;
            GET_NEXT_COMBINATION:
            while (!combinationFound) {
                if (tableOfIterations.getTotalCombinationsCount() == tableOfIterations.getNumberOfReturnedCombinations()) {
                    alwaysReturnNull = true;
                    return null;
                }

                for (int i = 0; i < numberOfPorts; i++) {
                    // if we didn't take the needed match before, take it now
                    if (currentIndexes[i] == takenMatches.get(i).size()) {
                        Match takenMatch = inputPorts.get(i).take();
                        if (takenMatch == null)
                            if (takenMatches.get(i).size() == 0) {
                                if (allowOneNull) {
                                    takenMatches.get(i).add(null);
                                    tableOfIterations.setPortEndReached(i, 1);
                                    currentIndexes[i] = 0;
                                } else {
                                    alwaysReturnNull = true;
                                    return null;
                                }
                            } else {
                                tableOfIterations.setPortEndReached(i, currentIndexes[i]);
                                currentIndexes[i]--;
                                calculateNextIndexes();
                                continue GET_NEXT_COMBINATION;
                        } else
                            takenMatches.get(i).add(takenMatch);
                    }
                    currentMatches[i] = takenMatches.get(i).get(currentIndexes[i]);
                }

                IncompatibleIndexes incompatibleIndexes = findIncompatibleIndexes(currentMatches, currentIndexes);
                if (incompatibleIndexes == null)
                    combinationFound = true;
                else {
                    /* mark invalid match as already returned in table of iterations, write found
                     incompatible indexes to table of iterations and continue search */
                    tableOfIterations.addReturnedCombination(currentIndexes);
                    tableOfIterations.addIncompatibleIndexes(incompatibleIndexes);
                    calculateNextIndexes();
                }
            }
            tableOfIterations.addReturnedCombination(currentIndexes);
            calculateNextIndexes();
            return combineMatches(currentMatches);
        }

        private Match takeFairSorted() {
            if (!sortingPerformed) {
                allMatchesFiltered = fillArrayForFairSorting(inputPorts, numberOfPorts);
                filteredMatchesCount = allMatchesFiltered.length;
                if (!multipleReads)
                    Arrays.sort(allMatchesFiltered, Comparator.comparingInt(match -> match.getRange().getLower()));
                else
                    Arrays.sort(allMatchesFiltered, Comparator.comparingInt(this::getMatchCoordinateWeight));
                sortingPerformed = true;
            }

            if (nextFairSortedMatch >= filteredMatchesCount) return null;

            return allMatchesFiltered[nextFairSortedMatch++];
        }

        /**
         * Get weight from match to perform fair sorting for multiple pattern matches by coordinate.
         * In multiple patterns, getWholePatternMatch() can return null values.
         *
         * @return weight: lower end of the range in the first non-null match
         */
        private int getMatchCoordinateWeight(Match match) {
            for (int i = 0; i < match.getNumberOfPatterns(); i++) {
                MatchedRange currentMatch = match.getMatchedRange(i);
                if (NullMatchedRange.class.isAssignableFrom(currentMatch.getClass())) continue;
                return currentMatch.getRange().getLower();
            }

            return 0;
        }

        /**
         * Calculate next indexes for matches arrays: which next combination will be returned.
         */
        private void calculateNextIndexes() {
            /* If all combinations already iterated, there is nothing to calculate */
            if (tableOfIterations.getTotalCombinationsCount() == tableOfIterations.getNumberOfReturnedCombinations())
                return;
            /* Iterate through port values, starting from the last port, ending with the first,
            and skipping all combinations with incompatible indexes */
            int[] innerArrayIndexes = new int[numberOfPorts];
            while (true) {
                if (!tableOfIterations.isCombinationReturned(innerArrayIndexes)
                        && (combinationContainsUnfinishedPort(innerArrayIndexes)
                        || tableOfIterations.isCompatible(matchValidationType == MatchValidationType.ORDER,
                        innerArrayIndexes))) {
                    System.arraycopy(innerArrayIndexes, 0, currentIndexes, 0, numberOfPorts);
                    return;
                }

                // Update innerArrayIndexes to switch to the next combination on next iteration of outer loop
                for (int i = numberOfPorts - 1; i >= 0; i--) {
                    if (!tableOfIterations.isPortEndReached(i)
                            || (innerArrayIndexes[i] + 1 < tableOfIterations.getPortMatchesQuantity(i))) {
                        innerArrayIndexes[i]++;
                        break;
                    }
                    // we need to update next index and reset current index to zero
                    innerArrayIndexes[i] = 0;
                    // if we looped through all combinations, stop the search
                    if (i == 0) {
                        alwaysReturnNull = true;
                        return;
                    }
                }
            }
        }

        /**
         * Returns true if combination contains at least 1 port from which the needed value has not been taken.
         *
         * @param indexes indexes for ports
         * @return true if there is a port for which we need to take the value
         */
        private boolean combinationContainsUnfinishedPort(int... indexes) {
            for (int i = 0; i < numberOfPorts; i++) {
                if (!tableOfIterations.isPortEndReached(i) && indexes[i] == takenMatches.get(i).size())
                    return true;
            }
            return false;
        }
    }
}
