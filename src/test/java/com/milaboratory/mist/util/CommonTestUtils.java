package com.milaboratory.mist.util;

import cc.redberry.pipe.CUtils;
import cc.redberry.pipe.OutputPort;
import com.milaboratory.core.alignment.*;
import com.milaboratory.core.sequence.*;
import com.milaboratory.mist.pattern.*;
import com.milaboratory.test.TestUtil;

import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.*;
import java.util.concurrent.Callable;
import java.util.function.Function;
import java.util.stream.*;

import static com.milaboratory.mist.cli.Defaults.*;
import static com.milaboratory.mist.pattern.PatternUtils.invertCoordinate;
import static com.milaboratory.mist.util.CommonTestUtils.RandomStringType.*;
import static org.junit.Assert.*;

public class CommonTestUtils {
    public static final Random rg = new Random();
    public static final String TEMP_DIR = System.getProperty("java.io.tmpdir") + "/";
    private static final String LN_STRING = "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789";
    private static final String QUERY_CHAR_STRING = " ATGCNatgcn()[]{}^$:+&|\\~0123456789-*";

    public static <T> Stream<T> streamPort(OutputPort<T> port) {
        return StreamSupport.stream(CUtils.it(port).spliterator(), false);
    }

    public static long countPortValues(OutputPort<MatchIntermediate> port) {
        return streamPort(port).count();
    }

    public static void printPortValues(OutputPort<MatchIntermediate> port) {
        long i = 0;
        for (MatchIntermediate match : CUtils.it(port))
            System.out.println(i++ + ": " + match.getRange() + " " + match.getScore() + " " + match.getValue());
    }

    public static long countMatches(MatchingResult matchingResult, boolean fair) {
        return countPortValues(matchingResult.getMatches(fair));
    }

    public static NucleotideSequenceCaseSensitive makeRandomInsertions(NucleotideSequenceCaseSensitive seq,
                                                                       int number) {
        NucleotideSequenceCaseSensitive result = seq;
        int currentLength;
        int currentInsertPosition;
        for (int i = 0; i < number; i++) {
            currentLength = seq.size() + i;
            currentInsertPosition = rg.nextInt(currentLength);
            result = SequencesUtils.concatenate(result.getRange(0, currentInsertPosition),
                    TestUtil.randomSequence(NucleotideSequenceCaseSensitive.ALPHABET, 1, 1),
                    result.getRange(currentInsertPosition, currentLength));
        }
        return result;
    }

