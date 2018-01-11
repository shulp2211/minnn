package com.milaboratory.mist.parser;

import com.milaboratory.core.sequence.NucleotideSequenceCaseSensitive;
import com.milaboratory.mist.pattern.*;
import org.junit.*;
import org.junit.rules.ExpectedException;

import java.util.ArrayList;
import java.util.Collections;

import static com.milaboratory.mist.parser.ParserFormat.SIMPLIFIED;
import static com.milaboratory.mist.util.CommonTestUtils.*;
import static org.junit.Assert.*;

public class SimplifiedTokenizerTest {
    @BeforeClass
    public static void init() throws Exception {
        PatternAligner.allowValuesOverride();
        PatternAligner.init(getTestScoring(), -1, 0, -1);
    }

    @Rule
    public final ExpectedException exception = ExpectedException.none();

    @Test
    public void twoSidesConversionTest() throws Exception {
        ArrayList<GroupEdgePosition> groups = new ArrayList<GroupEdgePosition>() {{
            add(new GroupEdgePosition(new GroupEdge("ABC", true), 1));
            add(new GroupEdgePosition(new GroupEdge("ABC", false), 3));
            add(new GroupEdgePosition(new GroupEdge("DEF", true), 6));
            add(new GroupEdgePosition(new GroupEdge("DEF", false), 7));
            add(new GroupEdgePosition(new GroupEdge("GH", true), 10));
            add(new GroupEdgePosition(new GroupEdge("GH", false), 11));
        }};

        FuzzyMatchPattern fuzzyMatchPattern1 = new FuzzyMatchPattern(Long.MIN_VALUE,
                new NucleotideSequenceCaseSensitive("gtggttgtgttgt"), groups);
        FuzzyMatchPattern fuzzyMatchPattern2 = new FuzzyMatchPattern(Long.MIN_VALUE,
                new NucleotideSequenceCaseSensitive("attg"));
        AndPattern andPattern = new AndPattern(Long.MIN_VALUE, fuzzyMatchPattern2, fuzzyMatchPattern2);
        PlusPattern plusPattern = new PlusPattern(Long.MIN_VALUE, andPattern, fuzzyMatchPattern2);
        OrPattern orPattern = new OrPattern(Long.MIN_VALUE, plusPattern, andPattern);
        ScoreFilter scoreFilter = new ScoreFilter(-3);
        FilterPattern scoreFilterPatternS = new FilterPattern(Long.MIN_VALUE, scoreFilter, plusPattern);
        MultiPattern multiPattern1 = new MultiPattern(Long.MIN_VALUE, orPattern, scoreFilterPatternS,
                fuzzyMatchPattern1, andPattern);
        MultiPattern multiPattern2 = new MultiPattern(Long.MIN_VALUE, scoreFilterPatternS,
                fuzzyMatchPattern2, andPattern);
        AndOperator andOperator1 = new AndOperator(Long.MIN_VALUE, multiPattern1, multiPattern2);
        AndOperator andOperator2 = new AndOperator(Long.MIN_VALUE, multiPattern2, multiPattern2);
        MultipleReadsFilterPattern scoreFilterPatternM = new MultipleReadsFilterPattern(Long.MIN_VALUE,
                scoreFilter, andOperator2);
        NotOperator notOperator = new NotOperator(Long.MIN_VALUE, scoreFilterPatternM);
        OrOperator orOperator = new OrOperator(Long.MIN_VALUE, andOperator1, notOperator, scoreFilterPatternM);

        Pattern parseResult = Parser.parseQuery(orOperator.toString(), Long.MIN_VALUE, SIMPLIFIED);
        assertNotNull(parseResult);
        assertEquals(orOperator.toString(), parseResult.toString());
    }

    @Test
    public void randomTest() throws Exception {
        for (int i = 0; i < rg.nextInt(50) + 300; i++) {
            int nestedSingleLevel = rg.nextInt(7) + 1;
            int nestedMultiLevel = rg.nextInt(5) + 1;
            ArrayList<SinglePattern> singlePatterns = new ArrayList<>();
            for (int j = 0; j < nestedSingleLevel; j++) {
                singlePatterns.add(getRandomSinglePattern(singlePatterns.toArray(new SinglePattern[singlePatterns.size()])));
                Collections.reverse(singlePatterns);
            }
            Pattern parseResult = Parser.parseQuery(singlePatterns.get(0).toString(),
                    rg.nextInt(200) - 100, SIMPLIFIED);
            assertNotNull(parseResult);
            assertEquals(singlePatterns.get(0).toString(), parseResult.toString());
            ArrayList<MultipleReadsOperator> multiPatterns = new ArrayList<>();
            multiPatterns.add(new MultiPattern(rg.nextInt(200) - 100, singlePatterns.get(0)));
            multiPatterns.add(getRandomMultiReadPattern());
            for (int j = 1; j < nestedMultiLevel; j++) {
                multiPatterns.add(getRandomMultiReadPattern(multiPatterns.toArray(
                        new MultipleReadsOperator[multiPatterns.size()])));
                Collections.reverse(multiPatterns);
            }
            parseResult = Parser.parseQuery(multiPatterns.get(0).toString(),
                    rg.nextInt(200) - 100, SIMPLIFIED);
            assertNotNull(parseResult);
            assertEquals(multiPatterns.get(0).toString(), parseResult.toString());
            multiPatterns.add(new MultiPattern(rg.nextInt(200) - 100, getRandomSinglePattern(
                    getRandomBasicPattern(true), getRandomBasicPattern(), singlePatterns.get(0))));
            parseResult = Parser.parseQuery(multiPatterns.get(multiPatterns.size() - 1).toString(),
                    rg.nextInt(200) - 100, SIMPLIFIED);
            assertNotNull(parseResult);
            assertEquals(multiPatterns.get(multiPatterns.size() - 1).toString(), parseResult.toString());
        }
    }

    @Test
    public void wrongOperandClassTest() throws Exception {
        exception.expect(ParserException.class);
        Parser.parseQuery("AndOperator([FuzzyMatchPattern(GATCACGTCGGGCTTCGT, -1, -1, []), "
                + "FuzzyMatchPattern(GATCACGTCGGGCTTCGT, -1, -1, [])])", 0, SIMPLIFIED);
    }
}
