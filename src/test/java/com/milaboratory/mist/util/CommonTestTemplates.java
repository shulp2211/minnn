package com.milaboratory.mist.util;

import cc.redberry.pipe.OutputPort;
import com.milaboratory.core.Range;
import com.milaboratory.core.sequence.NSequenceWithQuality;
import com.milaboratory.mist.pattern.CaptureGroupMatch;
import com.milaboratory.mist.pattern.Match;
import com.milaboratory.mist.pattern.MatchValidationType;

import java.util.HashMap;
import java.util.Map;

import static com.milaboratory.mist.pattern.Match.WHOLE_PATTERN_MATCH_GROUP_NAME_PREFIX;
import static com.milaboratory.mist.pattern.Match.COMMON_GROUP_NAME_PREFIX;
import static org.junit.Assert.*;

class CommonTestTemplates {
    static void predefinedMatchesApproximateSorterTest(boolean sortByScore, boolean fairSorting) throws Exception {
        NSequenceWithQuality seqSingle = new NSequenceWithQuality("AATTAAGGCAAA");
        NSequenceWithQuality seqMulti1 = new NSequenceWithQuality("AATTAAGGCAAA");
        NSequenceWithQuality seqMulti2 = new NSequenceWithQuality("ATTAGACA");

        Map<String, CaptureGroupMatch> testGroupsSingle1 = new HashMap<String, CaptureGroupMatch>() {{
            put(WHOLE_PATTERN_MATCH_GROUP_NAME_PREFIX + "0", new CaptureGroupMatch(seqSingle, (byte)1, new Range(6, 9)));
            put(COMMON_GROUP_NAME_PREFIX + "0", new CaptureGroupMatch(seqSingle, (byte)1, new Range(6, 7)));
            put(COMMON_GROUP_NAME_PREFIX + "1", new CaptureGroupMatch(seqSingle, (byte)1, new Range(7, 9)));
        }};

        Map<String, CaptureGroupMatch> testGroupsSingle2 = new HashMap<String, CaptureGroupMatch>() {{
            put(WHOLE_PATTERN_MATCH_GROUP_NAME_PREFIX + "0", new CaptureGroupMatch(seqSingle, (byte)1, new Range(0, 8)));
            put(COMMON_GROUP_NAME_PREFIX + "2", new CaptureGroupMatch(seqSingle, (byte)1, new Range(0, 4)));
            put(COMMON_GROUP_NAME_PREFIX + "3", new CaptureGroupMatch(seqSingle, (byte)1, new Range(5, 8)));
        }};

        Map<String, CaptureGroupMatch> testGroupsSingle3 = new HashMap<String, CaptureGroupMatch>() {{
            put(WHOLE_PATTERN_MATCH_GROUP_NAME_PREFIX + "0", new CaptureGroupMatch(seqSingle, (byte)1, new Range(3, 5)));
        }};

        Map<String, CaptureGroupMatch> testGroupsSingle4 = new HashMap<String, CaptureGroupMatch>() {{
            put(WHOLE_PATTERN_MATCH_GROUP_NAME_PREFIX + "0", new CaptureGroupMatch(seqSingle, (byte)1, new Range(0, 2)));
        }};

        Map<String, CaptureGroupMatch> testGroupsMulti1 = new HashMap<String, CaptureGroupMatch>() {{
            put(WHOLE_PATTERN_MATCH_GROUP_NAME_PREFIX + "0", new CaptureGroupMatch(seqMulti1, (byte)1, new Range(0, 9)));
            put(COMMON_GROUP_NAME_PREFIX + "0", new CaptureGroupMatch(seqMulti1, (byte)1, new Range(1, 4)));
            put(COMMON_GROUP_NAME_PREFIX + "1", new CaptureGroupMatch(seqMulti1, (byte)1, new Range(4, 8)));
            put(WHOLE_PATTERN_MATCH_GROUP_NAME_PREFIX + "1", new CaptureGroupMatch(seqMulti2, (byte)1, new Range(0, 8)));
            put(COMMON_GROUP_NAME_PREFIX + "2", new CaptureGroupMatch(seqMulti2, (byte)1, new Range(0, 4)));
            put(COMMON_GROUP_NAME_PREFIX + "3", new CaptureGroupMatch(seqMulti2, (byte)1, new Range(5, 8)));
        }};

        Map<String, CaptureGroupMatch> testGroupsMulti2 = new HashMap<String, CaptureGroupMatch>() {{
            put(WHOLE_PATTERN_MATCH_GROUP_NAME_PREFIX + "0", new CaptureGroupMatch(seqMulti1, (byte)1, new Range(1, 5)));
            put(WHOLE_PATTERN_MATCH_GROUP_NAME_PREFIX + "1", new CaptureGroupMatch(seqMulti2, (byte)1, new Range(2, 4)));
        }};

        Map<String, CaptureGroupMatch> testGroupsMulti3 = new HashMap<String, CaptureGroupMatch>() {{
            put(WHOLE_PATTERN_MATCH_GROUP_NAME_PREFIX + "0", new CaptureGroupMatch(seqMulti1, (byte)1, new Range(4, 6)));
            put(COMMON_GROUP_NAME_PREFIX + "0", new CaptureGroupMatch(seqMulti1, (byte)1, new Range(4, 6)));
            put(WHOLE_PATTERN_MATCH_GROUP_NAME_PREFIX + "1", new CaptureGroupMatch(seqMulti2, (byte)1, new Range(1, 8)));
            put(COMMON_GROUP_NAME_PREFIX + "2", new CaptureGroupMatch(seqMulti2, (byte)1, new Range(1, 4)));
            put(COMMON_GROUP_NAME_PREFIX + "3", new CaptureGroupMatch(seqMulti2, (byte)1, new Range(5, 8)));
        }};

        Match testMatchSingle1 = new Match(1, 10, testGroupsSingle1);
        Match testMatchSingle2 = new Match(1, -3, testGroupsSingle2);
        Match testMatchSingle3 = new Match(1, -3, testGroupsSingle3);
        Match testMatchSingle4 = new Match(1, -4.5f, testGroupsSingle4);
        Match testMatchMulti1 = new Match(2, 4.0f, testGroupsMulti1);
        Match testMatchMulti2 = new Match(2, 3.9f, testGroupsMulti2);
        Match testMatchMulti3 = new Match(2, 4.0f, testGroupsMulti3);

        ApproximateSorter sorterSingle1, sorterSingle2, sorterSingle3, sorterSingle4, sorterSingle5;
        ApproximateSorter sorterMulti1, sorterMulti2, sorterMulti3;
        if (sortByScore) {
            sorterSingle1 = new SorterByScore(false, false, true,
                    fairSorting, MatchValidationType.INTERSECTION);
            sorterSingle2 = new SorterByScore(false, false, false,
                    fairSorting, MatchValidationType.INTERSECTION);
            sorterSingle3 = new SorterByScore(false, false, true,
                    fairSorting, MatchValidationType.ORDER);
            sorterSingle4 = new SorterByScore(false, false, false,
                    fairSorting, MatchValidationType.ORDER);
            sorterSingle5 = new SorterByScore(false, false, true,
                    fairSorting, MatchValidationType.ALWAYS);
            sorterMulti1 = new SorterByScore(true, true, true,
                    fairSorting, MatchValidationType.ALWAYS);
            sorterMulti2 = new SorterByScore(true, true, false,
                    fairSorting, MatchValidationType.ALWAYS);
            sorterMulti3 = new SorterByScore(true, false, true,
                    fairSorting, MatchValidationType.ALWAYS);
        } else {
            sorterSingle1 = new SorterByCoordinate(false, false, true,
                    fairSorting, MatchValidationType.INTERSECTION);
            sorterSingle2 = new SorterByCoordinate(false, false, false,
                    fairSorting, MatchValidationType.INTERSECTION);
            sorterSingle3 = new SorterByCoordinate(false, false, true,
                    fairSorting, MatchValidationType.ORDER);
            sorterSingle4 = new SorterByCoordinate(false, false, false,
                    fairSorting, MatchValidationType.ORDER);
            sorterSingle5 = new SorterByCoordinate(false, false, true,
                    fairSorting, MatchValidationType.ALWAYS);
            sorterMulti1 = new SorterByCoordinate(true, true, true,
                    fairSorting, MatchValidationType.ALWAYS);
            sorterMulti2 = new SorterByCoordinate(true, true, false,
                    fairSorting, MatchValidationType.ALWAYS);
            sorterMulti3 = new SorterByCoordinate(true, false, true,
                    fairSorting, MatchValidationType.ALWAYS);
        }

        TestMatchesOutputPort testPortSingle1 = new TestMatchesOutputPort(testMatchSingle1, testMatchSingle2, testMatchSingle2);
        TestMatchesOutputPort testPortSingle2 = new TestMatchesOutputPort(testMatchSingle3, testMatchSingle4, testMatchSingle3);
        TestMatchesOutputPort testPortSingle3;
        if (sortByScore)
            testPortSingle3 = new TestMatchesOutputPort(testMatchSingle1, testMatchSingle2, testMatchSingle3, testMatchSingle4);
        else
            testPortSingle3 = new TestMatchesOutputPort(testMatchSingle4, testMatchSingle2, testMatchSingle3, testMatchSingle1);
        TestMatchesOutputPort testPortSingle4 = new TestMatchesOutputPort(testMatchSingle4);
        TestMatchesOutputPort testPortMulti1 = new TestMatchesOutputPort(testMatchMulti1);
        TestMatchesOutputPort testPortMulti2 = new TestMatchesOutputPort(testMatchMulti2, testMatchMulti3, testMatchMulti2);
        TestMatchesOutputPort testPortMulti3 = new TestMatchesOutputPort(testMatchMulti3, testMatchMulti2, testMatchMulti1);
        TestMatchesOutputPort testPortEmpty = new TestMatchesOutputPort();

        assertEquals(3, countPortValues(sorterSingle1.getOutputPort(new TestMatchesOutputPort[] {
                testPortSingle1.getCopy(), testPortSingle2.getCopy()})));
        assertEquals(3, countPortValues(sorterSingle1.getOutputPort(new TestMatchesOutputPort[] {
                testPortSingle1.getCopy(), testPortSingle2.getCopy()})));
        assertEquals(3, countPortValues(sorterSingle1.getOutputPort(new TestMatchesOutputPort[] {
                testPortSingle1.getCopy(), testPortSingle2.getCopy(), testPortSingle3.getCopy()})));
        assertEquals(3, countPortValues(sorterSingle2.getOutputPort(new TestMatchesOutputPort[] {
                testPortSingle1.getCopy(), testPortSingle2.getCopy()})));
        assertEquals(0, countPortValues(sorterSingle3.getOutputPort(new TestMatchesOutputPort[] {
                testPortSingle1.getCopy(), testPortSingle2.getCopy()})));
        assertEquals(1, countPortValues(sorterSingle4.getOutputPort(new TestMatchesOutputPort[] {
                testPortSingle4.getCopy(), testPortSingle1.getCopy()})));
        assertEquals(1, countPortValues(sorterSingle5.getOutputPort(new TestMatchesOutputPort[] {
                testPortSingle4.getCopy(), testPortSingle4.getCopy()})));
        assertEquals(0, countPortValues(sorterSingle5.getOutputPort(new TestMatchesOutputPort[] {
                testPortSingle1.getCopy(), testPortEmpty, testPortSingle4.getCopy()})));
        assertEquals(3, countPortValues(sorterMulti1.getOutputPort(new TestMatchesOutputPort[] {
                testPortMulti1.getCopy(), testPortEmpty, testPortMulti2.getCopy()})));
        assertEquals(3, countPortValues(sorterMulti1.getOutputPort(new TestMatchesOutputPort[] {
                testPortMulti1.getCopy(), testPortEmpty, testPortMulti2.getCopy()})));
        assertEquals(3, countPortValues(sorterMulti2.getOutputPort(new TestMatchesOutputPort[] {
                testPortMulti1.getCopy(), testPortEmpty, testPortMulti2.getCopy()})));
        assertEquals(0, countPortValues(sorterMulti3.getOutputPort(new TestMatchesOutputPort[] {
                testPortMulti1.getCopy(), testPortEmpty, testPortMulti2.getCopy()})));
        assertEquals(3, countPortValues(sorterMulti3.getOutputPort(new TestMatchesOutputPort[] {
                testPortMulti1.getCopy(), testPortMulti2.getCopy()})));
        assertEquals(9, countPortValues(sorterMulti3.getOutputPort(new TestMatchesOutputPort[] {
                testPortMulti1.getCopy(), testPortMulti2.getCopy(), testPortMulti3.getCopy()})));

        if (sortByScore) {
            assertEquals(7, sorterSingle1.getOutputPort(new TestMatchesOutputPort[]{
                    testPortSingle1.getCopy(), testPortSingle2.getCopy()})
                    .take().getScore(), 0.0001);
            if (fairSorting)
                assertEquals(8, sorterMulti1.getOutputPort(new TestMatchesOutputPort[]{
                        testPortMulti1.getCopy(), testPortEmpty, testPortMulti2.getCopy()})
                        .take().getScore(), 0.0001);
            else
                assertEquals(7.9, sorterMulti1.getOutputPort(new TestMatchesOutputPort[]{
                        testPortMulti1.getCopy(), testPortEmpty, testPortMulti2.getCopy()})
                        .take().getScore(), 0.0001);
            assertEquals(7, sorterSingle1.getOutputPort(new TestMatchesOutputPort[]{
                    testPortSingle1.getCopy(), testPortSingle3.getCopy()})
                    .take().getScore(), 0.0001);
            assertEquals(10, sorterSingle2.getOutputPort(new TestMatchesOutputPort[]{
                    testPortSingle1.getCopy(), testPortSingle3.getCopy()})
                    .take().getScore(), 0.0001);

            OutputPort<Match> testPort1 = sorterSingle1.getOutputPort(new TestMatchesOutputPort[]{
                    testPortSingle1.getCopy(), testPortSingle3.getCopy()});
            assertNotNull(testPort1.take());
            assertEquals(5.5, testPort1.take().getScore(), 0.0001);
            assertNull(testPort1.take());
        } else {
            if (fairSorting)
                assertEquals(new Range(0, 9), sorterSingle1.getOutputPort(new TestMatchesOutputPort[]{
                        testPortSingle1.getCopy(), testPortSingle2.getCopy()})
                        .take().getWholePatternMatch().getRange());
            else
                assertEquals(new Range(3, 9), sorterSingle1.getOutputPort(new TestMatchesOutputPort[]{
                    testPortSingle1.getCopy(), testPortSingle2.getCopy()})
                    .take().getWholePatternMatch().getRange());
            assertEquals(new Range(0, 8), sorterMulti1.getOutputPort(new TestMatchesOutputPort[]{
                        testPortMulti1.getCopy(), testPortEmpty, testPortMulti2.getCopy()})
                        .take().getWholePatternMatch(1).getRange());
            assertEquals(new Range(0, 9), sorterSingle1.getOutputPort(new TestMatchesOutputPort[]{
                    testPortSingle1.getCopy(), testPortSingle3.getCopy()})
                    .take().getWholePatternMatch().getRange());
            assertEquals(new Range(0, 9), sorterSingle3.getOutputPort(new TestMatchesOutputPort[]{
                    testPortSingle3.getCopy(), testPortSingle1.getCopy()})
                    .take().getWholePatternMatch().getRange());

            OutputPort<Match> testPort1 = sorterSingle1.getOutputPort(new TestMatchesOutputPort[]{
                    testPortSingle1.getCopy(), testPortSingle3.getCopy()});
            assertNotNull(testPort1.take());
            assertEquals(new Range(3, 9), testPort1.take().getWholePatternMatch().getRange());
            assertNull(testPort1.take());
        }

        OutputPort<Match> testPort2 = sorterSingle1.getOutputPort(new TestMatchesOutputPort[] {
                testPortSingle3.getCopy(), testPortSingle3.getCopy()});
        System.out.println();
        for (int i = 0; i < 6; i++) {
            Match currentMatch = testPort2.take();
            System.out.println(currentMatch.getScore() + ", " + currentMatch.getWholePatternMatch().getRange());
        }
        System.out.println();
        assertNull(testPort2.take());
    }

    static int countPortValues(OutputPort<Match> port) {
        int counter = 0;
        while (port.take() != null)
            counter++;
        return counter;
    }
}
