package com.milaboratory.mist.util;

import cc.redberry.pipe.OutputPort;
import com.milaboratory.core.Range;
import com.milaboratory.core.sequence.NSequenceWithQuality;
import com.milaboratory.core.sequence.NucleotideSequence;
import com.milaboratory.core.sequence.SequencesUtils;
import com.milaboratory.mist.pattern.CaptureGroupMatch;
import com.milaboratory.mist.pattern.FuzzyMatchPattern;
import com.milaboratory.mist.pattern.Match;
import com.milaboratory.mist.pattern.MatchValidationType;
import com.milaboratory.test.TestUtil;

import java.util.*;

import static com.milaboratory.mist.pattern.Match.WHOLE_PATTERN_MATCH_GROUP_NAME_PREFIX;
import static com.milaboratory.mist.pattern.Match.COMMON_GROUP_NAME_PREFIX;
import static org.junit.Assert.*;

class CommonTestTemplates {
    static void predefinedMatchesApproximateSorterTest(boolean sortByScore, boolean fairSorting) throws Exception {
        NSequenceWithQuality seqSingle = new NSequenceWithQuality("AATTAAGGCAAA");
        NSequenceWithQuality seqMulti1 = new NSequenceWithQuality("AATTAAGGCAAA");
        NSequenceWithQuality seqMulti2 = new NSequenceWithQuality("ATTAGACA");

        Map<String, CaptureGroupMatch> testGroupsSingle1 = new HashMap<String, CaptureGroupMatch>() {{
            put(WHOLE_PATTERN_MATCH_GROUP_NAME_PREFIX + "0", new CaptureGroupMatch(seqSingle, (byte)1, new Range(6, 9)));
            put(COMMON_GROUP_NAME_PREFIX + "0", new CaptureGroupMatch(seqSingle, (byte)1, new Range(6, 7)));
            put(COMMON_GROUP_NAME_PREFIX + "1", new CaptureGroupMatch(seqSingle, (byte)1, new Range(7, 9)));
        }};

        Map<String, CaptureGroupMatch> testGroupsSingle2 = new HashMap<String, CaptureGroupMatch>() {{
            put(WHOLE_PATTERN_MATCH_GROUP_NAME_PREFIX + "0", new CaptureGroupMatch(seqSingle, (byte)1, new Range(0, 8)));
            put(COMMON_GROUP_NAME_PREFIX + "2", new CaptureGroupMatch(seqSingle, (byte)1, new Range(0, 4)));
            put(COMMON_GROUP_NAME_PREFIX + "3", new CaptureGroupMatch(seqSingle, (byte)1, new Range(5, 8)));
        }};

        Map<String, CaptureGroupMatch> testGroupsSingle3 = new HashMap<String, CaptureGroupMatch>() {{
            put(WHOLE_PATTERN_MATCH_GROUP_NAME_PREFIX + "0", new CaptureGroupMatch(seqSingle, (byte)1, new Range(3, 5)));
        }};

        Map<String, CaptureGroupMatch> testGroupsSingle4 = new HashMap<String, CaptureGroupMatch>() {{
            put(WHOLE_PATTERN_MATCH_GROUP_NAME_PREFIX + "0", new CaptureGroupMatch(seqSingle, (byte)1, new Range(0, 2)));
        }};

        Map<String, CaptureGroupMatch> testGroupsMulti1 = new HashMap<String, CaptureGroupMatch>() {{
            put(WHOLE_PATTERN_MATCH_GROUP_NAME_PREFIX + "0", new CaptureGroupMatch(seqMulti1, (byte)1, new Range(0, 9)));
            put(COMMON_GROUP_NAME_PREFIX + "0", new CaptureGroupMatch(seqMulti1, (byte)1, new Range(1, 4)));
            put(COMMON_GROUP_NAME_PREFIX + "1", new CaptureGroupMatch(seqMulti1, (byte)1, new Range(4, 8)));
            put(WHOLE_PATTERN_MATCH_GROUP_NAME_PREFIX + "1", new CaptureGroupMatch(seqMulti2, (byte)1, new Range(0, 8)));
            put(COMMON_GROUP_NAME_PREFIX + "2", new CaptureGroupMatch(seqMulti2, (byte)1, new Range(0, 4)));
            put(COMMON_GROUP_NAME_PREFIX + "3", new CaptureGroupMatch(seqMulti2, (byte)1, new Range(5, 8)));
        }};

        Map<String, CaptureGroupMatch> testGroupsMulti2 = new HashMap<String, CaptureGroupMatch>() {{
            put(WHOLE_PATTERN_MATCH_GROUP_NAME_PREFIX + "0", new CaptureGroupMatch(seqMulti1, (byte)1, new Range(1, 5)));
            put(WHOLE_PATTERN_MATCH_GROUP_NAME_PREFIX + "1", new CaptureGroupMatch(seqMulti2, (byte)1, new Range(2, 4)));
        }};

        Map<String, CaptureGroupMatch> testGroupsMulti3 = new HashMap<String, CaptureGroupMatch>() {{
            put(WHOLE_PATTERN_MATCH_GROUP_NAME_PREFIX + "0", new CaptureGroupMatch(seqMulti1, (byte)1, new Range(4, 6)));
            put(COMMON_GROUP_NAME_PREFIX + "0", new CaptureGroupMatch(seqMulti1, (byte)1, new Range(4, 6)));
            put(WHOLE_PATTERN_MATCH_GROUP_NAME_PREFIX + "1", new CaptureGroupMatch(seqMulti2, (byte)1, new Range(1, 8)));
            put(COMMON_GROUP_NAME_PREFIX + "2", new CaptureGroupMatch(seqMulti2, (byte)1, new Range(1, 4)));
            put(COMMON_GROUP_NAME_PREFIX + "3", new CaptureGroupMatch(seqMulti2, (byte)1, new Range(5, 8)));
        }};

        Match testMatchSingle1 = new Match(1, 10, testGroupsSingle1);
        Match testMatchSingle2 = new Match(1, -3, testGroupsSingle2);
        Match testMatchSingle3 = new Match(1, -3, testGroupsSingle3);
        Match testMatchSingle4 = new Match(1, -4.5f, testGroupsSingle4);
        Match testMatchMulti1 = new Match(2, 4.0f, testGroupsMulti1);
        Match testMatchMulti2 = new Match(2, 3.9f, testGroupsMulti2);
        Match testMatchMulti3 = new Match(2, 4.0f, testGroupsMulti3);

        ApproximateSorter sorterSingle1, sorterSingle2, sorterSingle3, sorterSingle4, sorterSingle5;
        ApproximateSorter sorterMulti1, sorterMulti2, sorterMulti3;
        if (sortByScore) {
            sorterSingle1 = new SorterByScore(false, false, true,
                    fairSorting, MatchValidationType.INTERSECTION);
            sorterSingle2 = new SorterByScore(false, false, false,
                    fairSorting, MatchValidationType.INTERSECTION);
            sorterSingle3 = new SorterByScore(false, false, true,
                    fairSorting, MatchValidationType.ORDER);
            sorterSingle4 = new SorterByScore(false, false, false,
                    fairSorting, MatchValidationType.ORDER);
            sorterSingle5 = new SorterByScore(false, false, true,
                    fairSorting, MatchValidationType.ALWAYS);
            sorterMulti1 = new SorterByScore(true, true, true,
                    fairSorting, MatchValidationType.ALWAYS);
            sorterMulti2 = new SorterByScore(true, true, false,
                    fairSorting, MatchValidationType.ALWAYS);
            sorterMulti3 = new SorterByScore(true, false, true,
                    fairSorting, MatchValidationType.ALWAYS);
        } else {
            sorterSingle1 = new SorterByCoordinate(false, false, true,
                    fairSorting, MatchValidationType.INTERSECTION);
            sorterSingle2 = new SorterByCoordinate(false, false, false,
                    fairSorting, MatchValidationType.INTERSECTION);
            sorterSingle3 = new SorterByCoordinate(false, false, true,
                    fairSorting, MatchValidationType.ORDER);
            sorterSingle4 = new SorterByCoordinate(false, false, false,
                    fairSorting, MatchValidationType.ORDER);
            sorterSingle5 = new SorterByCoordinate(false, false, true,
                    fairSorting, MatchValidationType.ALWAYS);
            sorterMulti1 = new SorterByCoordinate(true, true, true,
                    fairSorting, MatchValidationType.ALWAYS);
            sorterMulti2 = new SorterByCoordinate(true, true, false,
                    fairSorting, MatchValidationType.ALWAYS);
            sorterMulti3 = new SorterByCoordinate(true, false, true,
                    fairSorting, MatchValidationType.ALWAYS);
        }

        TestMatchesOutputPort testPortSingle1 = new TestMatchesOutputPort(testMatchSingle1, testMatchSingle2, testMatchSingle2);
        TestMatchesOutputPort testPortSingle2 = new TestMatchesOutputPort(testMatchSingle3, testMatchSingle4, testMatchSingle3);
        TestMatchesOutputPort testPortSingle3;
        if (sortByScore)
            testPortSingle3 = new TestMatchesOutputPort(testMatchSingle1, testMatchSingle2, testMatchSingle3, testMatchSingle4);
        else
            testPortSingle3 = new TestMatchesOutputPort(testMatchSingle4, testMatchSingle2, testMatchSingle3, testMatchSingle1);
        TestMatchesOutputPort testPortSingle4 = new TestMatchesOutputPort(testMatchSingle4);
        TestMatchesOutputPort testPortMulti1 = new TestMatchesOutputPort(testMatchMulti1);
        TestMatchesOutputPort testPortMulti2 = new TestMatchesOutputPort(testMatchMulti2, testMatchMulti3, testMatchMulti2);
        TestMatchesOutputPort testPortMulti3 = new TestMatchesOutputPort(testMatchMulti3, testMatchMulti2, testMatchMulti1);
        TestMatchesOutputPort testPortEmpty = new TestMatchesOutputPort();

        assertEquals(3, countPortValues(sorterSingle1.getOutputPort(new ArrayList<OutputPort<Match>>() {{
                add(testPortSingle1.getCopy()); add(testPortSingle2.getCopy()); }})));
        assertEquals(3, countPortValues(sorterSingle1.getOutputPort(new ArrayList<OutputPort<Match>>() {{
                add(testPortSingle1.getCopy()); add(testPortSingle2.getCopy()); }})));
        assertEquals(3, countPortValues(sorterSingle1.getOutputPort(new ArrayList<OutputPort<Match>>() {{
                add(testPortSingle1.getCopy()); add(testPortSingle2.getCopy()); add(testPortSingle3.getCopy()); }})));
        assertEquals(3, countPortValues(sorterSingle2.getOutputPort(new ArrayList<OutputPort<Match>>() {{
                add(testPortSingle1.getCopy()); add(testPortSingle2.getCopy()); }})));
        assertEquals(0, countPortValues(sorterSingle3.getOutputPort(new ArrayList<OutputPort<Match>>() {{
                add(testPortSingle1.getCopy()); add(testPortSingle2.getCopy()); }})));
        assertEquals(1, countPortValues(sorterSingle4.getOutputPort(new ArrayList<OutputPort<Match>>() {{
                add(testPortSingle4.getCopy()); add(testPortSingle1.getCopy()); }})));
        assertEquals(1, countPortValues(sorterSingle5.getOutputPort(new ArrayList<OutputPort<Match>>() {{
                add(testPortSingle4.getCopy()); add(testPortSingle4.getCopy()); }})));
        assertEquals(0, countPortValues(sorterSingle5.getOutputPort(new ArrayList<OutputPort<Match>>() {{
                add(testPortSingle1.getCopy()); add(testPortEmpty); add(testPortSingle4.getCopy()); }})));
        assertEquals(3, countPortValues(sorterMulti1.getOutputPort(new ArrayList<OutputPort<Match>>() {{
                add(testPortMulti1.getCopy()); add(testPortEmpty); add(testPortMulti2.getCopy()); }})));
        assertEquals(3, countPortValues(sorterMulti1.getOutputPort(new ArrayList<OutputPort<Match>>() {{
                add(testPortMulti1.getCopy()); add(testPortEmpty); add(testPortMulti2.getCopy()); }})));
        assertEquals(3, countPortValues(sorterMulti2.getOutputPort(new ArrayList<OutputPort<Match>>() {{
                add(testPortMulti1.getCopy()); add(testPortEmpty); add(testPortMulti2.getCopy()); }})));
        assertEquals(0, countPortValues(sorterMulti3.getOutputPort(new ArrayList<OutputPort<Match>>() {{
                add(testPortMulti1.getCopy()); add(testPortEmpty); add(testPortMulti2.getCopy()); }})));
        assertEquals(3, countPortValues(sorterMulti3.getOutputPort(new ArrayList<OutputPort<Match>>() {{
                add(testPortMulti1.getCopy()); add(testPortMulti2.getCopy()); }})));
        assertEquals(9, countPortValues(sorterMulti3.getOutputPort(new ArrayList<OutputPort<Match>>() {{
                add(testPortMulti1.getCopy()); add(testPortMulti2.getCopy()); add(testPortMulti3.getCopy()); }})));

        if (sortByScore) {
            assertEquals(7, sorterSingle1.getOutputPort(new ArrayList<OutputPort<Match>>() {{
                    add(testPortSingle1.getCopy()); add(testPortSingle2.getCopy()); }})
                    .take().getScore(), 0.0001);
            if (fairSorting)
                assertEquals(8, sorterMulti1.getOutputPort(new ArrayList<OutputPort<Match>>() {{
                        add(testPortMulti1.getCopy()); add(testPortEmpty); add(testPortMulti2.getCopy()); }})
                        .take().getScore(), 0.0001);
            else
                assertEquals(7.9, sorterMulti1.getOutputPort(new ArrayList<OutputPort<Match>>() {{
                        add(testPortMulti1.getCopy()); add(testPortEmpty); add(testPortMulti2.getCopy()); }})
                        .take().getScore(), 0.0001);
            assertEquals(7, sorterSingle1.getOutputPort(new ArrayList<OutputPort<Match>>() {{
                    add(testPortSingle1.getCopy()); add(testPortSingle3.getCopy()); }})
                    .take().getScore(), 0.0001);
            assertEquals(10, sorterSingle2.getOutputPort(new ArrayList<OutputPort<Match>>() {{
                    add(testPortSingle1.getCopy()); add(testPortSingle3.getCopy()); }})
                    .take().getScore(), 0.0001);

            OutputPort<Match> testPort1 = sorterSingle1.getOutputPort(new ArrayList<OutputPort<Match>>() {{
                    add(testPortSingle1.getCopy()); add(testPortSingle3.getCopy()); }});
            assertNotNull(testPort1.take());
            assertEquals(5.5, testPort1.take().getScore(), 0.0001);
            assertNull(testPort1.take());
        } else {
            if (fairSorting)
                assertEquals(new Range(0, 9), sorterSingle1.getOutputPort(new ArrayList<OutputPort<Match>>() {{
                        add(testPortSingle1.getCopy()); add(testPortSingle2.getCopy()); }})
                        .take().getWholePatternMatch().getRange());
            else
                assertEquals(new Range(3, 9), sorterSingle1.getOutputPort(new ArrayList<OutputPort<Match>>() {{
                    add(testPortSingle1.getCopy()); add(testPortSingle2.getCopy()); }})
                    .take().getWholePatternMatch().getRange());
            assertEquals(new Range(0, 8), sorterMulti1.getOutputPort(new ArrayList<OutputPort<Match>>() {{
                        add(testPortMulti1.getCopy()); add(testPortEmpty); add(testPortMulti2.getCopy()); }})
                        .take().getWholePatternMatch(1).getRange());
            assertEquals(new Range(0, 9), sorterSingle1.getOutputPort(new ArrayList<OutputPort<Match>>() {{
                    add(testPortSingle1.getCopy()); add(testPortSingle3.getCopy()); }})
                    .take().getWholePatternMatch().getRange());
            assertEquals(new Range(0, 9), sorterSingle3.getOutputPort(new ArrayList<OutputPort<Match>>() {{
                    add(testPortSingle3.getCopy()); add(testPortSingle1.getCopy()); }})
                    .take().getWholePatternMatch().getRange());

            OutputPort<Match> testPort1 = sorterSingle1.getOutputPort(new ArrayList<OutputPort<Match>>() {{
                    add(testPortSingle1.getCopy()); add(testPortSingle3.getCopy()); }});
            assertNotNull(testPort1.take());
            assertEquals(new Range(3, 9), testPort1.take().getWholePatternMatch().getRange());
            assertNull(testPort1.take());
        }

        OutputPort<Match> testPort2 = sorterSingle1.getOutputPort(new ArrayList<OutputPort<Match>>() {{
                add(testPortSingle3.getCopy()); add(testPortSingle3.getCopy()); }});
        System.out.println();
        for (int i = 0; i < 6; i++) {
            Match currentMatch = testPort2.take();
            System.out.println(currentMatch.getScore() + ", " + currentMatch.getWholePatternMatch().getRange());
        }
        System.out.println();
        assertNull(testPort2.take());
    }

