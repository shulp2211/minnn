package com.milaboratory.mist.pattern;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;

abstract class MultiplePatternsOperator extends SinglePattern {
    protected final SinglePattern[] operandPatterns;
    protected final ArrayList<GroupEdge> groupEdges;

    MultiplePatternsOperator(long scoreThreshold, SinglePattern... operandPatterns) {
        this(scoreThreshold, true, operandPatterns);
    }

    /**
     * Common constructor for multiple patterns operator.
     *
     * @param scoreThreshold all matches with score lower than this threshold will be ignored
     * @param checkGroupEdges true if check that operands contain equal group edges must be performed
     * @param operandPatterns patterns that come as operands for the operator
     */
    MultiplePatternsOperator(long scoreThreshold, boolean checkGroupEdges, SinglePattern... operandPatterns) {
        super(scoreThreshold);
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

    @Override
    void setTargetId(byte targetId) {
        super.setTargetId(targetId);
        Arrays.stream(operandPatterns).forEach(sp -> sp.setTargetId(targetId));
    }
}
