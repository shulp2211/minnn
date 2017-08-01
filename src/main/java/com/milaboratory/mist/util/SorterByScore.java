package com.milaboratory.mist.util;

import cc.redberry.pipe.OutputPort;
import com.milaboratory.mist.pattern.*;

import java.util.*;

public final class SorterByScore extends ApproximateSorter {
    public SorterByScore(PatternAligner patternAligner, boolean multipleReads, boolean combineScoresBySum,
                         boolean fairSorting, MatchValidationType matchValidationType) {
        super(patternAligner, multipleReads, combineScoresBySum, fairSorting, matchValidationType);
    }

    @Override
    public OutputPort<Match> getOutputPort(List<ApproximateSorterOperandPort> inputPorts) {
        int numberOfPorts = inputPorts.size();
        if (numberOfPorts == 0)
            throw new IllegalArgumentException("List of input ports is empty!");
        return new MatchesOutputPort(inputPorts, numberOfPorts);
    }

    private class MatchesOutputPort implements OutputPort<Match> {
        private final ArrayList<ArrayList<Match>> takenMatches;
        private final List<ApproximateSorterOperandPort> inputPorts;
        private final int numberOfPorts;
        private final int[] currentIndexes;
        private final Match[] currentMatches;
        private final TableOfIterations tableOfIterations;
        private boolean alwaysReturnNull = false;
        private int numberOfSkippedIterations = 0;

        // data structures used for fair sorting
        private Match[] allMatchesFiltered;
        private int filteredMatchesCount = 0;
        private int nextFairSortedMatch = 0;
        private boolean sortingPerformed = false;

        MatchesOutputPort(List<ApproximateSorterOperandPort> inputPorts, int numberOfPorts) {
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
            Match combinedMatch = null;

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
                    ArrayList<Match> currentPortMatches = takenMatches.get(i);
                    ApproximateSorterOperandPort currentPort = inputPorts.get(i);
                    // if we didn't take the needed match before, take it now
                    if (currentIndexes[i] == currentPortMatches.size()) {
                        Match takenMatch = currentPort.outputPort.take();
                        if (takenMatch == null)
                            if (currentPortMatches.size() == 0) {
                                if (areNullMatchesAllowed()) {
                                    currentPortMatches.add(null);
                                    tableOfIterations.setPortEndReached(i, 1);
                                    currentIndexes[i] = 0;
                                } else {
                                    alwaysReturnNull = true;
                                    return null;
                                }
                            } else {
                                tableOfIterations.setPortEndReached(i, currentIndexes[i]);
                                currentIndexes[i]--;
                                numberOfSkippedIterations++;
                                calculateNextIndexes();
                                continue GET_NEXT_COMBINATION;
                        } else {
                            currentPortMatches.add(takenMatch);
                            if (currentPortMatches.size() == currentPort.unfairSorterPortLimit)
                                tableOfIterations.setPortEndReached(i, currentPort.unfairSorterPortLimit);
                        }
                    }
                    currentMatches[i] = currentPortMatches.get(currentIndexes[i]);
                }

