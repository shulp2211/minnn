package com.milaboratory.mist.util;

import cc.redberry.pipe.OutputPort;
import com.milaboratory.core.Range;
import com.milaboratory.core.sequence.NSequenceWithQuality;
import com.milaboratory.mist.pattern.*;

import java.util.*;

import static com.milaboratory.mist.pattern.MatchValidationType.*;
import static com.milaboratory.mist.util.DebugUtils.countCall;
import static com.milaboratory.mist.util.DebugUtils.countExecutionTime;
import static com.milaboratory.mist.util.DebugUtils.maxSize;
import static com.milaboratory.mist.util.RangeTools.*;

public abstract class ApproximateSorter {
    protected final ApproximateSorterConfiguration conf;
    private final ArrayList<SpecificOutputPort> unfairOutputPorts = new ArrayList<>();
    private final boolean sortingByScore;

    protected int unfairSorterTakenValues = 0;

    /**
     * This sorter allows to get output port for approximately sorted matches by score or coordinate from
     * input ports. Specific sorters (by score, coordinate and with different rules) are extending this class.
     *
     * @param conf sorter configuration
     * @param sortingByScore set in constructor of child class: true if sorting by score, false if by coordinate
     */
    protected ApproximateSorter(ApproximateSorterConfiguration conf, boolean sortingByScore) {
        this.conf = conf;
        this.sortingByScore = sortingByScore;
    }

    /**
     * Get output port for sorted combined matches.
     *
     * @return output port
     */
    public abstract OutputPort<Match> getOutputPort();

    /**
     * Get combined match from a group of input matches. It uses multipleReads flag to determine how to combine matches
     * (by combining ranges for single read or by numbering the matched ranges for multiple reads).
     *
     * @param matches input matches
     * @return combined match
     */
    protected Match combineMatches(Match... matches) {
        List<Match> tempMatches = new ArrayList<>();
        tempMatches.add(null);
        countCall("combine");
        countExecutionTime("combine", () -> {

            ArrayList<MatchedItem> matchedItems = new ArrayList<>();

            if (conf.multipleReads) {
                int patternIndex = 0;
                boolean allMatchesAreNull = true;
                for (Match match : matches) {
                    if (match == null) {
                        if (conf.matchValidationType == LOGICAL_OR) {
                            matchedItems.add(new NullMatchedRange(patternIndex++));
                            continue;
                        } else throw new IllegalStateException("Found null match when MatchValidationType doesn't allow them");
                    } else allMatchesAreNull = false;
                    for (int i = 0; i < match.getNumberOfPatterns(); i++) {
                        MatchedRange currentMatchedRange = match.getMatchedRange(i);
                        if (currentMatchedRange instanceof NullMatchedRange) {
                            if (match.getMatchedGroupEdgesByPattern(i).size() > 0)
                                throw new IllegalStateException("Null pattern contains "
                                        + match.getMatchedGroupEdgesByPattern(i).size() + " group edges");
                            matchedItems.add(new NullMatchedRange(patternIndex++));
                        } else {
                            matchedItems.add(new MatchedRange(currentMatchedRange.getTarget(), currentMatchedRange.getTargetId(),
                                    patternIndex, currentMatchedRange.getRange()));
                            for (MatchedGroupEdge matchedGroupEdge : match.getMatchedGroupEdgesByPattern(i))
                                matchedItems.add(new MatchedGroupEdge(matchedGroupEdge.getTarget(), matchedGroupEdge.getTargetId(),
                                        patternIndex, matchedGroupEdge.getGroupEdge(), matchedGroupEdge.getPosition()));
                            patternIndex++;
                        }
                    }
                }
                if (allMatchesAreNull) return null;
                tempMatches.set(0, new Match(patternIndex, combineMatchScores(matches), matchedItems));
                return null;
            } else if (conf.matchValidationType == FIRST) {
                boolean matchExist = false;
                int bestMatchPort = 0;
                int bestCoordinate = Integer.MAX_VALUE;
                long bestScore = Long.MIN_VALUE;
                for (int i = 0; i < matches.length; i++)
                    if ((matches[i] != null)
                            && ((sortingByScore && (matches[i].getScore() > bestScore))
                            || (!sortingByScore && (matches[i].getRange().getLower() < bestCoordinate)))) {
                        matchExist = true;
                        if (sortingByScore) bestScore = matches[i].getScore();
                        else bestCoordinate = matches[i].getRange().getLower();
                        bestMatchPort = i;
                    }
                if (matchExist) {
                    tempMatches.set(0, matches[bestMatchPort]);
                    return null;
                } else
                    return null;
            } else {
                NSequenceWithQuality target = matches[0].getMatchedRange().getTarget();
                byte targetId = matches[0].getMatchedRange().getTargetId();
                Range[] ranges = new Range[matches.length];
                ArrayList<ArrayList<MatchedGroupEdge>> matchedGroupEdgesFromOperands = new ArrayList<>();

                for (int i = 0; i < matches.length; i++) {
                    matchedGroupEdgesFromOperands.add(new ArrayList<>());
                    matchedGroupEdgesFromOperands.get(i).addAll(matches[i].getMatchedGroupEdges());
                    ranges[i] = matches[i].getRange();
                }

                Arrays.sort(ranges, Comparator.comparingInt(Range::getLower));
                CombinedRange combinedRange = combineRanges(conf.patternAligner, matchedGroupEdgesFromOperands,
                        target, conf.matchValidationType == FOLLOWING, ranges);
                matchedItems.addAll(combinedRange.getMatchedGroupEdges());
                matchedItems.add(new MatchedRange(target, targetId, 0, combinedRange.getRange()));
                tempMatches.set(0, new Match(1, combineMatchScores(matches) + combinedRange.getScorePenalty(), matchedItems));
            }

            return null;
        });
        return tempMatches.get(0);

    }

