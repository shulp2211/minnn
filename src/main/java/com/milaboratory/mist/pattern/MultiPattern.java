package com.milaboratory.mist.pattern;

import com.milaboratory.core.Range;
import com.milaboratory.core.sequence.MultiNSequenceWithQuality;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.stream.IntStream;

import static com.milaboratory.mist.pattern.Match.WHOLE_PATTERN_MATCH_GROUP_NAME_PREFIX;
import static com.milaboratory.mist.pattern.MultiplePatternsOperator.sumMatchesScore;

public class MultiPattern implements Pattern {
    private final SinglePattern[] singlePatterns;

    public MultiPattern(SinglePattern... singlePatterns) {
        this.singlePatterns = singlePatterns;
    }

    @Override
    public MatchingResult match(MultiNSequenceWithQuality input) {
        // if ranges array not provided, match in the whole sequences
        return this.match(input, IntStream.range(0, input.numberOfSequences())
                .mapToObj(i -> new Range(0, input.get(i).getSequence().size())).toArray(Range[]::new));
    }

    public MatchingResult match(MultiNSequenceWithQuality input, Range... ranges) {
        // if reverseComplements array not provided, match without reverse complements only
        return this.match(input, ranges, IntStream.generate(() -> 1).limit(input.numberOfSequences()).toArray());
    }

    /**
     * Match a group of patterns in a group of reads.
     *
     * @param input multiple sequences that come from multiple reads
     * @param ranges ranges for input reads
     * @param reverseComplements 1 if non-reversed match only, -1 if reversed complement only, 0 if both;
     *                           one array element for one read in input
     * @return matching result
     */
    public MatchingResult match(MultiNSequenceWithQuality input, Range[] ranges, int[] reverseComplements) {
        final Match[] bestMatches = new Match[singlePatterns.length];
        // 2 dimensional array: [][0] items contain non-reversed matching results, [][1] - reversed
        final MatchingResult[][] allResults = new MatchingResult[singlePatterns.length][2];

        if (input.numberOfSequences() != reverseComplements.length)
            throw new IllegalStateException("Mismatched number of reads (" + input.numberOfSequences()
                    + ") and reverse complement flags array size (" + reverseComplements.length + ")!");
        if (input.numberOfSequences() != singlePatterns.length)
            throw new IllegalStateException("Mismatched number of reads (" + input.numberOfSequences()
                    + ") and patterns (" + singlePatterns.length + ")!");

        /* fill allResults array, and if at least 1 pattern didn't match, return no results;
           also collect best matches to provide quickBestMatch */
        for (int i = 0; i < singlePatterns.length; i++) {
            MatchingResult currentResult;
            switch (reverseComplements[i]) {
                case 1:
                    currentResult = singlePatterns[i].match(input.get(i), ranges[i], (byte) (i + 1));
                    if (!currentResult.isFound())
                        return new MultiplePatternsMatchingResult();
                    allResults[i][0] = currentResult;
                    allResults[i][1] = null;
                    bestMatches[i] = currentResult.getBestMatch();
                    break;
                case -1:
                    currentResult = singlePatterns[i].match(input.get(i).getReverseComplement(), ranges[i].inverse(), (byte) (-i - 1));
                    if (!currentResult.isFound())
                        return new MultiplePatternsMatchingResult();
                    allResults[i][0] = null;
                    allResults[i][1] = currentResult;
                    bestMatches[i] = currentResult.getBestMatch();
                    break;
                case 0:
                    currentResult = singlePatterns[i].match(input.get(i), ranges[i], (byte) (i + 1));
                    if (currentResult.isFound())
                        allResults[i][0] = currentResult;
                    else
                        allResults[i][0] = null;
                    currentResult = singlePatterns[i].match(input.get(i).getReverseComplement(), ranges[i].inverse(), (byte) (-i - 1));
                    if (currentResult.isFound())
                        allResults[i][1] = currentResult;
                    else
                        allResults[i][1] = null;
                    if ((allResults[i][0] == null) && (allResults[i][1] == null))
                        return new MultiplePatternsMatchingResult();
                    Match bestMatch0 = allResults[i][0].getBestMatch();
                    Match bestMatch1 = allResults[i][1].getBestMatch();
                    if (bestMatch0.getScore() >= bestMatch1.getScore())
                        bestMatches[i] = bestMatch0;
                    else
                        bestMatches[i] = bestMatch1;
                    break;
            }
        }

        final MultiPatternMatchesSearch matchesSearch = new MultiPatternMatchesSearch(allResults);
        final MatchesOutputPort allMatchesByScore = new MatchesOutputPort(matchesSearch, true);
        final MatchesOutputPort allMatchesByCoordinate = new MatchesOutputPort(matchesSearch, false);

        return new MultiplePatternsMatchingResult(allMatchesByScore, allMatchesByCoordinate, combineMatches(bestMatches));
    }

