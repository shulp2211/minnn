package com.milaboratory.mist.pattern;

public interface CanFixBorders {
    /**
     * Set fixed left or right border for this pattern.
     *
     * @param left true for left border, false for right border
     * @param position coordinate for fixed border
     */
    void fixBorder(boolean left, int position);

    /**
     * Check whether this pattern already has fixed border.
     *
     * @param left true if we check left border, false for right border
     * @return true if border is fixed
     */
    boolean isBorderFixed(boolean left);

    default boolean isBorderFixed() {
        return isBorderFixed(true) || isBorderFixed(false);
    }
}
