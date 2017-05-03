package com.milaboratory.mist.pattern;

import cc.redberry.pipe.OutputPort;
import com.milaboratory.core.Range;
import com.milaboratory.core.sequence.MultiNSequenceWithQuality;
import com.milaboratory.core.sequence.NSequenceWithQuality;
import com.milaboratory.core.sequence.NucleotideSequence;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;

import java.util.HashMap;

import static com.milaboratory.mist.pattern.GroupUtils.getGroupsFromMatch;
import static com.milaboratory.mist.pattern.MatchUtils.countMatches;
import static org.junit.Assert.*;

public class LogicalOperatorsTest {
    @Rule
    public final ExpectedException exception = ExpectedException.none();

    @Test
    public void logicTest() throws Exception {
        FuzzyMatchPattern pattern1 = new FuzzyMatchPattern(new NucleotideSequence("ATTAGACA"));
        FuzzyMatchPattern pattern2 = new FuzzyMatchPattern(new NucleotideSequence("GTTATTACCA"));
        AndPattern pattern3 = new AndPattern(new FuzzyMatchPattern(new NucleotideSequence("AT")),
                new FuzzyMatchPattern(new NucleotideSequence("GCAT")));
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
        FuzzyMatchPattern pattern1 = new FuzzyMatchPattern(new NucleotideSequence("ATTAGACA"));
        FuzzyMatchPattern pattern2 = new FuzzyMatchPattern(new NucleotideSequence("GTTATTACCA"));
        AndPattern pattern3 = new AndPattern(new FuzzyMatchPattern(new NucleotideSequence("AT")),
                new FuzzyMatchPattern(new NucleotideSequence("GCAT")));
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

        assertEquals(0, countMatches(notTrueResult, true));
        assertEquals(0, countMatches(notFalseResult, true));
        assertEquals(0, countMatches(orFalseResult, true));
        assertEquals(0, countMatches(andFalseResult, true));
        assertEquals(1, countMatches(andTrueResult, true));
        assertEquals(1, countMatches(orTrueResult, true));

        Match testMatch = andTrueResultR.getMatches().take();
        assertEquals("GTTATTACCA", testMatch.getMatchedRange(5).getValue().getSequence().toString());
        assertEquals("GCATAT", testMatch.getMatchedRange(6).getValue().getSequence().toString());

        exception.expect(IllegalArgumentException.class);
        new NotOperator(orOperatorTrue, orOperatorFalse);
    }

