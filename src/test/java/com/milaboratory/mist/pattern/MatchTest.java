package com.milaboratory.mist.pattern;

import com.milaboratory.core.Range;
import com.milaboratory.core.sequence.NSequenceWithQuality;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;

import java.util.HashMap;
import java.util.Map;

import static com.milaboratory.mist.pattern.Match.COMMON_GROUP_NAME_PREFIX;
import static com.milaboratory.mist.pattern.Match.WHOLE_PATTERN_MATCH_GROUP_NAME_PREFIX;
import static org.junit.Assert.*;

public class MatchTest {
    @Rule
    public final ExpectedException exception = ExpectedException.none();

    @Test
    public void matchTest() throws Exception {
        NSequenceWithQuality seqWhole0 = new NSequenceWithQuality("AATTAAGGCAAA");
        NSequenceWithQuality seqWhole1 = new NSequenceWithQuality("ATTAGACA");
        NSequenceWithQuality seqGroup0 = new NSequenceWithQuality("ATT");
        NSequenceWithQuality seqGroup1 = new NSequenceWithQuality("AAGG");
        NSequenceWithQuality seqGroup2 = new NSequenceWithQuality("ATTA");
        NSequenceWithQuality seqGroup3 = new NSequenceWithQuality("ACA");

        Map<String, CaptureGroupMatch> testGroups = new HashMap<String, CaptureGroupMatch>() {{
            put(WHOLE_PATTERN_MATCH_GROUP_NAME_PREFIX + "0", new CaptureGroupMatch(seqWhole0, (byte)0, new Range(0, 9)));
            put(COMMON_GROUP_NAME_PREFIX + "0", new CaptureGroupMatch(seqGroup0, (byte)0, new Range(1, 4)));
            put(COMMON_GROUP_NAME_PREFIX + "1", new CaptureGroupMatch(seqGroup1, (byte)0, new Range(4, 8)));
            put(WHOLE_PATTERN_MATCH_GROUP_NAME_PREFIX + "1", new CaptureGroupMatch(seqWhole1, (byte)1, new Range(0, 8)));
            put(COMMON_GROUP_NAME_PREFIX + "2", new CaptureGroupMatch(seqGroup2, (byte)1, new Range(0, 4)));
            put(COMMON_GROUP_NAME_PREFIX + "3", new CaptureGroupMatch(seqGroup3, (byte)1, new Range(5, 8)));
        }};
        Match testMatch = new Match(2, 10, testGroups);
        assertEquals(true, testMatch.isFound());
        assertEquals(2, testMatch.getNumberOfPatterns());
        assertEquals(10, testMatch.getScore());
        assertEquals(new Range(0, 9), testMatch.getWholePatternMatch(0).getRange());
        assertEquals(new Range(0, 8), testMatch.getWholePatternMatch(1).getRange());
        assertEquals(new Range(4, 8), testMatch.groupMatches.get(COMMON_GROUP_NAME_PREFIX + "1").getRange());

        exception.expect(IllegalStateException.class);
        testMatch.getWholePatternMatch();
    }
}
