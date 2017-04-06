package com.milaboratory.mist.pattern;

import com.milaboratory.core.Range;
import com.milaboratory.core.sequence.MultiNSequenceWithQuality;

import java.util.stream.IntStream;

public class MultiPattern extends MultipleReadsOperator {
    public MultiPattern(SinglePattern... singlePatterns) {
        super(singlePatterns);
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

    private final class MultiPatternMatchesSearch extends MatchesSearchWithQuickBestMatch {
        private final MatchingResult[] matchingResults;

        MultiPatternMatchesSearch(MatchingResult[] matchingResults) {
            this.matchingResults = matchingResults;
            // if there are no matches, we should already return empty MatchingResult
            quickSearchPerformed = true;
            matchFound = true;
        }

        @Override
        protected void performSearch(boolean quickSearch) {
            /* Search for all matches and for best match if not already searched;
               found matches will be added to allMatches list */
            Match returnedBestMatch = findAllMatchesFromMatchingResults(matchingResults, allMatches, !quickBestMatchSearchPerformed);
            if (!quickBestMatchSearchPerformed) bestMatch = returnedBestMatch;

            quickBestMatchFound = true;
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
