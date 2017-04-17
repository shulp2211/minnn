package com.milaboratory.mist.pattern;

import com.milaboratory.core.Range;
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
        HashMap<String, Range> groups1 = new HashMap<String, Range>() {{
            put("ABC", new Range(1, 3));
            put("DEF", new Range(6, 7));
            put("GH", new Range(10, 11));
        }};
        HashMap<String, Range> groups2 = new HashMap<String, Range>() {{
            put("XYZ", new Range(1, 3));
            put("GH", new Range(9, 10));
        }};
        FuzzyMatchPattern pattern1 = new FuzzyMatchPattern(new NucleotideSequence("GTGGTTGTGTTGT"), groups1);
        FuzzyMatchPattern pattern2 = new FuzzyMatchPattern(new NucleotideSequence("GTGGTTGTGTTGT"), groups2);
        exception.expect(IllegalStateException.class);
        new AndPattern(pattern1, pattern2);
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
        FuzzyMatchPattern pattern1 = new FuzzyMatchPattern(new NucleotideSequence("GTGGTTGTGTTGT"), groups1);
        FuzzyMatchPattern pattern2 = new FuzzyMatchPattern(new NucleotideSequence("GTGGTTGTGTTGT"), groups2);
        exception.expect(IllegalStateException.class);
        new PlusPattern(pattern1, pattern2);
    }

    @Test
    public void groupNamesTest3() throws Exception {
        HashMap<String, Range> groups = new HashMap<String, Range>() {{
            put("ABC", new Range(1, 3));
            put("DEF", new Range(6, 7));
            put("GH", new Range(10, 11));
        }};
        FuzzyMatchPattern pattern = new FuzzyMatchPattern(new NucleotideSequence("GTGGTTGTGTTGT"), groups);
        exception.expect(IllegalStateException.class);
        new AndPattern(pattern, pattern);
    }
}
