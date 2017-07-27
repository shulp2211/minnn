package com.milaboratory.mist.parser;

import com.milaboratory.core.Range;
import com.milaboratory.core.sequence.NSequenceWithQuality;
import com.milaboratory.mist.pattern.*;
import org.junit.Test;

import java.util.*;
import java.util.stream.Collectors;

import static com.milaboratory.mist.util.CommonTestUtils.*;
import static org.junit.Assert.*;

public class ParserTest {
    @Test
    public void sampleNormalSyntaxQueriesTest() throws Exception {
        String[] queries = {
            "N{5}ATTAGACAT{4:6}",                                       // 0
            "ATTA + GACA + A & G + T",                                  // 1
            "ATTA + GACA + [A & G] + T",                                // 2
            "[ATTA][(UMI:GACA) >{2}]",                                  // 3
            " [ [ATTA|| GACA] + [ TTTT ||GGGG]]"                        // 4
        };
        String[] targets = {
            "TAATCATCCATTAGACATTTTTTTA",                                // 0
            "AGTAATTACCGGACATGCAT",                                     // 1
            "TATTAGA",                                                  // 2
            "GACAGGGG"                                                  // 3
        };
        Parser parser = new Parser(getTestPatternAligner(-100, 2, 0,
                -1));
        List<Pattern> patterns = Arrays.stream(queries).map(rethrow(parser::parseQuery)).collect(Collectors.toList());
        List<List<Match>> bestMatches = patterns.stream().map(p -> Arrays.stream(targets)
                .map(t -> p.match(new NSequenceWithQuality(t)).getBestMatch(true))
                .collect(Collectors.toList())).collect(Collectors.toList());
        List<List<Range>> matchedRanges = bestMatches.stream().map((List<Match> p) -> p.stream()
                .map(orNull(Match::getRange)).collect(Collectors.toList())).collect(Collectors.toList());

        // first get() is query number, second is target number
        assertEquals(new Range(4, 23), matchedRanges.get(0).get(0));
        assertEquals(new Range(1, 19), matchedRanges.get(1).get(1));
        assertEquals(new Range(4, 20), matchedRanges.get(2).get(1));
        assertEquals(new Range(1, 7), matchedRanges.get(3).get(2));
        assertEquals(new Range(0, 8), matchedRanges.get(4).get(3));
    }
}