    @Test
    public void groupNamesTest() throws Exception {
        NucleotideSequence testSeq = new NucleotideSequence("GTGGTTGTGTTGT");
        HashMap<GroupEdge, Integer> groups1 = new HashMap<GroupEdge, Integer>() {{
            put(new GroupEdge("ABC", true), 1);
            put(new GroupEdge("ABC", false), 3);
            put(new GroupEdge("DEF", true), 6);
            put(new GroupEdge("DEF", false), 7);
            put(new GroupEdge("GH", true), 10);
            put(new GroupEdge("GH", false), 11);
        }};
        HashMap<GroupEdge, Integer> groups2 = new HashMap<GroupEdge, Integer>() {{
            put(new GroupEdge("XYZ", true), 1);
            put(new GroupEdge("XYZ", false), 3);
            put(new GroupEdge("GH", false), 10);
        }};
        HashMap<GroupEdge, Integer> groups3 = new HashMap<GroupEdge, Integer>() {{
            put(new GroupEdge("123", true), 2);
            put(new GroupEdge("123", false), 4);
            put(new GroupEdge("456", true), 5);
            put(new GroupEdge("456", false), 7);
        }};
        HashMap<GroupEdge, Integer> groups4 = new HashMap<GroupEdge, Integer>() {{
            put(new GroupEdge("789", true), 0);
            put(new GroupEdge("0", true), 4);
            put(new GroupEdge("0", false), 5);
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
        HashMap<GroupEdge, Integer> groups = new HashMap<GroupEdge, Integer>() {{
            put(new GroupEdge("0", true), 0); }};
        FuzzyMatchPattern pattern = new FuzzyMatchPattern(new NucleotideSequence("A"), groups);
        MultiPattern multiPattern = new MultiPattern(pattern);

        exception.expect(IllegalStateException.class);
        new NotOperator(multiPattern);
    }

    @Test
    public void groupsTest() throws Exception {
        HashMap<GroupEdge, Integer> groups1 = new HashMap<GroupEdge, Integer>() {{
            put(new GroupEdge("1", true), 0);
            put(new GroupEdge("1", false), 1);
            put(new GroupEdge("2", true), 1);
            put(new GroupEdge("2", false), 3);
            put(new GroupEdge("4", true), 4);
            put(new GroupEdge("4", false), 5);
        }};
        HashMap<GroupEdge, Integer> groups2 = new HashMap<GroupEdge, Integer>() {{
            put(new GroupEdge("3", true), 1);
            put(new GroupEdge("3", false), 3);
            put(new GroupEdge("5", true), 5);
            put(new GroupEdge("5", false), 6);
        }};

        FuzzyMatchPattern pattern1 = new FuzzyMatchPattern(new NucleotideSequence("TAGCC"), groups1);
        FuzzyMatchPattern pattern2 = new FuzzyMatchPattern(new NucleotideSequence("CAGATGCA"), groups2);
        FuzzyMatchPattern pattern3 = new FuzzyMatchPattern(new NucleotideSequence("A"));
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

        assertEquals("1", result.getBestMatch().getMatchedGroupEdge("1", false).getGroupName());
        assertEquals(6, result.getBestMatch().getMatchedGroupEdge("3", false).getPosition());
        assertEquals(9, result.getBestMatch().getMatchedGroupEdge("4", true).getPosition());
        assertTrue(result.getBestMatch().getMatchedGroupEdge("5", true).isStart());
        assertFalse(result.getBestMatch().getMatchedGroupEdge("5", false).isStart());

        assertEquals(5, getGroupsFromMatch(result.getBestMatch()).size());
        for (MatchedGroup group : getGroupsFromMatch(result.getBestMatch())) {
            switch (group.getGroupName()) {
                case "1":
                    assertEquals(new Range(5, 6), group.getRange());
                    break;
                case "2":
                    assertEquals(new Range(6, 8), group.getRange());
                    break;
                case "3":
                    assertEquals(new Range(4, 6), group.getRange());
                    break;
                case "4":
                    assertEquals(new Range(9, 10), group.getRange());
                    break;
                case "5":
                    assertEquals(new Range(8, 9), group.getRange());
                    break;
                default:
                    throw new IllegalStateException("Must not be here!");
            }
        }

        OutputPort<Match> matchOutputPort = result.getMatches();
        for (int i = 0; i < 15; i++)
            assertNotNull(matchOutputPort.take());
        assertNull(matchOutputPort.take());
        for (int i = 0; i < 16; i++)
            assertNotNull(result.getMatches().take());
    }

    @Test
    public void alignmentTest() throws Exception {
        FuzzyMatchPattern fuzzyPattern = new FuzzyMatchPattern(new NucleotideSequence("ATTAGACA"), 2);

        NSequenceWithQuality[] sequences = {
                new NSequenceWithQuality("ATTAGTTA"),
                new NSequenceWithQuality("ATTAGAAG"),
                new NSequenceWithQuality("ACAGACA"),
                new NSequenceWithQuality("ATTTAGAA"),
                new NSequenceWithQuality("TACAGACATTTAGAA")
        };

        MatchingResult[] matchingResults = new MatchingResult[4];
        for (int i = 0; i < 4; i++)
            matchingResults[i] = fuzzyPattern.match(sequences[i]);

        assertEquals(new NSequenceWithQuality("ATTAGTTA"), matchingResults[0].getBestMatch().getValue());
        assertEquals(new NSequenceWithQuality("ATTAGAA"), matchingResults[1].getBestMatch().getValue());
        assertEquals(new NSequenceWithQuality("ACAGACA"), matchingResults[2].getBestMatch().getValue());
        assertEquals(new NSequenceWithQuality("ATTTAGAA"), matchingResults[3].getBestMatch().getValue());

        AndPattern andPattern = new AndPattern(fuzzyPattern, fuzzyPattern);
        PlusPattern plusPattern = new PlusPattern(fuzzyPattern, fuzzyPattern);

        assertEquals(new NSequenceWithQuality("ACAGACATTTAGAA"), andPattern.match(sequences[4]).getBestMatch().getValue());
        assertEquals(new NSequenceWithQuality("ACAGACATTTAGAA"), plusPattern.match(sequences[4]).getBestMatch().getValue());
        assertEquals(new NSequenceWithQuality("ACAGACATTTAGAA"), andPattern.match(sequences[4]).getMatches().take().getValue());
        assertEquals(new NSequenceWithQuality("ACAGACATTTAGAA"), plusPattern.match(sequences[4]).getMatches().take().getValue());

        MultiPattern multiPattern = new MultiPattern(fuzzyPattern, andPattern, plusPattern);
        NotOperator notOperator = new NotOperator(multiPattern);
        OrOperator orOperator = new OrOperator(multiPattern, notOperator, multiPattern);
        AndOperator andOperator = new AndOperator(orOperator, multiPattern, orOperator);

        MultiNSequenceWithQuality mseq = new MultiNSequenceWithQuality() {
            @Override
            public int numberOfSequences() {
                return 3;
            }

            @Override
            public NSequenceWithQuality get(int id) {
                switch (id) {
                    case 0:
                        return sequences[1];
                    case 1:
                    case 2:
                        return sequences[4];
                }
                return null;
            }

        };

        MatchingResult result = andOperator.match(mseq);

        assertEquals(new NSequenceWithQuality("ATTAGAA"), result.getBestMatch().getMatchedRange(0).getValue());
        assertEquals(new NSequenceWithQuality("ACAGACATTTAGAA"), result.getBestMatch().getMatchedRange(1).getValue());
        assertEquals(new NSequenceWithQuality("ACAGACATTTAGAA"), result.getBestMatch().getMatchedRange(2).getValue());
        assertNull(result.getBestMatch().getMatchedRange(3));
        assertEquals(new NSequenceWithQuality("ATTAGAA"), result.getBestMatch().getMatchedRange(14).getValue());

        exception.expect(ArrayIndexOutOfBoundsException.class);
        result.getBestMatch().getMatchedRange(17);
    }
}
