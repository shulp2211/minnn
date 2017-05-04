package com.milaboratory.mist.pattern;

/**
 * When combination of operand matches considered valid:
 * "LOGICAL_OR" used in Or operator: all match combinations considered valid except all nulls combination
 * "LOGICAL_AND" used in AndOperator and for combining MultiPattern: valid combinations doesn't contain null matches
 * "INTERSECTION" used in low-level And: match ranges must not intersect
 * "ORDER" used in low-level Plus: match ranges must be in order from left to right
 */
public enum MatchValidationType {
    LOGICAL_OR,
    LOGICAL_AND,
    INTERSECTION,
    ORDER
}
