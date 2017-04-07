package com.milaboratory.mist.pattern;

import com.milaboratory.core.Range;
import com.milaboratory.core.sequence.NSequenceWithQuality;
import com.milaboratory.core.sequence.NucleotideSequence;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;

import java.util.HashMap;
import java.util.Map;

import static com.milaboratory.mist.pattern.Match.WHOLE_PATTERN_MATCH_GROUP_NAME_PREFIX;
import static com.milaboratory.mist.pattern.Match.COMMON_GROUP_NAME_PREFIX;
import static org.junit.Assert.*;

public class MultiplePatternsOperatorTest {
    @Rule
    public final ExpectedException exception = ExpectedException.none();

    @Test
    public void checkRangesIntersection() throws Exception {
        Range[] ranges1 = {new Range(1, 3), new Range(3, 5)};
        Range[] ranges2 = {new Range(2, 4), new Range(3, 5)};
        Range[] ranges3 = {new Range(10, 12), new Range(16, 18), new Range(20, 30), new Range(5, 8)};
        Range[] ranges4 = {new Range(6, 11), new Range(13, 15), new Range(10, 11)};
        Range[] ranges5 = {};
        Range[] ranges6 = {new Range(0, 1)};
        assertEquals(false, MultiplePatternsOperator.checkRangesIntersection(ranges1));
        assertEquals(true, MultiplePatternsOperator.checkRangesIntersection(ranges2));
        assertEquals(false, MultiplePatternsOperator.checkRangesIntersection(ranges3));
        assertEquals(true, MultiplePatternsOperator.checkRangesIntersection(ranges4));
        assertEquals(false, MultiplePatternsOperator.checkRangesIntersection(ranges5));
        assertEquals(false, MultiplePatternsOperator.checkRangesIntersection(ranges6));
    }

    @Test
    public void combine2Ranges() throws Exception {
        assertEquals(new Range(2, 13), MultiplePatternsOperator.combine2Ranges(new Range(2, 6), new Range(9, 13)));
        assertEquals(new Range(1, 3), MultiplePatternsOperator.combine2Ranges(new Range(2, 3), new Range(1, 2)));
        assertEquals(new Range(4, 11), MultiplePatternsOperator.combine2Ranges(new Range(8, 11), new Range(4, 10)));
    }

    @Test
    public void combineRanges() throws Exception {
        assertEquals(new Range(2, 13), MultiplePatternsOperator.combineRanges(new Range(2, 13)));
        assertEquals(new Range(3, 10), MultiplePatternsOperator.combineRanges(new Range(5, 10), new Range(8, 10), new Range(3, 6)));
        assertEquals(new Range(0, 20), MultiplePatternsOperator.combineRanges(new Range(0, 2), new Range(17, 20), new Range(10, 14), new Range(6, 11)));
        exception.expect(IllegalStateException.class);
        MultiplePatternsOperator.combineRanges();
    }

    @Test
    public void combineMatches() throws Exception {
        NSequenceWithQuality seq = new NSequenceWithQuality("ATTAGACA");

        Map<String, CaptureGroupMatch> testGroups1 = new HashMap<String, CaptureGroupMatch>() {{
            put(COMMON_GROUP_NAME_PREFIX + "0", new CaptureGroupMatch(seq, (byte)3, new Range(2, 3)));
            put(WHOLE_PATTERN_MATCH_GROUP_NAME_PREFIX + "0", new CaptureGroupMatch(seq, (byte)3, new Range(2, 4)));
        }};
        Map<String, CaptureGroupMatch> testGroups2 = new HashMap<String, CaptureGroupMatch>() {{
            put(COMMON_GROUP_NAME_PREFIX + "1", new CaptureGroupMatch(seq, (byte)3, new Range(3, 4)));
            put(WHOLE_PATTERN_MATCH_GROUP_NAME_PREFIX + "0", new CaptureGroupMatch(seq, (byte)3, new Range(3, 5)));
            put(COMMON_GROUP_NAME_PREFIX + "2", new CaptureGroupMatch(seq, (byte)3, new Range(4, 5)));
        }};
        Map<String, CaptureGroupMatch> testGroups3 = new HashMap<String, CaptureGroupMatch>() {{
            put(WHOLE_PATTERN_MATCH_GROUP_NAME_PREFIX + "0", new CaptureGroupMatch(seq, (byte)3, new Range(4, 7)));
            put(COMMON_GROUP_NAME_PREFIX + "2", new CaptureGroupMatch(seq, (byte)3, new Range(4, 6)));
            put(COMMON_GROUP_NAME_PREFIX + "3", new CaptureGroupMatch(seq, (byte)3, new Range(6, 7)));
        }};
        Match testMatch1 = new Match(1, 1, testGroups1);
        Match testMatch2 = new Match(1, 3, testGroups2);
        Match testMatch3 = new Match(1, 2, testGroups3);

        Match combinedMatch = MultiplePatternsOperator.combineMatches(seq, (byte)3, testMatch1, testMatch2, testMatch3);
        assertEquals(combinedMatch.getWholePatternMatch(), combinedMatch.getWholePatternMatch(0));
        assertEquals(combinedMatch.getWholePatternMatch(), combinedMatch.groupMatches.get(WHOLE_PATTERN_MATCH_GROUP_NAME_PREFIX + "0"));
        assertEquals(new Range(2, 7), combinedMatch.getWholePatternMatch(0).getRange());
        assertEquals(new Range(2, 3), combinedMatch.groupMatches.get(COMMON_GROUP_NAME_PREFIX + "0").getRange());
        assertEquals(new Range(3, 4), combinedMatch.groupMatches.get(COMMON_GROUP_NAME_PREFIX + "1").getRange());
        assertEquals(new Range(4, 6), combinedMatch.groupMatches.get(COMMON_GROUP_NAME_PREFIX + "2").getRange());
        assertEquals(new Range(6, 7), combinedMatch.groupMatches.get(COMMON_GROUP_NAME_PREFIX + "3").getRange());
        assertEquals(3, combinedMatch.getWholePatternMatch().getTargetId());
        assertEquals(seq, combinedMatch.groupMatches.get(WHOLE_PATTERN_MATCH_GROUP_NAME_PREFIX + "0").getTarget());
    }

