package com.milaboratory.mist.pattern;

import cc.redberry.pipe.OutputPort;
import com.milaboratory.core.Range;
import com.milaboratory.core.sequence.*;
import com.milaboratory.test.TestUtil;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;

import java.util.ArrayList;
import java.util.Random;

import static com.milaboratory.mist.pattern.MatchUtils.countMatches;
import static com.milaboratory.mist.util.CommonTestUtils.getTestPatternAligner;
import static org.junit.Assert.*;

public class MultiPatternTest {
    @Rule
    public final ExpectedException exception = ExpectedException.none();

    @Test
    public void mismatchedReadsAndPatternsTest() throws Exception {
        FuzzyMatchPattern pattern1 = new FuzzyMatchPattern(getTestPatternAligner(), new NucleotideSequence("ATTAGACA"));
        FuzzyMatchPattern pattern2 = new FuzzyMatchPattern(getTestPatternAligner(), new NucleotideSequence("GCGAT"));
        MultiPattern multiPattern = new MultiPattern(getTestPatternAligner(), pattern1, pattern2);
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
        FuzzyMatchPattern pattern = new FuzzyMatchPattern(getTestPatternAligner(), new NucleotideSequence("ATTAGACA"));
        MultiPattern multiPattern = new MultiPattern(getTestPatternAligner(), pattern);
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
        FuzzyMatchPattern pattern = new FuzzyMatchPattern(getTestPatternAligner(), new NucleotideSequence("ATTAGACA"));
        MultiPattern multiPattern = new MultiPattern(getTestPatternAligner(), pattern);
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
        FuzzyMatchPattern pattern = new FuzzyMatchPattern(getTestPatternAligner(), new NucleotideSequence("ATTAGACA"));
        MultiPattern multiPattern = new MultiPattern(getTestPatternAligner(), pattern);
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
        FuzzyMatchPattern pattern1 = new FuzzyMatchPattern(getTestPatternAligner(), new NucleotideSequence("ATTAGACA"));
        FuzzyMatchPattern pattern2 = new FuzzyMatchPattern(getTestPatternAligner(), new NucleotideSequence("GTTATTACCA"));
        AndPattern pattern3 = new AndPattern(getTestPatternAligner(), new FuzzyMatchPattern(getTestPatternAligner(),
                new NucleotideSequence("AT")), new FuzzyMatchPattern(getTestPatternAligner(), new NucleotideSequence("GCAT")));
        MultiPattern multiPattern = new MultiPattern(getTestPatternAligner(), pattern1, pattern2, pattern3);
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
                patterns[s] = new FuzzyMatchPattern(getTestPatternAligner(), motifSeq);
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
            MultiPattern multiPattern = new MultiPattern(getTestPatternAligner(), patterns);
            assertEquals(isMatching, multiPattern.match(mseq).isFound());
            assertEquals(isMatching, multiPattern.match(mseq).getBestMatch() != null);
            assertEquals(isMatching, multiPattern.match(mseq).getMatches(true, false).take() != null);
            assertEquals(isMatching, multiPattern.match(mseq).getMatches(false, false).take() != null);

            /* do fair test sorting only for variants with no more than 10000 combinations
               (because big arrays sorting is too slow) */
            long totalNumberOfCombinations = 1;
            for (int s = 0; s < sequencesNum; s++)
                totalNumberOfCombinations *= countMatches(patterns[s].match(sequences[s]));
            if (totalNumberOfCombinations <= 10000)
                assertEquals(isMatching, multiPattern.match(mseq).getBestMatch(true) != null);
        }
    }

    @Test
    public void groupsTest() throws Exception {
        ArrayList<GroupEdgePosition> groups1 = new ArrayList<GroupEdgePosition>() {{
            add(new GroupEdgePosition(new GroupEdge("ABC", true), 1));
            add(new GroupEdgePosition(new GroupEdge("ABC", false), 3));
            add(new GroupEdgePosition(new GroupEdge("DEF", true), 6));
            add(new GroupEdgePosition(new GroupEdge("DEF", false), 7));
            add(new GroupEdgePosition(new GroupEdge("GH", true), 10));
            add(new GroupEdgePosition(new GroupEdge("GH", false), 11));
        }};

        ArrayList<GroupEdgePosition> groups2 = new ArrayList<GroupEdgePosition>() {{
            add(new GroupEdgePosition(new GroupEdge("XYZ", true), 1));
            add(new GroupEdgePosition(new GroupEdge("XYZ", false), 3));
        }};

        FuzzyMatchPattern pattern1 = new FuzzyMatchPattern(getTestPatternAligner(),
                new NucleotideSequence("ATAGGAGGGTAGCC"), groups1);
        FuzzyMatchPattern pattern2 = new FuzzyMatchPattern(getTestPatternAligner(),
                new NucleotideSequence("TTTTCAATGCATTAG").getReverseComplement(), groups2);
        MultiPattern multiPattern = new MultiPattern(getTestPatternAligner(), pattern1, pattern2);
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

        FuzzyMatchPattern pattern1 = new FuzzyMatchPattern(getTestPatternAligner(),
                new NucleotideSequence("GTGGTTGTGTTGT"), groups1);
        FuzzyMatchPattern pattern2 = new FuzzyMatchPattern(getTestPatternAligner(),
                new NucleotideSequence("GTGGTTGTGTTGT"), groups2);
        exception.expect(IllegalStateException.class);
        new MultiPattern(getTestPatternAligner(), pattern1, pattern2);
    }

    @Test
    public void groupNamesTest2() throws Exception {
        ArrayList<GroupEdgePosition> groups = new ArrayList<GroupEdgePosition>() {{
            add(new GroupEdgePosition(new GroupEdge("ABC", true), 1));
            add(new GroupEdgePosition(new GroupEdge("ABC", false), 3));
            add(new GroupEdgePosition(new GroupEdge("DEF", true), 6));
            add(new GroupEdgePosition(new GroupEdge("DEF", false), 7));
            add(new GroupEdgePosition(new GroupEdge("GH", true), 10));
            add(new GroupEdgePosition(new GroupEdge("GH", false), 11));
        }};

        FuzzyMatchPattern pattern = new FuzzyMatchPattern(getTestPatternAligner(),
                new NucleotideSequence("GTGGTTGTGTTGT"), groups);
        exception.expect(IllegalStateException.class);
        new MultiPattern(getTestPatternAligner(), pattern, pattern);
    }

    @Test
    public void scoringRandomTest() throws Exception {
        int its = TestUtil.its(100, 200);
        for (int i = 0; i < its; ++i) {
            NucleotideSequence motifs[] = new NucleotideSequence[2];
            motifs[0] = TestUtil.randomSequence(NucleotideSequence.ALPHABET, 5, 50);
            motifs[1] = TestUtil.randomSequence(NucleotideSequence.ALPHABET, 5, 50);
            MultiNSequenceWithQuality target = new MultiNSequenceWithQuality() {
                @Override
                public int numberOfSequences() {
                    return 2;
                }

                @Override
                public NSequenceWithQuality get(int id) {
                    return new NSequenceWithQuality(motifs[id], SequenceQuality.getUniformQuality(
                            SequenceQuality.GOOD_QUALITY_VALUE, motifs[id].getSequence().size()));
                }
            };
            FuzzyMatchPattern pattern0 = new FuzzyMatchPattern(getTestPatternAligner(), motifs[0]);
            FuzzyMatchPattern pattern1 = new FuzzyMatchPattern(getTestPatternAligner(), motifs[1]);
            MultiPattern multiPattern0 = new MultiPattern(getTestPatternAligner(), pattern0, pattern1);
            MultiPattern multiPattern1 = new MultiPattern(getTestPatternAligner(), pattern1, pattern0);
            assertEquals(pattern0.match(target.get(0)).getBestMatch().getScore()
                    + pattern1.match(target.get(1)).getBestMatch().getScore(),
                    multiPattern0.match(target).getBestMatch().getScore(), 0.0001);
            if (!motifs[0].toString().equals(motifs[1].toString()))
                assertNull(multiPattern1.match(target).getBestMatch());
        }
    }
}
