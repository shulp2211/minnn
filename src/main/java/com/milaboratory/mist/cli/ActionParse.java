package com.milaboratory.mist.cli;

import com.milaboratory.core.alignment.LinearGapAlignmentScoring;
import com.milaboratory.core.sequence.NucleotideSequence;
import com.milaboratory.mist.pattern.BasePatternAligner;
import com.milaboratory.mist.pattern.PatternAligner;
import org.antlr.v4.runtime.tree.ParseTree;

import java.util.ArrayList;

import static com.milaboratory.mist.cli.CommandLineParser.exitWithError;
import static com.milaboratory.mist.cli.Defaults.*;

final class ActionParse {
    static void executeActionParse(ArrayList<ParseTree> options) {
        String query = null;
        // single file = one read or multi-read file; multiple files = 1 file for each read; empty list = use stdin/stdout
        ArrayList<String> inputFileNames = new ArrayList<>();
        ArrayList<String> outputFileNames = new ArrayList<>();
        boolean oriented = false;
        int matchScore = DEFAULT_MATCH_SCORE;
        int mismatchScore = DEFAULT_MISMATCH_SCORE;
        int gapScore = DEFAULT_GAP_SCORE;
        long penaltyThreshold = DEFAULT_PENALTY_THRESHOLD;
        long singleOverlapPenalty = DEFAULT_SINGLE_OVERLAP_PENALTY;
        int bitapMaxErrors = DEFAULT_BITAP_MAX_ERRORS;
        for (ParseTree option : options) {
            String optionName = option.getChild(0).getText();
            int childCount = option.getChildCount();
            switch (optionName) {
                case "--pattern":
                    if (childCount != 2)
                        exitWithError("Invalid number of arguments for --pattern: " + option.toStringTree());
                    query = option.getChild(1).getText().replaceAll("^\"|\"$", "");
                    break;
                case "--oriented":
                    oriented = true;
                    break;
                case "--scoring":
                    break;
                case "--input":
                    if (childCount < 2)
                        exitWithError("Missing arguments for --input");
                    for (int i = 1; i < childCount; i++)
                        inputFileNames.add(option.getChild(i).getText());
                    break;
                case "--output":
                    if (childCount < 2)
                        exitWithError("Missing arguments for --output");
                    for (int i = 1; i < childCount; i++)
                        outputFileNames.add(option.getChild(i).getText());
                    break;
                default:
                    exitWithError("Option not recognized: " + optionName);
            }
        }
        if (query == null)
            exitWithError("Pattern not specified!");
        LinearGapAlignmentScoring<NucleotideSequence> scoring = new LinearGapAlignmentScoring<>(
                DEFAULT_ALPHABET, matchScore, mismatchScore, gapScore);
        PatternAligner patternAligner = new BasePatternAligner(scoring, penaltyThreshold, singleOverlapPenalty,
                bitapMaxErrors);
    }
}