    /**
     * Combine match scores. It is used in combineMatches function. Different patterns may combine operand scores
     * by sum or by max value of operand scores.
     *
     * @param matches matches from which we will get the scores
     * @return combined score
     */
    private long combineMatchScores(Match... matches) {
        long resultScore;
        if (conf.combineScoresBySum) {
            resultScore = 0;
            for (Match match : matches)
                if (match != null)
                    resultScore += match.getScore();
        } else {
            resultScore = Long.MIN_VALUE;
            for (Match match : matches)
                if (match != null)
                    if (match.getScore() > resultScore)
                        resultScore = match.getScore();
        }
        return resultScore;
    }

    /**
     * Returns true if null match taken from operand does not guarantee that this operator will not match.
     *
     * @return true if null matches taken from operands must not automatically discard the current combination
     */
    protected boolean areNullMatchesAllowed() {
        return ((conf.matchValidationType == LOGICAL_OR) || (conf.matchValidationType == FIRST));
    }

    /**
     * Fills array for fair sorting. Array will be already filtered: match combinations that contain incompatible
     * ranges will not be saved to array.
     *
     * @return array for fair sorting
     */
    protected Match[] fillArrayForFairSorting() {
        ArrayList<ArrayList<Match>> allMatches = new ArrayList<>();
        ArrayList<Match> allMatchesFiltered = new ArrayList<>();
        int numberOfOperands = conf.operandPatterns.length;
        TableOfIterations tableOfIterations = new TableOfIterations(numberOfOperands);
        Match currentMatch;
        int totalNumberOfCombinations = 1;

        // get all matches from all operands
        for (int i = 0; i < numberOfOperands; i++) {
            OutputPort<Match> currentPort = conf.multipleReads
                    ? conf.operandPatterns[i].match(conf.target)
                        .getMatches(sortingByScore, true)
                    : ((SinglePattern)conf.operandPatterns[i]).match(conf.target.get(0), conf.from(), conf.to())
                        .getMatches(sortingByScore, true);
            allMatches.add(new ArrayList<>());
            do {
                currentMatch = currentPort.take();
                if ((currentMatch != null) || (areNullMatchesAllowed() && (allMatches.get(i).size() == 0)))
                    allMatches.get(i).add(currentMatch);
            } while (currentMatch != null);
            totalNumberOfCombinations *= allMatches.get(i).size();
        }

        int[] innerArrayIndexes = new int[numberOfOperands];
        Match[] currentMatches = new Match[numberOfOperands];
        long penaltyThreshold = conf.patternAligner.penaltyThreshold();
        for (int i = 0; i < totalNumberOfCombinations; i++) {
            if (tableOfIterations.isCompatible(false, innerArrayIndexes)) {
                for (int j = 0; j < numberOfOperands; j++)
                    currentMatches[j] = allMatches.get(j).get(innerArrayIndexes[j]);
                IncompatibleIndexes incompatibleIndexes = findIncompatibleIndexes(currentMatches, innerArrayIndexes);
                if (incompatibleIndexes != null)
                    tableOfIterations.addIncompatibleIndexes(incompatibleIndexes);
                else {
                    Match combinedMatch = combineMatches(currentMatches);
                    if ((combinedMatch != null) && (combinedMatch.getScore() >= penaltyThreshold))
                        allMatchesFiltered.add(combinedMatch);
                }
            }

            // Update innerArrayIndexes to switch to the next combination on next iteration of outer loop
            for (int j = 0; j < numberOfOperands; j++) {
                int currentIndex = innerArrayIndexes[j];
                if (currentIndex + 1 < allMatches.get(j).size()) {
                    innerArrayIndexes[j] = currentIndex + 1;
                    break;
                }
                // we need to update next index and reset current index to zero
                innerArrayIndexes[j] = 0;
            }
        }

        return allMatchesFiltered.toArray(new Match[allMatchesFiltered.size()]);
    }

