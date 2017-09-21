package com.milaboratory.mist.util;

class CommonTestTemplates {

//    static void randomMatchesFromOperatorsApproximateSorterTest(boolean sortByScore) throws Exception {
//        ApproximateSorter sorter;
//        int its = TestUtil.its(300, 500);
//        for (int i = 0; i < its; ++i) {
//            int singleOverlapPenalty = -rg.nextInt(1000);
//            PatternAligner patternAligner = getTestPatternAligner(500 * singleOverlapPenalty,
//                    0, -rg.nextInt(1000), singleOverlapPenalty);
//            int numberOfFragments = rg.nextInt(3) + 4;
//            int expectedMatchesNum = numberOfFragments * (numberOfFragments - 1) * (numberOfFragments - 2)
//                    * (numberOfFragments - 3);
//            int spaceLength = rg.nextInt(3);
//            if (sortByScore)
//                sorter = new SorterByScore(patternAligner, false, rg.nextBoolean(), rg.nextBoolean(),
//                        INTERSECTION);
//            else
//                sorter = new SorterByCoordinate(patternAligner, false, rg.nextBoolean(), rg.nextBoolean(),
//                        INTERSECTION);
//
//            NucleotideSequence target = TestUtil.randomSequence(NucleotideSequence.ALPHABET, 0, spaceLength);
//            NucleotideSequence fragment = TestUtil.randomSequence(NucleotideSequence.ALPHABET, 50, 63);
//            for (int j = 0; j < numberOfFragments; j++) {
//                NucleotideSequence space = TestUtil.randomSequence(NucleotideSequence.ALPHABET, 0, spaceLength);
//                target = SequencesUtils.concatenate(target, fragment, space);
//            }
//
//            final NSequenceWithQuality finalTarget = new NSequenceWithQuality(target.toString());
//
//            FuzzyMatchPattern pattern = new FuzzyMatchPattern(patternAligner, fragment);
//
//            OutputPort<Match> testPort = sorter.getOutputPort(addInfiniteLimits(new ArrayList<OutputPort<Match>>() {{
//                add(pattern.match(finalTarget).getMatches()); add(pattern.match(finalTarget).getMatches());
//                add(pattern.match(finalTarget).getMatches()); add(pattern.match(finalTarget).getMatches()); }}));
//
//            assertEquals(expectedMatchesNum, countPortValues(testPort));
//        }
//    }
}
