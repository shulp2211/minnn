package com.milaboratory.mist.util;

import cc.redberry.pipe.OutputPort;
import com.milaboratory.mist.pattern.*;

import java.util.*;

import static com.milaboratory.mist.pattern.MatchValidationType.*;
import static com.milaboratory.mist.util.UnfairSorterConfiguration.unfairSorterPortLimits;

public final class SorterByCoordinate extends ApproximateSorter {
    public SorterByCoordinate(ApproximateSorterConfiguration conf) {
        super(conf, false);
    }

    @Override
    public OutputPort<Match> getOutputPort() {
        if (conf.operandPatterns.length == 0)
            throw new IllegalArgumentException("Operand patterns array is empty!");
        return new MatchesOutputPort(conf.operandPatterns);
    }

    private class MatchesOutputPort implements OutputPort<Match> {
        private final Pattern[] operandPatterns;
        private final int numberOfOperands;
        private int currentIndexes[];
        private boolean alwaysReturnNull = false;

        // data structures used for fair sorting
        private Match[] allMatchesFiltered;
        private int filteredMatchesCount = 0;
        private int nextFairSortedMatch = 0;
        private boolean sortingPerformed = false;

        MatchesOutputPort(Pattern[] operandPatterns) {
            this.operandPatterns = operandPatterns;
            this.numberOfOperands = operandPatterns.length;
            this.currentIndexes = new int[numberOfOperands];
        }

        @Override
        public Match take() {
            if (alwaysReturnNull) return null;
            if (conf.fairSorting) return takeFairSorted();
            if (unfairSorterTakenValues++ > conf.unfairSorterLimit) {
                alwaysReturnNull = true;
                return null;
            }

            long penaltyThreshold = conf.patternAligner.penaltyThreshold();
            boolean combinationFound = false;
            while (!combinationFound) {
                if (areCompatible(currentIndexes)) {
                    Match[] currentMatches = getMatchesByIndexes(currentIndexes);
                    IncompatibleIndexes incompatibleIndexes = findIncompatibleIndexes(currentMatches, currentIndexes);
                    if (incompatibleIndexes != null)
                        allIncompatibleIndexes.add(incompatibleIndexes);
                    else {
                        Match combinedMatch = combineMatches(currentMatches);
                        if ((combinedMatch != null) && (combinedMatch.getScore() >= penaltyThreshold))
                            ....
                    }


                }
            }


            Match combinedMatch = null;
            boolean combinationFound = false;
            GET_NEXT_COMBINATION:
            while (!combinationFound) {
                if (tableOfIterations.getTotalCombinationsCount() == tableOfIterations.getNumberOfReturnedCombinations()) {
                    alwaysReturnNull = true;
                    return null;
                }

                for (int i = 0; i < numberOfOperands; i++) {
                    Pattern currentPattern = operandPatterns[i];


                    // if we didn't take the needed match before, take it now
                    if (currentIndexes.get(i) == currentPatternMatches.size()) {
                        Match takenMatch = currentPort.outputPort.take();
                        if (takenMatch == null) {
                            if (currentPatternMatches.size() == 0) {
                                if (areNullMatchesAllowed()) {
                                    currentPatternMatches.add(null);
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
                                calculateNextIndexes();
                                continue GET_NEXT_COMBINATION;
                            }
                        } else {
                            currentPatternMatches.add(takenMatch);
                            int currentPortLimit = unfairSorterPortLimits.get(currentPattern.getClass());
                            if (currentPatternMatches.size() == currentPortLimit)
                                tableOfIterations.setPortEndReached(i, currentPortLimit);
                        }
                    }
                    currentMatches[i] = currentPatternMatches.get(currentIndexes.get(i));
                }

                IncompatibleIndexes incompatibleIndexes = findIncompatibleIndexes(currentMatches, currentIndexes);
                if (incompatibleIndexes == null) {
                    combinedMatch = combineMatches(currentMatches);
                    if ((combinedMatch != null) && (combinedMatch.getScore() >= penaltyThreshold))
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
                allMatchesFiltered = fillArrayForFairSorting();
                filteredMatchesCount = allMatchesFiltered.length;
                if (!conf.multipleReads)
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
                if (currentMatch instanceof NullMatchedRange)
                    continue;
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
            while (true) {
                if (!tableOfIterations.isCombinationReturned(innerArrayIndexes)
                        && (combinationContainsUnfinishedPort(innerArrayIndexes)
                        || tableOfIterations.isCompatible(conf.matchValidationType == ORDER,
                            innerArrayIndexes))) {
                    currentIndexes = new ArrayList<>(innerArrayIndexes);
                    return;
                }

                // Update innerArrayIndexes to switch to the next combination on next iteration of outer loop
                for (int i = numberOfOperands - 1; i >= 0; i--) {
                    int currentIndex = innerArrayIndexes.get(i);
                    if (!tableOfIterations.isPortEndReached(i)
                            || (currentIndex + 1 < tableOfIterations.getPortMatchesQuantity(i))) {
                        innerArrayIndexes.set(i, currentIndex + 1);
                        break;
                    }
                    // we need to update next index and reset current index to zero
                    innerArrayIndexes.set(i, 0);
                    // if we looped through all combinations, stop the search
                    if (i == 0) {
                        alwaysReturnNull = true;
                        return;
                    }
                }
            }
        }

//        /**
//         * Returns true if combination contains at least 1 port from which the needed value has not been taken.
//         *
//         * @param indexes indexes for ports
//         * @return true if there is a port for which we need to take the value
//         */
//        private boolean combinationContainsUnfinishedPort(ArrayList<Integer> indexes) {
//            for (int i = 0; i < numberOfOperands; i++) {
//                if (!tableOfIterations.isPortEndReached(i) && (indexes.get(i) == takenMatches.get(i).size()))
//                    return true;
//            }
//            return false;
//        }
    }
}
