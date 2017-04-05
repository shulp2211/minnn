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

    public MatchingResult match(MultiNSequenceWithQuality input, boolean... reverseComplements) {
        if (input.numberOfSequences() != reverseComplements.length)
            throw new IllegalStateException("Mismatched number of reads (" + input.numberOfSequences()
                    + ") and reverse complement flags (" + reverseComplements.length + ")!");
        // for reverse complement reads automatically inverse generated ranges
        return this.match(input, IntStream.range(0, input.numberOfSequences())
                .mapToObj(i -> new Range(0, input.get(i).getSequence().size(), reverseComplements[i])).toArray(Range[]::new), reverseComplements);
    }

    public MatchingResult match(MultiNSequenceWithQuality input, Range... ranges) {
        if (input.numberOfSequences() != ranges.length)
            throw new IllegalStateException("Mismatched number of reads (" + input.numberOfSequences()
                    + ") and ranges (" + ranges.length + ")!");
        // if reverseComplements array not provided, match without reverse complements only
        return this.match(input, ranges, new boolean[input.numberOfSequences()]);
    }

    /**
     * Match a group of patterns in a group of reads.
     *
     * @param input multiple sequences that come from multiple reads
     * @param ranges ranges for input reads
     * @param reverseComplements false if non-reversed match, true if reversed complement;
     *                           one array element for one read in input
     * @return matching result
     */
    public MatchingResult match(MultiNSequenceWithQuality input, Range[] ranges, boolean[] reverseComplements) {
        final MatchingResult[] allResults = new MatchingResult[singlePatterns.length];

        if (input.numberOfSequences() != ranges.length)
            throw new IllegalStateException("Mismatched number of reads (" + input.numberOfSequences()
                    + ") and ranges (" + ranges.length + ")!");
        if (input.numberOfSequences() != reverseComplements.length)
            throw new IllegalStateException("Mismatched number of reads (" + input.numberOfSequences()
                    + ") and reverse complement flags (" + reverseComplements.length + ")!");
        if (input.numberOfSequences() != singlePatterns.length)
            throw new IllegalStateException("Mismatched number of reads (" + input.numberOfSequences()
                    + ") and patterns (" + singlePatterns.length + ")!");

        // fill allResults array, and if at least 1 pattern didn't match, return no results
        for (int i = 0; i < singlePatterns.length; i++) {
            MatchingResult currentResult;
            if (!reverseComplements[i])
                currentResult = singlePatterns[i].match(input.get(i), ranges[i], (byte) (i + 1));
            else
                currentResult = singlePatterns[i].match(input.get(i).getReverseComplement(), ranges[i].inverse(), (byte) (-i - 1));
            if (!currentResult.isFound())
                return new SimpleMatchingResult();
            allResults[i] = currentResult;
        }

        final MultiPatternMatchesSearch matchesSearch = new MultiPatternMatchesSearch(allResults);
        final MatchesOutputPort allMatchesByScore = new MatchesOutputPort(matchesSearch, true);
        final MatchesOutputPort allMatchesByCoordinate = new MatchesOutputPort(matchesSearch, false);

        return new SimpleMatchingResult(allMatchesByScore, allMatchesByCoordinate);
    }

    private Match combineMatches(Match... matches) {
        Map<String, CaptureGroupMatch> groupMatches = new HashMap<>();

        for (int i = 0; i < matches.length; i++) {
            groupMatches.putAll(matches[i].groupMatches);
            // put whole pattern match with read index; reads are numbered from 1
            groupMatches.put(WHOLE_PATTERN_MATCH_GROUP_NAME_PREFIX + i, matches[i].getWholePatternMatch(0));
        }
        return new Match(matches.length, sumMatchesScore(matches), groupMatches);
    }

    private final class MultiPatternMatchesSearch extends MatchesSearchWithQuickBestMatch {
        private final MatchingResult[] matchingResults;
        private ArrayList<ArrayList<Match>> matches = new ArrayList<>();
        private int[] matchArraySizes;
        private int[] innerArrayIndexes;
        private int totalCombinationCount = 1;

        MultiPatternMatchesSearch(MatchingResult[] matchingResults) {
            this.matchingResults = matchingResults;
            // if there are no matches, we should already return empty MatchingResult
            quickSearchPerformed = true;
            matchFound = true;
        }

        @Override
        protected void performSearch(boolean quickSearch) {
            int bestScore = 0;
            int numOperands = matchingResults.length;

            // initialize arrays and get matches for all operands
            matchArraySizes = new int[numOperands];
            innerArrayIndexes = new int[numOperands];
            for (int i = 0; i < numOperands; i++) {
                matches.add(new ArrayList<>());
                matchArraySizes[i] = Math.toIntExact(matchingResults[i].getMatchesNumber());
                totalCombinationCount *= matchArraySizes[i];
            }

            /* Loop through all combinations, fill allMatches and find bestMatch,
               or leave bestMatch = null if nothing found */
            for (int i = 0; i < totalCombinationCount; i++) {
                Match[] currentMatches = new Match[numOperands];
                for (int j = 0; j < numOperands; j++) {
                    // if current array element doesn't exist, we didn't take that match, so let's take it now
                    if (innerArrayIndexes[j] == matches.get(j).size())
                        matches.get(j).add(matchingResults[j].getMatches().take());
                    currentMatches[j] = matches.get(j).get(innerArrayIndexes[j]);
                }

                Match currentMatch = combineMatches(currentMatches);
                if (!quickBestMatchFound) {
                    int currentSum = sumMatchesScore(currentMatches);
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

            quickBestMatchSearchPerformed = true;
            fullSearchPerformed = true;
        }

        @Override
        protected void performQuickBestMatchSearch() {
            final Match[] bestMatches = new Match[singlePatterns.length];

            for (int i = 0; i < singlePatterns.length; i++)
                bestMatches[i] = matchingResults[i].getBestMatch();

            bestMatch = combineMatches(bestMatches);

            quickBestMatchFound = true;
            quickBestMatchSearchPerformed = true;
        }
    }
}
