package com.milaboratory.mist.util;

import com.milaboratory.mist.pattern.*;

import java.util.HashMap;

public final class UnfairSorterConfiguration {
    public static final HashMap<Class, Integer> unfairSorterPortLimits = new HashMap<>();
    static {
        unfairSorterPortLimits.put(FuzzyMatchPattern.class, 500);
        unfairSorterPortLimits.put(RepeatPattern.class, 500);
        unfairSorterPortLimits.put(AnyPattern.class, 1);
        unfairSorterPortLimits.put(FilterPattern.class, 50);
        unfairSorterPortLimits.put(AndPattern.class, 20);
        unfairSorterPortLimits.put(PlusPattern.class, 20);
        unfairSorterPortLimits.put(SequencePattern.class, 20);
        unfairSorterPortLimits.put(OrPattern.class, 50);
        unfairSorterPortLimits.put(MultiPattern.class, 25);
        unfairSorterPortLimits.put(AndOperator.class, 25);
        unfairSorterPortLimits.put(OrOperator.class, 25);
        unfairSorterPortLimits.put(NotOperator.class, 1);
        unfairSorterPortLimits.put(MultipleReadsFilterPattern.class, 25);
    }
}
