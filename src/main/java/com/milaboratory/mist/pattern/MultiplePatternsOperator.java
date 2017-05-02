package com.milaboratory.mist.pattern;

import java.util.ArrayList;
import java.util.HashSet;

public abstract class MultiplePatternsOperator extends SinglePattern {
    protected final SinglePattern[] operandPatterns;
    protected final ArrayList<GroupEdge> groupEdges;

    MultiplePatternsOperator(SinglePattern... operandPatterns) {
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
