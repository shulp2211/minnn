package com.milaboratory.mist.pattern;

import com.milaboratory.core.Range;
import com.milaboratory.core.sequence.MultiNSequenceWithQuality;

import java.util.*;
import java.util.stream.IntStream;

abstract class MultipleReadsOperator extends Pattern {
    protected final MultipleReadsOperator[] operandPatterns;
    protected final SinglePattern[] singlePatterns;
    protected final ArrayList<GroupEdge> groupEdges;

    MultipleReadsOperator(MultipleReadsOperator... operandPatterns) {
        this.operandPatterns = operandPatterns;
        this.singlePatterns = new SinglePattern[0];
        this.groupEdges = new ArrayList<>();
        getGroupEdgesFromOperands(operandPatterns);
    }

    MultipleReadsOperator(SinglePattern... singlePatterns) {
        this.singlePatterns = singlePatterns;
        this.operandPatterns = new MultipleReadsOperator[0];
        this.groupEdges = new ArrayList<>();
        getGroupEdgesFromOperands(singlePatterns);
    }

    @Override
    public MatchingResult match(MultiNSequenceWithQuality target) {
        // if ranges array not provided, match in the whole sequences
        return this.match(target, IntStream.range(0, target.numberOfSequences())
                .mapToObj(i -> new Range(0, target.get(i).getSequence().size())).toArray(Range[]::new));
    }

    public MatchingResult match(MultiNSequenceWithQuality target, boolean... reverseComplements) {
        if (target.numberOfSequences() != reverseComplements.length)
            throw new IllegalArgumentException("Mismatched number of reads (" + target.numberOfSequences()
                    + ") and reverse complement flags (" + reverseComplements.length + ")!");
        // for reverse complement reads automatically inverse generated ranges
        return this.match(target, IntStream.range(0, target.numberOfSequences())
                .mapToObj(i -> new Range(0, target.get(i).getSequence().size(),
                        reverseComplements[i])).toArray(Range[]::new), reverseComplements);
    }

    public MatchingResult match(MultiNSequenceWithQuality target, Range... ranges) {
        if (target.numberOfSequences() != ranges.length)
            throw new IllegalArgumentException("Mismatched number of reads (" + target.numberOfSequences()
                    + ") and ranges (" + ranges.length + ")!");
        // if reverseComplements array not provided, match without reverse complements only
        boolean[] reverseComplements = new boolean[ranges.length];
        for (int i = 0; i < ranges.length; i++)
            reverseComplements[i] = ranges[i].isReverse();
        return this.match(target, ranges, reverseComplements);
    }

    /**
     * Match a group of patterns in a group of reads.
     *
     * @param target multiple sequences that come from multiple reads
     * @param ranges ranges for target reads
     * @param reverseComplements false if non-reversed match, true if reversed complement;
     *                           one array element for one read in target
     * @return matching result
     */
    public abstract MatchingResult match(MultiNSequenceWithQuality target, Range[] ranges, boolean[] reverseComplements);

    @Override
    public ArrayList<GroupEdge> getGroupEdges() {
        return groupEdges;
    }

    private <T extends Pattern> void getGroupEdgesFromOperands(T[] patterns) {
        for (T pattern : patterns)
            groupEdges.addAll(pattern.getGroupEdges());
        if (groupEdges.size() != new HashSet<>(groupEdges).size())
            throw new IllegalStateException("Operands contain equal group edges!");
    }
}
