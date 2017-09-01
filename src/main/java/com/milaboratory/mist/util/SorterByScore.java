package com.milaboratory.mist.util;

import cc.redberry.pipe.OutputPort;
import com.milaboratory.mist.pattern.*;

import java.util.*;

import static com.milaboratory.mist.util.DebugUtils.countCall;
import static com.milaboratory.mist.util.DebugUtils.countExecutionTime;

public final class SorterByScore extends ApproximateSorter {
    public SorterByScore(PatternAligner patternAligner, boolean multipleReads, boolean combineScoresBySum,
                         boolean fairSorting, MatchValidationType matchValidationType, int unfairSorterLimit) {
        super(patternAligner, multipleReads, combineScoresBySum, fairSorting, matchValidationType, unfairSorterLimit);
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
        private ArrayList<Integer> currentIndexes;
        private ArrayList<Integer> stage3Indexes;
        private final Match[] currentMatches;
        private final TableOfIterations tableOfIterations;
        private boolean alwaysReturnNull = false;
        private int numberOfSkippedIterations = 0;

        /* maximum number of values to take from single port on stage3;
           it will be increased if all combinations with this depth will end */
        private int stage3CurrentDepth = 3;

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
            this.currentIndexes = new ArrayList<>(Collections.nCopies(numberOfPorts, 0));
            this.stage3Indexes = new ArrayList<>(Collections.nCopies(numberOfPorts, 0));
            this.currentMatches = new Match[numberOfPorts];
            this.tableOfIterations = new TableOfIterations(numberOfPorts);
        }

