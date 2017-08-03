package com.milaboratory.mist.pattern;

import com.milaboratory.core.Range;
import com.milaboratory.core.sequence.MultiNSequenceWithQuality;
import com.milaboratory.core.sequence.NSequenceWithQuality;

import java.util.List;
import java.util.stream.Collectors;

public abstract class SinglePattern extends Pattern {
    SinglePattern(PatternAligner patternAligner) {
        super(patternAligner);
    }

    @Override
    public MatchingResult match(MultiNSequenceWithQuality target) {
        if (!(target instanceof NSequenceWithQuality))
            throw new IllegalArgumentException("Supports only single NSequenceWithQuality.");
        return match((NSequenceWithQuality) target);
    }

    public MatchingResult match(NSequenceWithQuality target, Range range) {
        return match(target, range, (byte) 1);
    }

    /**
     * Search this pattern in target sequence
     *
     * @param target target sequence
     * @param range searching range in target sequence, non-reversed only
     * @param targetId number of read where sequence is matched, numbers start from 1
     *                 negative if matched in reverse complement
     *                 0 if complex pattern uses multiple reads to match
     * @return matching result
     */
    public MatchingResult match(NSequenceWithQuality target, Range range, byte targetId) {
        if (range.isReverse())
            throw new IllegalArgumentException("Doesn't support reversed ranges.");
        return match(target, range.getFrom(), range.getTo(), targetId);
    }

    public MatchingResult match(NSequenceWithQuality target) {
        return match(target, 0, target.size(), (byte) 1);
    }

    public MatchingResult match(NSequenceWithQuality target, int from, int to) {
        return match(target, from, to, (byte) 1);
    }

    /**
     * Search this pattern in target sequence
     *
     * @param target target sequence
     * @param from starting point in target sequence (inclusive)
     * @param to ending point in target sequence (exclusive)
     * @param targetId number of read where sequence is matched, numbers start from 1
     *                 negative if matched in reverse complement
     *                 0 if complex pattern uses multiple reads to match
     * @return matching result
     */
    public abstract MatchingResult match(NSequenceWithQuality target, int from, int to, byte targetId);

    /**
     * Fix group edge positions to make them not get beyond the right border of pattern sequence; and move group
     * edge positions if specified.
     *
     * @param groupEdgePositions group edge positions
     * @param move if not 0, add this value to all group edge positions; but never move positions below 0
     * @param maxPosition maximum allowed position for group edge; this is size of current sequence
     * @return new group edge positions
     */
    protected static List<GroupEdgePosition> fixGroupEdgePositions(List<GroupEdgePosition> groupEdgePositions,
                                                                   int move, int maxPosition) {
        return groupEdgePositions.stream()
                .map(gp -> (move == 0) ? gp : new GroupEdgePosition(gp.getGroupEdge(),
                        (gp.getPosition() + move >= 0) ? gp.getPosition() + move : 0))
                .map(gp -> (gp.getPosition() <= maxPosition) ? gp
                        : new GroupEdgePosition(gp.getGroupEdge(), maxPosition))
                .collect(Collectors.toList());
    }
}
