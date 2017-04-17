package com.milaboratory.mist.pattern;

import com.milaboratory.core.Range;
import com.milaboratory.core.sequence.NSequenceWithQuality;

import java.util.*;

import static com.milaboratory.mist.pattern.Match.WHOLE_PATTERN_MATCH_GROUP_NAME_PREFIX;
import static com.milaboratory.mist.util.RangeTools.combineRanges;

public abstract class MultiplePatternsOperator extends SinglePattern {
    protected final SinglePattern[] operandPatterns;
    protected final ArrayList<String> groupNames;

    MultiplePatternsOperator(SinglePattern... operandPatterns) {
        this.operandPatterns = operandPatterns;
        this.groupNames = new ArrayList<>();
        for (SinglePattern pattern : operandPatterns)
            groupNames.addAll(pattern.getGroupNames());
        if (groupNames.size() != new HashSet<>(groupNames).size())
            throw new IllegalStateException("Operands contain groups with equal names!");
    }

    @Override
    public ArrayList<String> getGroupNames() {
        return groupNames;
    }

    protected Match combineMatches(NSequenceWithQuality target, byte targetId, Match... matches) {
        Map<String, CaptureGroupMatch> groupMatches = new HashMap<>();
        Range[] ranges = new Range[matches.length];

        for (int i = 0; i < matches.length; i++) {
            groupMatches.putAll(matches[i].getGroupMatches(true));
            ranges[i] = matches[i].getWholePatternMatch().getRange();
        }

        CaptureGroupMatch wholePatternMatch = new CaptureGroupMatch(target, targetId, combineRanges(ranges));
        groupMatches.put(WHOLE_PATTERN_MATCH_GROUP_NAME_PREFIX + 0, wholePatternMatch);
        return new Match(1, combineMatchScores(matches), groupMatches);
    }
}
