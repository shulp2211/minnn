package com.milaboratory.mist.pattern;

import cc.redberry.pipe.OutputPort;
import com.milaboratory.core.sequence.NSequenceWithQuality;
import com.milaboratory.core.sequence.NucleotideSequence;
import com.milaboratory.test.TestUtil;
import org.junit.Test;

import java.util.ArrayList;

import static com.milaboratory.mist.util.CommonTestUtils.*;
import static org.junit.Assert.*;

public class AnyPatternTest {
    @Test
    public void randomGroupsTest() throws Exception {
        for (int i = 0; i < 30000; i++) {
            PatternAligner patternAligner = getTestPatternAligner(rg.nextInt(10));
            ArrayList<GroupEdge> groupEdges = new ArrayList<>();
            int numGroupEdges = rg.nextInt(40);
            int targetSize = rg.nextInt(1000) + 1;
            for (int j = 0; j < numGroupEdges; j++)
                groupEdges.add(new GroupEdge("1", rg.nextBoolean()));
            AnyPattern pattern = new AnyPattern(patternAligner, groupEdges);
            NucleotideSequence targetSeq = TestUtil.randomSequence(NucleotideSequence.ALPHABET, targetSize, targetSize);
            NSequenceWithQuality target = new NSequenceWithQuality(targetSeq.toString());
            OutputPort<Match> port = pattern.match(target).getMatches(rg.nextBoolean(), rg.nextBoolean());
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
