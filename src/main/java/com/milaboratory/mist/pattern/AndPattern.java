package com.milaboratory.mist.pattern;

import cc.redberry.pipe.OutputPort;
import com.milaboratory.core.Range;
import com.milaboratory.core.sequence.NSequenceWithQuality;

import java.util.ArrayList;

public class AndPattern extends MultiplePatternsOperator {
    public AndPattern(SinglePattern... operandPatterns) {
        super(operandPatterns);
    }

    @Override
    public MatchingResult match(NSequenceWithQuality input, int from, int to, byte targetId) {
        final AndMatchesSearch matchesSearch = new AndMatchesSearch(operandPatterns, input, from, to, targetId);
        final MatchesOutputPort allMatchesByScore = new MatchesOutputPort(matchesSearch, true);
        final MatchesOutputPort allMatchesByCoordinate = new MatchesOutputPort(matchesSearch, false);

        // If one pattern doesn't match, AndPattern doesn't match
        for (SinglePattern operandPattern : operandPatterns) {
            MatchingResult result = operandPattern.match(input, from, to, targetId);
            if (!result.isFound())
                return new SimpleMatchingResult();
        }

        return new SimpleMatchingResult(allMatchesByScore, allMatchesByCoordinate);
    }

    private final class AndMatchesSearch extends MatchesSearchWithQuickBestMatch {
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
        private int totalCombinationCount = 1;

        AndMatchesSearch(SinglePattern[] operandPatterns, NSequenceWithQuality input, int from, int to, byte targetId) {
            this.operandPatterns = operandPatterns;
            this.input = input;
            this.from = from;
            this.to = to;
            this.targetId = targetId;
        }

        @Override
        protected void performSearch(boolean quickSearch) {
            float bestScore = -Float.MAX_VALUE;
            int numOperands = operandPatterns.length;

            // initialize arrays and get matches for all operands
            if (!quickSearchPerformed) {
                matchingResults = new MatchingResult[numOperands];
                matchArraySizes = new int[numOperands];
                innerArrayIndexes = new int[numOperands];
                for (int i = 0; i < numOperands; i++) {
                    matches.add(new ArrayList<>());
                    matchingResults[i] = operandPatterns[i].match(input, from, to, targetId);
                    matchArraySizes[i] = Math.toIntExact(matchingResults[i].getMatchesNumber());
                    matchOutputPorts.add(matchingResults[i].getMatches());
                    totalCombinationCount *= matchArraySizes[i];
                }
            }

            /* Loop through all combinations, fill allMatches and find bestMatch,
               or leave bestMatch = null if nothing found */
            for (int i = 0; i < totalCombinationCount; i++) {
                Match[] currentMatches = new Match[numOperands];
                Range[] currentRanges = new Range[numOperands];
                for (int j = 0; j < numOperands; j++) {
                    // if current array element doesn't exist, we didn't take that match, so let's take it now
                    if (innerArrayIndexes[j] == matches.get(j).size())
                        matches.get(j).add(matchOutputPorts.get(j).take());
                    currentMatches[j] = matches.get(j).get(innerArrayIndexes[j]);
                    currentRanges[j] = currentMatches[j].getWholePatternMatch().getRange();
                }
                if (!checkRangesIntersection(currentRanges)) {
                    matchFound = true;
                    // for quick search stop on first found valid match
                    if (quickSearch) {
                        quickSearchPerformed = true;
                        return;
                    }
                    Match currentMatch = combineMatches(input, targetId, currentMatches);
                    if (!quickBestMatchFound) {
                        float currentSum = combineMatchScores(currentMatches);
                        if (currentSum > bestScore) {
                            bestMatch = currentMatch;
                            bestScore = currentSum;
                        }
                    }
                    allMatches.add(currentMatch);
                }

                // Update innerArrayIndexes to switch to the next combination on next iteration of outer loop
                for (int j = 0; j < numOperands; j++) {
                    if (innerArrayIndexes[j] + 1 < matchArraySizes[j]) {
                        innerArrayIndexes[j]++;
                        break;
                    }
                    // we need to update next index and reset current index to zero
                    innerArrayIndexes[j] = 0;
                }
            }

            quickSearchPerformed = true;
            quickBestMatchSearchPerformed = true;
            fullSearchPerformed = true;
        }

        @Override
        protected void performQuickBestMatchSearch() {
            final Match[] bestMatches = new Match[operandPatterns.length];
            final Range[] bestMatchRanges = new Range[operandPatterns.length];
            boolean rangeIntersection = false;

            OUTER:
            for (int patternNumber = 0; patternNumber < operandPatterns.length; patternNumber++) {
                bestMatches[patternNumber] = operandPatterns[patternNumber].match(input, from, to, targetId).getBestMatch();
                Range currentRange = bestMatches[patternNumber].getWholePatternMatch().getRange();
                bestMatchRanges[patternNumber] = currentRange;
                for (int i = 0; i < patternNumber; i++)  // Compare with all previously added matches
                    if (bestMatchRanges[i].intersectsWith(currentRange)) {
                        rangeIntersection = true;
                        break OUTER;
                    }
            }

            if (!rangeIntersection) {
                quickBestMatchFound = true;
                quickSearchPerformed = true;
                matchFound = true;
                bestMatch = combineMatches(input, targetId, bestMatches);
            }

            quickBestMatchSearchPerformed = true;
        }
    }
}