    private Match combineMatches(Match... matches) {
        Map<String, CaptureGroupMatch> groupMatches = new HashMap<>();

        for (int i = 0; i < matches.length; i++) {
            groupMatches.putAll(matches[i].groupMatches);
            // put whole pattern match with read index; reads are numbered from 1
            groupMatches.put(WHOLE_PATTERN_MATCH_GROUP_NAME_PREFIX + (i + 1), matches[i].getWholePatternMatch(0));
        }

        groupMatches.remove(WHOLE_PATTERN_MATCH_GROUP_NAME_PREFIX + 0);
        return new Match(matches.length, sumMatchesScore(matches), groupMatches);
    }

    private final class MultiPatternMatchesSearch extends MatchesSearch {
        private final MatchingResult[][] matchingResults;
        private ArrayList<ArrayList<Match>> matches = new ArrayList<>();
        private int[] matchArraySizes;
        private int[] innerArrayIndexes;
        private int totalCombinationCount = 1;

        MultiPatternMatchesSearch(MatchingResult[][] matchingResults) {
            this.matchingResults = matchingResults;
            // if there are no matches, we should already return empty MatchingResult
            quickSearchPerformed = true;
            matchFound = true;
        }

        @Override
        protected void performSearch(boolean quickSearch) {
            int bestScore = 0;
            int numOperands = matchingResults[0].length;

            // initialize arrays and get matches for all operands
            matchArraySizes = new int[numOperands];
            innerArrayIndexes = new int[numOperands];
            for (int i = 0; i < numOperands; i++) {
                matches.add(new ArrayList<>());
                matchArraySizes[i] = 0;
                for (int reverse = 0; reverse <= 1; reverse++)
                    if (matchingResults[i][reverse] != null)
                        matchArraySizes[i] += Math.toIntExact(matchingResults[i][reverse].getMatchesNumber());
                totalCombinationCount *= matchArraySizes[i];
            }

            /* Loop through all combinations, fill allMatches and find bestMatch,
               or leave bestMatch = null if nothing found */
            for (int i = 0; i < totalCombinationCount; i++) {
                Match[] currentMatches = new Match[numOperands];
                for (int j = 0; j < numOperands; j++)
                    for (int reverse = 0; reverse <= 1; reverse++) {
                        if (matchingResults[j][reverse] == null) continue;
                        // if current array element doesn't exist, we didn't take that match, so let's take it now
                        if (innerArrayIndexes[j] == matches.get(j).size())
                            matches.get(j).add(matchingResults[j][reverse].getMatches().take());
                        currentMatches[j] = matches.get(j).get(innerArrayIndexes[j]);
                    }

                Match currentMatch = combineMatches(currentMatches);
                int currentSum = sumMatchesScore(currentMatches);
                if (currentSum > bestScore) {
                    bestMatch = currentMatch;
                    bestScore = currentSum;
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
            fullSearchPerformed = true;
        }
    }
}
