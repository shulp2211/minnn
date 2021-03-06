/*
 * Copyright (c) 2016-2020, MiLaboratory LLC
 * All Rights Reserved
 *
 * Permission to use, copy, modify and distribute any part of this program for
 * educational, research and non-profit purposes, by non-profit institutions
 * only, without fee, and without a written agreement is hereby granted,
 * provided that the above copyright notice, this paragraph and the following
 * three paragraphs appear in all copies.
 *
 * Those desiring to incorporate this work into commercial products or use for
 * commercial purposes should contact MiLaboratory LLC, which owns exclusive
 * rights for distribution of this program for commercial purposes, using the
 * following email address: licensing@milaboratory.com.
 *
 * IN NO EVENT SHALL THE INVENTORS BE LIABLE TO ANY PARTY FOR DIRECT, INDIRECT,
 * SPECIAL, INCIDENTAL, OR CONSEQUENTIAL DAMAGES, INCLUDING LOST PROFITS,
 * ARISING OUT OF THE USE OF THIS SOFTWARE, EVEN IF THE INVENTORS HAS BEEN
 * ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 *
 * THE SOFTWARE PROVIDED HEREIN IS ON AN "AS IS" BASIS, AND THE INVENTORS HAS
 * NO OBLIGATION TO PROVIDE MAINTENANCE, SUPPORT, UPDATES, ENHANCEMENTS, OR
 * MODIFICATIONS. THE INVENTORS MAKES NO REPRESENTATIONS AND EXTENDS NO
 * WARRANTIES OF ANY KIND, EITHER IMPLIED OR EXPRESS, INCLUDING, BUT NOT
 * LIMITED TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY OR FITNESS FOR A
 * PARTICULAR PURPOSE, OR THAT THE USE OF THE SOFTWARE WILL NOT INFRINGE ANY
 * PATENT, TRADEMARK OR OTHER RIGHTS.
 */
package com.milaboratory.minnn.outputconverter;

import com.milaboratory.core.io.sequence.*;
import com.milaboratory.core.sequence.NSequenceWithQuality;
import com.milaboratory.minnn.pattern.*;
import org.junit.*;

import java.util.*;

import static com.milaboratory.minnn.util.CommonTestUtils.*;
import static org.junit.Assert.*;

public class ParsedReadTest {
    @Before
    public void setUp() {
        ParsedRead.clearStaticCache();
    }

    private NSequenceWithQuality[] testReadValues = new NSequenceWithQuality[] {
            new NSequenceWithQuality("ATT"), new NSequenceWithQuality("TA"), new NSequenceWithQuality("GACA")
    };
    private long testReadId = 4328602;
    private SingleRead[] testReads = new SingleRead[] {
            new SingleReadImpl(testReadId, testReadValues[0], "0"),
            new SingleReadImpl(testReadId, testReadValues[1], "1"),
            new SingleReadImpl(testReadId, testReadValues[2], "2")
    };
    private MultiRead testMultiRead = new MultiRead(testReads);
    private ArrayList<MatchedGroupEdge> testMatchedGroupEdges = new ArrayList<MatchedGroupEdge>() {{
        add(new MatchedGroupEdge(testReadValues[0], (byte)1, new GroupEdge("R1", true), 0));
        add(new MatchedGroupEdge(testReadValues[0], (byte)1, new GroupEdge("R1", false), 3));
        add(new MatchedGroupEdge(testReadValues[1], (byte)2, new GroupEdge("R2", true), 0));
        add(new MatchedGroupEdge(testReadValues[1], (byte)2, new GroupEdge("R2", false), 2));
        add(new MatchedGroupEdge(testReadValues[2], (byte)3, new GroupEdge("R3", true), 0));
        add(new MatchedGroupEdge(testReadValues[2], (byte)3, new GroupEdge("R3", false), 4));
        add(new MatchedGroupEdge(testReadValues[0], (byte)1, new GroupEdge("G1-1", true), 1));
        add(new MatchedGroupEdge(testReadValues[0], (byte)1, new GroupEdge("G1-1", false), 2));
        add(new MatchedGroupEdge(testReadValues[0], (byte)1, new GroupEdge("G1-2", true), 0));
        add(new MatchedGroupEdge(testReadValues[0], (byte)1, new GroupEdge("G1-2", false), 2));
        add(new MatchedGroupEdge(testReadValues[2], (byte)3, new GroupEdge("G3-1", true), 1));
        add(new MatchedGroupEdge(testReadValues[2], (byte)3, new GroupEdge("G3-1", false), 4));
    }};
    private long testScore = -5;
    private Match testMatch = new Match(3, testScore, testMatchedGroupEdges);
    private ParsedRead testParsedRead = new ParsedRead(testMultiRead, false, -1,
            testMatch, 0);
    private ArrayList<GroupEdge> testGroupEdges = new ArrayList<GroupEdge>() {{
        for (int i = 1; i <= 6; i++) {
            add(new GroupEdge("G" + i, true));
            add(new GroupEdge("G" + i, false));
        }
    }};

