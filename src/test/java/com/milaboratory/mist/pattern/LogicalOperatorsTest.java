package com.milaboratory.mist.pattern;

import com.milaboratory.core.Range;
import com.milaboratory.core.sequence.MultiNSequenceWithQuality;
import com.milaboratory.core.sequence.NSequenceWithQuality;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;

import java.util.HashMap;

import static com.milaboratory.mist.pattern.Match.COMMON_GROUP_NAME_PREFIX;
import static org.junit.Assert.*;

public class LogicalOperatorsTest {
    @Rule
    public final ExpectedException exception = ExpectedException.none();

    @Test
    public void logicTest() throws Exception {
        FuzzyMatchPattern pattern1 = new FuzzyMatchPattern(new NSequenceWithQuality("ATTAGACA"));
        FuzzyMatchPattern pattern2 = new FuzzyMatchPattern(new NSequenceWithQuality("GTTATTACCA"));
        AndPattern pattern3 = new AndPattern(new FuzzyMatchPattern(new NSequenceWithQuality("AT")),
                new FuzzyMatchPattern(new NSequenceWithQuality("GCAT")));
        MultiPattern multiPattern1 = new MultiPattern(pattern1, pattern2, pattern3);
        MultiPattern multiPattern2 = new MultiPattern(pattern1, pattern3);
        MultiPattern multiPattern3 = new MultiPattern(pattern3, pattern2);
        MultiPattern multiPattern4 = new MultiPattern(pattern1);

        MultiNSequenceWithQuality mseq1 = new MultiNSequenceWithQuality() {
            @Override
            public int numberOfSequences() {
                return 3;
            }

            @Override
            public NSequenceWithQuality get(int id) {
                switch (id) {
                    case 0:
                        return new NSequenceWithQuality("ACAATTAGACA");
                    case 1:
                        return new NSequenceWithQuality("GTTATTACCA");
                    case 2:
                        return new NSequenceWithQuality("AACTTGCATAT");
                }
                return null;
            }
        };

        MultiNSequenceWithQuality mseq2 = new MultiNSequenceWithQuality() {
            @Override
            public int numberOfSequences() {
                return 2;
            }

            @Override
            public NSequenceWithQuality get(int id) {
                switch (id) {
                    case 0:
                        return new NSequenceWithQuality("AACTTGCATAT");
                    case 1:
                        return new NSequenceWithQuality("GTTATTACCA").getReverseComplement();
                }
                return null;
            }
        };

        MultiNSequenceWithQuality mseq3 = new MultiNSequenceWithQuality() {
            @Override
            public int numberOfSequences() {
                return 1;
            }

            @Override
            public NSequenceWithQuality get(int id) {
                return new NSequenceWithQuality("ATTAGACA");
            }
        };

        AndOperator andOperatorS1_1 = new AndOperator(multiPattern1);
        OrOperator orOperatorS1_1 = new OrOperator(multiPattern1);
        NotOperator notOperatorS1_1 = new NotOperator(multiPattern1);

        assertTrue(andOperatorS1_1.match(mseq1).isFound());
        assertTrue(orOperatorS1_1.match(mseq1).isFound());
        assertFalse(notOperatorS1_1.match(mseq1).isFound());

        AndOperator andOperatorS1_2 = new AndOperator(andOperatorS1_1, notOperatorS1_1);
        AndOperator andOperatorS1_3 = new AndOperator(new NotOperator(notOperatorS1_1), orOperatorS1_1);
        OrOperator orOperatorS1_2 = new OrOperator(andOperatorS1_1, notOperatorS1_1);
        OrOperator orOperatorS1_3 = new OrOperator(notOperatorS1_1, notOperatorS1_1, orOperatorS1_1, notOperatorS1_1);
        OrOperator orOperatorS1_4 = new OrOperator(notOperatorS1_1, new NotOperator(andOperatorS1_1));

        assertFalse(andOperatorS1_2.match(mseq1).isFound());
        assertTrue(andOperatorS1_3.match(mseq1).isFound());
        assertTrue(orOperatorS1_2.match(mseq1).isFound());
        assertTrue(orOperatorS1_3.match(mseq1).isFound());
        assertFalse(orOperatorS1_4.match(mseq1).isFound());

        AndOperator andOperatorS2_1 = new AndOperator(multiPattern2, multiPattern3);
        OrOperator orOperatorS2_1 = new OrOperator(multiPattern2, multiPattern3);
        AndOperator andOperatorS2_2 = new AndOperator(new NotOperator(multiPattern2), multiPattern3);
        OrOperator orOperatorS2_2 = new OrOperator(new NotOperator(multiPattern2), multiPattern3);

        Range ranges[] = {new Range(3, 11), new Range(0, 10, true)};

        MatchingResult andResultS2_1 = andOperatorS2_1.match(mseq2, ranges);
        MatchingResult orResultS2_1 = orOperatorS2_1.match(mseq2, ranges);
        MatchingResult andResultS2_2 = andOperatorS2_2.match(mseq2, ranges);
        MatchingResult orResultS2_2 = orOperatorS2_2.match(mseq2, ranges);

        assertFalse(andResultS2_1.isFound());
        assertTrue(orResultS2_1.isFound());
        assertTrue(andResultS2_2.isFound());
        assertTrue(orResultS2_2.isFound());

        assertTrue(new AndOperator(multiPattern4).match(mseq3).isFound());
        assertTrue(new OrOperator(multiPattern4).match(mseq3).isFound());
        assertFalse(new NotOperator(multiPattern4).match(mseq3).isFound());
        assertFalse(new AndOperator(new NotOperator(multiPattern4)).match(mseq3).isFound());
        assertFalse(new OrOperator(new NotOperator(multiPattern4)).match(mseq3).isFound());
    }

