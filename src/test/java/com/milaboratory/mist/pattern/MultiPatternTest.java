package com.milaboratory.mist.pattern;

import cc.redberry.pipe.OutputPort;
import com.milaboratory.core.Range;
import com.milaboratory.core.sequence.MultiNSequenceWithQuality;
import com.milaboratory.core.sequence.NSequenceWithQuality;
import com.milaboratory.core.sequence.NucleotideSequence;
import com.milaboratory.core.sequence.SequenceQuality;
import com.milaboratory.test.TestUtil;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;

import java.util.HashMap;
import java.util.Random;

import static org.junit.Assert.*;

public class MultiPatternTest {
    @Rule
    public final ExpectedException exception = ExpectedException.none();

    @Test
    public void mismatchedReadsAndPatternsTest() throws Exception {
        FuzzyMatchPattern pattern1 = new FuzzyMatchPattern(new NucleotideSequence("ATTAGACA"));
        FuzzyMatchPattern pattern2 = new FuzzyMatchPattern(new NucleotideSequence("GCGAT"));
        MultiPattern multiPattern = new MultiPattern(pattern1, pattern2);
        MultiNSequenceWithQuality mseq = new MultiNSequenceWithQuality() {
            @Override
            public int numberOfSequences() {
                return 1;
            }

            @Override
            public NSequenceWithQuality get(int id) {
                return new NSequenceWithQuality("AT");
            }
        };

        exception.expect(IllegalArgumentException.class);
        multiPattern.match(mseq);
    }

    @Test
    public void mismatchedReadsAndRangesTest() throws Exception {
        FuzzyMatchPattern pattern = new FuzzyMatchPattern(new NucleotideSequence("ATTAGACA"));
        MultiPattern multiPattern = new MultiPattern(pattern);
        MultiNSequenceWithQuality mseq = new MultiNSequenceWithQuality() {
            @Override
            public int numberOfSequences() {
                return 3;
            }

            @Override
            public NSequenceWithQuality get(int id) {
                return new NSequenceWithQuality("AT");
            }
        };

        exception.expect(IllegalArgumentException.class);
        multiPattern.match(mseq, new Range(0, 2), new Range(2, 3));
    }

    @Test
    public void mismatchedReadsAndComplementsTest1() throws Exception {
        FuzzyMatchPattern pattern = new FuzzyMatchPattern(new NucleotideSequence("ATTAGACA"));
        MultiPattern multiPattern = new MultiPattern(pattern);
        MultiNSequenceWithQuality mseq = new MultiNSequenceWithQuality() {
            @Override
            public int numberOfSequences() {
                return 1;
            }

            @Override
            public NSequenceWithQuality get(int id) {
                return new NSequenceWithQuality("AT");
            }
        };

        exception.expect(IllegalArgumentException.class);
        multiPattern.match(mseq, new Range[]{new Range(0, 2)}, new boolean[]{false, false});
    }