    static void randomMatchesApproximateSorterTest(boolean sortByScore, boolean fairSorting) throws Exception {
        ApproximateSorter sorter;
        Random randomGenerator = new Random();
        int its = TestUtil.its(30, 100);
        for (int i = 0; i < its; ++i) {
            int numberOfMatches = randomGenerator.nextInt(10) + 1;
            Match[] matches = new Match[numberOfMatches];
            ArrayList<Map<String, CaptureGroupMatch>> testGroups = new ArrayList<>();
            if (sortByScore)
                sorter = new SorterByScore(false, randomGenerator.nextBoolean(),
                        randomGenerator.nextBoolean(), fairSorting, MatchValidationType.INTERSECTION);
            else
                sorter = new SorterByCoordinate(false, randomGenerator.nextBoolean(),
                        randomGenerator.nextBoolean(), randomGenerator.nextBoolean(), MatchValidationType.INTERSECTION);

            NSequenceWithQuality target = new NSequenceWithQuality(TestUtil.randomSequence(NucleotideSequence.ALPHABET,
                    numberOfMatches, numberOfMatches + randomGenerator.nextInt(100)).toString());
            for (int j = 0; j < numberOfMatches; j++) {
                final int currentRangeLeft = j;
                testGroups.add(new HashMap<String, CaptureGroupMatch>() {{
                    put(WHOLE_PATTERN_MATCH_GROUP_NAME_PREFIX + "0", new CaptureGroupMatch(target,
                            (byte)randomGenerator.nextInt(200), new Range(currentRangeLeft, currentRangeLeft + 1)));
                }});
                matches[j] = new Match(1, randomGenerator.nextFloat() * 1000 - 500, testGroups.get(j));
            }

            TestMatchesOutputPort testPort = new TestMatchesOutputPort(matches);
            int expectedMatchesNum = numberOfMatches * (numberOfMatches - 1);

            assertEquals(expectedMatchesNum, countPortValues(sorter.getOutputPort(new ArrayList<OutputPort<Match>>() {{
                add(testPort.getCopy()); add(testPort.getCopy()); }})));
        }
    }

