package com.milaboratory.mist.cli;

import com.beust.jcommander.Parameter;
import com.beust.jcommander.ParameterException;
import com.beust.jcommander.Parameters;
import com.milaboratory.cli.Action;
import com.milaboratory.cli.ActionHelper;
import com.milaboratory.cli.ActionParameters;
import com.milaboratory.core.alignment.LinearGapAlignmentScoring;
import com.milaboratory.core.sequence.NucleotideSequence;
import com.milaboratory.mist.io.MistDataFormat;
import com.milaboratory.mist.io.ReadProcessor;
import com.milaboratory.mist.parser.Parser;
import com.milaboratory.mist.parser.ParserException;
import com.milaboratory.mist.pattern.BasePatternAligner;
import com.milaboratory.mist.pattern.Pattern;
import com.milaboratory.mist.pattern.PatternAligner;

import java.util.ArrayList;
import java.util.List;

import static com.milaboratory.mist.cli.Defaults.*;
import static com.milaboratory.mist.io.MistDataFormatNames.parameterNames;
import static com.milaboratory.mist.parser.ParserFormat.*;
import static com.milaboratory.mist.util.SystemUtils.exitWithError;

public final class ParseAction implements Action {
    private final ParseActionParameters params = new ParseActionParameters();

    @Override
    public void go(ActionHelper helper) throws Exception {
        LinearGapAlignmentScoring<NucleotideSequence> scoring = new LinearGapAlignmentScoring<>(
                DEFAULT_ALPHABET, params.matchScore, params.mismatchScore, params.gapScore);
        PatternAligner patternAligner = new BasePatternAligner(scoring, params.penaltyThreshold,
                params.singleOverlapPenalty, params.bitapMaxErrors, params.maxOverlap);
        Parser patternParser = new Parser(patternAligner);
        Pattern pattern;
        try {
            pattern = params.simplifiedSyntax ? patternParser.parseQuery(params.query, SIMPLIFIED)
                    : patternParser.parseQuery(params.query);
        } catch (ParserException e) {
            System.err.println("Error while parsing the pattern!");
            throw exitWithError(e.getMessage());
        }
        MistDataFormat inputFormat = parameterNames.get(params.inputFormat);
        MistDataFormat outputFormat = parameterNames.get(params.outputFormat);
        ReadProcessor readProcessor = new ReadProcessor(params.inputFileNames, params.outputFileNames, pattern,
                params.oriented, params.fairSorting, params.firstReadNumber, params.threads, params.copyOldComments,
                inputFormat, outputFormat);
        readProcessor.processReadsParallel();
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

        @Parameter(description = "Input data format. \"fastq\" (default) or \"mif\".",
                names = {"--input-format"})
        String inputFormat = DEFAULT_INPUT_FORMAT;

        @Parameter(description = "Output data format. \"fastq\" (default) or \"mif\".",
                names = {"--output-format"})
        String outputFormat = DEFAULT_OUTPUT_FORMAT;

        @Parameter(description = "By default, if there are 2 or more reads, 2 last reads are checked in direct " +
                "and reverse order. With this flag, only in direct order.",
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

        @Parameter(description = "Max allowed overlap for 2 intersecting operands in +, & and pattern sequences.",
                names = {"--max-overlap"})
        int maxOverlap = DEFAULT_MAX_OVERLAP;

        @Parameter(description = "Use fair sorting and fair best match by score for all patterns.",
                names = {"--fair-sorting"})
        boolean fairSorting = false;

        @Parameter(description = "Use \"simplified\" parser syntax with class names and their arguments in parentheses",
                names = {"--devel-parser-syntax"})
        boolean simplifiedSyntax = false;

        @Parameter(description = "First read number, default is 1.",
                names = {"--first-read-number"})
        int firstReadNumber = 1;

        @Parameter(description = "Number of threads for parsing reads.",
                names = {"--threads"})
        int threads = DEFAULT_THREADS;

        @Parameter(description = "Write comment from original read to the beginning of comment of parsed read.",
                names = {"--copy-original-comments"})
        boolean copyOldComments = false;

        @Override
        public void validate() {
            if (query == null)
                throw new ParameterException("Pattern not specified!");
            if (parameterNames.get(inputFormat) == null)
                throw new ParameterException("Unknown input format: " + inputFormat);
            if (parameterNames.get(outputFormat) == null)
                throw new ParameterException("Unknown output format: " + outputFormat);
        }
    }
}
