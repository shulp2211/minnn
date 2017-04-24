package com.milaboratory.mist.util;

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
import static com.milaboratory.mist.util.CommonTestTemplates.countPortValues;
import static com.milaboratory.mist.util.CommonTestTemplates.predefinedMatchesApproximateSorterTest;
import static org.junit.Assert.*;

public class SorterByScoreTest {
    @Rule
    public final ExpectedException exception = ExpectedException.none();

    @Test
    public void simplePredefinedMatchesTest() throws Exception {
        predefinedMatchesApproximateSorterTest(true, false);
    }

    @Test
    public void matchesWithMisplacedRangesTest() throws Exception {
        NSequenceWithQuality seq = new NSequenceWithQuality("AATTAAGGCAAAGTAAATTGAGCA");
        Map<String, CaptureGroupMatch> testGroups1 = new HashMap<String, CaptureGroupMatch>() {{
            put(WHOLE_PATTERN_MATCH_GROUP_NAME_PREFIX + "0", new CaptureGroupMatch(seq, (byte)1, new Range(0, 3)));
        }};
        Map<String, CaptureGroupMatch> testGroups2 = new HashMap<String, CaptureGroupMatch>() {{
            put(WHOLE_PATTERN_MATCH_GROUP_NAME_PREFIX + "0", new CaptureGroupMatch(seq, (byte)1, new Range(3, 7)));
        }};
        Map<String, CaptureGroupMatch> testGroups3 = new HashMap<String, CaptureGroupMatch>() {{
            put(WHOLE_PATTERN_MATCH_GROUP_NAME_PREFIX + "0", new CaptureGroupMatch(seq, (byte)1, new Range(9, 12)));
        }};
        Map<String, CaptureGroupMatch> testGroups4 = new HashMap<String, CaptureGroupMatch>() {{
            put(WHOLE_PATTERN_MATCH_GROUP_NAME_PREFIX + "0", new CaptureGroupMatch(seq, (byte)1, new Range(11, 15)));
        }};

        Match testMatch1 = new Match(1, 10, testGroups1);
        Match testMatch2 = new Match(1, 10, testGroups2);
        Match testMatch3 = new Match(1, 10, testGroups3);
        Match testMatch4 = new Match(1, 10, testGroups4);

        TestMatchesOutputPort testPort1 = new TestMatchesOutputPort(testMatch1, testMatch2, testMatch3, testMatch2, testMatch1);
        TestMatchesOutputPort testPort2 = new TestMatchesOutputPort(testMatch3, testMatch3, testMatch3, testMatch3, testMatch3);
        TestMatchesOutputPort testPort3 = new TestMatchesOutputPort(testMatch4, testMatch4, testMatch4, testMatch4, testMatch4);

        ApproximateSorter sorter1 = new SorterByScore(false, false, true,
                false, MatchValidationType.INTERSECTION);
        ApproximateSorter sorter2 = new SorterByScore(false, false, false,
                false, MatchValidationType.INTERSECTION);
        ApproximateSorter sorter3 = new SorterByScore(false, false, true,
                false, MatchValidationType.ORDER);
        ApproximateSorter sorter4 = new SorterByScore(false, false, false,
                false, MatchValidationType.ORDER);

        assertEquals(0, countPortValues(sorter1.getOutputPort(new TestMatchesOutputPort[] {
                testPort2.getCopy(), testPort3.getCopy()})));
        assertEquals(0, countPortValues(sorter2.getOutputPort(new TestMatchesOutputPort[] {
                testPort2.getCopy(), testPort3.getCopy()})));
        assertEquals(0, countPortValues(sorter3.getOutputPort(new TestMatchesOutputPort[] {
                testPort3.getCopy(), testPort1.getCopy()})));
        assertEquals(0, countPortValues(sorter4.getOutputPort(new TestMatchesOutputPort[] {
                testPort3.getCopy(), testPort1.getCopy()})));
    }