    static void randomMatchesFromOperatorsApproximateSorterTest(boolean sortByScore) throws Exception {
        ApproximateSorter sorter;
        Random randomGenerator = new Random();
        int its = TestUtil.its(30, 100);
        for (int i = 0; i < its; ++i) {
            int numberOfFragments = randomGenerator.nextInt(3) + 4;
            int expectedMatchesNum = numberOfFragments * (numberOfFragments - 1) * (numberOfFragments - 2) * (numberOfFragments - 3);
            int spaceLength = randomGenerator.nextInt(3);
            if (sortByScore)
                sorter = new SorterByScore(false, randomGenerator.nextBoolean(),
                        randomGenerator.nextBoolean(), randomGenerator.nextBoolean(), MatchValidationType.INTERSECTION);
            else
                sorter = new SorterByCoordinate(false, randomGenerator.nextBoolean(),
                        randomGenerator.nextBoolean(), randomGenerator.nextBoolean(), MatchValidationType.INTERSECTION);

            NucleotideSequence target = TestUtil.randomSequence(NucleotideSequence.ALPHABET, 0, spaceLength);
            NucleotideSequence fragment = TestUtil.randomSequence(NucleotideSequence.ALPHABET, 50, 63);
            for (int j = 0; j < numberOfFragments; j++) {
                NucleotideSequence space = TestUtil.randomSequence(NucleotideSequence.ALPHABET, 0, spaceLength);
                target = SequencesUtils.concatenate(target, fragment, space);
            }

            final NSequenceWithQuality finalTarget = new NSequenceWithQuality(target.toString());

            FuzzyMatchPattern pattern = new FuzzyMatchPattern(fragment);

            OutputPort<Match> testPort = sorter.getOutputPort(new ArrayList<OutputPort<Match>>() {{
                add(pattern.match(finalTarget).getMatches()); add(pattern.match(finalTarget).getMatches());
                add(pattern.match(finalTarget).getMatches()); add(pattern.match(finalTarget).getMatches()); }});

            assertEquals(expectedMatchesNum, countPortValues(testPort));
        }
    }

    static int countPortValues(OutputPort<Match> port) {
        int counter = 0;
        while (port.take() != null)
            counter++;
        return counter;
    }
}
