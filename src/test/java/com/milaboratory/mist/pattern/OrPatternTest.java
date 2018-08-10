/*
 * Copyright (c) 2016-2018, MiLaboratory LLC
 * All Rights Reserved
 *
 * Permission to use, copy, modify and distribute any part of this program for
 * educational, research and non-profit purposes, by non-profit institutions
 * only, without fee, and without a written agreement is hereby granted,
 * provided that the above copyright notice, this paragraph and the following
 * three paragraphs appear in all copies.
 *
 * Those desiring to incorporate this work into commercial products or use for
 * commercial purposes should contact MiLaboratory LLC, which owns exclusive
 * rights for distribution of this program for commercial purposes, using the
 * following email address: licensing@milaboratory.com.
 *
 * IN NO EVENT SHALL THE INVENTORS BE LIABLE TO ANY PARTY FOR DIRECT, INDIRECT,
 * SPECIAL, INCIDENTAL, OR CONSEQUENTIAL DAMAGES, INCLUDING LOST PROFITS,
 * ARISING OUT OF THE USE OF THIS SOFTWARE, EVEN IF THE INVENTORS HAS BEEN
 * ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 *
 * THE SOFTWARE PROVIDED HEREIN IS ON AN "AS IS" BASIS, AND THE INVENTORS HAS
 * NO OBLIGATION TO PROVIDE MAINTENANCE, SUPPORT, UPDATES, ENHANCEMENTS, OR
 * MODIFICATIONS. THE INVENTORS MAKES NO REPRESENTATIONS AND EXTENDS NO
 * WARRANTIES OF ANY KIND, EITHER IMPLIED OR EXPRESS, INCLUDING, BUT NOT
 * LIMITED TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY OR FITNESS FOR A
 * PARTICULAR PURPOSE, OR THAT THE USE OF THE SOFTWARE WILL NOT INFRINGE ANY
 * PATENT, TRADEMARK OR OTHER RIGHTS.
 */
package com.milaboratory.mist.pattern;

import cc.redberry.pipe.OutputPort;
import com.milaboratory.core.Range;
import com.milaboratory.core.sequence.*;
import com.milaboratory.test.TestUtil;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;

import java.util.ArrayList;

import static com.milaboratory.mist.util.CommonTestUtils.*;
import static org.junit.Assert.*;

public class OrPatternTest {
    @Rule
    public final ExpectedException exception = ExpectedException.none();

    @Test
    public void simpleTest() throws Exception {
        FuzzyMatchPattern pattern1 = new FuzzyMatchPattern(getTestPatternAligner(),
                new NucleotideSequenceCaseSensitive("attagaca"));
        FuzzyMatchPattern pattern2 = new FuzzyMatchPattern(getTestPatternAligner(),
                new NucleotideSequenceCaseSensitive("tttag"));
        FuzzyMatchPattern pattern3 = new FuzzyMatchPattern(getTestPatternAligner(),
                new NucleotideSequenceCaseSensitive("agta"));
        FuzzyMatchPattern pattern4 = new FuzzyMatchPattern(getTestPatternAligner(),
                new NucleotideSequenceCaseSensitive("agtag"));
        NSequenceWithQuality nseq1 = new NSequenceWithQuality("ACTGCGATAAATTAGACGTACGTAA");
        NSequenceWithQuality nseq2 = new NSequenceWithQuality("TTAGTAGAGTATTTAGAGA");
        NSequenceWithQuality nseq3 = new NSequenceWithQuality("ATTAGACAAGTAATTAGACATTAG");
        OrPattern orPattern1 = new OrPattern(getTestPatternAligner(), pattern1, pattern3);
        OrPattern orPattern2 = new OrPattern(getTestPatternAligner(), pattern2, pattern3);
        OrPattern orPattern3 = new OrPattern(getTestPatternAligner(), pattern2, pattern1, pattern3);
        OrPattern orPattern4 = new OrPattern(getTestPatternAligner(), pattern4);
        OrPattern orPattern5 = new OrPattern(getTestPatternAligner());
        OrPattern orPattern6 = new OrPattern(getTestPatternAligner(), pattern1);

        assertEquals(false, orPattern1.match(nseq1).isFound());
        assertEquals(false, orPattern1.match(nseq1, 0, 25).isFound());
        assertEquals(false, orPattern1.match(nseq1, new Range(0, 25)).isFound());
        assertEquals(true, orPattern2.match(nseq3).isFound());
        assertEquals(true, orPattern2.match(nseq3, 0, 24).isFound());
        assertEquals(true, orPattern2.match(nseq3, new Range(0, 24)).isFound());
        assertEquals(false, orPattern4.match(nseq3, new Range(0, 24)).isFound());
        assertEquals(true, orPattern3.match(nseq3, new Range(0, 24)).isFound());
        assertEquals(false, orPattern3.match(nseq1).isFound());
        assertEquals(false, orPattern6.match(nseq2).isFound());
        assertEquals(true, orPattern6.match(nseq3).isFound());
        assertEquals(false, orPattern2.match(nseq3, new Range(12, 21)).isFound());

        assertEquals(new Range(0, 8), orPattern3.match(nseq3, new Range(0, 24)).getBestMatch().getRange());
        assertEquals(new Range(2, 6),
                orPattern2.match(nseq2, new Range(1, 8)).getMatches(true).take().getRange());
        assertEquals(null, orPattern2.match(nseq3, new Range(12, 21)).getBestMatch());

        exception.expect(IllegalArgumentException.class);
        orPattern5.match(nseq1).getBestMatch();
    }

