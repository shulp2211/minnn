package com.milaboratory.mist.pattern;

import com.milaboratory.core.Range;
import com.milaboratory.core.motif.Motif;
import com.milaboratory.core.sequence.NSequenceWithQuality;
import com.milaboratory.core.sequence.NucleotideSequence;

import java.util.HashMap;
import java.util.Map;

import static com.milaboratory.mist.pattern.Match.WHOLE_PATTERN_MATCH_GROUP_NAME_PREFIX;

public class PerfectMatchPattern implements SinglePattern {
    private final Motif<NucleotideSequence> motif;

    public PerfectMatchPattern(Motif<NucleotideSequence> motif) {
        this.motif = motif;
    }

    @Override
    public MatchingResult match(NSequenceWithQuality input, int from, int to, byte targetId, boolean quickMatch) {
        NucleotideSequence target = input.getSequence().getRange(new Range(from, to, false));
        int result = motif.getBitapPattern().exactSearch(target);
        if (result >= 0)
            if (quickMatch)
                return new QuickMatchingResult(true);
            else {
                Range foundRange = new Range(from + result, from + result + motif.size(), false);
                CaptureGroupMatch wholePatternMatch = new CaptureGroupMatch(input, targetId, foundRange);
                Map<String, CaptureGroupMatch> groupMatchMap = new HashMap<String, CaptureGroupMatch>() {{
                   put(WHOLE_PATTERN_MATCH_GROUP_NAME_PREFIX + "0", wholePatternMatch);
                }};

                // TODO: create scoring rules
                return new SingleMatchingResult(new Match(1, 1, groupMatchMap));
            }
        else
            if (quickMatch)
                return new QuickMatchingResult(false);
            else
                return new SingleMatchingResult(null);
    }
}
