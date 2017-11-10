package com.milaboratory.mist.pattern;

import com.milaboratory.core.Range;
import com.milaboratory.core.alignment.Alignment;
import com.milaboratory.core.sequence.NSequenceWithQuality;
import com.milaboratory.core.sequence.NucleotideSequenceCaseSensitive;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

public final class PatternUtils {
    public static int invertCoordinate(int x) {
        return -2 - x;
    }

    /**
     * Fix group edge positions to make them not get beyond the right border of pattern sequence; and move group
     * edge positions if specified.
     *
     * @param groupEdgePositions group edge positions
     * @param move if not 0, add this value to all group edge positions; but never move positions below 0
     * @param maxPosition maximum allowed position for group edge; this is size of current sequence
     * @return new group edge positions
     */
    static List<GroupEdgePosition> fixGroupEdgePositions(List<GroupEdgePosition> groupEdgePositions,
                                                         int move, int maxPosition) {
        return groupEdgePositions.stream()
                .map(gp -> (move == 0) ? gp : new GroupEdgePosition(gp.getGroupEdge(),
                        (gp.getPosition() + move >= 0) ? gp.getPosition() + move : 0))
                .map(gp -> (gp.getPosition() <= maxPosition) ? gp
                        : new GroupEdgePosition(gp.getGroupEdge(), maxPosition))
                .collect(Collectors.toList());
    }

    /**
     * Generate match from alignment, for FuzzyMatchPattern and RepeatPattern.
     */
    static Match generateMatch(Alignment<NucleotideSequenceCaseSensitive> alignment, NSequenceWithQuality target,
                               byte targetId, List<GroupEdgePosition> groupEdgePositions) {
        Range foundRange = alignment.getSequence2Range();
        long foundScore = (long)alignment.getScore();
        MatchedRange matchedRange = new MatchedRange(target, targetId, 0, foundRange);
        ArrayList<MatchedItem> matchedItems = new ArrayList<>();
        matchedItems.add(matchedRange);

        for (GroupEdgePosition groupEdgePosition : groupEdgePositions) {
            int foundGroupEdgePosition = alignment.convertToSeq2Position(groupEdgePosition.getPosition());
            if (foundGroupEdgePosition == -1)
                if (groupEdgePosition.getPosition() < alignment.getSequence1Range().getLower())
                    foundGroupEdgePosition = foundRange.getLower();
                else if (groupEdgePosition.getPosition() > alignment.getSequence1Range().getUpper())
                    foundGroupEdgePosition = foundRange.getUpper();
                else
                    throw new IllegalStateException("Unexpected state when converting group edge positions: "
                            + "Sequence1Range=" + alignment.getSequence1Range()
                            + ", Sequence2Range=" + alignment.getSequence2Range()
                            + ", GroupEdgePosition=" + groupEdgePosition.getPosition());
            else if (foundGroupEdgePosition < 0)
                foundGroupEdgePosition = invertCoordinate(foundGroupEdgePosition);
            MatchedGroupEdge matchedGroupEdge = new MatchedGroupEdge(target, targetId, 0,
                    groupEdgePosition.getGroupEdge(), foundGroupEdgePosition);
            matchedItems.add(matchedGroupEdge);
        }

        return new Match(1, foundScore, matchedItems);
    }

    /**
     * Check if there is at least 1 uppercase letter in the specified range in pattern nucleotide sequence.
     * Specified range is allowed to be out of bounds of sequence.
     *
     * @param sequence case sensitive nucleotide sequence
     * @param from starting coordinate, inclusive
     * @param to ending coordinate, exclusive
     * @return true if there is at least 1 uppercase letter in the specified range
     */
    public static boolean containsUppercase(NucleotideSequenceCaseSensitive sequence, int from, int to) {
        from = Math.max(0, from);
        to = Math.min(sequence.size(), to);
        for (int i = from; i < to; i++) {
            if (Character.isUpperCase(sequence.symbolAt(i)))
                return true;
        }
        return false;
    }
}
