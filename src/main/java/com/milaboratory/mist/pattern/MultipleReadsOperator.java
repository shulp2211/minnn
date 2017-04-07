package com.milaboratory.mist.pattern;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import static com.milaboratory.mist.pattern.Match.WHOLE_PATTERN_MATCH_GROUP_NAME_PREFIX;

public abstract class MultipleReadsOperator implements Pattern {
    protected final Pattern[] operandPatterns;
    protected final SinglePattern[] singlePatterns;
    private final boolean useSinglePatterns;

    public MultipleReadsOperator(Pattern[] operandPatterns) {
        this.operandPatterns = operandPatterns;
        this.singlePatterns = new SinglePattern[0];
        useSinglePatterns = false;
    }

    public MultipleReadsOperator(SinglePattern[] singlePatterns) {
        this.singlePatterns = singlePatterns;
        this.operandPatterns = new Pattern[0];
        useSinglePatterns = true;
    }

    @Override
    public boolean areGroupsInside() {
        if (useSinglePatterns)
            for (SinglePattern singlePattern : singlePatterns)
                if (singlePattern.areGroupsInside()) return true;
        else
            for (Pattern pattern : operandPatterns)
                if (pattern.areGroupsInside()) return true;
        return false;
    }

    protected Match combineMatches(Match... matches) {
        Map<String, CaptureGroupMatch> groupMatches = new HashMap<>();

        for (int i = 0; i < matches.length; i++) {
            if (matches[i] == null)
                if (!useSinglePatterns) {
                    // if we use MultiPatterns, null values are valid because of possible NotOperator patterns
                    groupMatches.put(WHOLE_PATTERN_MATCH_GROUP_NAME_PREFIX + i, null);
                    continue;
                } else throw new IllegalStateException("Must not combine null matches for single patterns!");
            groupMatches.putAll(matches[i].groupMatches);
            groupMatches.put(WHOLE_PATTERN_MATCH_GROUP_NAME_PREFIX + i, matches[i].getWholePatternMatch(0));
        }
        return new Match(matches.length, sumMatchesScore(matches), groupMatches);
    }

    protected int sumMatchesScore(Match... matches) {
        int score = 0;
        for (Match match : matches) {
            if (match == null)
                if (!useSinglePatterns) {
                    // if we use MultiPatterns, null values are valid because of possible NotOperator patterns
                    continue;
                } else throw new IllegalStateException("Must not sum null matches score for single patterns!");
            score += match.getScore();
        }
        return score;
    }

    /**
     * Get all matches from results, combine them in all combinations and put to allMatches;
     * and also calculate bestMatch.
     *
     * @param results matching results that came from function operands
     * @param allMatches to this list all generated combinations of matches will be added
     * @param returnBestMatch true if we need to find and return best match or false if we don't need to search for it
     * @return best match if found, otherwise null
     */
    protected Match findAllMatchesFromMatchingResults(final MatchingResult[] results, ArrayList<Match> allMatches, boolean returnBestMatch) {
        Match bestMatch = null;
        ArrayList<ArrayList<Match>> matches = new ArrayList<>();
        int[] matchArraySizes;
        int[] innerArrayIndexes;
        int totalCombinationCount = 1;
        int bestScore = 0;
        int numOperands = results.length;

        // initialize arrays and get matches for all operands
        matchArraySizes = new int[numOperands];
        innerArrayIndexes = new int[numOperands];
        for (int i = 0; i < numOperands; i++) {
            matches.add(new ArrayList<>());
            matchArraySizes[i] = Math.toIntExact(results[i].getMatchesNumber());
            /* Quick search must be already performed; use 1 "null" match instead of 0 matches;
               this can appear in "Or" operator or because of "Not" result as operand */
            if (matchArraySizes[i] == 0) matchArraySizes[i] = 1;
            totalCombinationCount *= matchArraySizes[i];
        }

        /* Loop through all combinations, fill allMatches and find bestMatch,
           or leave bestMatch unchanged if nothing found. */
        for (int i = 0; i < totalCombinationCount; i++) {
            Match[] currentMatches = new Match[numOperands];
            for (int j = 0; j < numOperands; j++) {
                /* If current array element doesn't exist, we didn't take that match, so let's take it now;
                   null values are valid here in case of multiple pattern operators */
                if (innerArrayIndexes[j] == matches.get(j).size())
                    matches.get(j).add(results[j].getMatches().take());
                currentMatches[j] = matches.get(j).get(innerArrayIndexes[j]);
            }

            // null values are valid here in case of multiple pattern operators
            Match currentMatch = combineMatches(currentMatches);
            if (returnBestMatch) {
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

        return bestMatch;
    }
}
