package com.milaboratory.mist.pattern;

import com.milaboratory.core.sequence.NucleotideSequenceCaseSensitive;
import org.junit.*;
import org.junit.rules.ExpectedException;

import java.util.ArrayList;

import static com.milaboratory.mist.util.CommonTestUtils.getTestScoring;

public class MultiplePatternsOperatorTest {
    @BeforeClass
    public static void init() throws Exception {
        PatternAligner.allowValuesOverride();
        PatternAligner.init(getTestScoring(), -1, 0, -1);
    }

    @Rule
    public final ExpectedException exception = ExpectedException.none();

    @Test
    public void groupNamesTest1() throws Exception {
        ArrayList<GroupEdgePosition> groups1 = new ArrayList<GroupEdgePosition>() {{
            add(new GroupEdgePosition(new GroupEdge("ABC", true), 1));
            add(new GroupEdgePosition(new GroupEdge("ABC", false), 3));
            add(new GroupEdgePosition(new GroupEdge("DEF", true), 6));
            add(new GroupEdgePosition(new GroupEdge("DEF", false), 7));
            add(new GroupEdgePosition(new GroupEdge("GH", true), 9));
            add(new GroupEdgePosition(new GroupEdge("GH", false), 10));
        }};

        ArrayList<GroupEdgePosition> groups2 = new ArrayList<GroupEdgePosition>() {{
            add(new GroupEdgePosition(new GroupEdge("GH", false), 11));
            add(new GroupEdgePosition(new GroupEdge("XYZ", true), 1));
            add(new GroupEdgePosition(new GroupEdge("XYZ", false), 3));
        }};

        FuzzyMatchPattern pattern1 = new FuzzyMatchPattern(Long.MIN_VALUE,
                new NucleotideSequenceCaseSensitive("gtggttgtgttgt"), groups1);
        FuzzyMatchPattern pattern2 = new FuzzyMatchPattern(Long.MIN_VALUE,
                new NucleotideSequenceCaseSensitive("gtggttgtgttgt"), groups2);
        exception.expect(IllegalStateException.class);
        new AndPattern(Long.MIN_VALUE, pattern1, pattern2);
    }

    @Test
    public void groupNamesTest2() throws Exception {
        ArrayList<GroupEdgePosition> groups1 = new ArrayList<GroupEdgePosition>() {{
            add(new GroupEdgePosition(new GroupEdge("ABC", true), 1));
            add(new GroupEdgePosition(new GroupEdge("ABC", false), 3));
            add(new GroupEdgePosition(new GroupEdge("DEF", true), 6));
            add(new GroupEdgePosition(new GroupEdge("DEF", false), 7));
            add(new GroupEdgePosition(new GroupEdge("GH", true), 9));
            add(new GroupEdgePosition(new GroupEdge("GH", false), 10));
        }};
        
        ArrayList<GroupEdgePosition> groups2 = new ArrayList<GroupEdgePosition>() {{
            add(new GroupEdgePosition(new GroupEdge("GH", false), 11));
            add(new GroupEdgePosition(new GroupEdge("XYZ", true), 1));
            add(new GroupEdgePosition(new GroupEdge("XYZ", false), 3));
        }};

        FuzzyMatchPattern pattern1 = new FuzzyMatchPattern(Long.MIN_VALUE,
                new NucleotideSequenceCaseSensitive("gtggttgtgttgt"), groups1);
        FuzzyMatchPattern pattern2 = new FuzzyMatchPattern(Long.MIN_VALUE,
                new NucleotideSequenceCaseSensitive("gtggttgtgttgt"), groups2);
        exception.expect(IllegalStateException.class);
        new PlusPattern(Long.MIN_VALUE, pattern1, pattern2);
    }

    @Test
    public void groupNamesTest3() throws Exception {
        ArrayList<GroupEdgePosition> groups = new ArrayList<GroupEdgePosition>() {{
            add(new GroupEdgePosition(new GroupEdge("ABC", true), 1));
            add(new GroupEdgePosition(new GroupEdge("ABC", false), 3));
            add(new GroupEdgePosition(new GroupEdge("DEF", true), 6));
            add(new GroupEdgePosition(new GroupEdge("DEF", false), 7));
            add(new GroupEdgePosition(new GroupEdge("GH", true), 9));
            add(new GroupEdgePosition(new GroupEdge("GH", false), 10));
        }};

        FuzzyMatchPattern pattern = new FuzzyMatchPattern(Long.MIN_VALUE,
                new NucleotideSequenceCaseSensitive("gtggttgtgttgt"), groups);
        exception.expect(IllegalStateException.class);
        new AndPattern(Long.MIN_VALUE, pattern, pattern);
    }
}
