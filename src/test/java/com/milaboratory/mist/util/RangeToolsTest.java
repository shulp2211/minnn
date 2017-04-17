package com.milaboratory.mist.util;

import com.milaboratory.core.Range;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;

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
        Range[] ranges4 = {new Range(6, 11), new Range(13, 15), new Range(10, 11)};
        Range[] ranges5 = {};
        Range[] ranges6 = {new Range(0, 1)};
        assertEquals(false, checkRangesIntersection(ranges1));
        assertEquals(true, checkRangesIntersection(ranges2));
        assertEquals(false, checkRangesIntersection(ranges3));
        assertEquals(true, checkRangesIntersection(ranges4));
        assertEquals(false, checkRangesIntersection(ranges5));
        assertEquals(false, checkRangesIntersection(ranges6));
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
        exception.expect(IllegalStateException.class);
        combineRanges();
    }
}