    public static NucleotideSequenceCaseSensitive makeRandomDeletions(NucleotideSequenceCaseSensitive seq,
                                                                      int number) {
        assertTrue(seq.size() > number);
        NucleotideSequenceCaseSensitive result = seq;
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

    public static NucleotideSequenceCaseSensitive makeRandomReplacements(NucleotideSequenceCaseSensitive seq,
                                                                         int number) {
        NucleotideSequenceCaseSensitive result = seq;
        int currentPosition;
        for (int i = 0; i < number; i++) {
            currentPosition = rg.nextInt(seq.size());
            result = SequencesUtils.concatenate(result.getRange(0, currentPosition),
                    TestUtil.randomSequence(NucleotideSequenceCaseSensitive.ALPHABET, 1, 1),
                    result.getRange(currentPosition + 1, seq.size()));
        }
        return result;
    }

    public static NucleotideSequenceCaseSensitive makeRandomErrors(NucleotideSequenceCaseSensitive seq, int number) {
        NucleotideSequenceCaseSensitive result = seq;
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

    public static NucleotideSequenceCaseSensitive toLowerCase(NucleotideSequenceCaseSensitive seq) {
        return new NucleotideSequenceCaseSensitive(seq.toString().toLowerCase());
    }

    public static NucleotideSequence getRandomSubsequence(NucleotideSequence seq) {
        assertTrue(seq.size() > 0);
        int position1 = rg.nextInt(seq.size());
        int position2 = rg.nextInt(seq.size());

        return seq.getRange(Math.min(position1, position2), Math.max(position1, position2) + 1);
    }

    public static NSequenceWithQuality setRandomQuality(String seq) {
        int length = seq.length();
        byte[] quality = new byte[length];
        for (int i = 0; i < length; i++)
            quality[i] = (byte)(rg.nextInt(DEFAULT_GOOD_QUALITY + 1 - DEFAULT_BAD_QUALITY) + DEFAULT_BAD_QUALITY);
        return new NSequenceWithQuality(new NucleotideSequence(seq), new SequenceQuality(quality));
    }

    public static PatternAndTargetAlignmentScoring getTestScoring() {
        return new PatternAndTargetAlignmentScoring(0, -9, -10,
                DEFAULT_GOOD_QUALITY, DEFAULT_BAD_QUALITY, 0);
    }

    public static PatternAndTargetAlignmentScoring getRandomScoring() {
        int matchScore = rg.nextInt(20) - 10;
        int mismatchScore = Math.min(-1, matchScore - 1 - rg.nextInt(10));
        int gapPenalty = Math.min(-1, matchScore - 1 - rg.nextInt(10));
        byte goodQuality = (byte)(rg.nextInt(10) + 24);
        byte badQuality = (byte)(rg.nextInt(10));
        int minMismatchDifference = Math.min(matchScore - mismatchScore, matchScore - gapPenalty);
        int maxQualityPenalty = (minMismatchDifference > 1) ? -rg.nextInt(minMismatchDifference) : 0;
        return new PatternAndTargetAlignmentScoring(matchScore, mismatchScore, gapPenalty, goodQuality, badQuality,
                maxQualityPenalty);
    }

    public static String inQuotes(String str) {
        return '"' + str + '"';
    }

    public static String repeatString(String str, int num) {
        return new String(new char[num]).replace("\0", str);
    }

    public enum RandomStringType {
        UNICODE,
        LIMITED,
        LETTERS_AND_NUMBERS,
        QUERY_CHARACTERS
    }

    public static String getRandomString(int length) {
        return getRandomString(length, "", UNICODE);
    }

    public static String getRandomString(int length, String exclude) {
        return getRandomString(length, exclude, UNICODE);
    }

    public static String getRandomString(int length, String exclude, RandomStringType type) {
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < length; i++) {
            char c;
            do {
                switch (type) {
                    case LIMITED:
                        c = (char)(rg.nextInt(rg.nextInt(128) + 128));
                        break;
                    case LETTERS_AND_NUMBERS:
                        c = LN_STRING.charAt(rg.nextInt(LN_STRING.length()));
                        break;
                    case QUERY_CHARACTERS:
                        c = QUERY_CHAR_STRING.charAt(rg.nextInt(QUERY_CHAR_STRING.length()));
                        break;
                    case UNICODE:
                    default:
                        c = (char)(rg.nextInt(rg.nextInt(2) == 0 ? 128 : Character.MAX_VALUE));
                }
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
            String groupName = getRandomString(rg.nextInt(30) + 1, "", LETTERS_AND_NUMBERS);
            if (groupEdgePositions.stream().anyMatch(g -> g.getGroupEdge().getGroupName().equals(groupName)))
                continue;
            int leftPosition = rg.nextInt(maxCoordinate);
            int rightPosition = rg.nextInt(maxCoordinate - leftPosition) + leftPosition + 1;
            groupEdgePositions.add(new GroupEdgePosition(new GroupEdge(groupName, true), leftPosition));
            groupEdgePositions.add(new GroupEdgePosition(new GroupEdge(groupName, false), rightPosition));
        }
        return groupEdgePositions;
    }

    public static void configureRandomPatternAligner() {
        PatternAligner.allowValuesOverride();
        PatternAligner.init(getRandomScoring(), -rg.nextInt(3), rg.nextInt(4), rg.nextInt(5) - 1);
    }

    public static FuzzyMatchPattern getRandomFuzzyPattern(long scoreThreshold, boolean withGroups) {
        int length = rg.nextInt(150) + 1;
        RandomBorders randomBorders = new RandomBorders(length);
        RandomCuts randomCuts = new RandomCuts(length);
        NucleotideSequenceCaseSensitive seq = TestUtil.randomSequence(NucleotideSequenceCaseSensitive.ALPHABET,
                length, length);
        return new FuzzyMatchPattern(scoreThreshold, seq, randomCuts.left, randomCuts.right,
                randomBorders.left, randomBorders.right,
                withGroups ? getRandomGroupsForFuzzyMatch(length) : new ArrayList<>());
    }

    public static RepeatPattern getRandomRepeatPattern(long scoreThreshold, boolean withGroups) {
        int minRepeats = rg.nextInt(10) + 1;
        int maxRepeats = rg.nextInt(100) + minRepeats;
        RandomBorders randomBorders = new RandomBorders(maxRepeats);
        NucleotideSequenceCaseSensitive seq = TestUtil.randomSequence(NucleotideSequenceCaseSensitive.ALPHABET,
                1, 1);
        return new RepeatPattern(scoreThreshold, seq, minRepeats, maxRepeats, randomBorders.left, randomBorders.right,
                withGroups ? getRandomGroupsForFuzzyMatch(maxRepeats) : new ArrayList<>());
    }

    public static AnyPattern getRandomAnyPattern(long scoreThreshold, boolean withGroups) {
        return new AnyPattern(scoreThreshold, withGroups ? getRandomGroupsForFuzzyMatch(1).stream()
                .map(GroupEdgePosition::getGroupEdge).collect(Collectors.toCollection(ArrayList::new))
                : new ArrayList<>());
    }

    public static SinglePattern getRandomBasicPattern() {
        return getRandomBasicPattern(false);
    }

    public static SinglePattern getRandomBasicPattern(long scoreThreshold) {
        return getRandomBasicPattern(scoreThreshold, false);
    }

    public static SinglePattern getRandomBasicPattern(boolean withGroups) {
        return getRandomBasicPattern(rg.nextInt(150) - 100, withGroups);
    }

    public static SinglePattern getRandomBasicPattern(long scoreThreshold, boolean withGroups) {
        switch (rg.nextInt(3)) {
            case 0:
                return getRandomFuzzyPattern(scoreThreshold, withGroups);
            case 1:
                return getRandomRepeatPattern(scoreThreshold, withGroups);
            default:
                return getRandomAnyPattern(scoreThreshold, withGroups);
        }
    }

    public static SinglePattern getRandomSinglePattern(SinglePattern... patterns) {
        return getRandomSinglePattern(rg.nextInt(150) - 100, patterns);
    }

    public static SinglePattern getRandomSinglePattern(long scoreThreshold, SinglePattern... singlePatterns) {
        SinglePattern[] patterns;
        if (singlePatterns.length == 0) {
            int numPatterns = rg.nextInt(5) + 1;
            patterns = new SinglePattern[numPatterns];
            for (int i = 0; i < numPatterns; i++)
                patterns[i] = getRandomBasicPattern(scoreThreshold);
        } else
            patterns = singlePatterns;
        boolean foundAnyPattern = Arrays.stream(patterns).anyMatch(p -> p instanceof AnyPattern);
        switch (patterns[0] instanceof AnyPattern ? 0 : rg.nextInt(foundAnyPattern ? 3 : 7)) {
            case 0:
                return patterns[0];
            case 1:
                return new FilterPattern(scoreThreshold, new ScoreFilter(-rg.nextInt(75)), patterns[0]);
            case 2:
                return new FilterPattern(scoreThreshold, new StickFilter(rg.nextBoolean(), rg.nextInt(30)),
                        patterns[0]);
            case 3:
                return new AndPattern(scoreThreshold, patterns);
            case 4:
                return new PlusPattern(scoreThreshold, patterns);
            case 5:
                return new SequencePattern(scoreThreshold, patterns);
            case 6:
            default:
                return new OrPattern(scoreThreshold, patterns);
        }
    }

    public static MultipleReadsOperator getRandomMultiReadPattern(MultipleReadsOperator... patterns) {
        return getRandomMultiReadPattern(rg.nextInt(150) - 100, -1, patterns);
    }

    public static MultipleReadsOperator getRandomMultiReadPattern(int numPatterns) {
        return getRandomMultiReadPattern(rg.nextInt(150) - 100, numPatterns);
    }

    public static MultipleReadsOperator getRandomMultiReadPattern(long scoreThreshold, int numPatterns,
                                                                  MultipleReadsOperator... patterns) {
        if (patterns.length == 0) {
            if (numPatterns == -1)
                numPatterns = rg.nextInt(5) + 1;
            SinglePattern[] basicPatterns = new SinglePattern[numPatterns];
            for (int i = 0; i < numPatterns; i++)
                basicPatterns[i] = getRandomBasicPattern(scoreThreshold);
            return new MultiPattern(scoreThreshold, basicPatterns);
        } else {
            switch (rg.nextInt(4)) {
                case 0:
                    return new AndOperator(scoreThreshold, patterns);
                case 1:
                    return new OrOperator(scoreThreshold, patterns);
                case 2:
                    return new NotOperator(scoreThreshold, patterns[0]);
                case 3:
                default:
                    return new MultipleReadsFilterPattern(scoreThreshold,
                            new ScoreFilter(-rg.nextInt(75)), patterns[0]);
            }
        }
    }

    public static MultipleReadsOperator[] singleToMultiPatterns(SinglePattern... singlePatterns) {
        return singleToMultiPatterns(singlePatterns[0].getScoreThreshold(), singlePatterns);
    }

    public static MultipleReadsOperator[] singleToMultiPatterns(long scoreThreshold,
                                                                SinglePattern... singlePatterns) {
        return Arrays.stream(singlePatterns).map(sp -> new MultiPattern(scoreThreshold, sp))
                .toArray(MultipleReadsOperator[]::new);
    }

    public static String bestToString(MatchingResult matchingResult) {
        return bestToString(matchingResult, true);
    }

    public static String bestToString(MatchingResult matchingResult, boolean fairSorting) {
        return matchingResult.getBestMatch(fairSorting).getValue().getSequence().toString();
    }

    public static MultiNSequenceWithQuality createMultiNSeq(String seq) {
        return new MultiNSequenceWithQualityImpl(new NSequenceWithQuality(seq));
    }

    public static MultiNSequenceWithQuality createMultiNSeq(String seq, int repeats) {
        NSequenceWithQuality singleSeq = new NSequenceWithQuality(seq);
        return new MultiNSequenceWithQualityImpl(Collections.nCopies(repeats, singleSeq)
                .toArray(new NSequenceWithQuality[repeats]));
    }

    public static void assertFileEquals(String fileName1, String fileName2) throws Exception {
        assertArrayEquals(Files.readAllBytes(Paths.get(fileName1)), Files.readAllBytes(Paths.get(fileName2)));
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

    public static class RandomCuts {
        public final int left;
        public final int right;

        public RandomCuts(int motifSize) {
            int leftCut = 0;
            int rightCut = 0;
            if (motifSize > 1)
                do {
                    leftCut = rg.nextInt(motifSize);
                    rightCut = rg.nextInt(motifSize);
                } while (leftCut + rightCut >= motifSize);
            this.left = leftCut;
            this.right = rightCut;
        }
    }
}
