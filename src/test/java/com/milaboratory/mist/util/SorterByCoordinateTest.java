package com.milaboratory.mist.util;

import com.milaboratory.core.Range;
import com.milaboratory.core.sequence.NSequenceWithQuality;
import com.milaboratory.mist.pattern.CaptureGroupMatch;
import com.milaboratory.mist.pattern.Match;
import com.milaboratory.mist.pattern.MatchValidationType;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;

import java.util.HashMap;
import java.util.Map;

import static com.milaboratory.mist.pattern.Match.WHOLE_PATTERN_MATCH_GROUP_NAME_PREFIX;
import static com.milaboratory.mist.util.CommonTestTemplates.countPortValues;
import static com.milaboratory.mist.util.CommonTestTemplates.predefinedMatchesApproximateSorterTest;
import static org.junit.Assert.*;

public class SorterByCoordinateTest {
    @Rule
    public final ExpectedException exception = ExpectedException.none();

    @Test
    public void simplePredefinedMatchesTest() throws Exception {
        predefinedMatchesApproximateSorterTest(false, false);
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

        ApproximateSorter sorter1 = new SorterByCoordinate(false, false, true,
                false, MatchValidationType.INTERSECTION);
        ApproximateSorter sorter2 = new SorterByCoordinate(false, false, false,
                false, MatchValidationType.INTERSECTION);
        ApproximateSorter sorter3 = new SorterByCoordinate(false, false, true,
                false, MatchValidationType.ORDER);
        ApproximateSorter sorter4 = new SorterByCoordinate(false, false, false,
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
        predefinedMatchesApproximateSorterTest(false, true);
    }

    @Test
    public void fairSortingRandomTest() throws Exception {

    }
}
