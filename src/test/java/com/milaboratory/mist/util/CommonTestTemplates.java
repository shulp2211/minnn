package com.milaboratory.mist.util;

import cc.redberry.pipe.OutputPort;
import com.milaboratory.core.Range;
import com.milaboratory.core.sequence.NSequenceWithQuality;
import com.milaboratory.core.sequence.NucleotideSequence;
import com.milaboratory.core.sequence.SequencesUtils;
import com.milaboratory.mist.pattern.*;
import com.milaboratory.test.TestUtil;

import java.util.*;

import static com.milaboratory.mist.util.CommonTestUtils.countPortValues;
import static org.junit.Assert.*;

class CommonTestTemplates {
    static void predefinedMatchesApproximateSorterTest(boolean sortByScore, boolean fairSorting) throws Exception {
        NSequenceWithQuality seqSingle = new NSequenceWithQuality("AATTAAGGCAAA");
        NSequenceWithQuality seqMulti1_1 = new NSequenceWithQuality("AATTAAGGCAAA");
        NSequenceWithQuality seqMulti1_2 = new NSequenceWithQuality("ATTAGACA");
        NSequenceWithQuality seqMulti2_1 = new NSequenceWithQuality("ACAATTAGCCA");
        NSequenceWithQuality seqMulti2_2 = new NSequenceWithQuality("TGGCAGATGCAC");

        ArrayList<MatchedItem> testMatchedItemsSingle1 = new ArrayList<MatchedItem>() {{
            add(new MatchedRange(seqSingle, (byte)1, 0, new Range(6, 9)));
            add(new MatchedGroupEdge(seqSingle, (byte)1, 0, new GroupEdge("0", true), 6));
            add(new MatchedGroupEdge(seqSingle, (byte)1, 0, new GroupEdge("0", false), 7));
            add(new MatchedGroupEdge(seqSingle, (byte)1, 0, new GroupEdge("1", true), 7));
            add(new MatchedGroupEdge(seqSingle, (byte)1, 0, new GroupEdge("1", false), 9));
        }};

        ArrayList<MatchedItem> testMatchedItemsSingle2 = new ArrayList<MatchedItem>() {{
            add(new MatchedRange(seqSingle, (byte)1, 0, new Range(0, 8)));
            add(new MatchedGroupEdge(seqSingle, (byte)1, 0, new GroupEdge("2", true), 0));
            add(new MatchedGroupEdge(seqSingle, (byte)1, 0, new GroupEdge("2", false), 4));
            add(new MatchedGroupEdge(seqSingle, (byte)1, 0, new GroupEdge("3", true), 5));
            add(new MatchedGroupEdge(seqSingle, (byte)1, 0, new GroupEdge("3", false), 8));
        }};

        ArrayList<MatchedItem> testMatchedItemsSingle3 = new ArrayList<MatchedItem>() {{
            add(new MatchedRange(seqSingle, (byte)1, 0, new Range(3, 5)));
        }};

        ArrayList<MatchedItem> testMatchedItemsSingle4 = new ArrayList<MatchedItem>() {{
            add(new MatchedRange(seqSingle, (byte)1, 0, new Range(0, 2)));
        }};

        ArrayList<MatchedItem> testMatchedItemsMulti1 = new ArrayList<MatchedItem>() {{
            add(new MatchedRange(seqMulti1_1, (byte)1, 0, new Range(0, 9)));
            add(new MatchedGroupEdge(seqMulti1_1, (byte)1, 0, new GroupEdge("0", true), 1));
            add(new MatchedGroupEdge(seqMulti1_1, (byte)1, 0, new GroupEdge("0", false), 4));
            add(new MatchedGroupEdge(seqMulti1_1, (byte)1, 0, new GroupEdge("1", true), 4));
            add(new MatchedGroupEdge(seqMulti1_1, (byte)1, 0, new GroupEdge("1", false), 8));
            add(new MatchedRange(seqMulti1_2, (byte)1, 1, new Range(0, 8)));
            add(new MatchedGroupEdge(seqMulti1_2, (byte)1, 1, new GroupEdge("2", true), 0));
            add(new MatchedGroupEdge(seqMulti1_2, (byte)1, 1, new GroupEdge("2", false), 4));
            add(new MatchedGroupEdge(seqMulti1_2, (byte)1, 1, new GroupEdge("3", true), 5));
            add(new MatchedGroupEdge(seqMulti1_2, (byte)1, 1, new GroupEdge("3", false), 8));
        }};

        ArrayList<MatchedItem> testMatchedItemsMulti2 = new ArrayList<MatchedItem>() {{
            add(new MatchedRange(seqMulti1_1, (byte)1, 0, new Range(1, 5)));
            add(new MatchedRange(seqMulti1_2, (byte)1, 1, new Range(2, 4)));
        }};

        ArrayList<MatchedItem> testMatchedItemsMulti3 = new ArrayList<MatchedItem>() {{
            add(new MatchedRange(seqMulti1_1, (byte)1, 0, new Range(4, 6)));
            add(new MatchedGroupEdge(seqMulti1_1, (byte)1, 0, new GroupEdge("0", true), 4));
            add(new MatchedGroupEdge(seqMulti1_1, (byte)1, 0, new GroupEdge("0", false), 6));
            add(new MatchedRange(seqMulti1_2, (byte)1, 1, new Range(1, 8)));
            add(new MatchedGroupEdge(seqMulti1_2, (byte)1, 1, new GroupEdge("2", true), 1));
            add(new MatchedGroupEdge(seqMulti1_2, (byte)1, 1, new GroupEdge("2", false), 4));
            add(new MatchedGroupEdge(seqMulti1_2, (byte)1, 1, new GroupEdge("3", true), 5));
            add(new MatchedGroupEdge(seqMulti1_2, (byte)1, 1, new GroupEdge("3", false), 8));
        }};

        ArrayList<MatchedItem> testMatchedItemsMulti4 = new ArrayList<MatchedItem>() {{
            add(new MatchedRange(seqMulti2_1, (byte)1, 0, new Range(0, 1)));
            add(new MatchedRange(seqMulti2_2, (byte)-2, 1, new Range(3, 11)));
        }};

        ArrayList<MatchedItem> testMatchedItemsMulti5 = new ArrayList<MatchedItem>() {{
            add(new MatchedRange(seqMulti2_1, (byte)1, 0, new Range(2, 3)));
            add(new MatchedRange(seqMulti2_2, (byte)-2, 1, new Range(3, 11)));
        }};

        Match testMatchSingle1 = new Match(1, 10, testMatchedItemsSingle1);
        Match testMatchSingle2 = new Match(1, -3, testMatchedItemsSingle2);
        Match testMatchSingle3 = new Match(1, -3, testMatchedItemsSingle3);
        Match testMatchSingle4 = new Match(1, -4.5f, testMatchedItemsSingle4);
        Match testMatchMulti1 = new Match(2, 4.0f, testMatchedItemsMulti1);
        Match testMatchMulti2 = new Match(2, 3.9f, testMatchedItemsMulti2);
        Match testMatchMulti3 = new Match(2, 4.0f, testMatchedItemsMulti3);
        Match testMatchMulti4 = new Match(2, 5.0f, testMatchedItemsMulti4);
        Match testMatchMulti5 = new Match(2, 5.0f, testMatchedItemsMulti5);

        ApproximateSorter sorterSingle1, sorterSingle2, sorterSingle3, sorterSingle4, sorterSingle5;
        ApproximateSorter sorterMulti1, sorterMulti2, sorterMulti3;
        if (sortByScore) {
            sorterSingle1 = new SorterByScore(false, true, fairSorting,
                    0, -1, MatchValidationType.INTERSECTION);
            sorterSingle2 = new SorterByScore(false, false, fairSorting,
                    0, -1, MatchValidationType.INTERSECTION);
            sorterSingle3 = new SorterByScore(false, true, fairSorting,
                    0, -1, MatchValidationType.ORDER);
            sorterSingle4 = new SorterByScore(false, false, fairSorting,
                    0, -1, MatchValidationType.ORDER);
            sorterSingle5 = new SorterByScore(false, true, fairSorting,
                    0, 0, MatchValidationType.LOGICAL_AND);
            sorterMulti1 = new SorterByScore(true, true, fairSorting,
                    0, 0, MatchValidationType.LOGICAL_OR);
            sorterMulti2 = new SorterByScore(true, false, fairSorting,
                    0, 0, MatchValidationType.LOGICAL_OR);
            sorterMulti3 = new SorterByScore(true, true, fairSorting,
                    0, 0, MatchValidationType.LOGICAL_AND);
        } else {
            sorterSingle1 = new SorterByCoordinate(false, true, fairSorting,
                    0, -1, MatchValidationType.INTERSECTION);
            sorterSingle2 = new SorterByCoordinate(false, false, fairSorting,
                    0, -1, MatchValidationType.INTERSECTION);
            sorterSingle3 = new SorterByCoordinate(false, true, fairSorting,
                    0, -1, MatchValidationType.ORDER);
            sorterSingle4 = new SorterByCoordinate(false, false, fairSorting,
                    0, -1, MatchValidationType.ORDER);
            sorterSingle5 = new SorterByCoordinate(false, true, fairSorting,
                    0, 0, MatchValidationType.LOGICAL_AND);
            sorterMulti1 = new SorterByCoordinate(true, true, fairSorting,
                    0, 0, MatchValidationType.LOGICAL_OR);
            sorterMulti2 = new SorterByCoordinate(true, false, fairSorting,
                    0, 0, MatchValidationType.LOGICAL_OR);
            sorterMulti3 = new SorterByCoordinate(true, true, fairSorting,
                    0, 0, MatchValidationType.LOGICAL_AND);
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
        TestMatchesOutputPort testPortMulti4 = new TestMatchesOutputPort(testMatchMulti4, testMatchMulti5, testMatchMulti4);
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
        assertEquals(3, countPortValues(sorterMulti3.getOutputPort(new ArrayList<OutputPort<Match>>() {{
                add(testPortMulti4.getCopy()); }})));

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
                        add(testPortSingle1.getCopy()); add(testPortSingle2.getCopy()); }}).take().getRange());
            else
                assertEquals(new Range(3, 9), sorterSingle1.getOutputPort(new ArrayList<OutputPort<Match>>() {{
                    add(testPortSingle1.getCopy()); add(testPortSingle2.getCopy()); }}).take().getRange());
            assertEquals(new Range(0, 8), sorterMulti1.getOutputPort(new ArrayList<OutputPort<Match>>() {{
                        add(testPortMulti1.getCopy()); add(testPortEmpty); add(testPortMulti2.getCopy()); }})
                        .take().getMatchedRange(1).getRange());
            assertEquals(new Range(0, 9), sorterSingle1.getOutputPort(new ArrayList<OutputPort<Match>>() {{
                    add(testPortSingle1.getCopy()); add(testPortSingle3.getCopy()); }}).take().getRange());
            assertEquals(new Range(0, 9), sorterSingle3.getOutputPort(new ArrayList<OutputPort<Match>>() {{
                    add(testPortSingle3.getCopy()); add(testPortSingle1.getCopy()); }}).take().getRange());

            OutputPort<Match> testPort1 = sorterSingle1.getOutputPort(new ArrayList<OutputPort<Match>>() {{
                    add(testPortSingle1.getCopy()); add(testPortSingle3.getCopy()); }});
            assertNotNull(testPort1.take());
            assertEquals(new Range(3, 9), testPort1.take().getRange());
            assertNull(testPort1.take());
        }

        OutputPort<Match> testPort2 = sorterSingle1.getOutputPort(new ArrayList<OutputPort<Match>>() {{
                add(testPortSingle3.getCopy()); add(testPortSingle3.getCopy()); }});
        System.out.println();
        for (int i = 0; i < 6; i++) {
            Match currentMatch = testPort2.take();
            System.out.println(currentMatch.getScore() + ", " + currentMatch.getRange());
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
            if (sortByScore)
                sorter = new SorterByScore(false, randomGenerator.nextBoolean(), fairSorting,
                        0, -1, MatchValidationType.INTERSECTION);
            else
                sorter = new SorterByCoordinate(false, randomGenerator.nextBoolean(), fairSorting,
                        0, -1, MatchValidationType.INTERSECTION);

            NSequenceWithQuality target = new NSequenceWithQuality(TestUtil.randomSequence(NucleotideSequence.ALPHABET,
                    numberOfMatches, numberOfMatches + randomGenerator.nextInt(100)).toString());
            for (int j = 0; j < numberOfMatches; j++) {
                final int currentRangeLeft = j;
                ArrayList<MatchedItem> testItems = new ArrayList<MatchedItem>() {{
                    add(new MatchedRange(target, (byte)randomGenerator.nextInt(200), 0,
                            new Range(currentRangeLeft, currentRangeLeft + 1))); }};
                matches[j] = new Match(1, randomGenerator.nextFloat() * 1000 - 500, testItems);
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
                sorter = new SorterByScore(false, randomGenerator.nextBoolean(), randomGenerator.nextBoolean(),
                        0, -1, MatchValidationType.INTERSECTION);
            else
                sorter = new SorterByCoordinate(false, randomGenerator.nextBoolean(), randomGenerator.nextBoolean(),
                        0, -1, MatchValidationType.INTERSECTION);

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
}
