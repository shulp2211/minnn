package com.milaboratory.mist.pattern;

import com.milaboratory.core.sequence.NucleotideSequence;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;

import java.util.HashMap;

import static org.junit.Assert.*;

public class MultiplePatternsOperatorTest {
    @Rule
    public final ExpectedException exception = ExpectedException.none();

    @Test
    public void groupNamesTest1() throws Exception {
        HashMap<GroupEdge, Integer> groups1 = new HashMap<GroupEdge, Integer>() {{
            put(new GroupEdge("ABC", true), 1);
            put(new GroupEdge("ABC", false), 3);
            put(new GroupEdge("DEF", true), 6);
            put(new GroupEdge("DEF", false), 7);
            put(new GroupEdge("GH", true), 9);
            put(new GroupEdge("GH", false), 10);
        }};
        HashMap<GroupEdge, Integer> groups2 = new HashMap<GroupEdge, Integer>() {{
            put(new GroupEdge("GH", false), 11);
            put(new GroupEdge("XYZ", true), 1);
            put(new GroupEdge("XYZ", false), 3);
        }};
        FuzzyMatchPattern pattern1 = new FuzzyMatchPattern(new NucleotideSequence("GTGGTTGTGTTGT"), groups1);
        FuzzyMatchPattern pattern2 = new FuzzyMatchPattern(new NucleotideSequence("GTGGTTGTGTTGT"), groups2);
        exception.expect(IllegalStateException.class);
        new AndPattern(pattern1, pattern2);
    }

    @Test
    public void groupNamesTest2() throws Exception {
        HashMap<GroupEdge, Integer> groups1 = new HashMap<GroupEdge, Integer>() {{
            put(new GroupEdge("ABC", true), 1);
            put(new GroupEdge("ABC", false), 3);
            put(new GroupEdge("DEF", true), 6);
            put(new GroupEdge("DEF", false), 7);
            put(new GroupEdge("GH", true), 9);
            put(new GroupEdge("GH", false), 10);
        }};
        HashMap<GroupEdge, Integer> groups2 = new HashMap<GroupEdge, Integer>() {{
            put(new GroupEdge("GH", false), 11);
            put(new GroupEdge("XYZ", true), 1);
            put(new GroupEdge("XYZ", false), 3);
        }};
        FuzzyMatchPattern pattern1 = new FuzzyMatchPattern(new NucleotideSequence("GTGGTTGTGTTGT"), groups1);
        FuzzyMatchPattern pattern2 = new FuzzyMatchPattern(new NucleotideSequence("GTGGTTGTGTTGT"), groups2);
        exception.expect(IllegalStateException.class);
        new PlusPattern(pattern1, pattern2);
    }

    @Test
    public void groupNamesTest3() throws Exception {
        HashMap<GroupEdge, Integer> groups = new HashMap<GroupEdge, Integer>() {{
            put(new GroupEdge("ABC", true), 1);
            put(new GroupEdge("ABC", false), 3);
            put(new GroupEdge("DEF", true), 6);
            put(new GroupEdge("DEF", false), 7);
            put(new GroupEdge("GH", true), 9);
            put(new GroupEdge("GH", false), 10);
        }};
        FuzzyMatchPattern pattern = new FuzzyMatchPattern(new NucleotideSequence("GTGGTTGTGTTGT"), groups);
        exception.expect(IllegalStateException.class);
        new AndPattern(pattern, pattern);
    }
}