    @Test
    public void simpleTest() throws Exception {
        FuzzyMatchPattern pattern1 = new FuzzyMatchPattern(new NSequenceWithQuality("ATTAGACA"));
        FuzzyMatchPattern pattern2 = new FuzzyMatchPattern(new NSequenceWithQuality("GTTATTACCA"));
        AndPattern pattern3 = new AndPattern(new FuzzyMatchPattern(new NSequenceWithQuality("AT")),
                new FuzzyMatchPattern(new NSequenceWithQuality("GCAT")));
        MultiPattern multiPattern = new MultiPattern(pattern1, pattern2, pattern3);

        MultiNSequenceWithQuality mseq = new MultiNSequenceWithQuality() {
            @Override
            public int numberOfSequences() {
                return 3;
            }

            @Override
            public NSequenceWithQuality get(int id) {
                switch (id) {
                    case 0:
                        return new NSequenceWithQuality("ACAATTAGACA").getReverseComplement();
                    case 1:
                        return new NSequenceWithQuality("GTTATTACCA");
                    case 2:
                        return new NSequenceWithQuality("AACTTGCATAT");
                }
                return null;
            }
        };

        NotOperator notOperatorFalse = new NotOperator(multiPattern);
        OrOperator orOperatorTrue = new OrOperator(notOperatorFalse, multiPattern, notOperatorFalse);
        AndOperator andOperatorTrue = new AndOperator(multiPattern, orOperatorTrue, multiPattern);
        AndOperator andOperatorFalse = new AndOperator(multiPattern, andOperatorTrue, orOperatorTrue, notOperatorFalse);
        OrOperator orOperatorFalse = new OrOperator(notOperatorFalse, notOperatorFalse, andOperatorFalse);
        NotOperator notOperatorTrue = new NotOperator(orOperatorFalse);

        Range ranges[] = {new Range(1, 11, true), new Range(0, 10), new Range(2, 11)};
        boolean reversed[] = {true, false, false};

        MatchingResult notFalseResult = notOperatorFalse.match(mseq, reversed);
        MatchingResult orFalseResult = orOperatorFalse.match(mseq, reversed);
        MatchingResult andFalseResult = andOperatorFalse.match(mseq, reversed);
        MatchingResult notTrueResult = notOperatorTrue.match(mseq, reversed);
        MatchingResult orTrueResult = orOperatorTrue.match(mseq, reversed);
        MatchingResult andTrueResult = andOperatorTrue.match(mseq, reversed);
        MatchingResult notFalseResultR = notOperatorFalse.match(mseq, ranges, reversed);
        MatchingResult orFalseResultR = orOperatorFalse.match(mseq, ranges, reversed);
        MatchingResult andFalseResultR = andOperatorFalse.match(mseq, ranges, reversed);
        MatchingResult notTrueResultR = notOperatorTrue.match(mseq, ranges, reversed);
        MatchingResult orTrueResultR = orOperatorTrue.match(mseq, ranges, reversed);
        MatchingResult andTrueResultR = andOperatorTrue.match(mseq, ranges, reversed);

        assertNull(notFalseResult.getBestMatch());
        assertNull(orFalseResult.getBestMatch());
        assertNull(andFalseResult.getBestMatch());
        assertNull(notFalseResultR.getBestMatch());
        assertNull(orFalseResultR.getBestMatch());
        assertNull(andFalseResultR.getBestMatch());
        assertNull(notTrueResult.getBestMatch());
        assertNotNull(orTrueResult.getBestMatch());
        assertNotNull(andTrueResult.getBestMatch());
        assertNull(notTrueResultR.getBestMatch());
        assertNotNull(orTrueResultR.getBestMatch());
        assertNotNull(andTrueResultR.getBestMatch());

        assertEquals(0, notTrueResult.getMatchesNumber());
        assertEquals(0, notFalseResult.getMatchesNumber());
        assertEquals(0, orFalseResult.getMatchesNumber());
        assertEquals(0, andFalseResult.getMatchesNumber());
        assertEquals(1, andTrueResult.getMatchesNumber());
        assertEquals(1, orTrueResult.getMatchesNumber());

        Match testMatch = andTrueResultR.getMatches().take();
        assertEquals("GTTATTACCA", testMatch.getWholePatternMatch(5).getValue().getSequence().toString());
        assertEquals("GCATAT", testMatch.getWholePatternMatch(6).getValue().getSequence().toString());

        exception.expect(IllegalStateException.class);
        new NotOperator(orOperatorTrue, orOperatorFalse);
    }