                IncompatibleIndexes incompatibleIndexes = findIncompatibleIndexes(currentMatches, currentIndexes);
                if (incompatibleIndexes == null) {
                    combinedMatch = combineMatches(true, currentMatches);
                    if ((combinedMatch != null) && (combinedMatch.getScore() >= patternAligner.penaltyThreshold()))
                        combinationFound = true;
                    else {
                        /* current combination doesn't fit the score threshold, mark it as returned
                         and continue search */
                        tableOfIterations.addReturnedCombination(currentIndexes);
                        calculateNextIndexes();
                    }
                } else {
                    /* mark invalid match as already returned in table of iterations, write found
                     incompatible indexes to table of iterations and continue search */
                    tableOfIterations.addReturnedCombination(currentIndexes);
                    tableOfIterations.addIncompatibleIndexes(incompatibleIndexes);
                    calculateNextIndexes();
                }
            }
            tableOfIterations.addReturnedCombination(currentIndexes);
            calculateNextIndexes();
            return combinedMatch;
        }

        private Match takeFairSorted() {
            if (!sortingPerformed) {
                allMatchesFiltered = fillArrayForFairSorting(inputPorts, numberOfPorts, true);
                filteredMatchesCount = allMatchesFiltered.length;
                Arrays.sort(allMatchesFiltered, Comparator.comparingLong(Match::getScore).reversed());
                sortingPerformed = true;
            }

            if (nextFairSortedMatch >= filteredMatchesCount) return null;

            return allMatchesFiltered[nextFairSortedMatch++];
        }

        /**
         * Calculate next indexes for matches arrays: which next combination will be returned.
         */
        private void calculateNextIndexes() {
            /* If all combinations already iterated, there is nothing to calculate */
            if (tableOfIterations.getTotalCombinationsCount() == tableOfIterations.getNumberOfReturnedCombinations())
                return;

            /* Stage 1: return combination of 1st values from each port, then combinations of 2nd value from
            one port and 1st values from other ports */
            while (tableOfIterations.getNumberOfReturnedCombinations() + numberOfSkippedIterations <= numberOfPorts) {
                for (int i = 0; i < numberOfPorts; i++)
                    if (i == tableOfIterations.getNumberOfReturnedCombinations() + numberOfSkippedIterations - 1)
                        if (areNullMatchesAllowed() && (takenMatches.get(i).get(0) == null)) {
                            numberOfSkippedIterations++;
                            currentIndexes[i] = 0;
                        } else
                            currentIndexes[i] = 1;
                    else
                        currentIndexes[i] = 0;

                // if we found valid combination, return it, otherwise continue search
                if (tableOfIterations.isCompatible(false, currentIndexes)
                        && !tableOfIterations.isCombinationReturned(currentIndexes))
                    return;
            }

            /* Stage 2: iterate over ports, trying to pick better score, based on deltas if we count total score
            based on sum, or based on max value if we count total score based on max value */
            while (tableOfIterations.getNumberOfEndedPorts() < numberOfPorts) {
                if (combineScoresBySum) {
                    int bestDeltaPort = 0;
                    long bestDelta = Long.MIN_VALUE;
                    for (int i = 0; i < numberOfPorts; i++) {
                        ArrayList<Match> currentPortMatches = takenMatches.get(i);
                        if (tableOfIterations.isPortEndReached(i)) continue;
                        int match1Number = (currentIndexes[i] == 0) ? 0 : currentIndexes[i] - 1;
                        int match2Number = (currentIndexes[i] == 0) ? 1 : currentIndexes[i];
                        Match match1 = currentPortMatches.get(match1Number);
                        Match match2 = currentPortMatches.get(match2Number);
                        long currentDelta = match2.getScore() - match1.getScore();
                        if (currentDelta > bestDelta) {
                            bestDelta = currentDelta;
                            bestDeltaPort = i;
                        }
                    }
                    currentIndexes[bestDeltaPort] += 1;
                } else {
                    int bestScorePort = 0;
                    long bestScore = Long.MIN_VALUE;
                    for (int i = 0; i < numberOfPorts; i++) {
                        if (tableOfIterations.isPortEndReached(i)) continue;
                        Match currentMatch = takenMatches.get(i).get(currentIndexes[i]);
                        if (currentMatch.getScore() > bestScore) {
                            bestScore = currentMatch.getScore();
                            bestScorePort = i;
                        }
                    }
                    currentIndexes[bestScorePort] += 1;
                }

                // if we found valid combination, return it, otherwise continue search
                if (tableOfIterations.isCompatible(false, currentIndexes)
                        && !tableOfIterations.isCombinationReturned(currentIndexes))
                    return;
            }

            /* Stage 3: iterate over all remaining combinations of ports */
            int[] innerArrayIndexes = new int[numberOfPorts];
            while (true) {
                if (!tableOfIterations.isCombinationReturned(innerArrayIndexes)
                        && tableOfIterations.isCompatible(false, innerArrayIndexes)) {
                    System.arraycopy(innerArrayIndexes, 0, currentIndexes, 0, numberOfPorts);
                    return;
                }

                // Update innerArrayIndexes to switch to the next combination on next iteration of outer loop
                for (int i = 0; i < numberOfPorts; i++) {
                    if (innerArrayIndexes[i] + 1 < tableOfIterations.getPortMatchesQuantity(i)) {
                        innerArrayIndexes[i]++;
                        break;
                    }
                    // we need to update next index and reset current index to zero
                    innerArrayIndexes[i] = 0;
                    // if we looped through all combinations, stop the search
                    if (i == numberOfPorts - 1) {
                        alwaysReturnNull = true;
                        return;
                    }
                }
            }
        }
    }
}
