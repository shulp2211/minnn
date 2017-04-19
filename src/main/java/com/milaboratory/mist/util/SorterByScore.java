package com.milaboratory.mist.util;

import cc.redberry.pipe.OutputPort;
import com.milaboratory.mist.pattern.Match;
import com.milaboratory.mist.pattern.MatchValidationType;

import java.util.ArrayList;

public class SorterByScore extends ApproximateSorter {
    public SorterByScore(boolean multipleReads, boolean combineScoresBySum, boolean fairSorting,
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
            /* Stage 1: return combination of 1st values from each port, then combinations of 2nd value from
            one port and 1st values from other ports */
            if (tableOfIterations.getNumberOfReturnedCombinations() + tableOfIterations.getNumberOfEndedPorts() <= numberOfPorts) {
                for (int i = 0; i < numberOfPorts; i++)
                    if (i == tableOfIterations.getNumberOfReturnedCombinations() + tableOfIterations.getNumberOfEndedPorts() - 1)
                        currentIndexes[i] = 1;
                    else
                        currentIndexes[i] = 0;
                return;
            }

            if (tableOfIterations.getNumberOfEndedPorts() < numberOfPorts) {
                /* Stage 2: iterate over ports, trying to pick better score, based on deltas if we count total score
                based on sum, or based on max value if we count total score based on max value */
                if (combineScoresBySum) {
                    int bestDeltaPort = 0;
                    float bestDelta = Float.NEGATIVE_INFINITY;
                    for (int i = 0; i < numberOfPorts; i++) {
                        if (tableOfIterations.isPortEndReached(i)) continue;
                        int match1Number = (currentIndexes[i] == 0) ? 0 : currentIndexes[i] - 1;
                        int match2Number = (currentIndexes[i] == 0) ? 1 : currentIndexes[i];
                        Match match1 = takenMatches.get(i).get(match1Number);
                        Match match2 = takenMatches.get(i).get(match2Number);
                        float currentDelta = match2.getScore() - match1.getScore();
                        if (currentDelta > bestDelta) {
                            bestDelta = currentDelta;
                            bestDeltaPort = i;
                        }
                    }
                    currentIndexes[bestDeltaPort] += 1;
                } else {
                    int bestScorePort = 0;
                    float bestScore = Float.NEGATIVE_INFINITY;
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
            } else {
                /* Stage 3: iterate over all remaining combinations of ports */
                int[] innerArrayIndexes = new int[numberOfPorts];
                for (int i = 0; i < tableOfIterations.getTotalCombinationsCount(); i++) {
                    if (!tableOfIterations.isCombinationReturned(innerArrayIndexes)) {
                        System.arraycopy(innerArrayIndexes, 0, currentIndexes, 0, numberOfPorts);
                        return;
                    }

                    // Update innerArrayIndexes to switch to the next combination on next iteration of outer loop
                    for (int j = 0; j < numberOfPorts; j++) {
                        if (innerArrayIndexes[j] + 1 < tableOfIterations.getPortMatchesQuantity(j)) {
                            innerArrayIndexes[j]++;
                            break;
                        }
                        // we need to update next index and reset current index to zero
                        innerArrayIndexes[j] = 0;
                    }
                }
            }
        }
    }
}
