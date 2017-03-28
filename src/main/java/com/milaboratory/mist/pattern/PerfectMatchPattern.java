package com.milaboratory.mist.pattern;

import com.milaboratory.core.Range;
import com.milaboratory.core.motif.BitapMatcher;
import com.milaboratory.core.motif.Motif;
import com.milaboratory.core.sequence.NSequenceWithQuality;
import com.milaboratory.core.sequence.NucleotideSequence;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import static com.milaboratory.mist.pattern.Match.WHOLE_PATTERN_MATCH_GROUP_NAME_PREFIX;

public class PerfectMatchPattern implements SinglePattern {
    private final Motif<NucleotideSequence> motif;

    public PerfectMatchPattern(Motif<NucleotideSequence> motif) {
        this.motif = motif;
    }

    /**
     * Find perfect match with bitap.
     *
     * @param input target sequence
     * @param from starting point in target sequence (inclusive)
     * @param to ending point in target sequence (exclusive)
     * @param targetId number of read where sequence is matched
     * @param quickMatch if true, match.isFound() returns true or false, other methods throw exception
     * @return matching result
     */
    @Override
    public MatchingResult match(NSequenceWithQuality input, int from, int to, byte targetId, boolean quickMatch) {
        BitapMatcher matcher = motif.getBitapPattern().exactMatcher(input.getSequence(), from, to);
        if (quickMatch)
            if (matcher.findNext() == -1)
                return new QuickMatchingResult(false);
            else
                return new QuickMatchingResult(true);
        else {
            ArrayList<Match> matches = new ArrayList<>();
            int currentMatch;
            do {
                currentMatch = matcher.findNext();
                if (currentMatch != -1) {
                    Range foundRange = new Range(currentMatch, currentMatch + motif.size(), false);
                    CaptureGroupMatch wholePatternMatch = new CaptureGroupMatch(input, targetId, foundRange);
                    Map<String, CaptureGroupMatch> groupMatchMap = new HashMap<String, CaptureGroupMatch>() {{
                        put(WHOLE_PATTERN_MATCH_GROUP_NAME_PREFIX + "0", wholePatternMatch);
                    }};
                    // TODO: create scoring rules
                    matches.add(new Match(1, 1, groupMatchMap));
                }
            } while (currentMatch != -1);

            if (matches.size() == 0)
                return new SimpleMatchingResult();
            else
                return new SimpleMatchingResult(matches.toArray(new Match[matches.size()]));
        }
    }
}
