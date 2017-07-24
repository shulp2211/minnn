package com.milaboratory.mist.parser;

import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;

import java.util.*;

import static com.milaboratory.mist.parser.BracketsDetector.*;
import static com.milaboratory.mist.parser.BracketsType.*;
import static com.milaboratory.mist.parser.QuotesType.*;
import static com.milaboratory.mist.util.CommonTestUtils.*;
import static org.junit.Assert.*;

public class BracketsDetectorTest {
    @Rule
    public final ExpectedException exception = ExpectedException.none();

    @Test
    public void getAllQuotesTest1() throws Exception {
        String string1 = "'\\\'\"12'37\"\\(0\\)'\\\\\"'))\\\"\\\\\\\'{\"'\"2\\\'3'\"'\"'\\>>{\"\"\"\\\'\"\\\"\"";
        String string2 = "<<AT'G1:ATA'GCGCGGC'AT&GC'|(AT&GG)";
        String string3 = "'\"''\"))\"";
        List<QuotesPair> result1 = getAllQuotes(string1);
        List<QuotesPair> result2 = getAllQuotes(string2);
        assertEquals(8, result1.size());
        assertEquals(5, result1.stream().filter(qp -> qp.quotesType == DOUBLE).count());
        assertEquals(3, result1.stream().filter(qp -> qp.quotesType == SINGLE).count());
        assertEquals(2, result2.size());
        assertEquals(0, result2.stream().filter(qp -> qp.quotesType == DOUBLE).count());
        assertEquals(2, result2.stream().filter(qp -> qp.quotesType == SINGLE).count());

        exception.expect(ParserException.class);
        getAllQuotes(string3);
    }

    @Test
    public void getAllQuotesTest2() throws Exception {
        exception.expect(ParserException.class);
        getAllQuotes("'''");
    }

    @Test
    public void getAllQuotesTest3() throws Exception {
        exception.expect(ParserException.class);
        getAllQuotes("'\"'\"");
    }

    @Test
    public void skipQuotesTest() throws Exception {
        String string = "'\\\'\"12'37\"\\(0\\)'\\\\\"'))\\\"\\\\\\\'{\"'\"2\\\'3'\"'\"'\\>>{\"\"\"\\\'\"\\\"\"";
        List<QuotesPair> quotesPairs = getAllQuotes(string);
        int counter1 = 0;
        int counter2 = 0;
        for (int i = 0; i < string.length(); i = nextNonQuotedPosition(quotesPairs, i)) {
            counter1++;
            if (!isInQuotes(quotesPairs, i)) counter2++;
        }
        assertEquals(8, counter1);
        assertEquals(7, counter2);
        assertEquals(41, nonQuotedIndexOf(quotesPairs, string, "\\", 0));
        assertEquals(41, nonQuotedIndexOf(quotesPairs, string, "\\", 41));
        assertEquals(41, nonQuotedIndexOf(quotesPairs, string, "\\>", 0));
        assertEquals(51, nonQuotedIndexOf(quotesPairs, string, "\\", 42));
        assertEquals(51, nonQuotedIndexOf(quotesPairs, string, "\\\"", 0));
        assertEquals(-1, nonQuotedIndexOf(quotesPairs, string, "\\'", 0));
    }

    @Test
    public void getAllBracketsTest1() throws Exception {
        String string1 = "()()(){[(())]}(([({[[123]]0}) ])00)12";
        String string2 = "<<AT(G1:ATA)GCGCGGC(AT&GC)|(AT&GG)";
        String string3 = "{[(())]}(([({[[123]}]0})";
        List<BracketsPair> result1p = getAllBrackets(PARENTHESES, string1);
        List<BracketsPair> result1s = getAllBrackets(SQUARE, string1);
        List<BracketsPair> result1b = getAllBrackets(BRACES, string1);
        List<BracketsPair> result2p = getAllBrackets(PARENTHESES, string2);
        List<BracketsPair> result2s = getAllBrackets(SQUARE, string2);
        List<BracketsPair> result2b = getAllBrackets(BRACES, string2);
        assertEquals(8, result1p.size());
        assertEquals(4, result1s.size());
        assertEquals(2, result1b.size());
        assertEquals(3, result2p.size());
        assertEquals(0, result2s.size());
        assertEquals(0, result2b.size());

        exception.expect(ParserException.class);
        getAllBrackets(PARENTHESES, string3);
    }

    @Test
    public void getAllBracketsTest2() throws Exception {
        exception.expect(ParserException.class);
        getAllBrackets(SQUARE, "{{()}");
    }

    @Test
    public void getAllBracketsTest3() throws Exception {
        exception.expect(ParserException.class);
        getAllBrackets(PARENTHESES, "([)]");
    }

    @Test
    public void getEndByStartTest() throws Exception {
        for (int i = 0; i < 500; i++) {
            BracketsType bracketsType = getRandomEnumItem(BracketsType.class);
            List<BracketsPair> bracketsPairs = new ArrayList<>();
            for (int j = 0; j < rg.nextInt(30); j++) {
                int start = rg.nextInt(100);
                int end = start + rg.nextInt(100) + 1;
                bracketsPairs.add(new BracketsPair(bracketsType, start, end, rg.nextInt(30)));
            }
            int newPairStart;
            while (true) {
                final int randomValue = rg.nextInt(100);
                if (bracketsPairs.stream().noneMatch(bp -> bp.start == randomValue)) {
                    newPairStart = randomValue;
                    break;
                }
            }
            BracketsPair newBracketsPair = new BracketsPair(bracketsType, newPairStart,
                    newPairStart + 1 + rg.nextInt(100), rg.nextInt(30));
            bracketsPairs.add(newBracketsPair);
            Collections.shuffle(bracketsPairs);
            assertEquals(newBracketsPair.end, getEndByStart(bracketsPairs, newBracketsPair.start));
        }
    }
}
