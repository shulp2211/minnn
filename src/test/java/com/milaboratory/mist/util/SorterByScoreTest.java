package com.milaboratory.mist.util;

import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;

import static com.milaboratory.mist.util.CommonTestTemplates.predefinedMatchesTest;
import static org.junit.Assert.*;

public class SorterByScoreTest {
    @Rule
    public final ExpectedException exception = ExpectedException.none();

    @Test
    public void simplePredefinedMatchesTest() throws Exception {
        predefinedMatchesTest(true, false);
    }

    @Test
    public void matchesWithMisplacedRangesTest() throws Exception {

    }

    @Test
    public void matchesWithNullValuesTest() throws Exception {

    }

    @Test
    public void matchesFromOperatorsTest() throws Exception {

    }

    @Test
    public void randomGeneratedMatchesTest() throws Exception {

    }

    @Test
    public void randomMatchesFromOperatorsTest() throws Exception {

    }

    @Test
    public void fairSortingSimpleTest() throws Exception {
        predefinedMatchesTest(true, true);
    }

    @Test
    public void fairSortingRandomTest() throws Exception {

    }
}
