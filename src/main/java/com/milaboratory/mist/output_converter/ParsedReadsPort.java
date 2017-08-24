package com.milaboratory.mist.output_converter;

import cc.redberry.pipe.OutputPort;
import com.milaboratory.core.io.sequence.MultiRead;
import com.milaboratory.core.io.sequence.SingleRead;
import com.milaboratory.core.io.sequence.SingleReadImpl;
import com.milaboratory.mist.pattern.*;

import java.util.ArrayList;
import java.util.stream.Collectors;

import static com.milaboratory.mist.output_converter.GroupUtils.*;

public class ParsedReadsPort implements OutputPort<ParsedRead> {
    private final MatchingResult matchingResult;
    private final boolean fairSorting;
    private final int firstReadNumber;
    private boolean readTaken = false;

    public ParsedReadsPort(MatchingResult matchingResult, boolean fairSorting, int firstReadNumber) {
        this.matchingResult = matchingResult;
        this.fairSorting = fairSorting;
        this.firstReadNumber = firstReadNumber;
    }

    @Override
    public ParsedRead take() {
        if (readTaken) return null;
        readTaken = true;
        Match bestMatch = matchingResult.getBestMatch(fairSorting);
        if (bestMatch == null) return null;
        int numberOfReads = bestMatch.getNumberOfPatterns();
        SingleRead[] reads = new SingleReadImpl[numberOfReads];
        for (int i = 0; i < numberOfReads; i++) {
            String mainGroupName = "R" + (firstReadNumber + i);
            ArrayList<MatchedGroup> currentGroups = getGroupsFromMatch(bestMatch, i);
            MatchedRange mainGroup = currentGroups.stream().filter(g -> g.getGroupName().equals(mainGroupName))
                    .map(g -> (MatchedRange)g).findFirst().orElse(bestMatch.getMatchedRange(i));
            ArrayList<MatchedGroup> groupsInsideMain = getGroupsInsideMain(currentGroups, mainGroup.getRange(),
                    true).stream().filter(g -> !g.getGroupName().equals(mainGroupName))
                    .collect(Collectors.toCollection(ArrayList::new));
            ArrayList<MatchedGroup> groupsNotInsideMain = getGroupsInsideMain(currentGroups, mainGroup.getRange(),
                    false);
            String description = groupsToReadDescription(groupsNotInsideMain, mainGroupName, false)
                    + (((groupsNotInsideMain.size() == 0) || (groupsInsideMain.size() == 0)) ? "" : '~')
                    + groupsToReadDescription(groupsInsideMain, mainGroupName, true);
            reads[i] = new SingleReadImpl(0, mainGroup.getValue(), description);
        }
        MultiRead multiRead = new MultiRead(reads);
        return new ParsedRead(null, multiRead, getGroupsFromMatch(bestMatch), false, 0);
    }
}
