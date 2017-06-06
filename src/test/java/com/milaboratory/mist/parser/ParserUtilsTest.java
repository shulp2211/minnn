package com.milaboratory.mist.parser;

import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;

import java.util.*;

import static com.milaboratory.mist.parser.ParserFormat.*;
import static com.milaboratory.mist.parser.ParserUtils.*;
import static com.milaboratory.mist.util.CommonTestUtils.getRandomString;
import static org.junit.Assert.*;

public class ParserUtilsTest {
    @Rule
    public final ExpectedException exception = ExpectedException.none();

    @Test
    public void getScoreThresholdsSimplifiedSyntaxTest() throws Exception {
        final String start = "FilterPattern(ScoreFilter(";
        Random randomGenerator = new Random();
        for (int i = 0; i < randomGenerator.nextInt(500) + 500; i++) {
            int repeats = randomGenerator.nextInt(10) + 1;
            int nested = randomGenerator.nextInt(10) + 1;
            ArrayList<ArrayList<Integer>> scores = new ArrayList<>();
            StringBuilder target = new StringBuilder();
            for (int r = 0; r < repeats; r++) {
                scores.add(new ArrayList<>());
                target.append(getRandomString(randomGenerator.nextInt(100)).replaceAll("[(){}\\[\\]]", ""));
                for (int n = 0; n < nested; n++) {
                    scores.get(r).add(-randomGenerator.nextInt(100));
                    target.append(start);
                    target.append(scores.get(r).get(n));
                    target.append("), ");
                    target.append(getRandomString(randomGenerator.nextInt(100) + 10).replaceAll("[(){}\\[\\]]", ""));
                }
                for (int n = 0; n < nested; n++) {
                    target.append(")");
                    target.append(getRandomString(randomGenerator.nextInt(100)).replaceAll("[(){}\\[\\]]", ""));
                }
            }
            ArrayList<ScoreThreshold> scoreThresholds = getScoreThresholds(target.toString(), SIMPLIFIED);
            assertEquals(repeats * nested, scoreThresholds.size());
            for (int r = 0; r < repeats; r++)
                for (int n = 0; n < nested; n++) {
                    assertEquals((int)(scores.get(r).get(n)), scoreThresholds.get(r * nested + n).threshold);
                    assertEquals(n, scoreThresholds.get(r * nested + n).nestedLevel);
                }
        }
        exception.expect(ParserException.class);
        getScoreThresholds(start, SIMPLIFIED);
    }
}
