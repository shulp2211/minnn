package com.milaboratory.mist.parser;

import com.milaboratory.core.sequence.NucleotideSequence;
import com.milaboratory.mist.pattern.*;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;

import java.util.ArrayList;

import static com.milaboratory.mist.parser.ParserFormat.SIMPLIFIED;
import static com.milaboratory.mist.util.CommonTestUtils.getTestPatternAligner;
import static org.junit.Assert.*;

public class SimplifiedTokenizerTest {
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

        FuzzyMatchPattern fuzzyMatchPattern1 = new FuzzyMatchPattern(getTestPatternAligner(),
                new NucleotideSequence("GTGGTTGTGTTGT"), groups);
        FuzzyMatchPattern fuzzyMatchPattern2 = new FuzzyMatchPattern(getTestPatternAligner(), new NucleotideSequence("ATTG"));
        AndPattern andPattern = new AndPattern(getTestPatternAligner(), fuzzyMatchPattern2, fuzzyMatchPattern2);
        PlusPattern plusPattern = new PlusPattern(getTestPatternAligner(), andPattern, fuzzyMatchPattern2);
        OrPattern orPattern = new OrPattern(getTestPatternAligner(), plusPattern, andPattern);
        BorderFilter borderFilter = new BorderFilter(getTestPatternAligner(), true,
                new NucleotideSequence("ATTA"), false);
        FilterPattern borderFilterPattern = new FilterPattern(getTestPatternAligner(), borderFilter, plusPattern);
        MultiPattern multiPattern1 = new MultiPattern(getTestPatternAligner(), orPattern, borderFilterPattern,
                fuzzyMatchPattern1, andPattern);
        MultiPattern multiPattern2 = new MultiPattern(getTestPatternAligner(), borderFilterPattern,
                fuzzyMatchPattern2, andPattern);
        AndOperator andOperator1 = new AndOperator(getTestPatternAligner(), multiPattern1, multiPattern2);
        AndOperator andOperator2 = new AndOperator(getTestPatternAligner(), multiPattern2, multiPattern2);
        ScoreFilter scoreFilter = new ScoreFilter(-3);
        MultipleReadsFilterPattern scoreFilterPattern = new MultipleReadsFilterPattern(getTestPatternAligner(),
                scoreFilter, andOperator2);
        NotOperator notOperator = new NotOperator(getTestPatternAligner(), scoreFilterPattern);
        OrOperator orOperator = new OrOperator(getTestPatternAligner(), andOperator1, notOperator, scoreFilterPattern);

        Parser parser = new Parser(getTestPatternAligner());
        Pattern parseResult = parser.parseQuery(orOperator.toString(), SIMPLIFIED);
        assertNotNull(parseResult);
        assertEquals(orOperator.toString(), parseResult.toString());
    }
}
