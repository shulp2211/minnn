package com.milaboratory.mist.pattern;

import com.milaboratory.core.Range;
import com.milaboratory.core.sequence.NSequenceWithQuality;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;

import java.util.ArrayList;

import static org.junit.Assert.*;

public class MatchTest {
    @Rule
    public final ExpectedException exception = ExpectedException.none();

    @Test
    public void matchTest() throws Exception {
        NSequenceWithQuality seq0 = new NSequenceWithQuality("AATTAAGGCAAA");
        NSequenceWithQuality seq1 = new NSequenceWithQuality("ATTAGACA");

        ArrayList<MatchedItem> testMatchedItems1 = new ArrayList<MatchedItem>() {{
            add(new MatchedRange(seq0, (byte)1, 0, new Range(0, 9)));
            add(new MatchedGroupEdge(seq0, (byte)1, 0, new GroupEdge("0", true), 1));
            add(new MatchedGroupEdge(seq0, (byte)1, 0, new GroupEdge("0", false), 4));
        }};

        ArrayList<MatchedItem> testMatchedItems2 = new ArrayList<MatchedItem>() {{
            add(new MatchedRange(seq0, (byte)1, 0, new Range(0, 9)));
            add(new MatchedGroupEdge(seq0, (byte)1, 0, new GroupEdge("0", true), 1));
            add(new MatchedGroupEdge(seq0, (byte)1, 0, new GroupEdge("0", false), 4));
            add(new MatchedGroupEdge(seq1, (byte)1, 1, new GroupEdge("1", true), 4));
            add(new MatchedGroupEdge(seq1, (byte)1, 1, new GroupEdge("1", false), 8));
            add(new MatchedRange(seq1, (byte)1, 1, new Range(0, 8)));
            add(new MatchedGroupEdge(seq1, (byte)1, 1, new GroupEdge("2", true), 0));
            add(new MatchedGroupEdge(seq1, (byte)1, 1, new GroupEdge("2", false), 4));
            add(new MatchedGroupEdge(seq1, (byte)1, 1, new GroupEdge("3", true), 5));
            add(new MatchedGroupEdge(seq1, (byte)1, 1, new GroupEdge("3", false), 8));
        }};

        Match testMatch1 = new Match(1, 5, testMatchedItems1);
        Match testMatch2 = new Match(2, 10, testMatchedItems2);

        assertEquals(1, testMatch1.getNumberOfPatterns());
        assertEquals(2, testMatch2.getNumberOfPatterns());
        assertEquals(5, testMatch1.getScore(), 0.0001);
        assertEquals(10, testMatch2.getScore(), 0.0001);
        assertEquals(new Range(0, 9), testMatch1.getMatchedRange(0).getRange());
        assertEquals(new Range(0, 9), testMatch2.getMatchedRange(0).getRange());
        assertEquals(new Range(0, 9), testMatch1.getMatchedRange().getRange());
        assertEquals(new Range(0, 9), testMatch1.getRange());
        assertEquals(new Range(0, 8), testMatch2.getMatchedRange(1).getRange());
        assertEquals("AATTAAGGC", testMatch1.getMatchedRange(0).getValue().getSequence().toString());
        assertEquals("AATTAAGGC", testMatch2.getMatchedRange(0).getValue().getSequence().toString());
        assertEquals("AATTAAGGC", testMatch1.getMatchedRange().getValue().getSequence().toString());
        assertEquals("AATTAAGGC", testMatch1.getValue().getSequence().toString());
        assertEquals("ATTAGACA", testMatch2.getMatchedRange(1).getValue().getSequence().toString());
        assertEquals(2, testMatch1.getMatchedGroupEdges().size());
        assertEquals(8, testMatch2.getMatchedGroupEdges().size());
        assertEquals(MatchedGroupEdge.class, testMatch1.getMatchedGroupEdges().get(1).getClass());
        assertEquals(MatchedGroupEdge.class, testMatch2.getMatchedGroupEdges().get(7).getClass());
        assertTrue(MatchedItem.class.isAssignableFrom(testMatch1.getMatchedItems().get(2).getClass()));
        assertTrue(MatchedItem.class.isAssignableFrom(testMatch2.getMatchedItems().get(9).getClass()));
        assertTrue(testMatch1.getMatchedGroupEdge("0", true).isStart());
        assertFalse(testMatch1.getMatchedGroupEdge("0", false).isStart());
        assertTrue(testMatch2.getMatchedGroupEdge("1", true).isStart());
        assertFalse(testMatch2.getMatchedGroupEdge("1", false).isStart());
        assertEquals("2", testMatch2.getMatchedGroupEdge("2", true).getGroupName());
        assertEquals("2", testMatch2.getMatchedGroupEdge("2", false).getGroupName());
        assertEquals(5, testMatch2.getMatchedGroupEdge("3", true).getPosition());
        assertEquals(8, testMatch2.getMatchedGroupEdge("3", false).getPosition());
        assertEquals(seq0, testMatch1.getMatchedRange().getTarget());
        assertEquals(seq1, testMatch2.getMatchedRange(1).getTarget());
        assertEquals(1, testMatch1.getMatchedRange().getTargetId());
        assertEquals(1, testMatch2.getMatchedRange(1).getTargetId());
        assertEquals(0, testMatch1.getMatchedGroupEdge("0", true).getPatternIndex());
        assertEquals(0, testMatch1.getMatchedGroupEdge("0", false).getPatternIndex());
        assertEquals(0, testMatch2.getMatchedGroupEdge("0", true).getPatternIndex());
        assertEquals(1, testMatch2.getMatchedGroupEdge("1", true).getPatternIndex());
        assertEquals(1, testMatch2.getMatchedGroupEdge("1", false).getPatternIndex());
        assertEquals(1, testMatch2.getMatchedGroupEdge("2", true).getPatternIndex());
        assertEquals(1, testMatch2.getMatchedGroupEdge("3", true).getPatternIndex());
        assertEquals(2, testMatch1.getMatchedGroupEdgesByPattern(0).size());
        assertEquals(6, testMatch2.getMatchedGroupEdgesByPattern(1).size());
        assertEquals("0", testMatch2.getMatchedGroupEdgesByPattern(0).get(0).getGroupName());

        exception.expect(IllegalStateException.class);
        testMatch2.getMatchedRange();
    }
}
