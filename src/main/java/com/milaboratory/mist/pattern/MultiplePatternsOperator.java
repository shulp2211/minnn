package com.milaboratory.mist.pattern;

import java.util.ArrayList;
import java.util.HashSet;

public abstract class MultiplePatternsOperator extends SinglePattern {
    protected final int maxErrors;
    protected final float errorScorePenalty;
    protected final SinglePattern[] operandPatterns;
    protected final ArrayList<GroupEdge> groupEdges;

    MultiplePatternsOperator(boolean checkGroupEdges, SinglePattern... operandPatterns) {
        this(0, 0, checkGroupEdges, operandPatterns);
    }

    MultiplePatternsOperator(int maxErrors, float errorScorePenalty, SinglePattern... operandPatterns) {
        this(maxErrors, errorScorePenalty, true, operandPatterns);
    }

    /**
     * Common constructor for multiple patterns operator.
     *
     * @param maxErrors maximum enabled number of errors for combining ranges
     * @param errorScorePenalty score penalty for 1 intersected letter when combining ranges; negative value
     * @param checkGroupEdges true if check that operands contain equal group edges must be performed
     * @param operandPatterns patterns that come as operands for the operator
     */
    MultiplePatternsOperator(int maxErrors, float errorScorePenalty, boolean checkGroupEdges, SinglePattern... operandPatterns) {
        this.maxErrors = maxErrors;
        this.errorScorePenalty = errorScorePenalty;
        this.operandPatterns = operandPatterns;
        this.groupEdges = new ArrayList<>();
        for (SinglePattern pattern : operandPatterns)
            groupEdges.addAll(pattern.getGroupEdges());
        if (checkGroupEdges && (groupEdges.size() != new HashSet<>(groupEdges).size()))
            throw new IllegalStateException("Operands contain equal group edges!");
    }

    @Override
    public ArrayList<GroupEdge> getGroupEdges() {
        return groupEdges;
    }
}
