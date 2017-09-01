package com.milaboratory.mist.pattern;

import com.milaboratory.core.Range;
import com.milaboratory.core.alignment.Alignment;
import com.milaboratory.core.sequence.NSequenceWithQuality;
import com.milaboratory.core.sequence.NucleotideSequence;

import java.util.ArrayList;
import java.util.List;

public final class PatternUtils {
    public static int invertCoordinate(int x) {
        return -2 - x;
    }

    /**
     * Generate match from alignment, for FuzzyMatchPattern and RepeatPattern.
     */
    static Match generateMatch(Alignment<NucleotideSequence> alignment, NSequenceWithQuality target, byte targetId,
                               List<GroupEdgePosition> groupEdgePositions) {
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
}
