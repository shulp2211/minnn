package com.milaboratory.mist.pattern;

import com.milaboratory.core.sequence.MultiNSequenceWithQuality;

import java.util.ArrayList;

public abstract class Pattern {
    /**
     * Search for matches for this pattern in the input.
     *
     * @param input nucleotide sequence(s), where the search for the pattern will be performed
     * @return matching result, lazy object that contains functions that will perform actual calculations to find matches
     */
    public abstract MatchingResult match(MultiNSequenceWithQuality input);

    /**
     * Get list of group edges that are inside this pattern.
     *
     * @return list of group edges
     */
    public abstract ArrayList<GroupEdge> getGroupEdges();

    /**
     * Pattern-specific procedure for combining match scores. Default implementation is for And pattern.
     * Null matches are allowed in upper-level logic operators and ignored when counting total score.
     *
     * @param matches matches to combine their scores
     * @return resulting match score
     */
    protected float combineMatchScores(Match... matches) {
        float resultScore = 0;
        for (Match match : matches)
            if (match != null)
                resultScore += match.getScore();
        return resultScore;
    }
}