    /**
     * Returns null if this match combination is valid or IncompatibleIndexes structure if it finds
     * 2 matches that have incompatible ranges.
     *
     * @param matches group of matches to check
     * @param indexes indexes of all provided matches for writing to IncompatibleIndexes structure
     * @return IncompatibleIndexes structure
     */
    protected IncompatibleIndexes findIncompatibleIndexes(Match[] matches, int[] indexes) {
        if (matches.length != indexes.length)
            throw new IllegalArgumentException("matches length is " + matches.length + ", indexes length is "
                + indexes.length + "; they must be equal!");

        List<IncompatibleIndexes> tempIndexes = new ArrayList<>();
        tempIndexes.add(null);
        countCall("find");
        countExecutionTime("find", () -> {
            IncompatibleIndexes result = null;
            switch (conf.matchValidationType) {
                case LOGICAL_OR:
                case LOGICAL_AND:
                case FIRST:
                    return null;
                case INTERSECTION:
                    Range ranges[] = new Range[matches.length];

                    OUTER:
                    for (int i = 0; i < matches.length; i++) {
                        if (matches[i] == null) continue;
                        Range currentRange = matches[i].getRange();
                        ranges[i] = currentRange;
                        for (int j = 0; j < i; j++)     // Compare with all previously added matches
                            if (checkFullIntersection(ranges[i], ranges[j])
                                    || checkOverlap(matches[0].getMatchedRange().getTarget(), ranges[i], ranges[j])) {
                                result = new IncompatibleIndexes(j, indexes[j], i, indexes[i]);
                                break OUTER;
                            }
                    }

                    tempIndexes.set(0, result);
                    return null;
                case ORDER:
                case FOLLOWING:
                    Range currentRange;
                    Range previousRange;

                    for (int i = 1; i < matches.length; i++) {
                        if (matches[i] == null) continue;
                        NSequenceWithQuality target = matches[0].getMatchedRange().getTarget();
                        currentRange = matches[i].getRange();
                        previousRange = matches[i - 1].getRange();
                        if ((previousRange.getLower() >= currentRange.getLower())
                                || checkFullIntersection(previousRange, currentRange)
                                || checkOverlap(target, previousRange, currentRange)
                                || checkInsertionPenalty(target, previousRange, currentRange)) {
                            result = new IncompatibleIndexes(i - 1, indexes[i - 1], i, indexes[i]);
                            break;
                        }
                    }
                    tempIndexes.set(0, result);
                    return null;
            }
            return null;
        });

        return tempIndexes.get(0);
    }

    /**
     * Check is overlap too big to invalidate this combination of ranges.
     *
     * @return true if overlap is too big and this combination of ranges is invalid
     */
    private boolean checkOverlap(NSequenceWithQuality target, Range range0, Range range1) {
        PatternAligner patternAligner = conf.patternAligner;
        Range intersection = range0.intersection(range1);
        return (intersection != null) && (((patternAligner.maxOverlap() != -1) && (patternAligner.maxOverlap()
                < intersection.length())) || (patternAligner.overlapPenalty(target, intersection.getLower(),
                intersection.length()) < patternAligner.penaltyThreshold()));
    }

    /**
     * Check is insertion penalty between ranges too big to invalidate this combination of ranges.
     *
     * @return true if insertion penalty is too big and this combination of ranges is invalid
     */
    private boolean checkInsertionPenalty(NSequenceWithQuality target, Range range0, Range range1) {
        PatternAligner patternAligner = conf.patternAligner;
        if (conf.matchValidationType == FOLLOWING) {
            int insertionLength = range1.getLower() - range0.getUpper();
            return (insertionLength > 0) && (patternAligner.insertionPenalty(target, range0.getUpper(), insertionLength)
                    < patternAligner.penaltyThreshold());
        } else return false;
    }

    private SpecificOutputPort getPortWithParams(int operandIndex) {
        return getPortWithParams(operandIndex, -1);
    }

