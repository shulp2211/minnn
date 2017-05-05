package com.milaboratory.mist.util;

import com.milaboratory.core.Range;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;

import java.util.HashMap;

import static com.milaboratory.mist.util.RangeTools.checkRangesIntersection;
import static com.milaboratory.mist.util.RangeTools.combine2Ranges;
import static com.milaboratory.mist.util.RangeTools.combineRanges;
import static org.junit.Assert.*;

public class RangeToolsTest {
    @Rule
    public final ExpectedException exception = ExpectedException.none();

    @Test
    public void checkRangesIntersectionTest() throws Exception {
        Range[] ranges1 = {new Range(1, 3), new Range(3, 5)};
        Range[] ranges2 = {new Range(2, 4), new Range(3, 5)};
        Range[] ranges3 = {new Range(10, 12), new Range(16, 18), new Range(20, 30), new Range(5, 8)};
        Range[] ranges4 = {new Range(6, 11), new Range(13, 15), new Range(9, 11)};
        Range[] ranges5 = {};
        Range[] ranges6 = {new Range(0, 1)};
        Range[] ranges7 = {new Range(0, 3), new Range(1, 4), new Range(2, 5), new Range(3, 6)};
        assertFalse(checkRangesIntersection(ranges1));
        assertTrue(checkRangesIntersection(ranges2));
        assertFalse(checkRangesIntersection(ranges3));
        assertTrue(checkRangesIntersection(ranges4));
        assertFalse(checkRangesIntersection(ranges5));
        assertFalse(checkRangesIntersection(ranges6));
        assertTrue(checkRangesIntersection(0, ranges2));
        assertFalse(checkRangesIntersection(1, ranges2));
        assertTrue(checkRangesIntersection(1, ranges4));
        assertFalse(checkRangesIntersection(2, ranges4));
        assertFalse(checkRangesIntersection(3, ranges4));
        assertTrue(checkRangesIntersection(7, ranges7));
        assertFalse(checkRangesIntersection(8, ranges7));
    }

    @Test
    public void combine2RangesTest() throws Exception {
        assertEquals(new Range(2, 13), combine2Ranges(new Range(2, 6), new Range(9, 13)));
        assertEquals(new Range(1, 3), combine2Ranges(new Range(2, 3), new Range(1, 2)));
        assertEquals(new Range(4, 11), combine2Ranges(new Range(8, 11), new Range(4, 10)));
    }

    @Test
    public void combineRangesTest() throws Exception {
        assertEquals(new Range(2, 13), combineRanges(new Range(2, 13)));
        assertEquals(new Range(3, 10), combineRanges(new Range(5, 10), new Range(8, 10), new Range(3, 6)));
        assertEquals(new Range(0, 20), combineRanges(new Range(0, 2), new Range(17, 20), new Range(10, 14), new Range(6, 11)));
        assertEquals(new HashMap.SimpleEntry<>(new Range(0, 10), -5.0f), combineRanges(-1.0f,
                new Range(0, 5), new Range(3, 6), new Range(4, 10)));
        exception.expect(IllegalArgumentException.class);
        combineRanges();
    }
}
