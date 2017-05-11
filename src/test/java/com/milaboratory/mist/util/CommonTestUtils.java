package com.milaboratory.mist.util;

import cc.redberry.pipe.OutputPort;
import com.milaboratory.core.sequence.NucleotideSequence;
import com.milaboratory.core.sequence.SequencesUtils;
import com.milaboratory.mist.pattern.Match;
import com.milaboratory.test.TestUtil;

import java.util.Random;

import static org.junit.Assert.*;

public class CommonTestUtils {
    public static int countPortValues(OutputPort<Match> port) {
        int counter = 0;
        while (port.take() != null)
            counter++;
        return counter;
    }

    public static NucleotideSequence makeRandomInsertions(NucleotideSequence seq, int number) {
        Random randomGenerator = new Random();
        NucleotideSequence result = seq;
        int currentLength;
        int currentInsertPosition;
        for (int i = 0; i < number; i++) {
            currentLength = seq.size() + i;
            currentInsertPosition = randomGenerator.nextInt(currentLength);
            result = SequencesUtils.concatenate(result.getRange(0, currentInsertPosition),
                    TestUtil.randomSequence(NucleotideSequence.ALPHABET, 1, 1),
                    result.getRange(currentInsertPosition, currentLength));
        }
        return result;
    }

    public static NucleotideSequence makeRandomDeletions(NucleotideSequence seq, int number) {
        assertTrue(seq.size() > number);
        Random randomGenerator = new Random();
        NucleotideSequence result = seq;
        int currentLength;
        int currentDeletePosition;
        for (int i = 0; i < number; i++) {
            currentLength = seq.size() - i;
            currentDeletePosition = randomGenerator.nextInt(currentLength);
            result = SequencesUtils.concatenate(result.getRange(0, currentDeletePosition),
                    result.getRange(currentDeletePosition + 1, currentLength));
        }
        return result;
    }

    public static NucleotideSequence makeRandomReplacements(NucleotideSequence seq, int number) {
        Random randomGenerator = new Random();
        NucleotideSequence result = seq;
        int currentPosition;
        for (int i = 0; i < number; i++) {
            currentPosition = randomGenerator.nextInt(seq.size());
            result = SequencesUtils.concatenate(result.getRange(0, currentPosition),
                    TestUtil.randomSequence(NucleotideSequence.ALPHABET, 1, 1),
                    result.getRange(currentPosition + 1, seq.size()));
        }
        return result;
    }

    public static NucleotideSequence makeRandomErrors(NucleotideSequence seq, int number) {
        Random randomGenerator = new Random();
        NucleotideSequence result = seq;
        for (int i = 0; i < number; i++) {
            switch (randomGenerator.nextInt(3)) {
                case 0:
                    result = makeRandomInsertions(result, 1);
                    break;
                case 1:
                    if (result.size() < 2) break;
                    result = makeRandomDeletions(result, 1);
                    break;
                case 2:
                    result = makeRandomReplacements(result, 1);
                    break;
            }
        }
        return result;
    }

    public static NucleotideSequence getRandomSubsequence(NucleotideSequence seq) {
        assertTrue(seq.size() > 0);
        Random randomGenerator = new Random();
        int position1 = randomGenerator.nextInt(seq.size());
        int position2 = randomGenerator.nextInt(seq.size());

        return seq.getRange(Math.min(position1, position2), Math.max(position1, position2) + 1);
    }
}
