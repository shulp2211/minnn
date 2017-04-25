package com.milaboratory.mist.pattern;

import com.milaboratory.core.Range;
import com.milaboratory.core.sequence.MultiNSequenceWithQuality;

import java.util.*;
import java.util.stream.IntStream;

public abstract class MultipleReadsOperator extends Pattern {
    protected final MultipleReadsOperator[] operandPatterns;
    protected final SinglePattern[] singlePatterns;
    protected final ArrayList<GroupEdge> groupEdges;

    public MultipleReadsOperator(MultipleReadsOperator... operandPatterns) {
        this.operandPatterns = operandPatterns;
        this.singlePatterns = new SinglePattern[0];
        this.groupEdges = new ArrayList<>();
        getGroupEdgesFromOperands(operandPatterns);
    }

    public MultipleReadsOperator(SinglePattern... singlePatterns) {
        this.singlePatterns = singlePatterns;
        this.operandPatterns = new MultipleReadsOperator[0];
        this.groupEdges = new ArrayList<>();
        getGroupEdgesFromOperands(singlePatterns);
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
                .mapToObj(i -> new Range(0, input.get(i).getSequence().size(),
                        reverseComplements[i])).toArray(Range[]::new), reverseComplements);
    }

    public MatchingResult match(MultiNSequenceWithQuality input, Range... ranges) {
        if (input.numberOfSequences() != ranges.length)
            throw new IllegalStateException("Mismatched number of reads (" + input.numberOfSequences()
                    + ") and ranges (" + ranges.length + ")!");
        // if reverseComplements array not provided, match without reverse complements only
        boolean[] reverseComplements = new boolean[ranges.length];
        for (int i = 0; i < ranges.length; i++)
            reverseComplements[i] = ranges[i].isReverse();
        return this.match(input, ranges, reverseComplements);
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
    public abstract MatchingResult match(MultiNSequenceWithQuality input, Range[] ranges, boolean[] reverseComplements);

    @Override
    public ArrayList<GroupEdge> getGroupEdges() {
        return groupEdges;
    }

    private <T extends Pattern> void getGroupEdgesFromOperands(T[] patterns) {
        for (T pattern : patterns)
            groupEdges.addAll(pattern.getGroupEdges());
        if (groupEdges.size() != new HashSet<>(groupEdges).size())
            throw new IllegalStateException("Operands contain equal group edges!");
    }

    protected Match combineMatches(Match... matches) {
        ArrayList<MatchedItem> matchedItems = new ArrayList<>();

        int matchedRangeIndex = 0;
        for (Match match : matches) {
            if (match == null) {
                matchedItems.add(new NullMatchedRange(matchedRangeIndex++));
                continue;
            }
            matchedItems.addAll(match.getMatchedGroupEdges());
            for (int i = 0; i < match.getNumberOfPatterns(); i++) {
                MatchedRange currentMatchedRange = match.getMatchedRange(i);
                if (NullMatchedRange.class.isAssignableFrom(currentMatchedRange.getClass()))
                    matchedItems.add(new NullMatchedRange(matchedRangeIndex++));
                else
                    matchedItems.add(new MatchedRange(currentMatchedRange.getTarget(), currentMatchedRange.getTargetId(),
                            matchedRangeIndex++, currentMatchedRange.getRange()));
            }
        }
        return new Match(matchedRangeIndex, combineMatchScores(matches), matchedItems);
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
        float bestScore = Float.NEGATIVE_INFINITY;
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

        return bestMatch;
    }
}
