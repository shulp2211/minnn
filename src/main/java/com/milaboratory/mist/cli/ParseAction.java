package com.milaboratory.mist.cli;

import com.beust.jcommander.Parameter;
import com.beust.jcommander.ParameterException;
import com.beust.jcommander.Parameters;
import com.milaboratory.cli.Action;
import com.milaboratory.cli.ActionHelper;
import com.milaboratory.cli.ActionParameters;
import com.milaboratory.core.alignment.LinearGapAlignmentScoring;
import com.milaboratory.core.sequence.NucleotideSequence;
import com.milaboratory.mist.input.TargetReader;
import com.milaboratory.mist.output_converter.ParsedReadsPort;
import com.milaboratory.mist.parser.Parser;
import com.milaboratory.mist.parser.ParserException;
import com.milaboratory.mist.pattern.BasePatternAligner;
import com.milaboratory.mist.pattern.Pattern;
import com.milaboratory.mist.pattern.PatternAligner;

import java.util.ArrayList;
import java.util.List;

import static com.milaboratory.mist.cli.Defaults.*;
import static com.milaboratory.mist.output.ResultWriter.writeResultsFromPort;
import static com.milaboratory.mist.parser.ParserFormat.SIMPLIFIED;
import static com.milaboratory.mist.util.SystemUtils.exitWithError;

public final class ParseAction implements Action {
    private final ParseActionParameters params = new ParseActionParameters();

    @Override
    public void go(ActionHelper helper) throws Exception {
        LinearGapAlignmentScoring<NucleotideSequence> scoring = new LinearGapAlignmentScoring<>(
                DEFAULT_ALPHABET, params.matchScore, params.mismatchScore, params.gapScore);
        PatternAligner patternAligner = new BasePatternAligner(scoring, params.penaltyThreshold,
                params.singleOverlapPenalty, params.bitapMaxErrors);
        Parser patternParser = new Parser(patternAligner);
        Pattern pattern = null;
        try {
            pattern = patternParser.parseQuery(params.query, SIMPLIFIED);
        } catch (ParserException e) {
            System.err.println("Error while parsing the pattern!");
            exitWithError(e.getMessage());
        }
        TargetReader targetReader = new TargetReader(pattern);
        ParsedReadsPort parsedReadsPort = new ParsedReadsPort(targetReader.getMatchingResult(params.inputFileNames));
        writeResultsFromPort(params.outputFileNames, parsedReadsPort);
    }

    @Override
    public String command() {
        return "parse";
    }

    @Override
    public ActionParameters params() {
        return params;
    }

    @Parameters(commandDescription =
            "Read target nucleotide sequence and find groups and patterns as specified in query.")
    private static final class ParseActionParameters extends ActionParameters {
        @Parameter(description = "Query, pattern specified in MiST format.",
                names = {"--pattern"})
        String query = null;

        @Parameter(description = "Input files. Single file means that there is 1 read or multi-read file; " +
                    "multiple files mean that there is 1 file for each read.",
                names = {"--input"}, variableArity = true)
        List<String> inputFileNames = new ArrayList<>();

        @Parameter(description = "Output files. Single file means that there is 1 read or multi-read file; " +
                    "multiple files mean that there is 1 file for each read.",
                names = {"--output"}, variableArity = true)
        List<String> outputFileNames = new ArrayList<>();

        @Parameter(description = "Query, pattern specified in MiST format.",
                names = {"--oriented"})
        boolean oriented = false;

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
        }
    }
}
