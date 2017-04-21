package com.milaboratory.mist.util;

import cc.redberry.pipe.OutputPort;
import com.milaboratory.core.Range;
import com.milaboratory.core.sequence.NSequenceWithQuality;
import com.milaboratory.mist.pattern.CaptureGroupMatch;
import com.milaboratory.mist.pattern.Match;
import com.milaboratory.mist.pattern.MatchValidationType;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;

import java.util.*;

import static com.milaboratory.mist.pattern.Match.WHOLE_PATTERN_MATCH_GROUP_NAME_PREFIX;
import static com.milaboratory.mist.pattern.Match.COMMON_GROUP_NAME_PREFIX;
import static org.junit.Assert.*;

public class SorterByScoreTest {
    @Rule
    public final ExpectedException exception = ExpectedException.none();

    @Test
    public void simplePredefinedMatchesTest() throws Exception {
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
        Match testMatchSingle2 = new Match(1, 9.5f, testGroupsSingle2);
        Match testMatchSingle3 = new Match(1, -3, testGroupsSingle3);
        Match testMatchSingle4 = new Match(1, -3, testGroupsSingle4);
        Match testMatchMulti1 = new Match(2, 4.0f, testGroupsMulti1);
        Match testMatchMulti2 = new Match(2, 3.9f, testGroupsMulti2);
        Match testMatchMulti3 = new Match(2, 4.0f, testGroupsMulti3);

        SorterByScore sorterSingle1 = new SorterByScore(false, false, true,
                false, MatchValidationType.INTERSECTION);
        SorterByScore sorterSingle2 = new SorterByScore(false, false, false,
                false, MatchValidationType.INTERSECTION);
        SorterByScore sorterSingle3 = new SorterByScore(false, false, true,
                false, MatchValidationType.ORDER);
        SorterByScore sorterSingle4 = new SorterByScore(false, false, false,
                false, MatchValidationType.ORDER);
        SorterByScore sorterSingle5 = new SorterByScore(false, false, true,
                false, MatchValidationType.ALWAYS);
        SorterByScore sorterMulti1 = new SorterByScore(true, true, true,
                false, MatchValidationType.ALWAYS);
        SorterByScore sorterMulti2 = new SorterByScore(true, true, false,
                false, MatchValidationType.ALWAYS);
        SorterByScore sorterMulti3 = new SorterByScore(true, false, true,
                false, MatchValidationType.ALWAYS);

        TestMatchesOutputPort testPortSingle1 = new TestMatchesOutputPort(testMatchSingle1, testMatchSingle2, testMatchSingle2);
        TestMatchesOutputPort testPortSingle2 = new TestMatchesOutputPort(testMatchSingle3, testMatchSingle4, testMatchSingle3);
        TestMatchesOutputPort testPortSingle3 = new TestMatchesOutputPort(testMatchSingle1, testMatchSingle2, testMatchSingle3, testMatchSingle4);
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
    }

    @Test
    public void matchesWithMisplacedRangesTest() throws Exception {

    }

    @Test
    public void matchesWithNullValuesTest() throws Exception {

    }

    @Test
    public void matchesFromOperatorsTest() throws Exception {

    }

    @Test
    public void randomGeneratedMatchesTest() throws Exception {

    }

    @Test
    public void randomMatchesFromOperatorsTest() throws Exception {

    }

    @Test
    public void fairSortingSimpleTest() throws Exception {

    }

    @Test
    public void fairSortingRandomTest() throws Exception {

    }

    private int countPortValues(OutputPort<Match> port) {
        int counter = 0;
        while (port.take() != null)
            counter++;
        return counter;
    }
}
