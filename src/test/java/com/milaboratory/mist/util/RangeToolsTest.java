package com.milaboratory.mist.util;

import com.milaboratory.core.Range;
import com.milaboratory.core.sequence.NSequenceWithQuality;
import com.milaboratory.core.sequence.NucleotideSequence;
import com.milaboratory.mist.pattern.GroupEdge;
import com.milaboratory.mist.pattern.MatchedGroupEdge;
import com.milaboratory.test.TestUtil;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;

import java.util.ArrayList;

import static com.milaboratory.mist.util.CommonTestUtils.getTestPatternAligner;
import static com.milaboratory.mist.util.RangeTools.*;
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
        NSequenceWithQuality testTarget =  new NSequenceWithQuality(TestUtil.randomSequence(NucleotideSequence.ALPHABET,
                20, 20).toString());
        ArrayList<ArrayList<MatchedGroupEdge>> testGroupEdgePositions = new ArrayList<>();
        for (int i = 0; i < 3; i++) {
            testGroupEdgePositions.add(new ArrayList<>());
            testGroupEdgePositions.get(i).add(new MatchedGroupEdge(testTarget, (byte)1, 0,
                    new GroupEdge("Test", true), 1));
        }
        CombinedRange combinedRange = combineRanges(getTestPatternAligner(Long.MIN_VALUE,
                0, 0, -1), testGroupEdgePositions, testTarget,
                new Range(0, 5), new Range(3, 6), new Range(4, 10));
        assertEquals(new Range(0, 10), combinedRange.getRange());
        assertEquals(-5, (int)combinedRange.getScorePenalty());
        assertEquals(1, combinedRange.getMatchedGroupEdges().get(0).getPosition());
        assertEquals(5, combinedRange.getMatchedGroupEdges().get(1).getPosition());
        assertEquals(6, combinedRange.getMatchedGroupEdges().get(2).getPosition());

        exception.expect(IllegalArgumentException.class);
        combineRanges();
    }
}
