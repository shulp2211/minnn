package com.milaboratory.mist.pattern;

/**
 * When combination of operand matches considered valid:
 * "ALWAYS" used in Or operator: all match combinations considered valid
 * "NOT_NULL" used in AndOperator and for combining MultiPattern: valid combinations doesn't contain null matches
 * "INTERSECTION" used in low-level And: match ranges must not intersect
 * "ORDER" used in low-level Plus: match ranges must be in order from left to right
 */
public enum MatchValidationType {
    ALWAYS,
    NOT_NULL,
    INTERSECTION,
    ORDER
}
