package com.milaboratory.mist.pattern;

import com.milaboratory.core.Range;
import com.milaboratory.core.sequence.NSequenceWithQuality;
import org.junit.Test;

import java.util.HashMap;
import java.util.Map;

import static com.milaboratory.mist.pattern.Match.WHOLE_PATTERN_MATCH_GROUP_NAME_PREFIX;
import static org.junit.Assert.*;

public class SingleMatchingResultTest {
    @Test
    public void singleMatchingResultTest() throws Exception {
        Map<String, CaptureGroupMatch> testGroups = new HashMap<String, CaptureGroupMatch>() {{
            put(WHOLE_PATTERN_MATCH_GROUP_NAME_PREFIX + "0", new CaptureGroupMatch(new NSequenceWithQuality("TAA"), (byte)0, new Range(1, 2)));
        }};
        Match testMatch = new Match(1, 10, testGroups);
        SingleMatchingResult result = new SingleMatchingResult(testMatch);
        assertEquals("TAA", result.getBestMatch().getWholePatternMatch().getTarget().getSequence().toString());
        assertEquals(new Range(1, 2), result.getMatches().take().getWholePatternMatch(0).getRange());
        assertEquals(null, result.getMatches().take());
    }
}
