package com.milaboratory.mist.pattern;

/**
 * When combination of operand matches considered valid:
 * "ALWAYS" used in upper level and for combining MultiPattern: all match combinations considered valid
 * "INTERSECTION" used in low-level And: match ranges must not intersect
 * "ORDER" used in low-level Plus: match ranges must be in order from left to right
 */
public enum MatchValidationType {
    ALWAYS,
    INTERSECTION,
    ORDER
}
