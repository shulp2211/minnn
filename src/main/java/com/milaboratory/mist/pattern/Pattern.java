package com.milaboratory.mist.pattern;

import com.milaboratory.core.sequence.MultiNSequenceWithQuality;

import java.util.ArrayList;

public interface Pattern {
    /**
     * Search for matches for this pattern in the input.
     *
     * @param input nucleotide sequence(s), where the search for the pattern will be performed
     * @return matching result, lazy object that contains functions that will perform actual calculations to find matches
     */
    MatchingResult match(MultiNSequenceWithQuality input);

    /**
     * Get list of names of groups that are inside this pattern.
     *
     * @return list of group names
     */
    ArrayList<String> getGroupNames();
}