    private SpecificOutputPort getPortWithParams(int operandIndex, int from) {
        SpecificOutputPort currentPort = unfairOutputPorts.stream().filter(p -> p.paramsEqualTo(operandIndex, from))
                .findFirst().orElse(null);
        if (currentPort == null) {
            Pattern currentPattern = conf.operandPatterns[operandIndex];
            int matchFrom = (from == -1) ? conf.from() : Math.max(conf.from(), from);
            int matchTo = conf.to();
            if (conf.matchValidationType == FOLLOWING) {
                int patternMaxLength = ((SinglePattern)currentPattern).estimateMaxLength();
                if (patternMaxLength != -1)
                    matchTo = Math.min(conf.to(), matchFrom + patternMaxLength);
            }
            currentPort = new SpecificOutputPort(conf.multipleReads
                    ? currentPattern.match(conf.target)
                        .getMatches(sortingByScore, false)
                    : ((SinglePattern)currentPattern).match(conf.target.get(0), matchFrom, matchTo)
                        .getMatches(sortingByScore, false),
                    operandIndex, from);
            unfairOutputPorts.add(currentPort);
        }
        return currentPort;
    }

    protected Match[] getMatchesByIndexes(int[] indexes) {
        int numberOfOperands = conf.operandPatterns.length;
        if (indexes.length != numberOfOperands)
            throw new IllegalArgumentException("indexes length is " + indexes.length + ", number of operands: "
                    + numberOfOperands);
        Match[] matches = new Match[numberOfOperands];
        if ((conf.matchValidationType == ORDER) || (conf.matchValidationType == FOLLOWING)) {
            int maxOverlap = conf.patternAligner.maxOverlap();
            int previousMatchEnd = -1;
            for (int i = 0; i < numberOfOperands; i++) {
                int thisMatchStart = (previousMatchEnd == -1) ? 0 : previousMatchEnd - maxOverlap;
                Match currentMatch = getPortWithParams(i, thisMatchStart).get(indexes[i]);
                if (currentMatch != null)
                    previousMatchEnd = currentMatch.getRange().getTo();
                else
                    previousMatchEnd = -1;
                matches[i] = currentMatch;
            }
        } else
            for (int i = 0; i < numberOfOperands; i++)
                matches[i] = getPortWithParams(i).get(indexes[i]);

        return matches;
    }

    protected static class IncompatibleIndexes {
        int port1;
        int index1;
        int port2;
        int index2;

        IncompatibleIndexes(int port1, int index1, int port2, int index2) {
            this.port1 = port1;
            this.index1 = index1;
            this.port2 = port2;
            this.index2 = index2;
        }

        @Override
        public boolean equals(Object other) {
            if (!(other instanceof IncompatibleIndexes))
                return false;

            IncompatibleIndexes that = (IncompatibleIndexes) other;

            return (this.port1 == that.port1) && (this.port2 == that.port2)
                    && (this.index1 == that.index1) && (this.index2 == that.index2);
        }

        @Override
        public int hashCode() {
            int hashCode = 1;

            hashCode = hashCode * 37 + this.port1;
            hashCode = hashCode * 37 + this.port2;
            hashCode = hashCode * 37 + this.index1;
            hashCode = hashCode * 37 + this.index2;

            return hashCode;
        }
    }

    protected static class TableOfIterations {
        private final HashSet<IncompatibleIndexes> incompatibleIndexes;
        private final int numberOfPorts;
        private int totalCombinationsCount = -1;

        TableOfIterations(int numberOfPorts) {
            incompatibleIndexes = new HashSet<>();
            this.numberOfPorts = numberOfPorts;
        }

        /**
         * Check if this combination of indexes contains incompatible indexes. Incompatible means that we
         * already know that matches with that indexes have misplaced ranges.
         *
         * @param allNextIncompatible true if operand matches are sorted by coordinate and matchValidationType == ORDER;
         *                            otherwise false. In fair sorting it must be always false. This flag marks that
         *                            next indexes can be considered incompatible to speed up search.
         * @param indexes indexes of matches
         * @return true if there are no incompatible indexes found; false if they are found
         */
        boolean isCompatible(boolean allNextIncompatible, int[] indexes) {
            countCall("isCompatible");
            List<Boolean> result = new ArrayList<>();
            result.add(true);
            countExecutionTime("isCompatible", () -> {
                for (IncompatibleIndexes currentIndexes : incompatibleIndexes)
                    if (allNextIncompatible)
                        if ((indexes[currentIndexes.port1] >= currentIndexes.index1)
                                && (indexes[currentIndexes.port2] <= currentIndexes.index2)) {
                            // if we find incompatible combination, mark it as already returned
                            result.set(0, false);
                            return null;
                        }
                        else
                        if ((indexes[currentIndexes.port1] == currentIndexes.index1)
                                && (indexes[currentIndexes.port2] == currentIndexes.index2)) {
                            result.set(0, false);
                            return null;
                        }
                return null;
            });
            return result.get(0);
        }

        void addIncompatibleIndexes(IncompatibleIndexes foundIncompatibleIndexes) {
            incompatibleIndexes.add(foundIncompatibleIndexes);
            if (incompatibleIndexes.size() > maxSize) maxSize = incompatibleIndexes.size();
        }
    }
}