    @Test
    public void groupNamesTest() throws Exception {
        NSequenceWithQuality testSeq = new NSequenceWithQuality("GTGGTTGTGTTGT");
        HashMap<String, Range> groups1 = new HashMap<String, Range>() {{
            put("ABC", new Range(1, 3));
            put("DEF", new Range(6, 7));
            put("GH", new Range(10, 11));
        }};
        HashMap<String, Range> groups2 = new HashMap<String, Range>() {{
            put("XYZ", new Range(1, 3));
            put("GH", new Range(9, 10));
        }};
        HashMap<String, Range> groups3 = new HashMap<String, Range>() {{
            put("123", new Range(2, 4));
            put("456", new Range(5, 7));
        }};
        HashMap<String, Range> groups4 = new HashMap<String, Range>() {{
            put("789", new Range(0, 1));
            put("0", new Range(4, 5));
        }};
        FuzzyMatchPattern pattern1 = new FuzzyMatchPattern(testSeq, groups1);
        FuzzyMatchPattern pattern2 = new FuzzyMatchPattern(testSeq, groups2);
        FuzzyMatchPattern pattern3 = new FuzzyMatchPattern(testSeq, groups3);
        FuzzyMatchPattern pattern4 = new FuzzyMatchPattern(testSeq, groups4);
        MultiPattern multiPattern1 = new MultiPattern(pattern1, pattern3);
        MultiPattern multiPattern2 = new MultiPattern(pattern2, pattern4);

        exception.expect(IllegalStateException.class);
        new OrOperator(multiPattern1, multiPattern2);
    }

    @Test
    public void groupsInNotTest() throws Exception {
        HashMap<String, Range> groups = new HashMap<String, Range>() {{ put("0", new Range(0, 1)); }};
        FuzzyMatchPattern pattern = new FuzzyMatchPattern(new NSequenceWithQuality("A"), groups);
        MultiPattern multiPattern = new MultiPattern(pattern);

        exception.expect(IllegalStateException.class);
        new NotOperator(multiPattern);
    }

    @Test
    public void groupsTest() throws Exception {
        HashMap<String, Range> groups1 = new HashMap<String, Range>() {{
            put("1", new Range(0, 1));
            put("2", new Range(1, 3));
            put("4", new Range(4, 5));
        }};
        HashMap<String, Range> groups2 = new HashMap<String, Range>() {{
            put("3", new Range(1, 3));
            put("5", new Range(5, 6));
        }};

        FuzzyMatchPattern pattern1 = new FuzzyMatchPattern(new NSequenceWithQuality("TAGCC"), groups1);
        FuzzyMatchPattern pattern2 = new FuzzyMatchPattern(new NSequenceWithQuality("CAGATGCA"), groups2);
        FuzzyMatchPattern pattern3 = new FuzzyMatchPattern(new NSequenceWithQuality("A"));
        MultiPattern multiPattern1 = new MultiPattern(pattern1, pattern3);
        MultiPattern multiPattern2 = new MultiPattern(pattern3, pattern2);
        MultiPattern multiPattern3 = new MultiPattern(pattern3, pattern3);
        NotOperator notOperator = new NotOperator(multiPattern3);
        OrOperator orOperator = new OrOperator(notOperator, multiPattern1, notOperator);
        AndOperator andOperator = new AndOperator(multiPattern2, orOperator);

        MultiNSequenceWithQuality mseq = new MultiNSequenceWithQuality() {
            @Override
            public int numberOfSequences() {
                return 2;
            }

            @Override
            public NSequenceWithQuality get(int id) {
                switch (id) {
                    case 0:
                        return new NSequenceWithQuality("ACAATTAGCCA");
                    case 1:
                        return new NSequenceWithQuality("GTGCATCTGCCA");
                }
                return null;
            }
        };
        MatchingResult result = andOperator.match(mseq, false, true);
        assertEquals("AG", result.getMatches(false).take().getGroupMatches(true)
                .get(COMMON_GROUP_NAME_PREFIX + "2").getValue().getSequence().toString());
        assertEquals(new Range(8, 9), result.getMatches(true).take().getGroupMatches(true)
                .get(COMMON_GROUP_NAME_PREFIX + "5").getRange());
        assertEquals("AG", result.getBestMatch().getGroupMatches(true)
                .get(COMMON_GROUP_NAME_PREFIX + "3").getValue().getSequence().toString());

        for (int i = 0; i < 14; i++)
            assertNotNull(result.getMatches().take());
        assertNull(result.getMatches().take());
    }
}
