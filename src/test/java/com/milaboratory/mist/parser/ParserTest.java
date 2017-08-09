package com.milaboratory.mist.parser;

import com.milaboratory.core.Range;
import com.milaboratory.core.sequence.*;
import com.milaboratory.mist.pattern.*;
import org.junit.Test;

import java.util.*;
import java.util.stream.Collectors;

import static com.milaboratory.mist.pattern.MatchUtils.countMatches;
import static com.milaboratory.mist.util.CommonTestUtils.*;
import static org.junit.Assert.*;

public class ParserTest {
    private static final Parser strictParser = new Parser(getTestPatternAligner(true));

    @Test
    public void sampleNormalSyntaxQueriesTest1() throws Exception {
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

    @Test
    public void sampleNormalSyntaxQueriesTest2() throws Exception {
        testSample("^<{3}ATTAGACA>>$", "AGA", new Range(0, 3));
        testSample("^<{3}ATTAGACA>$", "AGA", null);
        testSample("^<{3}ATTAGACA>$", "AGAC", new Range(0, 4));
        testSample("^ATTA + GACA", "GATTATTAGACACTA", null);
        testSample("^ATTA + GACA", "ATTATTAGACACTA", new Range(0, 11));
        testSample("GACA$ || ^AT{:2}A", "ATTAGACA", new Range(4, 8));
        testSample("^GACA || ^AT{:2}A", "ATTAGACA", new Range(0, 4));
        testSample("^GACA || AT{:2}A$", "ATTAGACA", null);
        testBadSample("AGTT>>> + [-1.1:<{3}GCAATGC & TTA] + AGC");
        testSample("AGTT>>> + [-1:<{1}GCA{2:2}TGC & T{2:}A] + [0:<{1}AGC]", "TATATTAATCAATGCCCAGCAGC",
                new Range(1, 20));
        testBadSample("^^ATTAGACA");
        testBadSample("ATTAGACA$$");
        testBadSample("^[^ATTAGACA]");
        testBadSample("[ATTAGACA$]$");
        testBadSample("^^A{3}");
        testBadSample("A{3}$$");
        testBadSample("^[^A{3}]");
        testBadSample("[A{3}$]$");
        testBadSample("<[<AAA]");
        testBadSample("<A{3}");
        testSample("[^ATTAGACA]", "ATTAGACAA", new Range(0, 8));
        testSample("[ATTAGACA$]", "GATTAGACA", new Range(1, 9));
        testSample("<{2}[0:^ATTAGACA$]>{2}", "ATTAGACA", new Range(0, 8));
        testSample("<{2}^[0:ATTAGACA]$>{2}", "ATTAGACA", new Range(0, 8));
        testSample("^<{2}[0:ATTAGACA]>{2}$", "ATTAGACA", new Range(0, 8));
        testSample("<{2}[0:^ATTAGACA$]>{2}", "ATTAGACA", new Range(0, 8));
        testSample("<{2}[0:^ATTAGACA]", "ATTAGACA", new Range(0, 8));
        testSample("<{2}^[0:ATTAGACA]", "ATTAGACA", new Range(0, 8));
        testSample("^<{2}[0:ATTAGACA]", "ATTAGACA", new Range(0, 8));
        testSample("<{2}[0:^ATTAGACA]", "ATTAGACA", new Range(0, 8));
        testBadSample("[^0:ATTAGACA]");
        testSample("^[ATTAGACA]", "ATTAGACAA", new Range(0, 8));
        testSample("[ATTAGACA]$", "GATTAGACA", new Range(1, 9));
        testSample("[^A{3}]", "AAAT", new Range(0, 3));
        testSample("[A{3}$]", "TAAA", new Range(1, 4));
        testSample("^[A{3}]", "AAAT", new Range(0, 3));
        testSample("[A{3}]$", "TAAA", new Range(1, 4));
        testSample("*", "ATTAGACA", new Range(0, 8));
        testSample("A{*}", "AAAAAAAA", new Range(0, 8));
        testSample("(GROUP:*)", "A", new Range(0, 1));
        testSample("(GROUP:A{*})", "A", new Range(0, 1));
        testSample("[ -1:ATTAGACA]", "ATTAGAC", null);
        testSample("[-1 :ATTA GACA ]", "TTAGACA", null);
        testSample(" [ 0 : [ A T T A ] [ G A C A ] ] ", "ATTAGACA", new Range(0, 8));
        testBadSample("[]");
        testBadSample("[:ATTA]");
        testBadSample("ATTA:GACA");
        testBadSample("(ATTAGACA)");
        testBadSample("");
        testBadSample("A{}");
        testBadSample("(:GACA)");
        testBadSample("(<{}GACA)");
        testBadSample("(<{4:}ATTAGACA)");
        testBadSample("{3}");
        testBadSample(":{3}");
        testBadSample("A*");
        testBadSample("*A");
        testBadSample("**");
        testBadSample("<*");
        testSample(" ( TEST1 : [  [  (TEST2:  *  )  ]  ] ) ", "AT", new Range(0, 2));
        testSample("<{1}[[ATTA>+<{1}GACA]>+<AATA$>{1}]&^<(X:GCGC)", "CGCTTACAT", new Range(0, 9));
    }

    @Test
    public void sampleNormalSyntaxQueriesTest3() throws Exception {

    }

    @Test
    public void testMatchedGroups() throws Exception {

    }

    private static void testSample(String query, String target, Range expectedRange) throws Exception {
        Pattern pattern = strictParser.parseQuery(query);
        Match bestMatch = pattern.match(new NSequenceWithQuality(target)).getBestMatch(true);
        Range matchedRange = (bestMatch == null) ? null : bestMatch.getRange();
        assertEquals(expectedRange, matchedRange);
    }

    private static void testMultiSample(String query, String multiTarget, long expectedCount) throws Exception {
        Pattern pattern = strictParser.parseQuery(query);
        String[] targets = multiTarget.split(" ");
        MultiNSequenceWithQuality multiSeq = new MultiNSequenceWithQuality() {
            @Override
            public int numberOfSequences() {
                return targets.length;
            }

            @Override
            public NSequenceWithQuality get(int id) {
                return new NSequenceWithQuality(targets[id]);
            }
        };
        MatchingResult results = pattern.match(multiSeq);
        assertEquals(expectedCount, countMatches(results, true));
    }

    private static void testBadSample(String query) throws Exception {
        assertException(ParserException.class, () -> {
            strictParser.parseQuery(query);
            return null;
        });
    }
}
