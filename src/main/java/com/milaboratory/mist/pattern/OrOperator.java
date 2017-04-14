package com.milaboratory.mist.pattern;

import com.milaboratory.core.Range;
import com.milaboratory.core.sequence.MultiNSequenceWithQuality;

public class OrOperator extends MultipleReadsOperator {
    public OrOperator(MultipleReadsOperator... operandPatterns) {
        super(operandPatterns);
    }

    @Override
    public MatchingResult match(MultiNSequenceWithQuality input, Range[] ranges, boolean[] reverseComplements) {
        final OrOperatorMatchesSearch matchesSearch = new OrOperatorMatchesSearch(operandPatterns, input, ranges, reverseComplements);
        final MatchesOutputPort allMatchesByScore = new MatchesOutputPort(matchesSearch, true);
        final MatchesOutputPort allMatchesByCoordinate = new MatchesOutputPort(matchesSearch, false);

        return new SimpleMatchingResult(allMatchesByScore, allMatchesByCoordinate);
    }

    @Override
    protected float combineMatchScores(Match... matches) {
        float bestScore = Float.NEGATIVE_INFINITY;
        for (Match match : matches)
            if (match != null)
                if (match.getScore() > bestScore)
                    bestScore = match.getScore();
        return bestScore;
    }

    private final class OrOperatorMatchesSearch extends MatchesSearchWithQuickBestMatch {
        private final MultipleReadsOperator[] operandPatterns;
        private final Range[] ranges;
        private final boolean[] reverseComplements;
        private final MultiNSequenceWithQuality input;

        OrOperatorMatchesSearch(MultipleReadsOperator[] operandPatterns, MultiNSequenceWithQuality input, Range[] ranges, boolean[] reverseComplements) {
            this.operandPatterns = operandPatterns;
            this.ranges = ranges;
            this.reverseComplements = reverseComplements;
            this.input = input;
        }

        @Override
        protected void performSearch(boolean quickSearch) {
            MatchingResult[] matchingResults = new MatchingResult[operandPatterns.length];

            matchFound = false;
            for (int i = 0; i < operandPatterns.length; i++) {
                matchingResults[i] = operandPatterns[i].match(input, ranges, reverseComplements);
                if (matchingResults[i].isFound()) {
                    matchFound = true;
                    quickSearchPerformed = true;
                    if (quickSearch) return;
                }
            }

            if (!matchFound) {
                quickBestMatchSearchPerformed = true;
                fullSearchPerformed = true;
                return;
            }

            /* Search for all matches and for best match if not already searched;
               found matches will be added to allMatches list */
            Match returnedBestMatch = findAllMatchesFromMatchingResults(matchingResults, allMatches, !quickBestMatchSearchPerformed);
            if (!quickBestMatchSearchPerformed) bestMatch = returnedBestMatch;

            quickBestMatchSearchPerformed = true;
            fullSearchPerformed = true;
            quickBestMatchFound = (bestMatch != null);
        }

        @Override
        protected void performQuickBestMatchSearch() {
            final Match[] bestMatches = new Match[operandPatterns.length];

            for (int i = 0; i < operandPatterns.length; i++) {
                MatchingResult currentResult = operandPatterns[i].match(input, ranges, reverseComplements);
                if (!quickSearchPerformed)
                    if (currentResult.isFound()) {
                        quickSearchPerformed = true;
                        quickBestMatchSearchPerformed = true;
                        matchFound = true;
                        quickBestMatchFound = true;
                    }

                // null values are valid here
                bestMatches[i] = currentResult.getBestMatch();
            }

            if (!matchFound) {
                quickSearchPerformed = true;
                quickBestMatchSearchPerformed = true;
                matchFound = false;
                quickBestMatchFound = false;
                return;
            }

            bestMatch = combineMatches(bestMatches);

            quickSearchPerformed = true;
            quickBestMatchSearchPerformed = true;
            matchFound = true;
            quickBestMatchFound = (bestMatch != null);
        }
    }
}
