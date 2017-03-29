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
    public MatchingResult match(NSequenceWithQuality input, int from, int to, byte targetId, boolean quickMatch) {
        final PlusMatchesSearch matchesSearch = new PlusMatchesSearch(operandPatterns, input, from, to, targetId);
        final OperatorOutputPort allMatchesByScore = new OperatorOutputPort(matchesSearch, true);
        final OperatorOutputPort allMatchesByCoordinate = new OperatorOutputPort(matchesSearch, false);
        final Match[] bestMatches = new Match[operandPatterns.length];
        boolean rangeMisplaced = false;

        // If one pattern doesn't match, PlusPattern doesn't match
        for (SinglePattern operandPattern : operandPatterns) {
            MatchingResult result = operandPattern.match(input, from, to, targetId, true);
            if (!result.isFound())
                if (quickMatch)
                    return new QuickMatchingResult(false);
                else
                    return new MultiplePatternsMatchingResult(null, new OperatorOutputPort(), new OperatorOutputPort());
        }

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

        if (!rangeMisplaced)
            if (quickMatch)
                return new QuickMatchingResult(true);
            else
                return new MultiplePatternsMatchingResult(combineMatches(input, targetId, bestMatches), allMatchesByScore, allMatchesByCoordinate);
        else {
            // Best matches from operands has misplaced ranges, calculate all matches now and get the best
            return new MultiplePatternsMatchingResult(matchesSearch.getBestMatch(quickMatch), allMatchesByScore, allMatchesByCoordinate);
        }
    }

    private final class PlusMatchesSearch implements MatchesSearch {
        private final SinglePattern[] operandPatterns;
        private final NSequenceWithQuality input;
        private final int from;
        private final int to;
        private final byte targetId;
        private ArrayList<Match> allMatches = new ArrayList<>();
        private Match bestMatch = null;
        private boolean searchPerformed = false;

        PlusMatchesSearch(SinglePattern[] operandPatterns, NSequenceWithQuality input, int from, int to, byte targetId) {
            this.operandPatterns = operandPatterns;
            this.input = input;
            this.from = from;
            this.to = to;
            this.targetId = targetId;
        }

        @Override
        public Match[] getAllMatches() {
            if (!searchPerformed) performSearch(false);
            return allMatches.toArray(new Match[allMatches.size()]);
        }

        @Override
        public long getMatchesNumber() {
            if (!searchPerformed) performSearch(false);
            return allMatches.size();
        }

        public Match getBestMatch(boolean quickMatch) {
            if (!searchPerformed) performSearch(quickMatch);
            return bestMatch;
        }

        /**
         * Find all matches and best match, calculate matches number.
         */
        private void performSearch(boolean quickMatch) {
            int bestScore = 0;
            int numOperands = operandPatterns.length;
            ArrayList<ArrayList<Match>> matches = new ArrayList<>();
            ArrayList<OutputPort<Match>> matchOutputPorts = new ArrayList<>();
            MatchingResult[] matchingResults = new MatchingResult[numOperands];
            int[] matchArraySizes = new int[numOperands];
            int[] innerArrayIndexes = new int[numOperands];
            // we can skip checks for matches whose lower range border is lower than previous operand's upper border
            int[] skippedMatches = new int[numOperands];
            int totalCombinationCount = 1;

            for (int i = 0; i < numOperands; i++) {
                matches.add(new ArrayList<>());
                matchingResults[i] = operandPatterns[i].match(input, from, to, targetId);
                matchArraySizes[i] = Math.toIntExact(matchingResults[i].getMatchesNumber());
                // get matches ordered by their left border position from left to right
                matchOutputPorts.add(matchingResults[i].getMatches(false));
                totalCombinationCount *= matchArraySizes[i];
            }

            // Loop through all combinations and find the best, or leave bestMatch = null if nothing found
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
                            if (skippedMatches[j] == matchArraySizes[j]) {
                                // if we skipped all matches for this operand, we can stop the search
                                break OUTER;
                            }
                            rangesMisplaced = true;
                        }
                }

                if (!rangesMisplaced) {
                    // for quick match stop on first found valid match
                    if (quickMatch) {
                        bestMatch = combineMatches(input, targetId, currentMatches);
                        return;
                    }
                    Match currentMatch = combineMatches(input, targetId, currentMatches);
                    int currentSum = sumMatchesScore(currentMatches);
                    if (currentSum > bestScore) {
                        bestMatch = currentMatch;
                        bestScore = currentSum;
                    }
                    allMatches.add(currentMatch);
                }

        /* Update innerArrayIndexes to switch to the next combination on next iteration of outer loop.
           Order is reversed because we check skipped matches by comparing with previous operand. */
                for (int j = numOperands - 1; j >= 0; j--) {
                    if (innerArrayIndexes[j] + 1 < matchArraySizes[j]) {
                        innerArrayIndexes[j]++;
                        break;
                    }
                    // if we reached the last combination, stop the search
                    if (j == 0) break OUTER;
                    // we need to update next index and reset current index to the first not skipped match
                    innerArrayIndexes[j] = skippedMatches[j];
                }
            }
        }
    }
}
