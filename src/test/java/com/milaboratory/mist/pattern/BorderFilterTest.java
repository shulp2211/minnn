package com.milaboratory.mist.pattern;

import cc.redberry.pipe.OutputPort;
import com.milaboratory.core.sequence.MultiNSequenceWithQuality;
import com.milaboratory.core.sequence.NSequenceWithQuality;
import com.milaboratory.core.sequence.NucleotideSequence;
import com.milaboratory.core.sequence.SequenceQuality;
import com.milaboratory.test.TestUtil;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;

import java.util.Random;

import static com.milaboratory.mist.pattern.MatchUtils.countMatches;
import static com.milaboratory.mist.util.CommonTestUtils.getTestPatternAligner;
import static org.junit.Assert.*;

public class BorderFilterTest {
    @Rule
    public final ExpectedException exception = ExpectedException.none();

    @Test
    public void simpleTest() throws Exception {
        NSequenceWithQuality target = new NSequenceWithQuality("AGGACCTATTAGACATTAGACAATTGGGATTAGACAG");
        NucleotideSequence motif = new NucleotideSequence("ATTAGACA");
        NucleotideSequence edgeLeftFullTarget = new NucleotideSequence("AGG");
        NucleotideSequence edgeLeftPartTarget = new NucleotideSequence("TTAGG");
        NucleotideSequence edgeRightFullTarget = new NucleotideSequence("ACAG");
        NucleotideSequence edgeRightPartTarget = new NucleotideSequence("ACAGAA");
        NucleotideSequence edgeLeftFullMotif = new NucleotideSequence("ATT");
        NucleotideSequence edgeLeftPartMotif = new NucleotideSequence("GTATT");
        NucleotideSequence edgeRightFullMotif = new NucleotideSequence("ACA");
        NucleotideSequence edgeRightPartMotif = new NucleotideSequence("ACAGAA");

        BorderFilter filterLeftFullTarget = new BorderFilter(getTestPatternAligner(), true, edgeLeftFullTarget);
        BorderFilter filterLeftPartTarget = new BorderFilter(getTestPatternAligner(), true, edgeLeftPartTarget, 3);
        BorderFilter filterLeftMismatchTarget = new BorderFilter(getTestPatternAligner(), true, edgeLeftPartTarget);
        BorderFilter filterRightFullTarget = new BorderFilter(getTestPatternAligner(), false, edgeRightFullTarget);
        BorderFilter filterRightPartTarget = new BorderFilter(getTestPatternAligner(), false, edgeRightPartTarget, 3);
        BorderFilter filterRightMismatchTarget = new BorderFilter(getTestPatternAligner(), false, edgeRightPartTarget);
        BorderFilter filterLeftFullMotif = new BorderFilter(getTestPatternAligner(), true, edgeLeftFullMotif, false);
        BorderFilter filterLeftPartMotif = new BorderFilter(getTestPatternAligner(), true, edgeLeftPartMotif, 3, false);
        BorderFilter filterLeftMismatchMotif = new BorderFilter(getTestPatternAligner(), true, edgeLeftPartMotif, false);
        BorderFilter filterRightFullMotif = new BorderFilter(getTestPatternAligner(), false, edgeRightFullMotif, false);
        BorderFilter filterRightPartMotif = new BorderFilter(getTestPatternAligner(), false, edgeRightPartMotif, 3, false);
        BorderFilter filterRightMismatchMotif = new BorderFilter(getTestPatternAligner(), false, edgeRightPartMotif, false);

        assertTrue(new FilterPattern(getTestPatternAligner(), filterLeftFullTarget,
                new FuzzyMatchPattern(getTestPatternAligner(), motif)).match(target).isFound());
        assertTrue(new FilterPattern(getTestPatternAligner(), filterLeftPartTarget,
                new FuzzyMatchPattern(getTestPatternAligner(), motif)).match(target).isFound());
        assertFalse(new FilterPattern(getTestPatternAligner(), filterLeftMismatchTarget,
                new FuzzyMatchPattern(getTestPatternAligner(), motif)).match(target).isFound());
        assertTrue(new FilterPattern(getTestPatternAligner(), filterRightFullTarget,
                new FuzzyMatchPattern(getTestPatternAligner(), motif)).match(target).isFound());
        assertTrue(new FilterPattern(getTestPatternAligner(), filterRightPartTarget,
                new FuzzyMatchPattern(getTestPatternAligner(), motif)).match(target).isFound());
        assertFalse(new FilterPattern(getTestPatternAligner(), filterRightMismatchTarget,
                new FuzzyMatchPattern(getTestPatternAligner(), motif)).match(target).isFound());
        assertTrue(new FilterPattern(getTestPatternAligner(), filterLeftFullMotif,
                new FuzzyMatchPattern(getTestPatternAligner(), motif)).match(target).isFound());
        assertTrue(new FilterPattern(getTestPatternAligner(), filterLeftPartMotif,
                new FuzzyMatchPattern(getTestPatternAligner(), motif)).match(target).isFound());
        assertFalse(new FilterPattern(getTestPatternAligner(), filterLeftMismatchMotif,
                new FuzzyMatchPattern(getTestPatternAligner(), motif)).match(target).isFound());
        assertTrue(new FilterPattern(getTestPatternAligner(), filterRightFullMotif,
                new FuzzyMatchPattern(getTestPatternAligner(), motif)).match(target).isFound());
        assertTrue(new FilterPattern(getTestPatternAligner(), filterRightPartMotif,
                new FuzzyMatchPattern(getTestPatternAligner(), motif)).match(target).isFound());
        assertFalse(new FilterPattern(getTestPatternAligner(), filterRightMismatchMotif,
                new FuzzyMatchPattern(getTestPatternAligner(), motif)).match(target).isFound());

        OutputPort<Match> wrongPatternPort = new FilterPattern(getTestPatternAligner(), filterLeftFullTarget,
                new MultiPattern(getTestPatternAligner(), new FuzzyMatchPattern(getTestPatternAligner(), motif),
                        new FuzzyMatchPattern(getTestPatternAligner(), motif))).match(new MultiNSequenceWithQuality() {
            @Override
            public int numberOfSequences() {
                return 2;
            }

            @Override
            public NSequenceWithQuality get(int id) {
                return target;
            }
        }).getMatches();
        exception.expect(IllegalArgumentException.class);
        wrongPatternPort.take();
    }