    @Test
    public void sumMatchesScore() throws Exception {
        Map<String, CaptureGroupMatch> testGroups = new HashMap<String, CaptureGroupMatch>() {{
            put(WHOLE_PATTERN_MATCH_GROUP_NAME_PREFIX + "0", new CaptureGroupMatch(new NSequenceWithQuality("ATTAGACA"), (byte)0, new Range(1, 3)));
            put(WHOLE_PATTERN_MATCH_GROUP_NAME_PREFIX + "1", new CaptureGroupMatch(new NSequenceWithQuality("ATTAGACA"), (byte)1, new Range(5, 7)));
        }};
        Match testMatch1 = new Match(1, 20, testGroups);
        Match testMatch2 = new Match(1, 10, testGroups);
        Match testMatch3 = new Match(2, 3, testGroups);
        assertEquals(33, MultiplePatternsOperator.sumMatchesScore(testMatch1, testMatch2, testMatch3));
    }

    @Test
    public void groupNamesTest1() throws Exception {
        HashMap<String, Range> groups1 = new HashMap<String, Range>() {{
            put("ABC", new Range(1, 3));
            put("DEF", new Range(6, 7));
            put("GH", new Range(10, 11));
        }};
        HashMap<String, Range> groups2 = new HashMap<String, Range>() {{
            put("XYZ", new Range(1, 3));
            put("GH", new Range(9, 10));
        }};
        PerfectMatchPattern pattern1 = new PerfectMatchPattern(new NucleotideSequence("GTGGTTGTGTTGT").toMotif(), groups1);
        PerfectMatchPattern pattern2 = new PerfectMatchPattern(new NucleotideSequence("GTGGTTGTGTTGT").toMotif(), groups1);
        exception.expect(IllegalStateException.class);
        AndPattern andPattern = new AndPattern(pattern1, pattern2);
    }

    @Test
    public void groupNamesTest2() throws Exception {
        HashMap<String, Range> groups1 = new HashMap<String, Range>() {{
            put("ABC", new Range(1, 3));
            put("DEF", new Range(6, 7));
            put("GH", new Range(10, 11));
        }};
        HashMap<String, Range> groups2 = new HashMap<String, Range>() {{
            put("XYZ", new Range(1, 3));
            put("GH", new Range(9, 10));
        }};
        PerfectMatchPattern pattern1 = new PerfectMatchPattern(new NucleotideSequence("GTGGTTGTGTTGT").toMotif(), groups1);
        PerfectMatchPattern pattern2 = new PerfectMatchPattern(new NucleotideSequence("GTGGTTGTGTTGT").toMotif(), groups1);
        exception.expect(IllegalStateException.class);
        PlusPattern plusPattern = new PlusPattern(pattern1, pattern2);
    }

    @Test
    public void groupNamesTest3() throws Exception {
        HashMap<String, Range> groups = new HashMap<String, Range>() {{
            put("ABC", new Range(1, 3));
            put("DEF", new Range(6, 7));
            put("GH", new Range(10, 11));
        }};
        PerfectMatchPattern pattern = new PerfectMatchPattern(new NucleotideSequence("GTGGTTGTGTTGT").toMotif(), groups);
        exception.expect(IllegalStateException.class);
        AndPattern andPattern = new AndPattern(pattern, pattern);
    }
}
