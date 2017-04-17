package com.milaboratory.mist.util;

import cc.redberry.pipe.OutputPort;
import com.milaboratory.core.Range;
import com.milaboratory.core.sequence.NSequenceWithQuality;
import com.milaboratory.mist.pattern.CaptureGroupMatch;
import com.milaboratory.mist.pattern.Match;

import java.util.HashMap;
import java.util.Map;

import static com.milaboratory.mist.pattern.Match.WHOLE_PATTERN_MATCH_GROUP_NAME_PREFIX;
import static com.milaboratory.mist.util.RangeTools.combineRanges;

public abstract class ApproximateSorter {
    protected final boolean multipleReads;
    protected final boolean combineScoresBySum;
    protected final OutputPort<Match>[] inputPorts;
    protected final int numberOfPorts;

    /**
     * This sorter allows to get output port for approximately sorted matches by score or coordinate from
     * input ports. Specific sorters (by score, coordinate and with different rules) are extending this class.
     *
     * @param multipleReads true if we combine matches from multiple reads; false if we combine matches
     *                      from single read
     * @param combineScoresBySum true if combined score must be equal to sum of match scores; false if combined
     *                           score must be the highest of match scores
     * @param inputPorts ports for input matches; we assume that they are already sorted, maybe approximately
     */
    public ApproximateSorter(boolean multipleReads, boolean combineScoresBySum, OutputPort<Match>[] inputPorts) {
        this.multipleReads = multipleReads;
        this.combineScoresBySum = combineScoresBySum;
        this.inputPorts = inputPorts;
        this.numberOfPorts = inputPorts.length;
    }

    /**
     * Get output port for sorted combined matches.
     *
     * @return output port
     */
    public abstract OutputPort<Match> getOutputPort();

    /**
     * Get combined match from a group of input matches. It uses multipleReads flag to determine how to combine matches
     * (by combining ranges for single read or by numbering the whole group matches for multiple reads).
     *
     * @param matches input matches
     * @return combined match
     */
    protected Match combineMatches(Match... matches) {
        Map<String, CaptureGroupMatch> groupMatches = new HashMap<>();

        if (multipleReads) {
            int wholeGroupIndex = 0;
            for (Match match : matches) {
                if (match == null) {
                    groupMatches.put(WHOLE_PATTERN_MATCH_GROUP_NAME_PREFIX + wholeGroupIndex++, null);
                    continue;
                }
                groupMatches.putAll(match.getGroupMatches(true));
                for (int i = 0; i < match.getNumberOfPatterns(); i++)
                    groupMatches.put(WHOLE_PATTERN_MATCH_GROUP_NAME_PREFIX + wholeGroupIndex++,
                            match.getWholePatternMatch(i));
            }
            return new Match(wholeGroupIndex, combineMatchScores(matches), groupMatches);
        } else {
            NSequenceWithQuality target = matches[0].getWholePatternMatch().getTarget();
            byte targetId = matches[0].getWholePatternMatch().getTargetId();
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

    /**
     * Combine match scores. It is used in combineMatches function. Different patterns may combine operand scores
     * by sum or by max value of operand scores.
     *
     * @param matches matches from which we will get the scores
     * @return combined score
     */
    protected float combineMatchScores(Match... matches) {
        float resultScore;
        if (combineScoresBySum) {
            resultScore = 0;
            for (Match match : matches)
                if (match != null)
                    resultScore += match.getScore();
        } else {
            resultScore = Float.NEGATIVE_INFINITY;
            for (Match match : matches)
                if (match != null)
                    if (match.getScore() > resultScore)
                        resultScore = match.getScore();
        }
        return resultScore;
    }
}