    @Test
    public void randomTest() throws Exception {
        Random randomGenerator = new Random();
        int its = TestUtil.its(500, 1000);
        for (int i = 0; i < its; ++i) {
            boolean leftSide = randomGenerator.nextBoolean();
            int motifSize = randomGenerator.nextInt(10) + 1;
            int cutSize = randomGenerator.nextInt(motifSize) + 1;
            int minMatchSize = randomGenerator.nextInt(cutSize) + 1;
            boolean useTarget = randomGenerator.nextBoolean();
            NucleotideSequence target = TestUtil.randomSequence(NucleotideSequence.ALPHABET, 1, 1000);
            NucleotideSequence motif = TestUtil.randomSequence(NucleotideSequence.ALPHABET, motifSize, motifSize);
            NucleotideSequence edgeMotif = TestUtil.randomSequence(NucleotideSequence.ALPHABET, cutSize, cutSize);
            NSequenceWithQuality targetQ = new NSequenceWithQuality(target,
                    SequenceQuality.getUniformQuality(SequenceQuality.GOOD_QUALITY_VALUE, target.getSequence().size()));
            FuzzyMatchPattern pattern = new FuzzyMatchPattern(getTestPatternAligner(), motif);
            FilterPattern filterPattern = new FilterPattern(getTestPatternAligner(),
                    new BorderFilter(getTestPatternAligner(), leftSide, edgeMotif, minMatchSize, useTarget), pattern);

            boolean isMatching = false;
            if (target.toString().contains(motif.toString()))
                for (int j = minMatchSize; j <= cutSize; j++) {
                    String targetStr = useTarget ? target.toString() : motif.toString();
                    String targetEdge = leftSide ? targetStr.substring(0, j)
                            : targetStr.substring(targetStr.length() - j, targetStr.length());
                    String requiredEdge = leftSide ? edgeMotif.toString().substring(cutSize - j, cutSize)
                            : edgeMotif.toString().substring(0, j);
                    if (requiredEdge.equals(targetEdge))
                        isMatching = true;
                }

            assertEquals(isMatching, filterPattern.match(targetQ).isFound());
            assertTrue(countMatches(pattern.match(targetQ)) >= countMatches(filterPattern.match(targetQ)));
            Match currentMatch;
            for (OutputPort<Match> filteredPort = filterPattern.match(targetQ).getMatches(
                    randomGenerator.nextBoolean(), randomGenerator.nextBoolean()); (currentMatch = filteredPort.take()) != null;)
                assertTrue((useTarget ? target.toString() : currentMatch.getValue().getSequence().toString()).contains(leftSide
                        ? edgeMotif.toString().substring(cutSize - minMatchSize, cutSize)
                        : edgeMotif.toString().substring(0, minMatchSize)));
        }
    }
}
