package com.milaboratory.mist.pattern;

import cc.redberry.pipe.OutputPort;
import com.milaboratory.core.Range;
import com.milaboratory.core.sequence.NSequenceWithQuality;

import java.util.ArrayList;

public class PlusPattern extends MultiplePatternsOperator {
    public PlusPattern(SinglePattern... operandPatterns) {
        super(operandPatterns);
    }

    @Override
    public MatchingResult match(NSequenceWithQuality input, int from, int to, byte targetId) {
        final PlusMatchesSearch matchesSearch = new PlusMatchesSearch(operandPatterns, input, from, to, targetId);
        final MatchesOutputPort allMatchesByScore = new MatchesOutputPort(matchesSearch, true);
        final MatchesOutputPort allMatchesByCoordinate = new MatchesOutputPort(matchesSearch, false);

        // If one pattern doesn't match, PlusPattern doesn't match
        for (SinglePattern operandPattern : operandPatterns) {
            MatchingResult result = operandPattern.match(input, from, to, targetId);
            if (!result.isFound())
                return new SimpleMatchingResult();
        }

        return new SimpleMatchingResult(allMatchesByScore, allMatchesByCoordinate);
    }

    private final class PlusMatchesSearch extends MatchesSearchWithQuickBestMatch {
        private final SinglePattern[] operandPatterns;
        private final NSequenceWithQuality input;
        private final int from;
        private final int to;
        private final byte targetId;
        private ArrayList<ArrayList<Match>> matches = new ArrayList<>();
        private ArrayList<OutputPort<Match>> matchOutputPorts = new ArrayList<>();
        private MatchingResult[] matchingResults;
        private int[] matchArraySizes;
        private int[] innerArrayIndexes;
        // we can skip checks for matches whose lower range border is lower than previous operand's upper border
        private int[] skippedMatches;
        private int totalCombinationCount = 1;

        PlusMatchesSearch(SinglePattern[] operandPatterns, NSequenceWithQuality input, int from, int to, byte targetId) {
            this.operandPatterns = operandPatterns;
            this.input = input;
            this.from = from;
            this.to = to;
            this.targetId = targetId;
        }

        @Override
        protected void performSearch(boolean quickSearch) {
            float bestScore = 0;
            int numOperands = operandPatterns.length;

            // initialize arrays and get matches for all operands
            if (!quickSearchPerformed) {
                matchingResults = new MatchingResult[numOperands];
                matchArraySizes = new int[numOperands];
                innerArrayIndexes = new int[numOperands];
                skippedMatches = new int[numOperands];
                for (int i = 0; i < numOperands; i++) {
                    matches.add(new ArrayList<>());
                    matchingResults[i] = operandPatterns[i].match(input, from, to, targetId);
                    matchArraySizes[i] = Math.toIntExact(matchingResults[i].getMatchesNumber());
                    // get matches ordered by their left border position from left to right
                    matchOutputPorts.add(matchingResults[i].getMatches(false));
                    totalCombinationCount *= matchArraySizes[i];
                }
            }

            /* Loop through all combinations, fill allMatches and find bestMatch,
               or leave bestMatch = null if nothing found */
            OUTER:
            for (int i = 0; i < totalCombinationCount; i++) {
                Match[] currentMatches = new Match[numOperands];
                Range[] currentRanges = new Range[numOperands];
                boolean rangesMisplaced = false;
                for (int j = 0; j < numOperands; j++) {
                    // if current array element doesn't exist, we didn't take that match, so let's take it now
                    if (innerArrayIndexes[j] == matches.get(j).size())
                        matches.get(j).add(matchOutputPorts.get(j).take());
                    currentMatches[j] = matches.get(j).get(innerArrayIndexes[j]);
                    currentRanges[j] = currentMatches[j].getWholePatternMatch().getRange();
                    // check for misplaced ranges
                    if (j > 0)
                        if (currentRanges[j - 1].getUpper() > currentRanges[j].getLower()) {
                            skippedMatches[j] = innerArrayIndexes[j] + 1;  // number of skipped matches counts from 1
                            rangesMisplaced = true;
                            if (skippedMatches[j] == matchArraySizes[j]) {
                                // if we skipped all matches for this operand, we can interrupt this search iteration
                                break;
                            }
                        }
                }

                if (!rangesMisplaced) {
                    matchFound = true;
                    // for quick search stop on first found valid match
                    if (quickSearch) {
                        quickSearchPerformed = true;
                        return;
                    }
                    Match currentMatch = combineMatches(input, targetId, currentMatches);
                    if (!quickBestMatchFound) {
                        float currentSum = sumMatchesScore(currentMatches);
                        if (currentSum > bestScore) {
                            bestMatch = currentMatch;
                            bestScore = currentSum;
                        }
                    }
                    allMatches.add(currentMatch);
                }

                /* Update innerArrayIndexes to switch to the next combination on next iteration of outer loop.
                   Order is reversed because we check skipped matches by comparing with previous operand. */
                int indexCountdown = numOperands - 1;
                while (indexCountdown >= 0) {
                    if (innerArrayIndexes[indexCountdown] + 1 < matchArraySizes[indexCountdown]) {
                        innerArrayIndexes[indexCountdown]++;
                        /* we must forget skipped matches that are 2 and more indexes to the right
                        to avoid skipping valid combinations */
                        for (int j = indexCountdown + 2; j < numOperands; j++) {
                            skippedMatches[j] = 0;
                            innerArrayIndexes[j] = 0;
                        }
                        break;
                    }
                    // if we reached the last combination, stop the search
                    if (indexCountdown == 0) break OUTER;
                    // we need to update next index and reset current index to the first not skipped match
                    innerArrayIndexes[indexCountdown] = skippedMatches[indexCountdown];
                    // if we skipped all matches on this iteration, we must go 2 indexes to the left immediately
                    if (innerArrayIndexes[indexCountdown] == matchArraySizes[indexCountdown]) {
                        indexCountdown--;
                        if (indexCountdown == 0) break OUTER;
                        innerArrayIndexes[indexCountdown] = skippedMatches[indexCountdown];
                    }
                    indexCountdown--;
                }
            }

            quickSearchPerformed = true;
            quickBestMatchSearchPerformed = true;
            fullSearchPerformed = true;
        }

        @Override
        protected void performQuickBestMatchSearch() {
            final Match[] bestMatches = new Match[operandPatterns.length];
            boolean rangeMisplaced = false;

            for (int patternNumber = 0; patternNumber < operandPatterns.length; patternNumber++) {
                bestMatches[patternNumber] = operandPatterns[patternNumber].match(input, from, to, targetId).getBestMatch();
                if (patternNumber > 0) {
                    int previousRangeUpper = bestMatches[patternNumber - 1].getWholePatternMatch().getRange().getUpper();
                    int currentRangeLower = bestMatches[patternNumber].getWholePatternMatch().getRange().getLower();
                    if (previousRangeUpper > currentRangeLower) {
                        rangeMisplaced = true;
                        break;
                    }
                }
            }

            if (!rangeMisplaced) {
                quickBestMatchFound = true;
                quickSearchPerformed = true;
                matchFound = true;
                bestMatch = combineMatches(input, targetId, bestMatches);
            }

            quickBestMatchSearchPerformed = true;
        }
    }
}
