package com.milaboratory.mist.pattern;

import cc.redberry.pipe.OutputPort;
import com.milaboratory.core.Range;
import com.milaboratory.core.sequence.NSequenceWithQuality;

import java.util.ArrayList;

public class AnyPattern extends SinglePattern {
    public AnyPattern(PatternAligner patternAligner) {
        super(patternAligner);
    }

    @Override
    public String toString() {
        return "AnyPattern()";
    }

    @Override
    public ArrayList<GroupEdge> getGroupEdges() {
        return new ArrayList<>();
    }

    @Override
    public MatchingResult match(NSequenceWithQuality target, int from, int to, byte targetId) {
        return new AnyPatternMatchingResult(target, from, to, targetId);
    }

    private static class AnyPatternMatchingResult extends MatchingResult {
        private final NSequenceWithQuality target;
        private final int from;
        private final int to;
        private final byte targetId;

        AnyPatternMatchingResult(NSequenceWithQuality target, int from, int to, byte targetId) {
            this.target = target;
            this.from = from;
            this.to = to;
            this.targetId = targetId;
        }

        @Override
        public OutputPort<Match> getMatches(boolean byScore, boolean fairSorting) {
            return new AnyPatternOutputPort(target, from, to, targetId);
        }

        private static class AnyPatternOutputPort implements OutputPort<Match> {
            private final NSequenceWithQuality target;
            private final int from;
            private final int to;
            private final byte targetId;
            private boolean firstTake = true;

            AnyPatternOutputPort(NSequenceWithQuality target, int from, int to, byte targetId) {
                this.target = target;
                this.from = from;
                this.to = to;
                this.targetId = targetId;
            }

            @Override
            public Match take() {
                if (!firstTake) return null;
                firstTake = false;

                ArrayList<MatchedItem> matchedItems = new ArrayList<>();
                matchedItems.add(new MatchedRange(target, targetId, 0, new Range(from, to)));
                return new Match(1, 0, matchedItems);
            }
        }
    }
}
