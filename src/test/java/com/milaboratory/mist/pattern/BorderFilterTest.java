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

        BorderFilter filterLeftFullTarget = new BorderFilter(true, edgeLeftFullTarget);
        BorderFilter filterLeftPartTarget = new BorderFilter(true, edgeLeftPartTarget, 3);
        BorderFilter filterLeftMismatchTarget = new BorderFilter(true, edgeLeftPartTarget);
        BorderFilter filterRightFullTarget = new BorderFilter(false, edgeRightFullTarget);
        BorderFilter filterRightPartTarget = new BorderFilter(false, edgeRightPartTarget, 3);
        BorderFilter filterRightMismatchTarget = new BorderFilter(false, edgeRightPartTarget);
        BorderFilter filterLeftFullMotif = new BorderFilter(true, edgeLeftFullMotif, false);
        BorderFilter filterLeftPartMotif = new BorderFilter(true, edgeLeftPartMotif, 3, false);
        BorderFilter filterLeftMismatchMotif = new BorderFilter(true, edgeLeftPartMotif, false);
        BorderFilter filterRightFullMotif = new BorderFilter(false, edgeRightFullMotif, false);
        BorderFilter filterRightPartMotif = new BorderFilter(false, edgeRightPartMotif, 3, false);
        BorderFilter filterRightMismatchMotif = new BorderFilter(false, edgeRightPartMotif, false);

        assertTrue(new FilterPattern(filterLeftFullTarget, new FuzzyMatchPattern(motif)).match(target).isFound());
        assertTrue(new FilterPattern(filterLeftPartTarget, new FuzzyMatchPattern(motif)).match(target).isFound());
        assertFalse(new FilterPattern(filterLeftMismatchTarget, new FuzzyMatchPattern(motif)).match(target).isFound());
        assertTrue(new FilterPattern(filterRightFullTarget, new FuzzyMatchPattern(motif)).match(target).isFound());
        assertTrue(new FilterPattern(filterRightPartTarget, new FuzzyMatchPattern(motif)).match(target).isFound());
        assertFalse(new FilterPattern(filterRightMismatchTarget, new FuzzyMatchPattern(motif)).match(target).isFound());
        assertTrue(new FilterPattern(filterLeftFullMotif, new FuzzyMatchPattern(motif)).match(target).isFound());
        assertTrue(new FilterPattern(filterLeftPartMotif, new FuzzyMatchPattern(motif)).match(target).isFound());
        assertFalse(new FilterPattern(filterLeftMismatchMotif, new FuzzyMatchPattern(motif)).match(target).isFound());
        assertTrue(new FilterPattern(filterRightFullMotif, new FuzzyMatchPattern(motif)).match(target).isFound());
        assertTrue(new FilterPattern(filterRightPartMotif, new FuzzyMatchPattern(motif)).match(target).isFound());
        assertFalse(new FilterPattern(filterRightMismatchMotif, new FuzzyMatchPattern(motif)).match(target).isFound());

        OutputPort<Match> wrongPatternPort = new FilterPattern(filterLeftFullTarget, new MultiPattern(
                new FuzzyMatchPattern(motif), new FuzzyMatchPattern(motif))).match(new MultiNSequenceWithQuality() {
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
            FuzzyMatchPattern pattern = new FuzzyMatchPattern(motif);
            FilterPattern filterPattern = new FilterPattern(new BorderFilter(leftSide, edgeMotif, minMatchSize, useTarget), pattern);

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