    @Test
    public void randomMatchTest() throws Exception {
        for (int i = 0; i < 10000; i++) {
            NucleotideSequenceCaseSensitive seqLeft = TestUtil.randomSequence(
                    NucleotideSequenceCaseSensitive.ALPHABET, 0, 40);
            NucleotideSequenceCaseSensitive seqMotif1 = TestUtil.randomSequence(
                    NucleotideSequenceCaseSensitive.ALPHABET, 1, 60);
            NucleotideSequenceCaseSensitive seqMotif2 = TestUtil.randomSequence(
                    NucleotideSequenceCaseSensitive.ALPHABET, 1, 60);
            NucleotideSequenceCaseSensitive seqRight = TestUtil.randomSequence(
                    NucleotideSequenceCaseSensitive.ALPHABET, 0, 40);
            NucleotideSequenceCaseSensitive fullSeq = SequencesUtils.concatenate(seqLeft, seqMotif1, seqRight);
            NSequenceWithQuality target = new NSequenceWithQuality(fullSeq.toString());
            FuzzyMatchPattern patternMotif1 = new FuzzyMatchPattern(getTestPatternAligner(), seqMotif1);
            FuzzyMatchPattern patternMotif2 = new FuzzyMatchPattern(getTestPatternAligner(), seqMotif2);
            OrPattern orPattern1 = new OrPattern(getTestPatternAligner(), patternMotif2, patternMotif1);
            OrPattern orPattern2 = new OrPattern(getTestPatternAligner(), patternMotif1, patternMotif2);
            OrPattern orPattern3 = new OrPattern(getTestPatternAligner(), patternMotif1, patternMotif1);
            assertTrue(orPattern1.match(target).isFound());
            assertTrue(orPattern2.match(target).isFound());
            assertTrue(orPattern3.match(target).isFound());
            assertNotNull(orPattern1.match(target).getBestMatch(false));
            assertNotNull(orPattern1.match(target).getMatches(false).take());
            assertNotNull(orPattern1.match(target).getBestMatch(true));
            assertNotNull(orPattern1.match(target).getMatches(true).take());

            NSequenceWithQuality foundSequence = orPattern1.match(target).getBestMatch(true).getValue();
            assertTrue(foundSequence.getSequence().toString().equals(
                    patternMotif1.match(target).getBestMatch(true).getValue().getSequence().toString())
                    || foundSequence.getSequence().toString().equals(
                    patternMotif2.match(target).getBestMatch(true).getValue().getSequence().toString()));

            foundSequence = orPattern1.match(target).getBestMatch(false).getValue();
            assertTrue(foundSequence.getSequence().toString().equals(
                    patternMotif1.match(target).getBestMatch(false).getValue().getSequence().toString())
                    || foundSequence.getSequence().toString().equals(
                    patternMotif2.match(target).getBestMatch(false).getValue().getSequence().toString()));
        }
    }

