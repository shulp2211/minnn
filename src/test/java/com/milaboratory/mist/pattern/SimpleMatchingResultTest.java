package com.milaboratory.mist.pattern;

import cc.redberry.pipe.OutputPort;
import com.milaboratory.core.Range;
import com.milaboratory.core.sequence.NSequenceWithQuality;
import org.junit.Test;

import java.util.HashMap;
import java.util.Map;

import static com.milaboratory.mist.pattern.Match.WHOLE_PATTERN_MATCH_GROUP_NAME_PREFIX;
import static org.junit.Assert.*;

public class SimpleMatchingResultTest {
    @Test
    public void singleMatchingResultTest() throws Exception {
        Map<String, CaptureGroupMatch> testGroups = new HashMap<String, CaptureGroupMatch>() {{
            put(WHOLE_PATTERN_MATCH_GROUP_NAME_PREFIX + "0", new CaptureGroupMatch(new NSequenceWithQuality("TAA"), (byte)0, new Range(1, 2)));
        }};
        Match testMatch = new Match(1, 10, testGroups);
        SimpleMatchingResult result = new SimpleMatchingResult(testMatch);
        assertEquals("TAA", result.getBestMatch().getWholePatternMatch().getTarget().getSequence().toString());
        assertEquals(new Range(1, 2), result.getMatches().take().getWholePatternMatch(0).getRange());
        OutputPort<Match> matches = result.getMatches();
        matches.take();
        assertEquals(null, matches.take());
        assertNotNull(result.getMatches().take());
        assertEquals(true, result.isFound());
        assertEquals(1, result.getMatchesNumber());
    }

    @Test
    public void noResultsTest() throws Exception {
        SimpleMatchingResult result = new SimpleMatchingResult();
        assertEquals(null, result.getBestMatch());
        assertEquals(null, result.getMatches().take());
        assertEquals(false, result.isFound());
        assertEquals(0, result.getMatchesNumber());
    }

    @Test
    public void multipleMatchingResultsTest() throws Exception {
        NSequenceWithQuality nseq = new NSequenceWithQuality("ATAGA");
        Map<String, CaptureGroupMatch> testGroups1 = new HashMap<String, CaptureGroupMatch>() {{
            put(WHOLE_PATTERN_MATCH_GROUP_NAME_PREFIX + "0", new CaptureGroupMatch(nseq, (byte)-1, new Range(0, 1)));
        }};
        Map<String, CaptureGroupMatch> testGroups2 = new HashMap<String, CaptureGroupMatch>() {{
            put(WHOLE_PATTERN_MATCH_GROUP_NAME_PREFIX + "0", new CaptureGroupMatch(nseq, (byte)-1, new Range(2, 3)));
        }};
        Map<String, CaptureGroupMatch> testGroups3 = new HashMap<String, CaptureGroupMatch>() {{
            put(WHOLE_PATTERN_MATCH_GROUP_NAME_PREFIX + "0", new CaptureGroupMatch(nseq, (byte)-1, new Range(4, 5)));
        }};
        SimpleMatchingResult result = new SimpleMatchingResult(
                new Match(1, 10, testGroups1),
                new Match(1, 15, testGroups2),
                new Match(1, 5, testGroups3));

        assertEquals(new Range(0, 1), result.getMatches(false).take().getWholePatternMatch().getRange());
        assertEquals(new Range(2, 3), result.getMatches().take().getWholePatternMatch().getRange());
        assertEquals(new Range(2, 3), result.getBestMatch().getWholePatternMatch().getRange());
        OutputPort<Match> matches = result.getMatches(true);
        matches.take();
        matches.take();
        assertEquals(new Range(4, 5), matches.take().getWholePatternMatch().getRange());
        assertEquals(true, result.isFound());
        assertEquals(3, result.getMatchesNumber());
    }
}
