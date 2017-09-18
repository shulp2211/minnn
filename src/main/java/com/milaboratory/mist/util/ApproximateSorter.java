package com.milaboratory.mist.util;

import cc.redberry.pipe.OutputPort;
import com.milaboratory.core.Range;
import com.milaboratory.core.sequence.NSequenceWithQuality;
import com.milaboratory.mist.pattern.*;

import java.util.*;

import static com.milaboratory.mist.pattern.MatchValidationType.*;
import static com.milaboratory.mist.util.DebugUtils.countCall;
import static com.milaboratory.mist.util.DebugUtils.countExecutionTime;
import static com.milaboratory.mist.util.RangeTools.*;
import static com.milaboratory.mist.util.UnfairSorterConfiguration.*;

public final class ApproximateSorter {
    private final ApproximateSorterConfiguration conf;
    private final ArrayList<SpecificOutputPort> unfairOutputPorts = new ArrayList<>();
    private final HashSet<IncompatibleIndexes> allIncompatibleIndexes = new HashSet<>();

    private int unfairSorterTakenValues = 0;

    /**
     * This sorter allows to get output port for approximately or fair sorted matches by score from input ports.
     *
     * @param conf sorter configuration
     */
    public ApproximateSorter(ApproximateSorterConfiguration conf) {
        this.conf = conf;
    }

    /**
     * Get output port for sorted combined matches.
     *
     * @return output port
     */
    public OutputPort<Match> getOutputPort() {
        return new MatchesOutputPort();
    }

