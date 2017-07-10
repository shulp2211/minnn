package com.milaboratory.mist.parser;

import com.milaboratory.core.sequence.NucleotideSequence;
import com.milaboratory.mist.pattern.*;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;

import java.util.ArrayList;
import java.util.Random;

import static com.milaboratory.mist.parser.GroupsChecker.checkGroups;
import static com.milaboratory.mist.parser.ParserFormat.*;
import static com.milaboratory.mist.util.CommonTestUtils.*;
import static org.junit.Assert.*;

public class GroupsCheckerTest {
    @Rule
    public final ExpectedException exception = ExpectedException.none();

    @Test
    public void simplifiedCorrectGroupsTest() throws Exception {
        Random rg = new Random();
        for (int i = 0; i < 100; i++) {
            PatternAligner patternAligner = getRandomPatternAligner();
            ArrayList<FuzzyMatchPattern> fuzzyMatchPatterns = new ArrayList<>();
            ArrayList<RepeatPattern> repeatPatterns = new ArrayList<>();
            ArrayList<OrPattern> orPatterns = new ArrayList<>();
            ArrayList<MultiPattern> multiPatterns = new ArrayList<>();
            ArrayList<OrOperator> orOperators = new ArrayList<>();
            ArrayList<String> groupCheckerQueries = new ArrayList<>();
            for (int j = 0; j < 3; j++) {
                fuzzyMatchPatterns.add(getRandomFuzzyPattern(patternAligner, true));
                repeatPatterns.add(getRandomRepeatPattern(patternAligner, true));
            }
            for (int j = 0; j < 5; j++) {
                orPatterns.add(new OrPattern(patternAligner, fuzzyMatchPatterns.get(rg.nextInt(3)),
                        getRandomSinglePattern(patternAligner, fuzzyMatchPatterns.get(rg.nextInt(3))),
                        repeatPatterns.get(rg.nextInt(3)),
                        getRandomSinglePattern(patternAligner, repeatPatterns.get(rg.nextInt(3)))));
                multiPatterns.add(new MultiPattern(patternAligner, getRandomSinglePattern(patternAligner,
                        rg.nextBoolean() ? fuzzyMatchPatterns.get(rg.nextInt(3)) : repeatPatterns.get(rg.nextInt(3))),
                        getRandomBasicPattern(patternAligner)));
                orOperators.add(new OrOperator(patternAligner, multiPatterns.get(j),
                        new AndOperator(patternAligner, multiPatterns.get(j)),
                        multiPatterns.get(rg.nextInt(multiPatterns.size()))));

                String randomGroupName = getRandomString(rg.nextInt(10) + 1, "", true);
                ArrayList<GroupEdgePosition> groupEdgePositions1 = new ArrayList<>();
                ArrayList<GroupEdgePosition> groupEdgePositions2 = new ArrayList<>();
                groupEdgePositions1.add(new GroupEdgePosition(new GroupEdge(randomGroupName, true), 0));
                groupEdgePositions2.add(new GroupEdgePosition(new GroupEdge(randomGroupName, false), 1));
                FuzzyMatchPattern fuzzyMatchPattern = new FuzzyMatchPattern(patternAligner, new NucleotideSequence("A"),
                        groupEdgePositions1);
                RepeatPattern repeatPattern = new RepeatPattern(patternAligner, new NucleotideSequence("A"),
                        1, 2, groupEdgePositions2);
                PlusPattern plusPattern1 = new PlusPattern(patternAligner, getRandomBasicPattern(patternAligner),
                        new FilterPattern(patternAligner, new ScoreFilter(0), fuzzyMatchPattern),
                        getRandomBasicPattern(patternAligner));
                PlusPattern plusPattern2 = new PlusPattern(patternAligner, getRandomBasicPattern(patternAligner),
                        new FilterPattern(patternAligner, new BorderFilter(patternAligner, true,
                                new NucleotideSequence("A")), repeatPattern), getRandomBasicPattern(patternAligner));

                groupCheckerQueries.add(new SequencePattern(patternAligner, plusPattern1, plusPattern2).toString());
                groupCheckerQueries.add(orPatterns.get(j).toString());
                groupCheckerQueries.add(orOperators.get(j).toString());
            }
            for (String query : groupCheckerQueries)
                checkGroups(query, SIMPLIFIED);
        }
    }
}
