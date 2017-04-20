package com.milaboratory.mist.util;

import cc.redberry.pipe.OutputPort;
import com.milaboratory.mist.pattern.Match;
import com.milaboratory.mist.pattern.MatchValidationType;

import java.util.ArrayList;

public class SorterByCoordinate extends ApproximateSorter {
    /**
     * Sorter by coordinate.
     *
     * @param multipleReads true if we combine matches from multiple reads; false if we combine matches
     *                      from single read
     * @param combineScoresBySum true if combined score must be equal to sum of match scores; false if combined
     *                           score must be the highest of match scores
     * @param fairSorting true if we need slow but fair sorting
     * @param matchValidationType type of validation used to determine that current matches combination is invalid
     * @param inputPorts ports for input matches; we assume that they are already sorted, maybe approximately
     */
    public SorterByCoordinate(boolean multipleReads, boolean combineScoresBySum, boolean fairSorting,
                              MatchValidationType matchValidationType, OutputPort<Match>[] inputPorts) {
        super(multipleReads, combineScoresBySum, fairSorting, matchValidationType, inputPorts);
    }

    @Override
    public OutputPort<Match> getOutputPort() {
        return new MatchesOutputPort(inputPorts, numberOfPorts);
    }

    private class MatchesOutputPort implements OutputPort<Match> {
        private final ArrayList<ArrayList<Match>> takenMatches;
        private final OutputPort<Match>[] inputPorts;
        private final int numberOfPorts;
        private final int[] currentIndexes;
        private final Match[] currentMatches;
        private final TableOfIterations tableOfIterations;

        public MatchesOutputPort(OutputPort<Match>[] inputPorts, int numberOfPorts) {
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
            if (fairSorting) return takeFairSorted();

            boolean combinationFound = false;
            GET_NEXT_COMBINATION:
            while (!combinationFound) {
                if (tableOfIterations.getTotalCombinationsCount() == tableOfIterations.getNumberOfReturnedCombinations())
                    return null;
                for (int i = 0; i < numberOfPorts; i++) {
                    // if we didn't take the needed match before, take it now
                    if (currentIndexes[i] == takenMatches.get(i).size()) {
                        Match takenMatch = inputPorts[currentIndexes[i]].take();
                        if (takenMatch == null) {
                            tableOfIterations.setPortEndReached(i, currentIndexes[i]);
                            calculateNextIndexes();
                            break GET_NEXT_COMBINATION;
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
            return null;
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
                        && tableOfIterations.isCompatible(innerArrayIndexes)) {
                    System.arraycopy(innerArrayIndexes, 0, currentIndexes, 0, numberOfPorts);
                    return;
                }

                // Update innerArrayIndexes to switch to the next combination on next iteration of outer loop
                for (int j = numberOfPorts - 1; j >= 0; j--) {
                    if (!tableOfIterations.isPortEndReached(j)
                            || (innerArrayIndexes[j] + 1 < tableOfIterations.getPortMatchesQuantity(j))) {
                        innerArrayIndexes[j]++;
                        break;
                    }
                    // we need to update next index and reset current index to zero
                    innerArrayIndexes[j] = 0;
                    // if we looped through all combinations, stop the search
                    if (j == 0) return;
                }
            }
        }
    }
}
