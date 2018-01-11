package com.milaboratory.mist.pattern;

import cc.redberry.pipe.OutputPort;
import com.milaboratory.core.sequence.*;
import com.milaboratory.test.TestUtil;
import org.junit.*;

import java.util.ArrayList;

import static com.milaboratory.mist.util.CommonTestUtils.*;
import static org.junit.Assert.*;

public class AnyPatternTest {
    @BeforeClass
    public static void init() throws Exception {
        PatternAligner.allowValuesOverride();
    }

    @Test
    public void randomGroupsTest() throws Exception {
        for (int i = 0; i < 30000; i++) {
            PatternAligner.init(getTestScoring(), -1, rg.nextInt(10), -1);
            ArrayList<GroupEdge> groupEdges = new ArrayList<>();
            int numGroupEdges = rg.nextInt(40);
            int targetSize = rg.nextInt(1000) + 1;
            for (int j = 0; j < numGroupEdges; j++)
                groupEdges.add(new GroupEdge("1", rg.nextBoolean()));
            AnyPattern pattern = new AnyPattern(Long.MIN_VALUE, groupEdges);
            NucleotideSequence targetSeq = TestUtil.randomSequence(NucleotideSequence.ALPHABET, targetSize, targetSize);
            NSequenceWithQuality target = new NSequenceWithQuality(targetSeq.toString());
            OutputPort<MatchIntermediate> port = pattern.match(target).getMatches(rg.nextBoolean());
            Match match = port.take();
            assertNull(port.take());
            for (int j = 0; j < numGroupEdges; j++) {
                MatchedGroupEdge matchedGroupEdge = match.getMatchedGroupEdges().get(j);
                if (groupEdges.get(j).isStart())
                    assertEquals(0, matchedGroupEdge.getPosition());
                else
                    assertEquals(target.size(), matchedGroupEdge.getPosition());
                assertTrue(matchedGroupEdge.getGroupName().equals("1"));
            }
        }
    }
}