    @Test
    public void allMatchesTest() throws Exception {
        FuzzyMatchPattern pattern1 = new FuzzyMatchPattern(getTestPatternAligner(),
                new NucleotideSequenceCaseSensitive("atta"));
        FuzzyMatchPattern pattern2 = new FuzzyMatchPattern(getTestPatternAligner(),
                new NucleotideSequenceCaseSensitive("gaca"));
        NSequenceWithQuality nseq = new NSequenceWithQuality("GACATTATTATTAGACAGACATTAGACATTATTAGACAGACATTAATTA");
        OrPattern orPattern1 = new OrPattern(getTestPatternAligner(), pattern1, pattern2);
        OrPattern orPattern2 = new OrPattern(getTestPatternAligner(), pattern1, pattern1, pattern2);
        assertNotNull(orPattern1.match(nseq).getBestMatch());
        assertNotNull(orPattern2.match(nseq).getBestMatch());
        assertEquals(48, countMatches(orPattern1.match(nseq), true));
        assertEquals(384, countMatches(orPattern2.match(nseq), true));
        OutputPort<MatchIntermediate> matchesPattern1 = orPattern1.match(nseq).getMatches(true);
        OutputPort<MatchIntermediate> matchesPattern2 = orPattern2.match(nseq).getMatches(true);
        for (int i = 0; i < 48; i++) {
            assertNotNull(matchesPattern1.take().getValue());
        }
        assertNull(matchesPattern1.take());
        for (int i = 0; i < 384; i++) {
            assertNotNull(matchesPattern2.take().getValue());
        }
        assertNull(matchesPattern2.take());
    }

    @Test
    public void quickSearchTest() throws Exception {
        FuzzyMatchPattern pattern1 = new FuzzyMatchPattern(getTestPatternAligner(),
                new NucleotideSequenceCaseSensitive("ata"));
        FuzzyMatchPattern pattern2 = new FuzzyMatchPattern(getTestPatternAligner(),
                new NucleotideSequenceCaseSensitive("gat"));
        OrPattern orPattern = new OrPattern(getTestPatternAligner(), pattern1, pattern2);
        NSequenceWithQuality nseq1 = new NSequenceWithQuality("ATATATATTATA");
        NSequenceWithQuality nseq2 = new NSequenceWithQuality("GCGGTGCGTAGCG");
        MatchingResult match1 = orPattern.match(nseq1);
        MatchingResult match2 = orPattern.match(nseq2);
        assertEquals(true, match1.isFound());
        assertEquals(false, match2.isFound());
        assertEquals(4, countMatches(match1, true));
        assertEquals(0, countMatches(match2, true));
        match1 = orPattern.match(nseq1);
        match2 = orPattern.match(nseq2);
        assertEquals(true, match1.isFound());
        assertEquals(false, match2.isFound());
        assertNotNull(match1.getMatches(true).take());
        assertNotNull(match1.getMatches().take());
        assertNotNull(match2.getMatches(true));
        assertNotNull(match2.getMatches());
        assertNull(match2.getMatches().take());
        assertEquals(4, countMatches(match1, true));
        assertEquals(0, countMatches(match2, true));
    }

    @Test
    public void groupsTest() throws Exception {
        ArrayList<GroupEdgePosition> groupEdgePositions = new ArrayList<GroupEdgePosition>() {{
            add(new GroupEdgePosition(new GroupEdge("1", true), 0));
            add(new GroupEdgePosition(new GroupEdge("1", false), 1));
            add(new GroupEdgePosition(new GroupEdge("2", true), 1));
            add(new GroupEdgePosition(new GroupEdge("2", false), 3));
            add(new GroupEdgePosition(new GroupEdge("4", true), 4));
            add(new GroupEdgePosition(new GroupEdge("4", false), 5));
        }};

        FuzzyMatchPattern pattern1 = new FuzzyMatchPattern(getTestPatternAligner(),
                new NucleotideSequenceCaseSensitive("tagcc"), groupEdgePositions);
        FuzzyMatchPattern pattern2 = new FuzzyMatchPattern(getTestPatternAligner(),
                new NucleotideSequenceCaseSensitive("cagatgca"), groupEdgePositions);
        OrPattern orPattern = new OrPattern(getTestPatternAligner(), pattern1, pattern2);
        NSequenceWithQuality nseq = new NSequenceWithQuality("AAACAGATGCAGACATAGC");
        MatchingResult result = orPattern.match(nseq);
        OutputPort<MatchIntermediate> matchOutputPort = result.getMatches(true);
        Match match = matchOutputPort.take();
        assertEquals(4, match.getMatchedGroupEdge("2", true).getPosition());
        assertEquals(4, match.getMatchedGroupEdge("1", false).getPosition());
        assertEquals("4", match.getMatchedGroupEdge("4", true).getGroupName());
        assertNull(matchOutputPort.take());
    }

