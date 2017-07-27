package com.milaboratory.mist.util;

import cc.redberry.pipe.OutputPort;
import com.milaboratory.core.alignment.Aligner;
import com.milaboratory.core.alignment.Alignment;
import com.milaboratory.core.alignment.BandedLinearAligner;
import com.milaboratory.core.alignment.LinearGapAlignmentScoring;
import com.milaboratory.core.sequence.NSequenceWithQuality;
import com.milaboratory.core.sequence.NucleotideSequence;
import com.milaboratory.core.sequence.SequencesUtils;
import com.milaboratory.mist.pattern.*;
import com.milaboratory.test.TestUtil;

import java.util.*;
import java.util.concurrent.Callable;
import java.util.function.Function;
import java.util.stream.Collectors;

import static com.milaboratory.mist.pattern.PatternUtils.invertCoordinate;
import static org.junit.Assert.*;

public class CommonTestUtils {
    public static final Random rg = new Random();
    private static final String LETTERS_AND_NUMBERS = "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789";

    public static int countPortValues(OutputPort<Match> port) {
        int counter = 0;
        while (port.take() != null)
            counter++;
        return counter;
    }

    public static NucleotideSequence makeRandomInsertions(NucleotideSequence seq, int number) {
        NucleotideSequence result = seq;
        int currentLength;
        int currentInsertPosition;
        for (int i = 0; i < number; i++) {
            currentLength = seq.size() + i;
            currentInsertPosition = rg.nextInt(currentLength);
            result = SequencesUtils.concatenate(result.getRange(0, currentInsertPosition),
                    TestUtil.randomSequence(NucleotideSequence.ALPHABET, 1, 1),
                    result.getRange(currentInsertPosition, currentLength));
        }
        return result;
    }

    public static NucleotideSequence makeRandomDeletions(NucleotideSequence seq, int number) {
        assertTrue(seq.size() > number);
        NucleotideSequence result = seq;
        int currentLength;
        int currentDeletePosition;
        for (int i = 0; i < number; i++) {
            currentLength = seq.size() - i;
            currentDeletePosition = rg.nextInt(currentLength);
            result = SequencesUtils.concatenate(result.getRange(0, currentDeletePosition),
                    result.getRange(currentDeletePosition + 1, currentLength));
        }
        return result;
    }

    public static NucleotideSequence makeRandomReplacements(NucleotideSequence seq, int number) {
        NucleotideSequence result = seq;
        int currentPosition;
        for (int i = 0; i < number; i++) {
            currentPosition = rg.nextInt(seq.size());
            result = SequencesUtils.concatenate(result.getRange(0, currentPosition),
                    TestUtil.randomSequence(NucleotideSequence.ALPHABET, 1, 1),
                    result.getRange(currentPosition + 1, seq.size()));
        }
        return result;
    }

