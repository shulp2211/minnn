package com.milaboratory.mist.pattern;

import com.milaboratory.core.Range;
import com.milaboratory.core.sequence.NSequenceWithQuality;

import java.util.*;

import static com.milaboratory.mist.util.RangeTools.combineRanges;

public abstract class MultiplePatternsOperator extends SinglePattern {
    protected final SinglePattern[] operandPatterns;
    protected final ArrayList<GroupEdge> groupEdges;

    MultiplePatternsOperator(SinglePattern... operandPatterns) {
        this.operandPatterns = operandPatterns;
        this.groupEdges = new ArrayList<>();
        for (SinglePattern pattern : operandPatterns)
            groupEdges.addAll(pattern.getGroupEdges());
        if (groupEdges.size() != new HashSet<>(groupEdges).size())
            throw new IllegalStateException("Operands contain equal group edges!");
    }

    @Override
    public ArrayList<GroupEdge> getGroupEdges() {
        return groupEdges;
    }

    protected Match combineMatches(NSequenceWithQuality target, byte targetId, Match... matches) {
        ArrayList<MatchedItem> matchedItems = new ArrayList<>();
        Range[] ranges = new Range[matches.length];

        for (int i = 0; i < matches.length; i++) {
            matchedItems.addAll(matches[i].getMatchedGroupEdges());
            ranges[i] = matches[i].getRange();
        }

        matchedItems.add(new MatchedRange(target, targetId, 0, combineRanges(ranges)));
        return new Match(1, combineMatchScores(matches), matchedItems);
    }
}
