package com.milaboratory.mist.parser;

import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;

import java.util.*;
import java.util.function.Function;
import java.util.stream.IntStream;

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

        StringBuilder forward = new StringBuilder();
        StringBuilder backward = new StringBuilder();
        for (int currentPosition = nextNonQuotedPosition(quotesPairs, -1);
             currentPosition < string.length();
             currentPosition = nextNonQuotedPosition(quotesPairs, currentPosition))
            forward.append(string.charAt(currentPosition));
        for (int currentPosition = previousNonQuotedPosition(quotesPairs, string.length());
             currentPosition >= 0;
             currentPosition = previousNonQuotedPosition(quotesPairs, currentPosition))
            backward.append(string.charAt(currentPosition));
        assertEquals(forward.toString(), backward.reverse().toString());
    }

    @Test
    public void skipQuotesRandomTest() throws Exception {
        final Function<String, Void> testRoutine = (String string) -> {
            StringBuilder forward = new StringBuilder();
            StringBuilder backward = new StringBuilder();
            List<QuotesPair> quotesPairs = rethrow(BracketsDetector::getAllQuotes).apply(string);
            for (int currentPosition = nextNonQuotedPosition(quotesPairs, -1);
                 currentPosition < string.length();
                 currentPosition = nextNonQuotedPosition(quotesPairs, currentPosition))
                forward.append(string.charAt(currentPosition));
            for (int currentPosition = previousNonQuotedPosition(quotesPairs, string.length());
                 currentPosition >= 0;
                 currentPosition = previousNonQuotedPosition(quotesPairs, currentPosition))
                backward.append(string.charAt(currentPosition));
            assertEquals(forward.toString(), backward.reverse().toString());
            return null;
        };
        for (int i = 0; i < 10000; i++) {
            String string = untilSuccess(rg.nextInt(1000) + 10, (length) -> {
                StringBuilder sb = new StringBuilder();
                for (int j = 0; j < length; j++) {
                    char c = (char)(rg.nextInt(256));
                    sb.append(c);
                }
                String generatedString = sb.toString();
                // invalidate strings without quotes
                if (!generatedString.contains("'") && !generatedString.contains("\""))
                    throw new RuntimeException();
                // invalidate strings that cause getAllQuotes to throw exception
                getAllQuotes(generatedString);
                return generatedString;
            });
            testRoutine.apply(string);
        }
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

    @Test
    public void bracesTest() throws Exception {
        ArrayList<String> testStrings = new ArrayList<String>() {{
            add("ATTA \\ G{4:5}ACA>{2} \\ [<{5}ATTAGACA]>{2} \\ TN{*}");
            add("[(TEST1:AGN{:2})T{3:}a{*}GCC{:4}>{3}] + [<<A{:}(1:TT)A{1}>] & [A] || [-13:n{5:}]");
            add("[(GROUP0:(G1:aa))a + [<{3}ttNtGa>>> || aA{1:5}] \\ [<{2}ATTAGACA ] ] && [<{4}NNANT \\ ATTAGa{1:4}ca]");
            add("A{:}");
        }};
        ArrayList<List<BracketsPair>> bracesPairs = new ArrayList<>();
        ArrayList<List<BorderFilterBracesPair>> borderFilterBracesPairs = new ArrayList<>();
        ArrayList<List<BracketsPair>> repeatPatternBracesPairs = new ArrayList<>();
        for (int i = 0; i < testStrings.size(); i++) {
            bracesPairs.add(getAllBrackets(BRACES, testStrings.get(i)));
            borderFilterBracesPairs.add(getBorderFilterBraces(testStrings.get(i), bracesPairs.get(i)));
            repeatPatternBracesPairs.add(getRepeatPatternBraces(bracesPairs.get(i), borderFilterBracesPairs.get(i)));
        }

        assertEquals(3, borderFilterBracesPairs.get(0).size());
        assertEquals(2, repeatPatternBracesPairs.get(0).size());
        assertEquals(1, borderFilterBracesPairs.get(1).size());
        assertEquals(7, repeatPatternBracesPairs.get(1).size());
        assertEquals(3, borderFilterBracesPairs.get(2).size());
        assertEquals(2, repeatPatternBracesPairs.get(2).size());
        assertEquals(0, borderFilterBracesPairs.get(3).size());
        assertEquals(1, repeatPatternBracesPairs.get(3).size());

        assertFalse(borderFilterBracesPairs.get(0).get(0).leftBorder);
        assertTrue(borderFilterBracesPairs.get(0).get(1).leftBorder);
        assertFalse(borderFilterBracesPairs.get(0).get(2).leftBorder);
        assertFalse(borderFilterBracesPairs.get(1).get(0).leftBorder);
        IntStream.range(0, 3).forEach(i -> assertTrue(borderFilterBracesPairs.get(2).get(i).leftBorder));

        assertEquals(5, borderFilterBracesPairs.get(0).get(1).numberOfRepeats);
        assertEquals(3, borderFilterBracesPairs.get(1).get(0).numberOfRepeats);
        assertEquals(4, borderFilterBracesPairs.get(2).get(2).numberOfRepeats);

        assertEquals(48, repeatPatternBracesPairs.get(0).get(1).end);
        assertEquals(44, repeatPatternBracesPairs.get(1).get(4).start);
        assertEquals(90, repeatPatternBracesPairs.get(2).get(1).start);
        assertEquals(1, repeatPatternBracesPairs.get(3).get(0).start);
    }
}
