package com.milaboratory.mist.pattern;

import cc.redberry.pipe.CUtils;
import cc.redberry.pipe.OutputPort;
import com.milaboratory.core.Range;
import com.milaboratory.core.motif.BitapMatcher;
import com.milaboratory.core.sequence.NSequenceWithQuality;
import com.milaboratory.core.sequence.NucleotideSequence;
import com.milaboratory.core.sequence.SequencesUtils;
import com.milaboratory.test.TestUtil;
import org.junit.Test;

import java.util.ArrayList;
import java.util.Arrays;

import static com.milaboratory.mist.util.CommonTestUtils.*;
import static org.junit.Assert.*;

public class RepeatPatternTest {
    @Test
    public void bestMatchTest() throws Exception {
        RepeatPattern pattern = new RepeatPattern(getTestPatternAligner(true),
                new NucleotideSequence("T"), 3, 6);
        NSequenceWithQuality nseq = new NSequenceWithQuality("TTTATTTTTGTTATTTTTTTATGTTTATGTTTTATGTTA");
        MatchingResult[] results = {
                pattern.match(nseq),
                pattern.match(nseq, 0, 33, (byte)0),
                pattern.match(nseq, 0, 39, (byte)0),
                pattern.match(nseq, 15, 31, (byte)0),
                pattern.match(nseq, new Range(15, 31))
        };
        for (boolean fairSorting : new boolean[] {true, false})
            for (int i = 0; i < results.length; i++) {
                Range expectedRange = (i < 3) ? new Range(13, 19) : new Range(15, 20);
                assertEquals(expectedRange.getLower(), results[i].getBestMatch(fairSorting).getRange().getLower());
                assertEquals(expectedRange.getUpper(), results[i].getBestMatch(fairSorting).getRange().getUpper());
                assertEquals((i < 3) ? "TTTTTT" : "TTTTT", bestToString(results[i], fairSorting));
                assertEquals(nseq, results[i].getBestMatch(fairSorting).getMatchedRange().getTarget());
                assertEquals(true, results[i].isFound());
                assertEquals((i < 3) ? 25 : 7, countMatches(results[i], true));
                assertEquals(1, results[i].getBestMatch(fairSorting).getNumberOfPatterns());
                assertEquals(1, results[i].getBestMatch(fairSorting).getMatchedRanges().size());
                assertEquals(0, results[i].getBestMatch(fairSorting).getMatchedGroupEdges().size());
            }
    }

    @Test
    public void noMatchesTest() throws Exception {
        RepeatPattern pattern = new RepeatPattern(getTestPatternAligner(),
                new NucleotideSequence("TAT"), 3, 4);
        NSequenceWithQuality nseq1 = new NSequenceWithQuality("TATATTATGACA");
        NSequenceWithQuality nseq2 = new NSequenceWithQuality("ATTATTATTAATGTATTATGTTATTATATAGACA");
        ArrayList<MatchingResult> results = new ArrayList<>(Arrays.asList(
                pattern.match(nseq1, 1, 12, (byte)0),
                pattern.match(nseq1, new Range(3, 10), (byte)0),
                pattern.match(nseq2)
        ));
        for (MatchingResult result : results) {
            assertEquals(null, result.getBestMatch());
            assertEquals(null, result.getMatches().take());
            assertEquals(false, result.isFound());
            assertEquals(0, countMatches(result, true));
        }
    }

    @Test
    public void randomMatchTest() throws Exception {
        for (int i = 0; i < 10000; i++) {
            RandomRepeats rr = new RandomRepeats();
            NucleotideSequence seqM = TestUtil.randomSequence(NucleotideSequence.ALPHABET, rr.motifSize, rr.motifSize);
            NucleotideSequence seqL = TestUtil.randomSequence(NucleotideSequence.ALPHABET, 0, 40);
            NucleotideSequence seqR = TestUtil.randomSequence(NucleotideSequence.ALPHABET, 0, 40);
            NucleotideSequence[] seqRepeats = new NucleotideSequence[rr.repeats];
            Arrays.fill(seqRepeats, seqM);
            NucleotideSequence fullSeq = SequencesUtils.concatenate(seqL, SequencesUtils.concatenate(seqRepeats), seqR);
            NSequenceWithQuality target = new NSequenceWithQuality(fullSeq.toString());
            RepeatPattern pattern = new RepeatPattern(getTestPatternAligner(), seqM, rr.minRepeats, rr.maxRepeats);
            assertTrue(pattern.match(target).isFound());
            assertNotNull(pattern.match(target).getBestMatch(rg.nextBoolean()));
            assertNotNull(pattern.match(target).getMatches(rg.nextBoolean(), rg.nextBoolean()).take());
        }
    }

