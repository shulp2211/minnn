package com.milaboratory.mist.pattern;

import com.milaboratory.core.Range;
import com.milaboratory.core.sequence.MultiNSequenceWithQuality;
import com.milaboratory.core.sequence.NSequenceWithQuality;
import com.milaboratory.core.sequence.NucleotideSequence;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;

import static org.junit.Assert.*;

public class LogicalOperatorsTest {
    @Rule
    public final ExpectedException exception = ExpectedException.none();

    @Test
    public void logicTest() throws Exception {
        PerfectMatchPattern pattern1 = new PerfectMatchPattern(new NucleotideSequence("ATTAGACA").toMotif());
        PerfectMatchPattern pattern2 = new PerfectMatchPattern(new NucleotideSequence("GTTATTACCA").toMotif());
        AndPattern pattern3 = new AndPattern(new PerfectMatchPattern(new NucleotideSequence("AT").toMotif()),
                new PerfectMatchPattern(new NucleotideSequence("GCAT").toMotif()));
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
        PerfectMatchPattern pattern1 = new PerfectMatchPattern(new NucleotideSequence("ATTAGACA").toMotif());
        PerfectMatchPattern pattern2 = new PerfectMatchPattern(new NucleotideSequence("GTTATTACCA").toMotif());
        AndPattern pattern3 = new AndPattern(new PerfectMatchPattern(new NucleotideSequence("AT").toMotif()),
                new PerfectMatchPattern(new NucleotideSequence("GCAT").toMotif()));
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

        Match testMatch = andTrueResultR.getMatches().take();
        assertEquals("GTTATTACCA", testMatch.getWholePatternMatch(5).getValue().getSequence().toString());
        assertEquals("GCATAT", testMatch.getWholePatternMatch(6).getValue().getSequence().toString());

        exception.expect(IllegalStateException.class);
        new NotOperator(orOperatorTrue, orOperatorFalse);
    }
}
