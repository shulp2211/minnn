package com.milaboratory.mist.pattern;

import com.milaboratory.core.Range;
import com.milaboratory.core.sequence.NSequenceWithQuality;

import java.util.ArrayList;

public class AndPattern extends MultiplePatternsOperator {
    public AndPattern(SinglePattern... operandPatterns) {
        super(operandPatterns);
    }

    @Override
    public MultiplePatternsMatchingResult match(NSequenceWithQuality input, int from, int to, byte targetId) {
        final Match[] bestMatches = new Match[operandPatterns.length];
        final Range[] bestMatchRanges = new Range[operandPatterns.length];
        boolean rangeIntersection = false;

        OUTER:
        for (int patternNumber = 0; patternNumber < operandPatterns.length; patternNumber++) {
            MatchingResult result = operandPatterns[patternNumber].match(input, from, to, targetId);
            if (result.getMatchesNumber() == 0) {
                // If one pattern doesn't match, all AndPattern doesn't match
                return new MultiplePatternsMatchingResult(null, 0);
            } else {
                bestMatches[patternNumber] = result.getBestMatch();
                Range currentRange = bestMatches[patternNumber].getWholePatternMatch().getRange();
                bestMatchRanges[patternNumber] = currentRange;
                for (int i = 0; i < patternNumber; i++)  // Compare with all previously added matches
                    if (bestMatchRanges[i].intersectsWith(currentRange)) {
                        rangeIntersection = true;
                        break OUTER;
                    }
            }
        }

        if (!rangeIntersection)
            return new MultiplePatternsMatchingResult(combineMatches(input, targetId, bestMatches), 1);
        else {
            // Best match has range intersection, check all other matches
            Match bestMatch = findBestMatchByScore(input, from, to, targetId);
            if (bestMatch != null)
                return new MultiplePatternsMatchingResult(bestMatch, 1);
            else
                return new MultiplePatternsMatchingResult(null, 0);
        }
    }

    private Match findBestMatchByScore(NSequenceWithQuality input, int from, int to, byte targetId) {
        Match bestMatch = null;
        int bestScore = 0;
        ArrayList<ArrayList<Match>> matches = new ArrayList<>();

        // Fill array with all matches for all operands
        for (SinglePattern pattern : operandPatterns) {
            ArrayList<Match> currentPatternMatches = new ArrayList<>();
            MatchingResult result = pattern.match(input, from, to, targetId);
            while (true) {
                Match currentResult = result.getMatches().take();
                if (currentResult != null) {
                    currentPatternMatches.add(currentResult);
                } else break;
            }
            matches.add(currentPatternMatches);
        }

        // Loop through all combinations and find the best, or leave bestMatch = null if nothing found
        int matchArraySizes[] = new int[matches.size()];
        int innerArrayIndexes[] = new int[matches.size()];
        int totalCombinationCount = 1;
        for (int i = 0; i < matches.size(); i++) {
            matchArraySizes[i] = matches.get(i).size();
            totalCombinationCount *= matchArraySizes[i];
        }

        for (int i = 0; i < totalCombinationCount; i++) {
            Match[] currentMatches = new Match[matches.size()];
            Range[] currentRanges = new Range[matches.size()];
            for (int j = 0; j < matches.size(); j++) {
                currentMatches[j] = matches.get(j).get(innerArrayIndexes[j]);
                currentRanges[j] = currentMatches[j].getWholePatternMatch(0).getRange();
            }
            if (!checkRangesIntersection(currentRanges)) {
                int currentSum = sumMatchesScore(currentMatches);
                if (currentSum > bestScore) {
                    bestMatch = combineMatches(input, targetId, currentMatches);
                    bestScore = currentSum;
                }
            }

            // Update innerArrayIndexes to switch to the next combination on next iteration of outer loop
            for (int j = 0; j < matches.size(); j++) {
                if (innerArrayIndexes[j] + 1 < matchArraySizes[j]) {
                    innerArrayIndexes[j]++;
                    break;
                }
                // we need to update next index and reset current index to zero
                innerArrayIndexes[j] = 0;
            }
        }

        return bestMatch;
    }
}
