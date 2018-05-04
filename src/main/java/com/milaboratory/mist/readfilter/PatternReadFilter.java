package com.milaboratory.mist.readfilter;

import com.milaboratory.core.alignment.PatternAndTargetAlignmentScoring;
import com.milaboratory.mist.outputconverter.ParsedRead;
import com.milaboratory.mist.parser.Parser;
import com.milaboratory.mist.parser.ParserException;
import com.milaboratory.mist.pattern.*;

import static com.milaboratory.mist.cli.Defaults.*;
import static com.milaboratory.mist.util.SystemUtils.*;

public class PatternReadFilter implements ReadFilter {
    private final String groupName;
    private final SinglePattern pattern;
    private final boolean fairSorting;

    public PatternReadFilter(String groupName, String patternQuery, boolean fairSorting) {
        this.groupName = groupName;
        PatternAndTargetAlignmentScoring scoring = new PatternAndTargetAlignmentScoring(0,
                -1, -1, DEFAULT_UPPERCASE_MISMATCH_SCORE,
                DEFAULT_GOOD_QUALITY, DEFAULT_BAD_QUALITY, 0);
        PatternAligner patternAligner = new BasePatternAligner(scoring, 0, -1,
                0, 0);
        Parser patternParser = new Parser(patternAligner);
        Pattern pattern;
        try {
            pattern = patternParser.parseQuery(patternQuery);
        } catch (ParserException e) {
            System.err.println("Error while parsing pattern " + patternQuery);
            throw exitWithError(e.getMessage());
        }
        if (!(pattern instanceof SinglePattern))
            throw exitWithError("Only single-read patterns are allowed in filters, found wrong pattern "
                    + patternQuery);
        this.pattern = (SinglePattern)pattern;
        this.fairSorting = fairSorting;
    }

    @Override
    public ParsedRead filter(ParsedRead parsedRead) {
        if (parsedRead.getGroups().stream()
                .anyMatch(group -> group.getGroupName().equals(groupName)
                        && (pattern.match(group.getValue()).getBestMatch(fairSorting) != null)))
            return parsedRead;
        else
            return new ParsedRead(parsedRead.getOriginalRead(), parsedRead.isReverseMatch(), null);
    }
}