    @Test
    public void randomTest() throws Exception {
        for (int i = 0; i < 10000; i++) {
            RandomRepeats rr = new RandomRepeats();
            NucleotideSequence target = TestUtil.randomSequence(NucleotideSequence.ALPHABET, 1, 1000);
            NucleotideSequence motif = TestUtil.randomSequence(NucleotideSequence.ALPHABET, rr.motifSize, rr.motifSize);
            NSequenceWithQuality targetQ = new NSequenceWithQuality(target.toString());
            RepeatPattern pattern = new RepeatPattern(getTestPatternAligner(), motif, rr.minRepeats, rr.maxRepeats);
            boolean isMatching = target.toString().contains(repeatString(motif.toString(), rr.minRepeats));
            assertEquals(isMatching, pattern.match(targetQ).isFound());
            assertEquals(isMatching, pattern.match(targetQ).getBestMatch(rg.nextBoolean()) != null);
            assertEquals(isMatching, pattern.match(targetQ).getMatches(rg.nextBoolean(),
                    rg.nextBoolean()).take() != null);
        }
    }

    @Test
    public void multipleMatchesTest() throws Exception {
        RepeatPattern pattern = new RepeatPattern(getTestPatternAligner(), new NucleotideSequence("TGT"),
                2, 4);
        NSequenceWithQuality nseq = new NSequenceWithQuality("ATAGGAATGTTGTTGTTGTTGTGTATAAAGGACCCAGAGCCCCATGTTGTAGTGTC");
        MatchingResult result = pattern.match(nseq);
        Match bestMatch1 = result.getBestMatch();
        Match firstMatchByScore = result.getMatches(true, true).take();
        Match bestMatch2 = result.getBestMatch();
        Match firstMatchByCoordinate = result.getMatches(false, true).take();
        Match bestMatch3 = result.getBestMatch();
        assertEquals(bestMatch1.getRange(), bestMatch2.getRange());
        assertEquals(bestMatch1.getRange(), bestMatch3.getRange());
        assertEquals(bestMatch1.getRange(), firstMatchByScore.getRange());
        assertEquals(bestMatch1.getRange(), firstMatchByCoordinate.getRange());
        assertEquals(true, result.isFound());
        assertEquals(10, countMatches(result, true));
        assertEquals(10, countMatches(result, false));
        result = pattern.match(nseq);
        OutputPort<Match> matches = result.getMatches(false, true);
        assertEquals(new Range(7, 19), matches.take().getRange());
        assertEquals("TGTTGTTGT", matches.take().getValue().getSequence().toString());
        assertEquals(new Range(7, 13), matches.take().getMatchedRanges().get(0).getRange());
        assertEquals(new Range(10, 22), matches.take().getMatchedRange(0).getRange());
    }

    @Test
    public void groupEdgeOutsideOfMotifTest() throws Exception {
        for (int i = 0; i < 1000; i++) {
            RandomRepeats rr = new RandomRepeats();
            ArrayList<GroupEdgePosition> groups = getRandomGroupsForFuzzyMatch(100);
            NucleotideSequence motif = TestUtil.randomSequence(NucleotideSequence.ALPHABET, rr.motifSize, rr.motifSize);
            RepeatPattern pattern = new RepeatPattern(getTestPatternAligner(), motif, rr.minRepeats, rr.maxRepeats, groups);
            Match match = pattern.match(new NSequenceWithQuality(repeatString(motif.toString(), rr.repeats))).getBestMatch();
            for (MatchedGroupEdge matchedGroupEdge : match.getMatchedGroupEdges())
                assertTrue(matchedGroupEdge.getPosition() <= motif.size() * rr.repeats);
        }
    }

    @Test
    public void masksTest() throws Exception {
        for (int i = 0; i < 10000; i++) {
            RandomRepeats rr = new RandomRepeats();
            NucleotideSequence target = TestUtil.randomSequence(NucleotideSequence.ALPHABET, 1, 1000);
            NucleotideSequence motif = TestUtil.randomSequence(NucleotideSequence.ALPHABET, rr.motifSize, rr.motifSize,
                    false);
            NucleotideSequence repeatedMotif = new NucleotideSequence(repeatString(motif.toString(), rr.minRepeats));
            NSequenceWithQuality targetQ = new NSequenceWithQuality(target.toString());
            RepeatPattern pattern = new RepeatPattern(getTestPatternAligner(), motif, rr.minRepeats, rr.maxRepeats);
            BitapMatcher matcher = repeatedMotif.toMotif().getBitapPattern().exactMatcher(target.getSequence(),
                    0, target.size());
            boolean isMatching = (matcher.findNext() != -1);
            assertEquals(isMatching, pattern.match(targetQ).isFound());
        }
    }

