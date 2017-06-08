package com.milaboratory.mist.util;

import cc.redberry.pipe.OutputPort;
import com.milaboratory.core.alignment.Alignment;
import com.milaboratory.core.alignment.BandedLinearAligner;
import com.milaboratory.core.alignment.LinearGapAlignmentScoring;
import com.milaboratory.core.sequence.NSequenceWithQuality;
import com.milaboratory.core.sequence.NucleotideSequence;
import com.milaboratory.core.sequence.SequencesUtils;
import com.milaboratory.mist.pattern.*;
import com.milaboratory.test.TestUtil;

import java.util.*;

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

    public static LinearGapAlignmentScoring getTestScoring() {
        return new LinearGapAlignmentScoring<>(NucleotideSequence.ALPHABET, 0, -9, -10);
    }

    public static PatternAligner getTestPatternAligner() {
        return getTestPatternAligner(0);
    }

    public static PatternAligner getTestPatternAligner(boolean zeroThreshold) {
        if (zeroThreshold) return getTestPatternAligner(0, 0, 0, -1);
        else return getTestPatternAligner();
    }

    public static PatternAligner getTestPatternAligner(int bitapMaxErrors) {
        return getTestPatternAligner(Integer.MIN_VALUE, bitapMaxErrors, 0, -1);
    }

    public static PatternAligner getTestPatternAligner(int penaltyThreshold, int bitapMaxErrors, int notResultScore,
                                                       int singleOverlapPenalty) {
        return getTestPatternAligner(penaltyThreshold, bitapMaxErrors, notResultScore, singleOverlapPenalty, true);
    }

    public static PatternAligner getTestPatternAligner(int penaltyThreshold, int bitapMaxErrors, int notResultScore,
                                                       int singleOverlapPenalty, boolean compatible) {
        return new PatternAligner() {
            @Override
            public Alignment<NucleotideSequence> align(NucleotideSequence pattern, NSequenceWithQuality target,
                                                       int rightMatchPosition) {
                int leftMatchPosition = rightMatchPosition + 1 - pattern.size() - bitapMaxErrors;
                if (leftMatchPosition < 0) leftMatchPosition = 0;
                return BandedLinearAligner.alignLeftAdded(getTestScoring(), pattern, target.getSequence(), 0,
                        pattern.size(), 0, leftMatchPosition, rightMatchPosition - leftMatchPosition + 1,
                        bitapMaxErrors, bitapMaxErrors);
            }

            @Override
            public int penaltyThreshold() {
                return penaltyThreshold;
            }

            @Override
            public int overlapPenalty(NSequenceWithQuality target, int overlapOffset, int overlapLength) {
                return singleOverlapPenalty * overlapLength;
            }

            @Override
            public int bitapMaxErrors() {
                return bitapMaxErrors;
            }

            @Override
            public int notResultScore() {
                return notResultScore;
            }

            @Override
            public boolean compatible(PatternAligner otherAligner) {
                return compatible;
            }

            @Override
            public PatternAligner overridePenaltyThreshold(int newThresholdValue) {
                return getTestPatternAligner(newThresholdValue, bitapMaxErrors, notResultScore, singleOverlapPenalty);
            }
        };
    }

    public static <T extends Enum<?>> T getRandomEnumItem(Class<T> enumClass){
        return enumClass.getEnumConstants()[new Random().nextInt(enumClass.getEnumConstants().length)];
    }

    public static String getRandomString(int length) {
        return getRandomString(length, "");
    }

    public static String getRandomString(int length, String exclude) {
        StringBuilder sb = new StringBuilder();
        Random r = new Random();
        for (int i = 0; i < length; i++) {
            char c;
            do {
                c = (char)(r.nextInt(r.nextInt(2) == 0 ? 128 : Character.MAX_VALUE));
            } while (exclude.contains(Character.toString(c)));
            sb.append(c);
        }
        return sb.toString();
    }

    public static ArrayList<GroupEdgePosition> getRandomGroupsForFuzzyMatch(int maxCoordinate) {
         return getRandomGroupsForFuzzyMatch(maxCoordinate, new Random().nextInt(40) + 1);
    }

    public static ArrayList<GroupEdgePosition> getRandomGroupsForFuzzyMatch(int maxCoordinate, int numGroups) {
        if (maxCoordinate < 1)
            throw new IllegalArgumentException("maxCoordinate=" + maxCoordinate);
        ArrayList<GroupEdgePosition> groupEdgePositions = new ArrayList<>();
        Random r = new Random();
        while (groupEdgePositions.size() < numGroups * 2) {
            String groupName = getRandomString(r.nextInt(30) + 1, "<>(){}[] '\"");
            if (groupEdgePositions.stream().anyMatch(g -> g.getGroupEdge().getGroupName().equals(groupName)))
                continue;
            int leftPosition = r.nextInt(maxCoordinate);
            int rightPosition = r.nextInt(maxCoordinate - leftPosition) + leftPosition + 1;
            groupEdgePositions.add(new GroupEdgePosition(new GroupEdge(groupName, true), leftPosition));
            groupEdgePositions.add(new GroupEdgePosition(new GroupEdge(groupName, false), rightPosition));
        }
        return groupEdgePositions;
    }

    public static PatternAligner getRandomPatternAligner() {
        Random r = new Random();
        return getTestPatternAligner(-r.nextInt(100), r.nextInt(4), -r.nextInt(4), -r.nextInt(3));
    }

    public static FuzzyMatchPattern getRandomFuzzyPattern() {
        return getRandomFuzzyPattern(false);
    }

    public static FuzzyMatchPattern getRandomFuzzyPattern(PatternAligner patternAligner) {
        return getRandomFuzzyPattern(patternAligner, false);
    }

    public static FuzzyMatchPattern getRandomFuzzyPattern(boolean withGroups) {
        return getRandomFuzzyPattern(getRandomPatternAligner(), withGroups);
    }

    public static FuzzyMatchPattern getRandomFuzzyPattern(PatternAligner patternAligner, boolean withGroups) {
        int length = new Random().nextInt(100) + 1;
        NucleotideSequence seq = TestUtil.randomSequence(NucleotideSequence.ALPHABET, length, length);
        return new FuzzyMatchPattern(patternAligner, seq, withGroups ? getRandomGroupsForFuzzyMatch(length)
                : new ArrayList<>());
    }

    public static SinglePattern getRandomSinglePattern(SinglePattern... patterns) {
        return getRandomSinglePattern(getRandomPatternAligner(), patterns);
    }

    public static SinglePattern getRandomSinglePattern(PatternAligner patternAligner, SinglePattern... singlePatterns) {
        Random r = new Random();
        SinglePattern[] patterns;
        if (singlePatterns.length == 0) {
            int numPatterns = r.nextInt(5) + 1;
            patterns = new FuzzyMatchPattern[numPatterns];
            for (int i = 0; i < numPatterns; i++)
                patterns[i] = getRandomFuzzyPattern(patternAligner);
        } else
            patterns = singlePatterns;
        switch (r.nextInt(6)) {
            case 0:
                return patterns[0];
            case 1:
                return new AndPattern(patternAligner, patterns);
            case 2:
                return new PlusPattern(patternAligner, patterns);
            case 3:
                return new OrPattern(patternAligner, patterns);
            case 4:
                return new FilterPattern(patternAligner, new ScoreFilter(-r.nextInt(75)), patterns[0]);
            case 5:
            default:
                int seqLength = r.nextInt(10) + 1;
                NucleotideSequence seq = TestUtil.randomSequence(NucleotideSequence.ALPHABET, seqLength, seqLength);
                BorderFilter borderFilter = new BorderFilter(patternAligner, r.nextBoolean(), seq, seqLength, r.nextBoolean());
                return new FilterPattern(patternAligner, borderFilter, patterns[0]);
        }
    }

    public static MultipleReadsOperator getRandomMultiReadPattern(MultipleReadsOperator... patterns) {
        return getRandomMultiReadPattern(getRandomPatternAligner(), patterns);
    }

    public static MultipleReadsOperator getRandomMultiReadPattern(PatternAligner patternAligner,
                                                                  MultipleReadsOperator... patterns) {
        Random r = new Random();
        if (patterns.length == 0) {
            int numPatterns = r.nextInt(5) + 1;
            FuzzyMatchPattern[] fuzzyMatchPatterns = new FuzzyMatchPattern[numPatterns];
            for (int i = 0; i < numPatterns; i++)
                fuzzyMatchPatterns[i] = getRandomFuzzyPattern(patternAligner);
            return new MultiPattern(patternAligner, fuzzyMatchPatterns);
        } else {
            switch (r.nextInt(4)) {
                case 0:
                    return new AndOperator(patternAligner, patterns);
                case 1:
                    return new OrOperator(patternAligner, patterns);
                case 2:
                    return new NotOperator(patternAligner, patterns[0]);
                case 3:
                default:
                    return new MultipleReadsFilterPattern(patternAligner, new ScoreFilter(-r.nextInt(75)), patterns[0]);
            }
        }
    }
}