    @Test
    public void retargetTest() {
        ParsedRead newParsedRead = testParsedRead.retarget("R1", "R2", "R3", "G3-1", "G1-2", "G1-1");
        assertEquals(testMultiRead, newParsedRead.getOriginalRead());
        assertFalse(newParsedRead.isReverseMatch());
        Match targetMatch = newParsedRead.getBestMatch();
        assertEquals(testScore, targetMatch.getScore());
        assertEquals(6, targetMatch.getNumberOfTargets());
        assertEquals(1, targetMatch.getMatchedGroupEdge("G3-1", true).getPosition());
        ArrayList<MatchedGroupEdge> targetGroupEdges = targetMatch.getMatchedGroupEdges();
        String[] expectedValues = new String[] { "R1", "R1", "G1-1", "G1-1", "G1-2", "G1-2", "R2", "R2",
                "R3", "R3", "G3-1", "G3-1", "G3-1", "G3-1", "G1-1", "G1-1", "G1-2", "G1-2", "G1-1", "G1-1" };
        int[] expectedPositions = new int[] {
                0, 3, 1, 2, 0, 2,       // R1
                0, 2,                   // R2
                0, 4, 1, 4,             // R3
                0, 3,                   // G3-1
                1, 2, 0, 2,             // G1-2
                0, 1                    // G1-1
        };
        for (int i = 0; i < expectedValues.length; i++) {
            assertEquals(expectedValues[i], targetGroupEdges.get(i).getGroupName());
            assertEquals(expectedPositions[i], targetGroupEdges.get(i).getPosition());
            assertEquals(i % 2 == 0, targetGroupEdges.get(i).getGroupEdge().isStart());
        }

        targetMatch = testParsedRead.retarget("G3-1", "R3").getBestMatch();
        assertEquals(2, targetMatch.getNumberOfTargets());
        assertEquals(0, targetMatch.getMatchedGroupEdge("G3-1", true).getPosition());
        targetGroupEdges = targetMatch.getMatchedGroupEdges();
        expectedValues = new String[] { "G3-1", "G3-1", "R3", "R3", "G3-1", "G3-1" };
        expectedPositions = new int[] {
                0, 3,                   // G3-1
                0, 4, 1, 4              // R3
        };
        for (int i = 0; i < expectedValues.length; i++) {
            assertEquals(expectedValues[i], targetGroupEdges.get(i).getGroupName());
            assertEquals(expectedPositions[i], targetGroupEdges.get(i).getPosition());
            assertEquals(i % 2 == 0, targetGroupEdges.get(i).getGroupEdge().isStart());
        }

        final Match finalTargetMatch = testParsedRead.retarget("G1-1").getBestMatch();
        assertEquals(1, finalTargetMatch.getNumberOfTargets());
        assertEquals(-1, finalTargetMatch.getMatchedGroupEdge("G3-1", true).getPosition());
        targetGroupEdges = finalTargetMatch.getMatchedGroupEdges();
        expectedValues = new String[] { "G1-1", "G1-1" };
        expectedPositions = new int[] { 0, 1 };
        for (int i = 0; i < expectedValues.length; i++) {
            assertEquals(expectedValues[i], targetGroupEdges.get(i).getGroupName());
            assertEquals(expectedPositions[i], targetGroupEdges.get(i).getPosition());
            assertEquals(i % 2 == 0, targetGroupEdges.get(i).getGroupEdge().isStart());
        }
    }

    @Test
    public void toSequenceReadTest() {
        SequenceRead sequenceRead = testParsedRead.toSequenceRead(true, testGroupEdges,
                "R1", "R2", "R3", "G3-1", "G1-2", "G1-1");
        assertEquals(6, sequenceRead.numberOfReads());
        assertEquals(testReadId, sequenceRead.getId());
        String[] expectedValues = new String[] { "ATT", "TA", "GACA", "ACA", "AT", "T" };
        for (int i = 0; i < expectedValues.length; i++)
            assertEquals(expectedValues[i], sequenceRead.getRead(i).getData().getSequence().toString());

        sequenceRead = testParsedRead.toSequenceRead(false, testGroupEdges,
                "G3-1", "R3");
        assertEquals(2, sequenceRead.numberOfReads());
        expectedValues = new String[] { "ACA", "GACA" };
        for (int i = 0; i < expectedValues.length; i++)
            assertEquals(expectedValues[i], sequenceRead.getRead(i).getData().getSequence().toString());

        sequenceRead = testParsedRead.toSequenceRead(false, testGroupEdges,
                "G1-1");
        assertEquals(1, sequenceRead.numberOfReads());
        assertEquals("T", sequenceRead.getRead(0).getData().getSequence().toString());
    }

    @Test
    public void fromSequenceReadTest() {

    }
}
