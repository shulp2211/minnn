package com.milaboratory.mist.pattern;

import cc.redberry.pipe.OutputPort;
import com.milaboratory.core.Range;
import com.milaboratory.core.sequence.MultiNSequenceWithQuality;
import com.milaboratory.core.sequence.NSequenceWithQuality;

import java.util.ArrayList;

/**
 * Filter pattern can be used for both single and multiple patterns; it overrides match() methods for both single
 * and multiple patterns. It filters matches from pattern with specified Filter. For usage with MultipleReadsOperator
 * patterns, it must be wrapped with MultipleReadsFilterPattern.
 */
public final class FilterPattern extends SinglePattern {
    private final Filter filter;
    private final Pattern pattern;

    public FilterPattern(PatternAligner patternAligner, Filter filter, Pattern pattern) {
        super(patternAligner);
        testAlignersCompatibility(pattern);
        this.filter = filter;
        this.pattern = pattern;
    }

    @Override
    public String toString() {
        return "FilterPattern(" + filter + ", " + pattern + ")";
    }

    @Override
    public ArrayList<GroupEdge> getGroupEdges() {
        return pattern.getGroupEdges();
    }

    @Override
    public MatchingResult match(MultiNSequenceWithQuality target) {
        return new FilterMatchingResult(filter, pattern, target);
    }

    public MatchingResult match(MultiNSequenceWithQuality target, Range[] ranges, boolean[] reverseComplements) {
        return new FilterMatchingResult(filter, pattern, target, ranges, reverseComplements);
    }

    @Override
    public MatchingResult match(NSequenceWithQuality target, int from, int to, byte targetId) {
        return new FilterMatchingResult(filter, pattern, target, from, to, targetId);
    }

    private static class FilterMatchingResult extends MatchingResult {
        private final Filter filter;
        private final Pattern pattern;
        private final MultiNSequenceWithQuality targetMulti;
        private final Range[] ranges;
        private final boolean[] reverseComplements;
        private final NSequenceWithQuality targetSingle;
        private final int from;
        private final int to;
        private final byte targetId;

        FilterMatchingResult(Filter filter, Pattern pattern, MultiNSequenceWithQuality targetMulti) {
            this(filter, pattern, targetMulti, null, null, null, 0, 0, (byte)0);
        }

        FilterMatchingResult(Filter filter, Pattern pattern, MultiNSequenceWithQuality targetMulti,
                             Range[] ranges, boolean[] reverseComplements) {
            this(filter, pattern, targetMulti, ranges, reverseComplements, null, 0, 0, (byte)0);
        }

        FilterMatchingResult(Filter filter, Pattern pattern, NSequenceWithQuality targetSingle,
                             int from, int to, byte targetId) {
            this(filter, pattern, null, null, null, targetSingle, from, to, targetId);
        }

        private FilterMatchingResult(Filter filter, Pattern pattern, MultiNSequenceWithQuality targetMulti,
                                     Range[] ranges, boolean[] reverseComplements,
                                     NSequenceWithQuality targetSingle, int from, int to, byte targetId) {
            this.filter = filter;
            this.pattern = pattern;
            this.targetMulti = targetMulti;
            this.ranges = ranges;
            this.reverseComplements = reverseComplements;
            this.targetSingle = targetSingle;
            this.from = from;
            this.to = to;
            this.targetId = targetId;
        }

        @Override
        public OutputPort<Match> getMatches(boolean byScore, boolean fairSorting) {
            if (targetMulti != null)
                if ((ranges != null) && (reverseComplements != null))
                    if (pattern instanceof MultipleReadsOperator)
                        return new FilterOutputPort(filter, ((MultipleReadsOperator)pattern)
                            .match(targetMulti, ranges, reverseComplements).getMatches(byScore, fairSorting));
                    else throw new IllegalArgumentException(
                            "Operand pattern is not MultipleReadsOperator, but ranges and reverseComplements are not null.");
                else
                    return new FilterOutputPort(filter, pattern.match(targetMulti).getMatches(byScore, fairSorting));
            else if (targetSingle != null) {
                if (!(pattern instanceof SinglePattern)) throw new IllegalArgumentException(
                        "Trying to use filter with multi-target pattern and single-target match arguments.");
                return new FilterOutputPort(filter, ((SinglePattern)pattern).match(targetSingle, from, to, targetId)
                        .getMatches(byScore, fairSorting));
            } else throw new IllegalStateException("Both targetMulti and targetSingle are null.");
        }

        private static class FilterOutputPort implements OutputPort<Match> {
            private final Filter filter;
            private final OutputPort<Match> operandPort;

            FilterOutputPort(Filter filter, OutputPort<Match> operandPort) {
                this.filter = filter;
                this.operandPort = operandPort;
            }

            @Override
            public Match take() {
                Match currentMatch, currentFilteredMatch;
                do {
                    currentMatch = operandPort.take();
                    if (currentMatch == null)
                        return null;
                    currentFilteredMatch = filter.checkMatch(currentMatch);
                } while (currentFilteredMatch == null);

                return currentFilteredMatch;
            }
        }
    }
}