    public static NucleotideSequence makeRandomErrors(NucleotideSequence seq, int number) {
        NucleotideSequence result = seq;
        for (int i = 0; i < number; i++) {
            switch (rg.nextInt(3)) {
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
        int position1 = rg.nextInt(seq.size());
        int position2 = rg.nextInt(seq.size());

        return seq.getRange(Math.min(position1, position2), Math.max(position1, position2) + 1);
    }

    public static LinearGapAlignmentScoring<NucleotideSequence> getTestScoring() {
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
        return getTestPatternAligner(Long.MIN_VALUE, bitapMaxErrors, 0, -1);
    }

    public static PatternAligner getTestPatternAligner(long penaltyThreshold, int bitapMaxErrors, long notResultScore,
                                                       long singleOverlapPenalty) {
        return getTestPatternAligner(penaltyThreshold, bitapMaxErrors, notResultScore, singleOverlapPenalty, true);
    }

    public static PatternAligner getTestPatternAligner(long penaltyThreshold, int bitapMaxErrors, long notResultScore,
                                                       long singleOverlapPenalty, boolean compatible) {
        return getTestPatternAligner(penaltyThreshold, bitapMaxErrors, notResultScore, singleOverlapPenalty,
                compatible, -1);
    }

    public static PatternAligner getTestPatternAligner(long penaltyThreshold, int bitapMaxErrors, long notResultScore,
                                                       long singleOverlapPenalty, boolean compatible, int maxOverlap) {
        return getTestPatternAligner(penaltyThreshold, bitapMaxErrors, notResultScore, singleOverlapPenalty,
                compatible, maxOverlap, -1);
    }

    public static PatternAligner getTestPatternAligner(long penaltyThreshold, int bitapMaxErrors, long notResultScore,
                                                       long singleOverlapPenalty, boolean compatible, int maxOverlap,
                                                       int fixedLeftBorder) {
        return new PatternAligner() {
            @Override
            public Alignment<NucleotideSequence> align(NucleotideSequence pattern, NSequenceWithQuality target,
                                                       int rightMatchPosition) {
                if (fixedLeftBorder == -1) {
                    int leftMatchPosition = rightMatchPosition + 1 - pattern.size() - bitapMaxErrors;
                    if (leftMatchPosition < 0) leftMatchPosition = 0;
                    return BandedLinearAligner.alignLeftAdded(getTestScoring(), pattern, target.getSequence(),
                            0, pattern.size(), 0, leftMatchPosition,
                            rightMatchPosition - leftMatchPosition + 1, bitapMaxErrors, bitapMaxErrors);
                } else {
                    NucleotideSequence targetPart = target.getSubSequence(fixedLeftBorder, rightMatchPosition + 1)
                            .getSequence();
                    Alignment<NucleotideSequence> partAlignment = Aligner.alignGlobal(getTestScoring(), pattern, targetPart);
                    return new Alignment<>(pattern, partAlignment.getAbsoluteMutations(),
                            partAlignment.getSequence1Range(), partAlignment.getSequence2Range().move(fixedLeftBorder),
                            partAlignment.getScore());
                }
            }

            @Override
            public long penaltyThreshold() {
                return penaltyThreshold;
            }

            @Override
            public long overlapPenalty(NSequenceWithQuality target, int overlapOffset, int overlapLength) {
                return singleOverlapPenalty * overlapLength;
            }

            @Override
            public long insertionPenalty(NSequenceWithQuality target, int insertionOffset, int insertionLength) {
                return singleOverlapPenalty * insertionLength;
            }

            @Override
            public int bitapMaxErrors() {
                return bitapMaxErrors;
            }

            @Override
            public long notResultScore() {
                return notResultScore;
            }

            @Override
            public int maxOverlap() {
                return maxOverlap;
            }

            @Override
            public boolean compatible(PatternAligner otherAligner) {
                return compatible;
            }

            @Override
            public PatternAligner overridePenaltyThreshold(long newThresholdValue) {
                return getTestPatternAligner(newThresholdValue, bitapMaxErrors, notResultScore, singleOverlapPenalty,
                        compatible, maxOverlap, fixedLeftBorder);
            }

            @Override
            public PatternAligner overrideMaxOverlap(int newMaxOverlap) {
                return getTestPatternAligner(penaltyThreshold, bitapMaxErrors, notResultScore, singleOverlapPenalty,
                        compatible, newMaxOverlap, fixedLeftBorder);
            }

            @Override
            public PatternAligner setLeftBorder(int leftBorder) {
                return getTestPatternAligner(penaltyThreshold, bitapMaxErrors, notResultScore, singleOverlapPenalty,
                        compatible, maxOverlap, leftBorder);
            }
        };
    }

    public static String inQuotes(String str) {
        return '"' + str + '"';
    }

    public static String repeatString(String str, int num) {
        return new String(new char[num]).replace("\0", str);
    }

    public static String getRandomString(int length) {
        return getRandomString(length, "", false);
    }

    public static String getRandomString(int length, String exclude) {
        return getRandomString(length, exclude, false);
    }

    public static String getRandomString(int length, String exclude, boolean lettersAndNumbers) {
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < length; i++) {
            char c;
            do {
                if (lettersAndNumbers)
                    c = LETTERS_AND_NUMBERS.charAt(rg.nextInt(LETTERS_AND_NUMBERS.length()));
                else
                    c = (char)(rg.nextInt(rg.nextInt(2) == 0 ? 128 : Character.MAX_VALUE));
            } while (exclude.contains(Character.toString(c)));
            sb.append(c);
        }
        return sb.toString();
    }

    public static <T extends Enum<?>> T getRandomEnumItem(Class<T> enumClass) {
        return enumClass.getEnumConstants()[rg.nextInt(enumClass.getEnumConstants().length)];
    }

    public static ArrayList<GroupEdgePosition> getRandomGroupsForFuzzyMatch(int maxCoordinate) {
         return getRandomGroupsForFuzzyMatch(maxCoordinate, rg.nextInt(40) + 1);
    }

    public static ArrayList<GroupEdgePosition> getRandomGroupsForFuzzyMatch(int maxCoordinate, int numGroups) {
        if (maxCoordinate < 1)
            throw new IllegalArgumentException("maxCoordinate=" + maxCoordinate);
        ArrayList<GroupEdgePosition> groupEdgePositions = new ArrayList<>();
        while (groupEdgePositions.size() < numGroups * 2) {
            String groupName = getRandomString(rg.nextInt(30) + 1, "", true);
            if (groupEdgePositions.stream().anyMatch(g -> g.getGroupEdge().getGroupName().equals(groupName)))
                continue;
            int leftPosition = rg.nextInt(maxCoordinate);
            int rightPosition = rg.nextInt(maxCoordinate - leftPosition) + leftPosition + 1;
            groupEdgePositions.add(new GroupEdgePosition(new GroupEdge(groupName, true), leftPosition));
            groupEdgePositions.add(new GroupEdgePosition(new GroupEdge(groupName, false), rightPosition));
        }
        return groupEdgePositions;
    }

    public static PatternAligner getRandomPatternAligner() {
        return getTestPatternAligner(-rg.nextInt(100), rg.nextInt(4), -rg.nextInt(4), -rg.nextInt(3));
    }

    public static FuzzyMatchPattern getRandomFuzzyPattern(PatternAligner patternAligner, boolean withGroups) {
        int length = rg.nextInt(63) + 1;
        RandomBorders randomBorders = new RandomBorders(length);
        NucleotideSequence seq = TestUtil.randomSequence(NucleotideSequence.ALPHABET, length, length);
        return new FuzzyMatchPattern(patternAligner, seq, randomBorders.left, randomBorders.right,
                withGroups ? getRandomGroupsForFuzzyMatch(length) : new ArrayList<>());
    }

    public static RepeatPattern getRandomRepeatPattern(PatternAligner patternAligner, boolean withGroups) {
        RandomRepeats rr = new RandomRepeats();
        RandomBorders randomBorders = new RandomBorders(rr.motifSize * rr.repeats);
        NucleotideSequence seq = TestUtil.randomSequence(NucleotideSequence.ALPHABET, rr.motifSize, rr.motifSize);
        return new RepeatPattern(patternAligner, seq, rr.minRepeats, rr.maxRepeats,
                randomBorders.left, randomBorders.right,
                withGroups ? getRandomGroupsForFuzzyMatch(rr.motifSize * rr.repeats) : new ArrayList<>());
    }

    public static AnyPattern getRandomAnyPattern(PatternAligner patternAligner, boolean withGroups) {
        return new AnyPattern(patternAligner, withGroups ? getRandomGroupsForFuzzyMatch(1).stream()
                .map(GroupEdgePosition::getGroupEdge).collect(Collectors.toCollection(ArrayList::new))
                : new ArrayList<>());
    }

    public static SinglePattern getRandomBasicPattern() {
        return getRandomBasicPattern(false);
    }

    public static SinglePattern getRandomBasicPattern(PatternAligner patternAligner) {
        return getRandomBasicPattern(patternAligner, false);
    }

    public static SinglePattern getRandomBasicPattern(boolean withGroups) {
        return getRandomBasicPattern(getRandomPatternAligner(), withGroups);
    }

    public static SinglePattern getRandomBasicPattern(PatternAligner patternAligner, boolean withGroups) {
        switch (rg.nextInt(3)) {
            case 0:
                return getRandomFuzzyPattern(patternAligner, withGroups);
            case 1:
                return getRandomRepeatPattern(patternAligner, withGroups);
            default:
                return getRandomAnyPattern(patternAligner, withGroups);
        }
    }

    public static SinglePattern getRandomSinglePattern(SinglePattern... patterns) {
        return getRandomSinglePattern(getRandomPatternAligner(), patterns);
    }

    public static SinglePattern getRandomSinglePattern(PatternAligner patternAligner, SinglePattern... singlePatterns) {
        SinglePattern[] patterns;
        if (singlePatterns.length == 0) {
            int numPatterns = rg.nextInt(5) + 1;
            patterns = new SinglePattern[numPatterns];
            for (int i = 0; i < numPatterns; i++)
                patterns[i] = getRandomBasicPattern(patternAligner);
        } else
            patterns = singlePatterns;
        switch (rg.nextInt(7)) {
            case 0:
                return patterns[0];
            case 1:
                return new AndPattern(patternAligner, patterns);
            case 2:
                return new PlusPattern(patternAligner, patterns);
            case 3:
                return new SequencePattern(patternAligner, patterns);
            case 4:
                return new OrPattern(patternAligner, patterns);
            case 5:
                return new FilterPattern(patternAligner, new ScoreFilter(-rg.nextInt(75)), patterns[0]);
            case 6:
            default:
                int seqLength = rg.nextInt(10) + 1;
                NucleotideSequence seq = TestUtil.randomSequence(NucleotideSequence.ALPHABET, seqLength, seqLength);
                BorderFilter borderFilter = new BorderFilter(patternAligner, rg.nextBoolean(), seq, seqLength,
                        rg.nextBoolean());
                return new FilterPattern(patternAligner, borderFilter, patterns[0]);
        }
    }

    public static MultipleReadsOperator getRandomMultiReadPattern(MultipleReadsOperator... patterns) {
        return getRandomMultiReadPattern(getRandomPatternAligner(), patterns);
    }

    public static MultipleReadsOperator getRandomMultiReadPattern(PatternAligner patternAligner,
                                                                  MultipleReadsOperator... patterns) {
        if (patterns.length == 0) {
            int numPatterns = rg.nextInt(5) + 1;
            SinglePattern[] basicPatterns = new SinglePattern[numPatterns];
            for (int i = 0; i < numPatterns; i++)
                basicPatterns[i] = getRandomBasicPattern(patternAligner);
            return new MultiPattern(patternAligner, basicPatterns);
        } else {
            switch (rg.nextInt(4)) {
                case 0:
                    return new AndOperator(patternAligner, patterns);
                case 1:
                    return new OrOperator(patternAligner, patterns);
                case 2:
                    return new NotOperator(patternAligner, patterns[0]);
                case 3:
                default:
                    return new MultipleReadsFilterPattern(patternAligner,
                            new ScoreFilter(-rg.nextInt(75)), patterns[0]);
            }
        }
    }

    public static MultipleReadsOperator[] singleToMultiPatterns(SinglePattern... singlePatterns) {
        return singleToMultiPatterns(getTestPatternAligner(), singlePatterns);
    }

    public static MultipleReadsOperator[] singleToMultiPatterns(PatternAligner patternAligner,
                                                                SinglePattern... singlePatterns) {
        return Arrays.stream(singlePatterns).map(sp -> new MultiPattern(patternAligner, sp))
                .toArray(MultipleReadsOperator[]::new);
    }

    public static String bestToString(MatchingResult matchingResult) {
        return bestToString(matchingResult, true);
    }

    public static String bestToString(MatchingResult matchingResult, boolean fairSorting) {
        return matchingResult.getBestMatch(fairSorting).getValue().getSequence().toString();
    }

    @FunctionalInterface
    public interface ThrowingFunction<T, R, E extends Exception> {
        R apply(T t) throws E;
    }

    public static <T, R> Function<T, R> rethrow(ThrowingFunction<T, R, Exception> function) {
        return t -> {
            try {
                return function.apply(t);
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        };
    }

    public static <T, R> Function<T, R> orNull(ThrowingFunction<T, R, Exception> function) {
        return t -> {
            try {
                return function.apply(t);
            } catch (Exception e) {
                return null;
            }
        };
    }

    public static <T, R> R untilSuccess(T inputValue, ThrowingFunction<T, R, Exception> function) {
        while (true) {
            R returnValue;
            try {
                returnValue = function.apply(inputValue);
            } catch (Exception e) {
                continue;
            }
            return returnValue;
        }
    }

    public static void assertException(Class exceptionClass, Callable<Void> f) {
        boolean exceptionThrown = false;
        try {
            f.call();
        } catch (Exception e) {
            if (e.getClass().equals(exceptionClass))
                exceptionThrown = true;
            else
                throw new RuntimeException(e);
        }
        assertTrue(exceptionThrown);
    }

    public static void repeatAndExpectExceptionEveryTime(int iterations, Class exceptionClass, Callable<Void> f) {
        for (int i = 0; i < iterations; i++)
            assertException(exceptionClass, f);
    }

    public static class RandomBorders {
        public final int left;
        public final int right;

        public RandomBorders(int targetSize) {
            if (targetSize < 1)
                throw new IllegalArgumentException("Cannot create RandomBorders for targetSize " + targetSize);
            int r = rg.nextInt(12);
            int minRight;
            if (r < 10) {
                minRight = 0;
                left = -1;
            } else if (r == 10) {
                minRight = rg.nextInt(targetSize);
                left = minRight;
            } else {
                minRight = rg.nextInt(targetSize);
                left = invertCoordinate(targetSize - 1 - minRight);
            }
            r = rg.nextInt(12);
            if (r < 10)
                right = -1;
            else if (r == 10)
                right = minRight + rg.nextInt(targetSize - minRight);
            else
                right = invertCoordinate(targetSize - 1 - minRight - rg.nextInt(targetSize - minRight));
        }
    }

    public static class RandomRepeats {
        public final int motifSize;
        public final int repeats;
        public final int minRepeats;
        public final int maxRepeats;

        public RandomRepeats() {
            motifSize = rg.nextInt(60) + 1;
            repeats = Math.max(1, rg.nextInt(60 / motifSize + 1));
            minRepeats = repeats - rg.nextInt(repeats);
            maxRepeats = Math.max(repeats, rg.nextInt(60 / motifSize + 1));
        }
    }
}