    @Test
    public void scoringTest() throws Exception {
        RepeatPattern[] patterns = {
                new RepeatPattern(getTestPatternAligner(0), new NucleotideSequence("TGCA"),
                        3, 5),
                new RepeatPattern(getTestPatternAligner(1), new NucleotideSequence("TGCA"),
                        3, 5),
                new RepeatPattern(getTestPatternAligner(0), new NucleotideSequence("TA"),
                        1, 4)
        };
        NSequenceWithQuality[] sequences = {
                new NSequenceWithQuality("TTAGACTTACCAGGAGCAGTTTGCATGCATGCAAGA"),
                new NSequenceWithQuality("AGACTTAGACCTCATGCATGCAGACTGCATGCATGCAGACA"),
                new NSequenceWithQuality("TGCATGCAATGCATGCA")
        };

        MatchingResult[][] matchingResults = new MatchingResult[3][3];
        Match previousMatch;
        for (int i = 0; i < 3; i++)
            for (int j = 0; j < 3; j++) {
                matchingResults[i][j] = patterns[i].match(sequences[j]);
                previousMatch = null;
                for (Match currentMatch : CUtils.it(matchingResults[i][j].getMatches(true, true))) {
                    if (previousMatch != null)
                        assertTrue(currentMatch.getScore() <= previousMatch.getScore());
                    previousMatch = currentMatch;
                }
            }

        for (boolean fairSorting : new boolean[] {true, false}) {
            assertEquals(0, matchingResults[0][0].getBestMatch(fairSorting).getScore());
            assertEquals(0, matchingResults[1][0].getBestMatch(fairSorting).getScore());
            assertEquals(0, matchingResults[2][0].getBestMatch(fairSorting).getScore());
            assertEquals(0, matchingResults[1][1].getBestMatch(fairSorting).getScore());
            assertEquals(-10, matchingResults[1][2].getBestMatch(fairSorting).getScore());
        }
    }

    @Test
    public void fixedBordersTest() throws Exception {
        PatternAligner patternAligner = getTestPatternAligner(1);
        RepeatPattern pattern1 = new RepeatPattern(patternAligner, new NucleotideSequence("TGGA"), 2, 3,
                2, -1, getRandomGroupsForFuzzyMatch(7));
        RepeatPattern pattern2 = new RepeatPattern(patternAligner, new NucleotideSequence("TGGA"), 2, 6,
                -1, 11, getRandomGroupsForFuzzyMatch(3));
        RepeatPattern pattern3 = new RepeatPattern(patternAligner, new NucleotideSequence("TGGA"), 2, 4,
                3, 9, getRandomGroupsForFuzzyMatch(9, 18));
        NSequenceWithQuality target1_1 = new NSequenceWithQuality("GTGGATGGA");
        NSequenceWithQuality target1_2 = new NSequenceWithQuality("TGGATGGA");
        NSequenceWithQuality target2_1 = new NSequenceWithQuality("GTCGATGGATGGATTA");
        NSequenceWithQuality target2_2 = new NSequenceWithQuality("GTACGATGGATGGATTA");
        NSequenceWithQuality target3_1 = new NSequenceWithQuality("TTATGGATGGATTA");
        NSequenceWithQuality target3_2 = new NSequenceWithQuality("TATGGATGGATTA");
        NSequenceWithQuality target3_3 = new NSequenceWithQuality("TTGGATGGATTA");
        for (boolean fairSorting : new boolean[] {true, false}) {
            assertEquals("GGATGGA", bestToString(pattern1.match(target1_1), fairSorting));
            assertNull(pattern1.match(target1_2).getBestMatch(fairSorting));
            assertEquals("GGATGGA", bestToString(pattern1.match(target1_1, 2, 9), fairSorting));
            assertNull(pattern1.match(target1_1, 2, 8).getBestMatch(fairSorting));

            assertEquals("ATGGATGG", bestToString(pattern2.match(target2_1), fairSorting));
            assertNull(pattern2.match(target2_2).getBestMatch(fairSorting));
            assertEquals("ATGGATGG", bestToString(pattern2.match(target2_1, 5, 12), fairSorting));
            assertNull(pattern2.match(target2_1, 6, 12).getBestMatch(fairSorting));

            assertEquals("TGGATGG", bestToString(pattern3.match(target3_1), fairSorting));
            assertEquals("GGATGGA", bestToString(pattern3.match(target3_2), fairSorting));
            assertNull(pattern3.match(target3_3).getBestMatch(fairSorting));
            assertEquals("TGGATGG", bestToString(pattern3.match(target3_1, 3, 10), fairSorting));
            assertNull(pattern3.match(target3_1, 4, 10).getBestMatch(fairSorting));
            assertNull(pattern3.match(target3_1, 3, 9).getBestMatch(fairSorting));
            assertNull(pattern3.match(target3_1, 4, 9).getBestMatch(fairSorting));
        }
    }
}