        @Override
        public Match take() {
            if (alwaysReturnNull) return null;
            if (fairSorting) return takeFairSorted();
            if (unfairSorterTakenValues++ > unfairSorterLimit) {
                alwaysReturnNull = true;
                return null;
            }

            List<Match> tempMatches = new ArrayList<>();
            tempMatches.add(null);
            countCall("take");
            countExecutionTime("take", () -> {
                Match combinedMatch = null;
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
                        if (currentIndexes.get(i) == currentPortMatches.size()) {
                            Match takenMatch = currentPort.outputPort.take();
                            if (takenMatch == null) {
                                if (currentPortMatches.size() == 0) {
                                    if (areNullMatchesAllowed()) {
                                        currentPortMatches.add(null);
                                        tableOfIterations.setPortEndReached(i, 1);
                                        currentIndexes.set(i, 0);
                                    } else {
                                        alwaysReturnNull = true;
                                        return null;
                                    }
                                } else {
                                    int currentIndex = currentIndexes.get(i);
                                    tableOfIterations.setPortEndReached(i, currentIndex);
                                    currentIndexes.set(i, currentIndex - 1);
                                    numberOfSkippedIterations++;
                                    calculateNextIndexes();
                                    continue GET_NEXT_COMBINATION;
                                }
                            } else {
                                currentPortMatches.add(takenMatch);
                                if (currentPortMatches.size() == currentPort.unfairSorterPortLimit)
                                    tableOfIterations.setPortEndReached(i, currentPort.unfairSorterPortLimit);
                            }
                        }
                        currentMatches[i] = currentPortMatches.get(currentIndexes.get(i));
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
                tempMatches.set(0, combinedMatch);

                return null;
            });
            return tempMatches.get(0);

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
            countCall("calculate");
            countExecutionTime("calculate", () -> {
                            /* If all combinations already iterated, there is nothing to calculate */
                if (tableOfIterations.getTotalCombinationsCount() == tableOfIterations.getNumberOfReturnedCombinations())
                    return null;

            /* Stage 1: return combination of 1st values from each port, then combinations of 2nd value from
            one port and 1st values from other ports */
                List<Boolean> needReturn = new ArrayList<>();
                needReturn.add(false);
                countExecutionTime("stage1", () -> {
                    while (tableOfIterations.getNumberOfReturnedCombinations() + numberOfSkippedIterations <= numberOfPorts) {
                        for (int i = 0; i < numberOfPorts; i++)
                            if (i == tableOfIterations.getNumberOfReturnedCombinations() + numberOfSkippedIterations - 1)
                                if (areNullMatchesAllowed() && (takenMatches.get(i).get(0) == null)) {
                                    numberOfSkippedIterations++;
                                    currentIndexes.set(i, 0);
                                } else
                                    currentIndexes.set(i, 1);
                            else
                                currentIndexes.set(i, 0);

                        // if we found valid combination, return it, otherwise continue search
                        if (tableOfIterations.isCompatible(false, currentIndexes)
                                && !tableOfIterations.isCombinationReturned(currentIndexes)) {
                            needReturn.set(0, true);
                            return null;
                        }
                    }
                    return null;
                });
                if (needReturn.get(0))
                    return null;

            /* Stage 2: iterate over ports, trying to pick better score, based on deltas if we count total score
            based on sum, or based on max value if we count total score based on max value */
                countExecutionTime("stage2", () -> {
                    while (tableOfIterations.getNumberOfEndedPorts() < numberOfPorts) {
                        if (combineScoresBySum) {
                            int bestDeltaPort = 0;
                            long bestDelta = Long.MIN_VALUE;
                            for (int i = 0; i < numberOfPorts; i++) {
                                ArrayList<Match> currentPortMatches = takenMatches.get(i);
                                int currentIndex = currentIndexes.get(i);
                                if (tableOfIterations.isPortEndReached(i)) continue;
                                int match1Number = (currentIndex == 0) ? 0 : currentIndex - 1;
                                int match2Number = (currentIndex == 0) ? 1 : currentIndex;
                                Match match1 = currentPortMatches.get(match1Number);
                                Match match2 = currentPortMatches.get(match2Number);
                                long currentDelta = match2.getScore() - match1.getScore();
                                if (currentDelta > bestDelta) {
                                    bestDelta = currentDelta;
                                    bestDeltaPort = i;
                                }
                            }
                            currentIndexes.set(bestDeltaPort, currentIndexes.get(bestDeltaPort) + 1);
                        } else {
                            int bestScorePort = 0;
                            long bestScore = Long.MIN_VALUE;
                            for (int i = 0; i < numberOfPorts; i++) {
                                if (tableOfIterations.isPortEndReached(i)) continue;
                                Match currentMatch = takenMatches.get(i).get(currentIndexes.get(i));
                                if (currentMatch.getScore() > bestScore) {
                                    bestScore = currentMatch.getScore();
                                    bestScorePort = i;
                                }
                            }
                            currentIndexes.set(bestScorePort, currentIndexes.get(bestScorePort) + 1);
                        }

                        // if we found valid combination, return it, otherwise continue search
                        if (tableOfIterations.isCompatible(false, currentIndexes)
                                && !tableOfIterations.isCombinationReturned(currentIndexes)) {
                            needReturn.set(0, true);
                            return null;
                        }
                    }

                    return null;
                });
                if (needReturn.get(0))
                    return null;


            /* Stage 3: iterate over all remaining combinations of ports */
                countExecutionTime("stage3", () -> {
                    while (true) {
                        if (!tableOfIterations.isCombinationReturned(stage3Indexes)
                                && tableOfIterations.isCompatible(false, stage3Indexes)) {
                            currentIndexes = new ArrayList<>(stage3Indexes);
                            return null;
                        }

                        // Update innerArrayIndexes to switch to the next combination on next iteration of outer loop
                        for (int i = 0; i < numberOfPorts; i++) {
                            int currentIndex = stage3Indexes.get(i);
                            if (currentIndex + 1 < Math.min(stage3CurrentDepth,
                                    tableOfIterations.getPortMatchesQuantity(i))) {
                                stage3Indexes.set(i, currentIndex + 1);
                                break;
                            }
                            // we need to update next index and reset current index to zero
                            stage3Indexes.set(i, 0);
                            // if we looped through all combinations, increase the depth or stop the search
                            if (i == numberOfPorts - 1) {
                                boolean allPortsEnded = true;
                                for (int j = 0; j < numberOfPorts; j++)
                                    if (tableOfIterations.getPortMatchesQuantity(j) > stage3CurrentDepth) {
                                        stage3CurrentDepth++;
                                        allPortsEnded = false;
                                    }
                                if (allPortsEnded) {
                                    alwaysReturnNull = true;
                                    return null;
                                }
                            }
                        }
                    }
                });
                return null;
            });
        }
    }
}
