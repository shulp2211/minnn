package com.milaboratory.mist.pattern;

import java.util.ArrayList;
import java.util.HashSet;

public abstract class MultiplePatternsOperator extends SinglePattern {
    protected final int maxErrors;
    protected final float errorScorePenalty;
    protected final SinglePattern[] operandPatterns;
    protected final ArrayList<GroupEdge> groupEdges;

    /**
     * Common constructor for multiple patterns operator.
     *
     * @param maxErrors maximum enabled number of errors for combining ranges
     * @param errorScorePenalty score penalty for 1 intersected letter when combining ranges; negative value
     * @param operandPatterns patterns that come as operands for the operator
     */
    MultiplePatternsOperator(int maxErrors, float errorScorePenalty, SinglePattern... operandPatterns) {
        this.maxErrors = maxErrors;
        this.errorScorePenalty = errorScorePenalty;
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
}