    @Test
    public void maxErrorsRandomTest() throws Exception {
        for (int i = 0; i < 2000; i++) {
            int targetLength = rg.nextInt(100) + 1;
            NucleotideSequence target = TestUtil.randomSequence(NucleotideSequence.ALPHABET,
                    targetLength, targetLength);
            NucleotideSequenceCaseSensitive motif1 = TestUtil.randomSequence(NucleotideSequenceCaseSensitive.ALPHABET,
                    1, 50);
            NucleotideSequenceCaseSensitive motif2 = TestUtil.randomSequence(NucleotideSequenceCaseSensitive.ALPHABET,
                    1, 50);
            NSequenceWithQuality targetQ = new NSequenceWithQuality(target.toString());
            int maxErrors = rg.nextInt(10);
            NucleotideSequenceCaseSensitive motif1WithErrors = makeRandomErrors(motif1, maxErrors);
            NucleotideSequenceCaseSensitive motif2WithErrors = makeRandomErrors(motif2, maxErrors);
            FuzzyMatchPattern pattern1 = new FuzzyMatchPattern(getTestPatternAligner(maxErrors), motif1WithErrors);
            FuzzyMatchPattern pattern2 = new FuzzyMatchPattern(getTestPatternAligner(maxErrors), motif2WithErrors);
            boolean targetContainsPattern1 = target.toString().contains(motif1.toString());
            boolean targetContainsPattern2 = target.toString().contains(motif2.toString());
            boolean isMatchingPattern1 = pattern1.match(targetQ).isFound();
            boolean isMatchingPattern2 = pattern2.match(targetQ).isFound();

            if (targetContainsPattern1) {
                assertTrue(pattern1.match(targetQ).isFound());
                assertTrue(pattern1.match(targetQ).getBestMatch(false) != null);
                assertTrue(pattern1.match(targetQ).getMatches(false).take() != null);
                assertTrue(pattern1.match(targetQ).getBestMatch(true) != null);
                assertTrue(pattern1.match(targetQ).getMatches(true).take() != null);
            }

            if (targetContainsPattern2) {
                assertTrue(pattern2.match(targetQ).isFound());
                assertTrue(pattern2.match(targetQ).getBestMatch(false) != null);
                assertTrue(pattern2.match(targetQ).getMatches(false).take() != null);
                assertTrue(pattern2.match(targetQ).getBestMatch(true) != null);
                assertTrue(pattern2.match(targetQ).getMatches(true).take() != null);
            }

            OrPattern orPattern = new OrPattern(getTestPatternAligner(), pattern1, pattern2);
            boolean orMustBeMatching = isMatchingPattern1 || isMatchingPattern2;

            assertEquals(orMustBeMatching, orPattern.match(targetQ).isFound());
            assertEquals(orMustBeMatching, orPattern.match(targetQ).getBestMatch(false) != null);
            assertEquals(orMustBeMatching, orPattern.match(targetQ).getMatches(false).take() != null);
            assertEquals(orMustBeMatching, orPattern.match(targetQ).getBestMatch(true) != null);
            assertEquals(orMustBeMatching, orPattern.match(targetQ).getMatches(true).take() != null);
        }
    }

    @Test
    public void scoringRandomTest() throws Exception {
        for (int i = 0; i < 1000; i++) {
            NucleotideSequenceCaseSensitive motif1 = TestUtil.randomSequence(NucleotideSequenceCaseSensitive.ALPHABET,
                    5, 50);
            NucleotideSequenceCaseSensitive motif2 = TestUtil.randomSequence(NucleotideSequenceCaseSensitive.ALPHABET,
                    5, 50);
            NucleotideSequenceCaseSensitive target = SequencesUtils.concatenate(motif1, motif2);
            NSequenceWithQuality targetQ = new NSequenceWithQuality(target.toString());
            FuzzyMatchPattern pattern1 = new FuzzyMatchPattern(getTestPatternAligner(), motif1);
            FuzzyMatchPattern pattern2 = new FuzzyMatchPattern(getTestPatternAligner(), motif2);
            OrPattern orPattern1 = new OrPattern(getTestPatternAligner(), pattern1, pattern2);
            OrPattern orPattern2 = new OrPattern(getTestPatternAligner(), pattern2, pattern1);
            assertEquals(Math.max(pattern1.match(targetQ).getBestMatch().getScore(),
                    pattern2.match(targetQ).getBestMatch().getScore()),
                    orPattern1.match(targetQ).getBestMatch().getScore());
            assertEquals(orPattern1.match(targetQ).getBestMatch().getScore(),
                    orPattern2.match(targetQ).getBestMatch().getScore());
        }
    }
}