    @Test
    public void matchesWithNullValuesTest() throws Exception {
        NSequenceWithQuality seq = new NSequenceWithQuality("TTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTTT");
        Map<String, CaptureGroupMatch> testGroupsSingle = new HashMap<String, CaptureGroupMatch>() {{
            put(WHOLE_PATTERN_MATCH_GROUP_NAME_PREFIX + "0", new CaptureGroupMatch(seq, (byte)1, new Range(0, 40)));
            put(COMMON_GROUP_NAME_PREFIX + "0", new CaptureGroupMatch(seq, (byte)1, new Range(0, 40)));
        }};
        Map<String, CaptureGroupMatch> testGroupsMulti = new HashMap<String, CaptureGroupMatch>() {{
            put(WHOLE_PATTERN_MATCH_GROUP_NAME_PREFIX + "0", new CaptureGroupMatch(seq, (byte)1, new Range(0, 40)));
            put(COMMON_GROUP_NAME_PREFIX + "0", new CaptureGroupMatch(seq, (byte)1, new Range(0, 40)));
            put(WHOLE_PATTERN_MATCH_GROUP_NAME_PREFIX + "1", new CaptureGroupMatch(seq, (byte)1, new Range(0, 40)));
            put(COMMON_GROUP_NAME_PREFIX + "1", new CaptureGroupMatch(seq, (byte)1, new Range(0, 40)));
        }};

        Match testMatchSingle = new Match(1, 0, testGroupsSingle);
        Match testMatchMulti = new Match(2, 0, testGroupsMulti);

        TestMatchesOutputPort testPortSingle = new TestMatchesOutputPort(testMatchSingle);
        TestMatchesOutputPort testPortMulti = new TestMatchesOutputPort(testMatchMulti);
        TestMatchesOutputPort testPortEmpty = new TestMatchesOutputPort();

        TestMatchesOutputPort[] testPortsSingleWithNull1 = new TestMatchesOutputPort[] {
                testPortSingle.getCopy(), testPortEmpty};
        TestMatchesOutputPort[] testPortsSingleWithNull2 = new TestMatchesOutputPort[] {
                testPortEmpty, testPortSingle.getCopy()};
        TestMatchesOutputPort[] testPortsMultiWithNull1Copy1 = new TestMatchesOutputPort[] {
                testPortMulti.getCopy(), testPortEmpty};
        TestMatchesOutputPort[] testPortsMultiWithNull1Copy2 = new TestMatchesOutputPort[] {
                testPortMulti.getCopy(), testPortEmpty};
        TestMatchesOutputPort[] testPortsMultiWithNull2Copy1 = new TestMatchesOutputPort[] {
                testPortEmpty, testPortMulti.getCopy()};
        TestMatchesOutputPort[] testPortsMultiWithNull2Copy2 = new TestMatchesOutputPort[] {
                testPortEmpty, testPortMulti.getCopy()};

        ApproximateSorter sorterSingle = new SorterByScore(false, false, true,
                false, MatchValidationType.INTERSECTION);
        ApproximateSorter sorterMulti1 = new SorterByScore(true, false, true,
                false, MatchValidationType.ALWAYS);
        ApproximateSorter sorterMulti2 = new SorterByScore(true, true, true,
                false, MatchValidationType.ALWAYS);

        assertEquals(0, countPortValues(sorterSingle.getOutputPort(testPortsSingleWithNull1)));
        assertEquals(0, countPortValues(sorterSingle.getOutputPort(testPortsSingleWithNull2)));
        assertEquals(0, countPortValues(sorterMulti1.getOutputPort(testPortsMultiWithNull1Copy1)));
        assertEquals(0, countPortValues(sorterMulti1.getOutputPort(testPortsMultiWithNull2Copy1)));
        assertEquals(1, countPortValues(sorterMulti2.getOutputPort(testPortsMultiWithNull1Copy2)));
        assertEquals(1, countPortValues(sorterMulti2.getOutputPort(testPortsMultiWithNull2Copy2)));
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
        predefinedMatchesApproximateSorterTest(true, true);
    }

    @Test
    public void fairSortingRandomTest() throws Exception {

    }
}