    /**
     * Get combined match from a group of input matches. It uses multipleReads flag to determine how to combine matches
     * (by combining ranges for single read or by numbering the matched ranges for multiple reads).
     *
     * @param matches input matches
     * @return combined match
     */
    private Match combineMatches(Match... matches) {
        ArrayList<MatchedItem> matchedItems = new ArrayList<>();

        if (conf.multipleReads) {
            int patternIndex = 0;
            boolean allMatchesAreNull = true;
            for (Match match : matches) {
                if (match == null) {
                    if (conf.matchValidationType == LOGICAL_OR) {
                        matchedItems.add(new NullMatchedRange(patternIndex++));
                        continue;
                    } else throw new IllegalStateException(
                            "Found null match when MatchValidationType doesn't allow them");
                } else allMatchesAreNull = false;
                for (int i = 0; i < match.getNumberOfPatterns(); i++) {
                    MatchedRange currentMatchedRange = match.getMatchedRange(i);
                    if (currentMatchedRange instanceof NullMatchedRange) {
                        if (match.getMatchedGroupEdgesByPattern(i).size() > 0)
                            throw new IllegalStateException("Null pattern contains "
                                    + match.getMatchedGroupEdgesByPattern(i).size() + " group edges");
                        matchedItems.add(new NullMatchedRange(patternIndex++));
                    } else {
                        matchedItems.add(new MatchedRange(currentMatchedRange.getTarget(),
                                currentMatchedRange.getTargetId(), patternIndex, currentMatchedRange.getRange()));
                        for (MatchedGroupEdge matchedGroupEdge : match.getMatchedGroupEdgesByPattern(i))
                            matchedItems.add(new MatchedGroupEdge(matchedGroupEdge.getTarget(),
                                    matchedGroupEdge.getTargetId(), patternIndex, matchedGroupEdge.getGroupEdge(),
                                    matchedGroupEdge.getPosition()));
                        patternIndex++;
                    }
                }
            }

            if (allMatchesAreNull)
                return null;
            else
                return new Match(patternIndex, combineMatchScores(matches), matchedItems);
        } else if (conf.matchValidationType == FIRST) {
            boolean matchExist = false;
            int bestMatchPort = 0;
            long bestScore = Long.MIN_VALUE;
            for (int i = 0; i < matches.length; i++)
                if ((matches[i] != null) && (matches[i].getScore() > bestScore)) {
                    matchExist = true;
                    bestScore = matches[i].getScore();
                    bestMatchPort = i;
                }

            if (matchExist) {
                return matches[bestMatchPort];
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

            return new Match(1, combineMatchScores(matches) + combinedRange.getScorePenalty(),
                    matchedItems);
        }
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
    private boolean areNullMatchesAllowed() {
        return ((conf.matchValidationType == LOGICAL_OR) || (conf.matchValidationType == FIRST));
    }

    /**
     * Take all matches with filtering: match combinations that contain incompatible ranges or have score
     * below threshold will not be included. For unfair sorting, output ports with match number limits will be used.
     *
     * @return list of all matches with filtering
     */
    private ArrayList<Match> takeFilteredMatches() {
        ArrayList<Match> allMatchesFiltered = new ArrayList<>();
        long penaltyThreshold = conf.patternAligner.penaltyThreshold();
        int numberOfOperands = conf.operandPatterns.length;
        int[] matchIndexes = new int[numberOfOperands];
        Match[] currentMatches = new Match[numberOfOperands];

        if (conf.fairSorting || !conf.specificOutputPorts) {
            ArrayList<ArrayList<Match>> allMatches = new ArrayList<>();
            OutputPort<Match> currentPort;
            ArrayList<Match> currentPortMatchesList;
            Match currentMatch;
            int totalNumberOfCombinations = 1;

            // take all matches from all operands
            for (int i = 0; i < numberOfOperands; i++) {
                if (conf.fairSorting) {
                    currentPort = conf.multipleReads
                            ? conf.operandPatterns[i].match(conf.target).getMatches(true)
                            : ((SinglePattern)conf.operandPatterns[i])
                                .match(conf.target.get(0), conf.from(), conf.to()).getMatches(true);
                    currentPortMatchesList = new ArrayList<>();
                    do {
                        currentMatch = currentPort.take();
                        if ((currentMatch != null) || (areNullMatchesAllowed() && (allMatches.get(i).size() == 0)))
                            currentPortMatchesList.add(currentMatch);
                    } while (currentMatch != null);
                } else
                    currentPortMatchesList = getPortWithParams(i).takeAll(areNullMatchesAllowed());

                allMatches.add(currentPortMatchesList);
                totalNumberOfCombinations *= currentPortMatchesList.size();
            }

            for (int i = 0; i < totalNumberOfCombinations; i++) {
                if (areCompatible(matchIndexes)) {
                    for (int j = 0; j < numberOfOperands; j++)
                        currentMatches[j] = allMatches.get(j).get(matchIndexes[j]);
                    IncompatibleIndexes incompatibleIndexes = findIncompatibleIndexes(currentMatches, matchIndexes);
                    if (incompatibleIndexes != null)
                        allIncompatibleIndexes.add(incompatibleIndexes);
                    else {
                        Match combinedMatch = combineMatches(currentMatches);
                        if ((combinedMatch != null) && (combinedMatch.getScore() >= penaltyThreshold))
                            allMatchesFiltered.add(combinedMatch);
                    }
                }

                // Update matchIndexes to switch to the next combination on next iteration of outer loop
                for (int j = 0; j < numberOfOperands; j++) {
                    int currentIndex = matchIndexes[j];
                    if (currentIndex + 1 < allMatches.get(j).size()) {
                        matchIndexes[j] = currentIndex + 1;
                        break;
                    }
                    // we need to update next index and reset current index to zero
                    matchIndexes[j] = 0;
                }
            }
        } else {
            int firstFoundNullIndex = numberOfOperands - 1;
            boolean allPortsFinished = false;
            while (!allPortsFinished) {
                currentMatches = getMatchesByIndexes(matchIndexes);
                if (Arrays.stream(currentMatches).noneMatch(Objects::isNull)) {
                    IncompatibleIndexes incompatibleIndexes = findIncompatibleIndexes(currentMatches, matchIndexes);
                    if (incompatibleIndexes == null) {
                        Match combinedMatch = combineMatches(currentMatches);
                        if ((combinedMatch != null) && (combinedMatch.getScore() >= penaltyThreshold))
                            allMatchesFiltered.add(combinedMatch);
                    }
                } else
                    for (int i = 0; i < numberOfOperands - 1; i++)
                        if (currentMatches[i] == null) {
                            firstFoundNullIndex = i;
                            break;
                        }

                // update matchIndexes
                if (currentMatches[firstFoundNullIndex] == null) {
                    if (firstFoundNullIndex == 0)
                        allPortsFinished = true;
                    else {
                        matchIndexes[firstFoundNullIndex - 1]++;
                        for (int i = firstFoundNullIndex; i < numberOfOperands; i++)
                            matchIndexes[i] = 0;
                    }
                } else
                    matchIndexes[numberOfOperands - 1]++;
            }
        }

        return allMatchesFiltered;
    }

    /**
     * Returns null if this match combination is valid or IncompatibleIndexes structure if it finds
     * 2 matches that have incompatible ranges.
     *
     * @param matches group of matches to check
     * @param indexes indexes of all provided matches for writing to IncompatibleIndexes structure
     * @return IncompatibleIndexes structure
     */
    private IncompatibleIndexes findIncompatibleIndexes(Match[] matches, int[] indexes) {
        if (matches.length != indexes.length)
            throw new IllegalArgumentException("matches length is " + matches.length + ", indexes length is "
                + indexes.length + "; they must be equal!");

        IncompatibleIndexes result = null;
        switch (conf.matchValidationType) {
            case LOGICAL_OR:
            case LOGICAL_AND:
            case FIRST:
                break;
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
                break;
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
        }

        return result;
    }

    /**
     * Check if this combination of indexes contains incompatible indexes. Incompatible means that we
     * already know that matches with that indexes have misplaced ranges.
     *
     * @param indexes indexes of matches
     * @return true if there are no incompatible indexes found; false if they are found
     */
    private boolean areCompatible(int[] indexes) {
        if (!conf.specificOutputPorts)
            for (IncompatibleIndexes currentIndexes : allIncompatibleIndexes)
                if ((indexes[currentIndexes.port1] == currentIndexes.index1)
                        && (indexes[currentIndexes.port2] == currentIndexes.index2))
                    return false;
        return true;
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
            int portLimit = unfairSorterPortLimits.get(currentPattern.getClass());
            if (conf.matchValidationType == FOLLOWING) {
                int patternMaxLength = ((SinglePattern)currentPattern).estimateMaxLength();
                if (patternMaxLength != -1)
                    matchTo = Math.min(conf.to(), matchFrom + patternMaxLength);
                portLimit = specificPortLimit;
            }
            currentPort = new SpecificOutputPort(conf.multipleReads
                    ? currentPattern.match(conf.target).getMatches(false)
                    : ((SinglePattern)currentPattern).match(conf.target.get(0), matchFrom, matchTo)
                        .getMatches(false),
                    operandIndex, from, portLimit);
            unfairOutputPorts.add(currentPort);
        }
        return currentPort;
    }

    /**
     * Get array of matches by array of match indexes in output ports.
     *
     * @param indexes array of indexes in output ports of pattern operands
     * @return array of matches
     */
    private Match[] getMatchesByIndexes(int[] indexes) {
        int numberOfOperands = conf.operandPatterns.length;
        if (indexes.length != numberOfOperands)
            throw new IllegalArgumentException("indexes length is " + indexes.length + ", number of operands: "
                    + numberOfOperands);
        Match[] matches = new Match[numberOfOperands];
        if (conf.specificOutputPorts) {
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

    private static class IncompatibleIndexes {
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

    private class MatchesOutputPort implements OutputPort<Match> {
        private final ArrayList<ArrayList<Match>> takenMatches;
        private final int numberOfPorts;
        private ArrayList<Integer> currentIndexes;
        private ArrayList<Integer> stage3Indexes;
        private final Match[] currentMatches;
        private boolean alwaysReturnNull = false;
        private int numberOfSkippedIterations = 0;

        /* maximum number of values to take from single port on stage3;
           it will be increased if all combinations with this depth will end */
        private int stage3CurrentDepth = 3;

        private ArrayList<Match> allMatchesFiltered;
        private int filteredMatchesCount = 0;
        private int currentMatchIndex = 0;
        private boolean sortingPerformed = false;

        MatchesOutputPort() {
            this.takenMatches = new ArrayList<>();
            for (int i = 0; i < numberOfPorts; i++)
                this.takenMatches.add(new ArrayList<>());
            this.numberOfPorts = numberOfPorts;
            this.currentIndexes = new ArrayList<>(Collections.nCopies(numberOfPorts, 0));
            this.stage3Indexes = new ArrayList<>(Collections.nCopies(numberOfPorts, 0));
            this.currentMatches = new Match[numberOfPorts];
        }

        @Override
        public Match take() {
            if (alwaysReturnNull) return null;
            if (conf.fairSorting) return takeFairSorted();
            if (unfairSorterTakenValues++ > conf.unfairSorterLimit) {
                alwaysReturnNull = true;
                return null;
            }

            long penaltyThreshold = conf.patternAligner.penaltyThreshold();
            List<Match> tempMatches = new ArrayList<>();
            tempMatches.add(null);
            countCall("take");
            countExecutionTime("take", () -> {
                Match combinedMatch = null;
                boolean combinationFound = false;
                GET_NEXT_COMBINATION:
                while (!combinationFound) {
                    if (tableOfIterations.getTotalCombinationsCount() == tableOfIterations.getNumberOfReturnedCombinations()) {
                        alwaysReturnNull = true;
                        return null;
                    }

                    for (int i = 0; i < numberOfPorts; i++) {
                        ArrayList<Match> currentPortMatches = takenMatches.get(i);
                        ApproximateSorterOperandPort currentPort = inputPorts.get(i);
                        // if we didn't take the needed match before, take it now
                        if (currentIndexes.get(i) == currentPortMatches.size()) {
                            Match takenMatch = currentPort.outputPort.take();
                            if (takenMatch == null) {
                                if (currentPortMatches.size() == 0) {
                                    if (areNullMatchesAllowed()) {
                                        currentPortMatches.add(null);
                                        tableOfIterations.setPortEndReached(i, 1);
                                        currentIndexes.set(i, 0);
                                    } else {
                                        alwaysReturnNull = true;
                                        return null;
                                    }
                                } else {
                                    int currentIndex = currentIndexes.get(i);
                                    tableOfIterations.setPortEndReached(i, currentIndex);
                                    currentIndexes.set(i, currentIndex - 1);
                                    numberOfSkippedIterations++;
                                    calculateNextIndexes();
                                    continue GET_NEXT_COMBINATION;
                                }
                            } else {
                                currentPortMatches.add(takenMatch);
                                if (currentPortMatches.size() == currentPort.unfairSorterPortLimit)
                                    tableOfIterations.setPortEndReached(i, currentPort.unfairSorterPortLimit);
                            }
                        }
                        currentMatches[i] = currentPortMatches.get(currentIndexes.get(i));
                    }

                    IncompatibleIndexes incompatibleIndexes = findIncompatibleIndexes(currentMatches, currentIndexes);
                    if (incompatibleIndexes == null) {
                        combinedMatch = combineMatches(currentMatches);
                        if ((combinedMatch != null) && (combinedMatch.getScore() >= penaltyThreshold))
                            combinationFound = true;
                        else {
                        /* current combination doesn't fit the score threshold, mark it as returned
                         and continue search */
                            tableOfIterations.addReturnedCombination(currentIndexes);
                            calculateNextIndexes();
                        }
                    } else {
                    /* mark invalid match as already returned in table of iterations, write found
                     incompatible indexes to table of iterations and continue search */
                        tableOfIterations.addReturnedCombination(currentIndexes);
                        tableOfIterations.addIncompatibleIndexes(incompatibleIndexes);
                        calculateNextIndexes();
                    }
                }
                tableOfIterations.addReturnedCombination(currentIndexes);
                calculateNextIndexes();
                tempMatches.set(0, combinedMatch);

                return null;
            });
            return tempMatches.get(0);

        }

        private Match takeFairSorted() {
            if (!sortingPerformed) {
                allMatchesFiltered = takeFilteredMatches();
                filteredMatchesCount = allMatchesFiltered.size();
                allMatchesFiltered.sort(Comparator.comparingLong(Match::getScore).reversed());
                sortingPerformed = true;
            }

            if (currentMatchIndex >= filteredMatchesCount) return null;

            return allMatchesFiltered.get(currentMatchIndex++);
        }

        /**
         * Calculate next indexes for matches arrays: which next combination will be returned.
         */
        private void calculateNextIndexes() {
            countCall("calculate");
            countExecutionTime("calculate", () -> {
                            /* If all combinations already iterated, there is nothing to calculate */
                if (tableOfIterations.getTotalCombinationsCount() == tableOfIterations.getNumberOfReturnedCombinations())
                    return null;

            /* Stage 1: return combination of 1st values from each port, then combinations of 2nd value from
            one port and 1st values from other ports */
                List<Boolean> needReturn = new ArrayList<>();
                needReturn.add(false);
                countExecutionTime("stage1", () -> {
                    while (tableOfIterations.getNumberOfReturnedCombinations() + numberOfSkippedIterations <= numberOfPorts) {
                        for (int i = 0; i < numberOfPorts; i++)
                            if (i == tableOfIterations.getNumberOfReturnedCombinations() + numberOfSkippedIterations - 1)
                                if (areNullMatchesAllowed() && (takenMatches.get(i).get(0) == null)) {
                                    numberOfSkippedIterations++;
                                    currentIndexes.set(i, 0);
                                } else
                                    currentIndexes.set(i, 1);
                            else
                                currentIndexes.set(i, 0);

                        // if we found valid combination, return it, otherwise continue search
                        if (tableOfIterations.isCompatible(false, currentIndexes)
                                && !tableOfIterations.isCombinationReturned(currentIndexes)) {
                            needReturn.set(0, true);
                            return null;
                        }
                    }
                    return null;
                });
                if (needReturn.get(0))
                    return null;

            /* Stage 2: iterate over ports, trying to pick better score, based on deltas if we count total score
            based on sum, or based on max value if we count total score based on max value */
                countExecutionTime("stage2", () -> {
                    while (tableOfIterations.getNumberOfEndedPorts() < numberOfPorts) {
                        if (conf.combineScoresBySum) {
                            int bestDeltaPort = 0;
                            long bestDelta = Long.MIN_VALUE;
                            for (int i = 0; i < numberOfPorts; i++) {
                                ArrayList<Match> currentPortMatches = takenMatches.get(i);
                                int currentIndex = currentIndexes.get(i);
                                if (tableOfIterations.isPortEndReached(i)) continue;
                                int match1Number = (currentIndex == 0) ? 0 : currentIndex - 1;
                                int match2Number = (currentIndex == 0) ? 1 : currentIndex;
                                Match match1 = currentPortMatches.get(match1Number);
                                Match match2 = currentPortMatches.get(match2Number);
                                long currentDelta = match2.getScore() - match1.getScore();
                                if (currentDelta > bestDelta) {
                                    bestDelta = currentDelta;
                                    bestDeltaPort = i;
                                }
                            }
                            currentIndexes.set(bestDeltaPort, currentIndexes.get(bestDeltaPort) + 1);
                        } else {
                            int bestScorePort = 0;
                            long bestScore = Long.MIN_VALUE;
                            for (int i = 0; i < numberOfPorts; i++) {
                                if (tableOfIterations.isPortEndReached(i)) continue;
                                Match currentMatch = takenMatches.get(i).get(currentIndexes.get(i));
                                if (currentMatch.getScore() > bestScore) {
                                    bestScore = currentMatch.getScore();
                                    bestScorePort = i;
                                }
                            }
                            currentIndexes.set(bestScorePort, currentIndexes.get(bestScorePort) + 1);
                        }

                        // if we found valid combination, return it, otherwise continue search
                        if (tableOfIterations.isCompatible(false, currentIndexes)
                                && !tableOfIterations.isCombinationReturned(currentIndexes)) {
                            needReturn.set(0, true);
                            return null;
                        }
                    }

                    return null;
                });
                if (needReturn.get(0))
                    return null;


            /* Stage 3: iterate over all remaining combinations of ports */
                countExecutionTime("stage3", () -> {
                    while (true) {
                        if (!tableOfIterations.isCombinationReturned(stage3Indexes)
                                && tableOfIterations.isCompatible(false, stage3Indexes)) {
                            currentIndexes = new ArrayList<>(stage3Indexes);
                            return null;
                        }

                        // Update innerArrayIndexes to switch to the next combination on next iteration of outer loop
                        for (int i = 0; i < numberOfPorts; i++) {
                            int currentIndex = stage3Indexes.get(i);
                            if (currentIndex + 1 < Math.min(stage3CurrentDepth,
                                    tableOfIterations.getPortMatchesQuantity(i))) {
                                stage3Indexes.set(i, currentIndex + 1);
                                break;
                            }
                            // we need to update next index and reset current index to zero
                            stage3Indexes.set(i, 0);
                            // if we looped through all combinations, increase the depth or stop the search
                            if (i == numberOfPorts - 1) {
                                boolean allPortsEnded = true;
                                for (int j = 0; j < numberOfPorts; j++)
                                    if (tableOfIterations.getPortMatchesQuantity(j) > stage3CurrentDepth) {
                                        stage3CurrentDepth++;
                                        allPortsEnded = false;
                                    }
                                if (allPortsEnded) {
                                    alwaysReturnNull = true;
                                    return null;
                                }
                            }
                        }
                    }
                });
                return null;
            });
        }
    }
}
