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
    public MatchingResult match(NSequenceWithQuality input, int from, int to, byte targetId, boolean quickMatch) {
        final Match[] bestMatches = new Match[operandPatterns.length];
        final Range[] bestMatchRanges = new Range[operandPatterns.length];
        boolean rangeIntersection = false;

        // If one pattern doesn't match, AndPattern doesn't match
        for (SinglePattern operandPattern : operandPatterns) {
            MatchingResult result = operandPattern.match(input, from, to, targetId, true);
            if (result.getMatchesNumber() == 0)
                if (quickMatch)
                    return new QuickMatchingResult(false);
                else
                    return new MultiplePatternsMatchingResult(null, 0);
        }

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

        if (!rangeIntersection)
            if (quickMatch)
                return new QuickMatchingResult(true);
            else
                return new MultiplePatternsMatchingResult(combineMatches(input, targetId, bestMatches), 1);
        else {
            // Best match has range intersection, check all other matches
            Match bestMatch = findBestMatchByScore(input, from, to, targetId, quickMatch);
            if (bestMatch != null)
                if (quickMatch)
                    return new QuickMatchingResult(true);
                else
                    return new MultiplePatternsMatchingResult(bestMatch, 1);
            else
                if (quickMatch)
                    return new QuickMatchingResult(false);
                else
                    return new MultiplePatternsMatchingResult(null, 0);
        }
    }

    private Match findBestMatchByScore(NSequenceWithQuality input, int from, int to, byte targetId, boolean quickMatch) {
        Match bestMatch = null;
        int bestScore = 0;
        int numOperands = operandPatterns.length;
        ArrayList<ArrayList<Match>> matches = new ArrayList<>();
        ArrayList<OutputPort<Match>> matchOutputPorts = new ArrayList<>();
        MatchingResult[] matchingResults = new MatchingResult[numOperands];
        int[] matchArraySizes = new int[numOperands];
        int[] innerArrayIndexes = new int[numOperands];
        int totalCombinationCount = 1;

        for (int i = 0; i < numOperands; i++) {
            matches.add(new ArrayList<>());
            matchingResults[i] = operandPatterns[i].match(input, from, to, targetId);
            matchArraySizes[i] = Math.toIntExact(matchingResults[i].getMatchesNumber());
            matchOutputPorts.add(matchingResults[i].getMatches());
            totalCombinationCount *= matchArraySizes[i];
        }

        // Loop through all combinations and find the best, or leave bestMatch = null if nothing found
        for (int i = 0; i < totalCombinationCount; i++) {
            Match[] currentMatches = new Match[numOperands];
            Range[] currentRanges = new Range[numOperands];
            for (int j = 0; j < numOperands; j++) {
                // if current array element doesn't exist, we didn't take that match, so let's take it now
                if (innerArrayIndexes[j] == matches.get(j).size())
                    matches.get(j).add(matchOutputPorts.get(j).take());
                currentMatches[j] = matches.get(j).get(innerArrayIndexes[j]);
                currentRanges[j] = currentMatches[j].getWholePatternMatch(0).getRange();
            }
            if (!checkRangesIntersection(currentRanges)) {
                // for quick match return first found valid match
                if (quickMatch)
                    return combineMatches(input, targetId, currentMatches);
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
