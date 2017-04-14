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
