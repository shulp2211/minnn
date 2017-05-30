package com.milaboratory.mist.pattern;

import cc.redberry.pipe.OutputPort;
import com.milaboratory.core.Range;
import com.milaboratory.core.sequence.MultiNSequenceWithQuality;
import com.milaboratory.core.sequence.NSequenceWithQuality;
import com.milaboratory.mist.util.ApproximateSorter;
import com.milaboratory.mist.util.SorterByCoordinate;
import com.milaboratory.mist.util.SorterByScore;

import java.util.ArrayList;
import java.util.Arrays;

public final class MultiPattern extends MultipleReadsOperator {
    public MultiPattern(PatternAligner patternAligner, SinglePattern... singlePatterns) {
        super(patternAligner, singlePatterns);
    }

    @Override
    public String toString() {
        return "MultiPattern(" + Arrays.toString(singlePatterns) + ")";
    }

    @Override
    public MatchingResult match(MultiNSequenceWithQuality target, Range[] ranges, boolean[] reverseComplements) {
        if (target.numberOfSequences() != ranges.length)
            throw new IllegalArgumentException("Mismatched number of reads (" + target.numberOfSequences()
                    + ") and ranges (" + ranges.length + ")!");
        if (target.numberOfSequences() != reverseComplements.length)
            throw new IllegalArgumentException("Mismatched number of reads (" + target.numberOfSequences()
                    + ") and reverse complement flags (" + reverseComplements.length + ")!");
        if (target.numberOfSequences() != singlePatterns.length)
            throw new IllegalArgumentException("Mismatched number of reads (" + target.numberOfSequences()
                    + ") and patterns (" + singlePatterns.length + ")!");

        return new MultiPatternMatchingResult(patternAligner, singlePatterns, target, ranges, reverseComplements);
    }

    private static class MultiPatternMatchingResult extends MatchingResult {
        private final PatternAligner patternAligner;
        private final SinglePattern[] singlePatterns;
        private final MultiNSequenceWithQuality target;
        private final Range[] ranges;
        private final boolean[] reverseComplements;

        MultiPatternMatchingResult(PatternAligner patternAligner, SinglePattern[] singlePatterns,
                                  MultiNSequenceWithQuality target, Range[] ranges, boolean[] reverseComplements) {
            this.patternAligner = patternAligner;
            this.singlePatterns = singlePatterns;
            this.target = target;
            this.ranges = ranges;
            this.reverseComplements = reverseComplements;
        }

        @Override
        public OutputPort<Match> getMatches(boolean byScore, boolean fairSorting) {
            ArrayList<OutputPort<Match>> operandPorts = new ArrayList<>();
            NSequenceWithQuality currentTarget;
            Range currentRange;
            byte currentTargetId;
            ApproximateSorter sorter;

            for (int patternIndex = 0; patternIndex < singlePatterns.length; patternIndex++) {
                if (reverseComplements[patternIndex]) {
                    currentTarget = target.get(patternIndex).getReverseComplement();
                    currentRange = ranges[patternIndex].inverse();
                    currentTargetId = (byte) (-patternIndex - 1);
                } else {
                    currentTarget = target.get(patternIndex);
                    currentRange = ranges[patternIndex];
                    currentTargetId = (byte) (patternIndex + 1);
                }
                operandPorts.add(singlePatterns[patternIndex].match(currentTarget, currentRange, currentTargetId)
                        .getMatches(byScore, fairSorting));
            }

            if (byScore)
                sorter = new SorterByScore(patternAligner, true, true, fairSorting,
                        MatchValidationType.LOGICAL_AND);
            else
                sorter = new SorterByCoordinate(patternAligner, true, true, fairSorting,
                        MatchValidationType.LOGICAL_AND);

            return sorter.getOutputPort(operandPorts);
        }
    }
}