    @Test
    public void mismatchedReadsAndComplementsTest2() throws Exception {
        FuzzyMatchPattern pattern = new FuzzyMatchPattern(new NucleotideSequence("ATTAGACA"));
        MultiPattern multiPattern = new MultiPattern(pattern);
        MultiNSequenceWithQuality mseq = new MultiNSequenceWithQuality() {
            @Override
            public int numberOfSequences() {
                return 3;
            }

            @Override
            public NSequenceWithQuality get(int id) {
                return new NSequenceWithQuality("AT");
            }
        };

        exception.expect(IllegalArgumentException.class);
        multiPattern.match(mseq, false, false);
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
                        return new NSequenceWithQuality("ACAATTAGACA");
                    case 1:
                        return new NSequenceWithQuality("GTTATTACCA").getReverseComplement();
                    case 2:
                        return new NSequenceWithQuality("AACTTGCATAT").getReverseComplement();
                }
                return null;
            }
        };
        assertTrue(multiPattern.match(mseq, new Range[]{new Range(0, 11),
                new Range(0, 10, true), new Range(0, 11, true)},
                new boolean[]{false, true, true}).isFound());
        assertTrue(multiPattern.match(mseq, false, true, true).isFound());
        assertFalse(multiPattern.match(mseq, new Range[]{new Range(0, 11),
                        new Range(1, 10, true), new Range(0, 11, true)},
                new boolean[]{false, true, true}).isFound());
        assertFalse(multiPattern.match(mseq, false, true, false).isFound());
        assertEquals("GCATAT", multiPattern.match(mseq, false, true, true)
                .getMatches().take().getMatchedRange(2).getValue().getSequence().toString());
        assertNull(multiPattern.match(mseq).getBestMatch());
        assertNotNull(multiPattern.match(mseq, false, true, true).getBestMatch());
        assertTrue(multiPattern.match(mseq, new Range(0, 11), new Range(0, 10, true),
                new Range(0, 11, true)).isFound());
    }

    @Test
    public void randomTest() throws Exception {
        int its = TestUtil.its(500, 1000);
        for (int i = 0; i < its; ++i) {
            int sequencesNum = new Random().nextInt(9) + 1;
            NSequenceWithQuality[] sequences = new NSequenceWithQuality[sequencesNum];
            FuzzyMatchPattern[] patterns = new FuzzyMatchPattern[sequencesNum];
            boolean isMatching = true;
            for (int s = 0; s < sequencesNum; s++) {
                NucleotideSequence seq = TestUtil.randomSequence(NucleotideSequence.ALPHABET, 1, 1000);
                NucleotideSequence motifSeq = TestUtil.randomSequence(NucleotideSequence.ALPHABET, 1, 5);
                NSequenceWithQuality seqQ = new NSequenceWithQuality(seq, SequenceQuality
                        .getUniformQuality(SequenceQuality.GOOD_QUALITY_VALUE, seq.getSequence().size()));
                sequences[s] = seqQ;
                patterns[s] = new FuzzyMatchPattern(motifSeq);
                isMatching = isMatching && seq.toString().contains(motifSeq.toString());
            }
            MultiNSequenceWithQuality mseq = new MultiNSequenceWithQuality() {
                @Override
                public int numberOfSequences() {
                    return sequencesNum;
                }

                @Override
                public NSequenceWithQuality get(int id) {
                    return sequences[id];
                }
            };
            MultiPattern multiPattern = new MultiPattern(patterns);
            assertEquals(isMatching, multiPattern.match(mseq).isFound());
            assertEquals(isMatching, multiPattern.match(mseq).getBestMatch() != null);
            assertEquals(isMatching, multiPattern.match(mseq).getMatches(true, false).take() != null);
            assertEquals(isMatching, multiPattern.match(mseq).getMatches(false, false).take() != null);
            // this test is slow, do it only 3 times
            if (i < 3)
                assertEquals(isMatching, multiPattern.match(mseq).getBestMatch(true) != null);
        }
    }

    @Test
    public void groupsTest() throws Exception {
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
        }};

        FuzzyMatchPattern pattern1 = new FuzzyMatchPattern(new NucleotideSequence("ATAGGAGGGTAGCC"), groups1);
        FuzzyMatchPattern pattern2 = new FuzzyMatchPattern(new NucleotideSequence("TTTTCAATGCATTAG").getReverseComplement(), groups2);
        MultiPattern multiPattern = new MultiPattern(pattern1, pattern2);
        MultiNSequenceWithQuality mseq = new MultiNSequenceWithQuality() {
            @Override
            public int numberOfSequences() {
                return 2;
            }

            @Override
            public NSequenceWithQuality get(int id) {
                switch (id) {
                    case 0:
                        return new NSequenceWithQuality("ATAGGAGGGTAGCCACAATTAGCCA");
                    case 1:
                        return new NSequenceWithQuality("GTGCATCTGCCATTTTCAATGCATTAG");
                }
                return null;
            }
        };
        MatchingResult result = multiPattern.match(mseq, false, true);
        OutputPort<Match> matchOutputPort = result.getMatches();
        assertEquals("ABC", result.getBestMatch().getMatchedGroupEdge("ABC", false).getGroupName());
        assertEquals(11, result.getBestMatch().getMatchedGroupEdge("GH", false).getPosition());
        assertEquals(1, matchOutputPort.take().getMatchedGroupEdge("XYZ", true).getPosition());
        assertNull(matchOutputPort.take());
    }

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
        new MultiPattern(pattern1, pattern2);
    }

    @Test
    public void groupNamesTest2() throws Exception {
        HashMap<GroupEdge, Integer> groups = new HashMap<GroupEdge, Integer>() {{
            put(new GroupEdge("ABC", true), 1);
            put(new GroupEdge("ABC", false), 3);
            put(new GroupEdge("DEF", true), 6);
            put(new GroupEdge("DEF", false), 7);
            put(new GroupEdge("GH", true), 10);
            put(new GroupEdge("GH", false), 11);
        }};
        FuzzyMatchPattern pattern = new FuzzyMatchPattern(new NucleotideSequence("GTGGTTGTGTTGT"), groups);
        exception.expect(IllegalStateException.class);
        new MultiPattern(pattern, pattern);
    }
}
