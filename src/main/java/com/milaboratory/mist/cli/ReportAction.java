package com.milaboratory.mist.cli;

import com.beust.jcommander.Parameter;
import com.beust.jcommander.ParameterException;
import com.beust.jcommander.Parameters;
import com.milaboratory.cli.Action;
import com.milaboratory.cli.ActionHelper;
import com.milaboratory.cli.ActionParameters;
import com.milaboratory.core.alignment.LinearGapAlignmentScoring;
import com.milaboratory.core.sequence.MultiNSequenceWithQuality;
import com.milaboratory.core.sequence.NucleotideSequence;
import com.milaboratory.mist.output_converter.MatchedGroup;
import com.milaboratory.mist.parser.Parser;
import com.milaboratory.mist.parser.ParserException;
import com.milaboratory.mist.pattern.*;

import java.util.ArrayList;

import static com.milaboratory.mist.cli.Defaults.*;
import static com.milaboratory.mist.output_converter.GroupUtils.getGroupsFromMatch;
import static com.milaboratory.mist.parser.ParserUtils.parseMultiTargetString;
import static com.milaboratory.mist.util.SystemUtils.exitWithError;

public class ReportAction implements Action {
    private final ParseActionParameters params = new ParseActionParameters();

    @Override
    public void go(ActionHelper helper) throws Exception {
        LinearGapAlignmentScoring<NucleotideSequence> scoring = new LinearGapAlignmentScoring<>(
                DEFAULT_ALPHABET, params.matchScore, params.mismatchScore, params.gapScore);
        PatternAligner patternAligner = new BasePatternAligner(scoring, params.penaltyThreshold,
                params.singleOverlapPenalty, params.bitapMaxErrors);
        Parser patternParser = new Parser(patternAligner);
        Pattern pattern;
        try {
            pattern = patternParser.parseQuery(params.query);
        } catch (ParserException e) {
            System.err.println("Error while parsing the pattern!");
            throw exitWithError(e.getMessage());
        }
        MultiNSequenceWithQuality target = parseMultiTargetString(params.target);
        try {
            for (int i = 0; i < target.numberOfSequences(); i++)
                target.get(i);
        } catch (Exception e) {
            System.err.println("Error while parsing the target!");
            throw exitWithError(e.getMessage());
        }
        if (pattern instanceof SinglePattern && target.numberOfSequences() > 1)
            throw exitWithError("Pattern is for single target, but found multi-target!");
        if (pattern instanceof MultipleReadsOperator && target.numberOfSequences() == 1)
            throw exitWithError("Pattern is for multi-target, but found single target!");
        Match bestMatch = pattern instanceof SinglePattern
                ? pattern.match(target.get(0)).getBestMatch(params.fairSorting)
                : pattern.match(target).getBestMatch(params.fairSorting);

        if (bestMatch == null)
            System.out.println("Pattern not found in the target.");
        else {
            ArrayList<MatchedGroup> matchedGroups = getGroupsFromMatch(bestMatch);
            if (target.numberOfSequences() == 1) {
                System.out.println("Found match in range " + bestMatch.getRange() + ": "
                        + bestMatch.getValue().getSequence() + "\n");
                for (MatchedGroup matchedGroup : matchedGroups) {
                    System.out.println("Found matched group " + matchedGroup.getGroupName() + ": "
                            + matchedGroup.getValue().getSequence());
                    System.out.println("Range in target string: " + matchedGroup.getRange() + "\n");
                }
            } else {
                for (MatchedRange matchedRange : bestMatch.getMatchedRanges()) {
                    System.out.println("Found match in target " + matchedRange.getTargetId() + " ("
                            + matchedRange.getTarget().getSequence() + "): " + matchedRange.getValue().getSequence());
                    System.out.println("Range in this target: " + matchedRange.getRange() + "\n");
                }
                for (MatchedGroup matchedGroup : matchedGroups) {
                    System.out.println("Found matched group " + matchedGroup.getGroupName() + " in target "
                            + matchedGroup.getTargetId() + " (" + matchedGroup.getTarget().getSequence() + "): "
                            + matchedGroup.getValue().getSequence());
                    System.out.println("Range in this target: " + matchedGroup.getRange() + "\n");
                }
            }
        }
    }

    @Override
    public String command() {
        return "report";
    }

    @Override
    public ActionParameters params() {
        return params;
    }

    @Parameters(commandDescription =
            "Find match and groups in query and display report on the screen.")
    private static final class ParseActionParameters extends ActionParameters {
        @Parameter(description = "Query, pattern specified in MiST format.",
                names = {"--pattern"})
        String query = null;

        @Parameter(description = "Target nucleotide sequence, where to search.",
                names = {"--target"})
        String target = null;

        @Parameter(description = "Use fair sorting and fair best match by score for all patterns.",
                names = {"--fair-sorting"})
        boolean fairSorting = true;

        @Parameter(description = "Score for perfectly matched nucleotide.",
                names = {"--match-score"})
        int matchScore = DEFAULT_MATCH_SCORE;

        @Parameter(description = "Score for mismatched nucleotide.",
                names = {"--mismatch-score"})
        int mismatchScore = DEFAULT_MISMATCH_SCORE;

        @Parameter(description = "Score for gap or insertion.",
                names = {"--gap-score"})
        int gapScore = DEFAULT_GAP_SCORE;

        @Parameter(description = "Score threshold, matches with score lower than this will not go to output.",
                names = {"--penalty-threshold"})
        long penaltyThreshold = DEFAULT_PENALTY_THRESHOLD;

        @Parameter(description = "Score penalty for 1 nucleotide overlap between neighbor patterns. Negative value.",
                names = {"--single-overlap-penalty"})
        long singleOverlapPenalty = DEFAULT_SINGLE_OVERLAP_PENALTY;

        @Parameter(description = "Maximum allowed number of errors for bitap matcher.",
                names = {"--bitap-max-errors"})
        int bitapMaxErrors = DEFAULT_BITAP_MAX_ERRORS;

        @Override
        public void validate() {
            if (query == null)
                throw new ParameterException("Pattern not specified!");
            if (target == null)
                throw new ParameterException("Target not specified!");
        }
    }
}
