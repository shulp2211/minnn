/*
 * Copyright (c) 2016-2019, MiLaboratory LLC
 * All Rights Reserved
 *
 * Permission to use, copy, modify and distribute any part of this program for
 * educational, research and non-profit purposes, by non-profit institutions
 * only, without fee, and without a written agreement is hereby granted,
 * provided that the above copyright notice, this paragraph and the following
 * three paragraphs appear in all copies.
 *
 * Those desiring to incorporate this work into commercial products or use for
 * commercial purposes should contact MiLaboratory LLC, which owns exclusive
 * rights for distribution of this program for commercial purposes, using the
 * following email address: licensing@milaboratory.com.
 *
 * IN NO EVENT SHALL THE INVENTORS BE LIABLE TO ANY PARTY FOR DIRECT, INDIRECT,
 * SPECIAL, INCIDENTAL, OR CONSEQUENTIAL DAMAGES, INCLUDING LOST PROFITS,
 * ARISING OUT OF THE USE OF THIS SOFTWARE, EVEN IF THE INVENTORS HAS BEEN
 * ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 *
 * THE SOFTWARE PROVIDED HEREIN IS ON AN "AS IS" BASIS, AND THE INVENTORS HAS
 * NO OBLIGATION TO PROVIDE MAINTENANCE, SUPPORT, UPDATES, ENHANCEMENTS, OR
 * MODIFICATIONS. THE INVENTORS MAKES NO REPRESENTATIONS AND EXTENDS NO
 * WARRANTIES OF ANY KIND, EITHER IMPLIED OR EXPRESS, INCLUDING, BUT NOT
 * LIMITED TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY OR FITNESS FOR A
 * PARTICULAR PURPOSE, OR THAT THE USE OF THE SOFTWARE WILL NOT INFRINGE ANY
 * PATENT, TRADEMARK OR OTHER RIGHTS.
 */
package com.milaboratory.minnn.cli;

import com.milaboratory.cli.*;
import com.milaboratory.minnn.io.FilterIO;
import com.milaboratory.minnn.readfilter.*;
import org.antlr.v4.runtime.CharStreams;
import org.antlr.v4.runtime.CodePointCharStream;
import org.antlr.v4.runtime.CommonTokenStream;
import org.antlr.v4.runtime.ParserRuleContext;
import org.antlr.v4.runtime.tree.ParseTreeWalker;
import picocli.CommandLine.*;

import java.util.*;

import static com.milaboratory.minnn.cli.CommonDescriptions.*;
import static com.milaboratory.minnn.cli.Defaults.*;
import static com.milaboratory.minnn.cli.FilterAction.FILTER_ACTION_NAME;
import static com.milaboratory.minnn.cli.PipelineConfigurationReaderMiNNN.pipelineConfigurationReaderInstance;
import static com.milaboratory.minnn.io.MifInfoExtractor.mifInfoExtractor;
import static com.milaboratory.minnn.util.CommonUtils.*;
import static com.milaboratory.minnn.util.SystemUtils.*;

@Command(name = FILTER_ACTION_NAME,
        sortOptions = false,
        showDefaultValues = true,
        separator = " ",
        description = "Filter target nucleotide sequences, pass only sequences matching the query.")
public final class FilterAction extends ACommandWithSmartOverwrite implements MiNNNCommand {
    public static final String FILTER_ACTION_NAME = "filter";

    public FilterAction() {
        super(APP_NAME, mifInfoExtractor, pipelineConfigurationReaderInstance);
    }

    @Override
    public void run1() {
        String filterQuery = stripQuotes(String.join("", filterQueryList));
        ReadFilter parsedReadFilter = parseFilterQuery(filterQuery);
        if (parsedReadFilter == null)
            throw exitWithError("Filter query not parsed: " + filterQuery);
        FilterIO filterIO = new FilterIO(getFullPipelineConfiguration(), parsedReadFilter,
                String.join("", filterQueryList), inputFileName, outputFileName, inputReadsLimit, threads,
                reportFileName, jsonReportFileName, debugMode);
        filterIO.go();
    }

    @Override
    public void validateInfo(String inputFile) {
        MiNNNCommand.super.validateInfo(inputFile);
    }

    @Override
    public void validate() {
        if (filterQueryList == null)
            throwValidationException("Filter query is not specified!");
        MiNNNCommand.super.validate(getInputFiles(), getOutputFiles());
    }

    @Override
    protected List<String> getInputFiles() {
        List<String> inputFileNames = new ArrayList<>();
        if (inputFileName != null)
            inputFileNames.add(inputFileName);
        return inputFileNames;
    }

    @Override
    protected List<String> getOutputFiles() {
        List<String> outputFileNames = new ArrayList<>();
        if (outputFileName != null)
            outputFileNames.add(outputFileName);
        return outputFileNames;
    }

    @Override
    public void handleExistenceOfOutputFile(String outFileName) {
        // disable smart overwrite if input is from pipe
        if (inputFileName == null)
            MiNNNCommand.super.handleExistenceOfOutputFile(outFileName, forceOverwrite || overwriteIfRequired);
        else
            super.handleExistenceOfOutputFile(outFileName);
    }

    @Override
    public ActionConfiguration getConfiguration() {
        return new FilterActionConfiguration(new FilterActionConfiguration.FilterActionParameters(
                String.join("", filterQueryList), fairSorting, inputReadsLimit));
    }

    @Override
    public PipelineConfiguration getFullPipelineConfiguration() {
        if (inputFileName != null)
            return PipelineConfiguration.appendStep(pipelineConfigurationReader.fromFile(inputFileName,
                    binaryFileInfoExtractor.getFileInfo(inputFileName)), getInputFiles(), getConfiguration(),
                    AppVersionInfo.get());
        else
            return PipelineConfiguration.mkInitial(new ArrayList<>(), getConfiguration(), AppVersionInfo.get());
    }

    @Parameters(arity = "1..*",
            description = "\"<filter_query>\"")
    private List<String> filterQueryList = null;

    @Option(description = IN_FILE_OR_STDIN,
            names = {"--input"})
    private String inputFileName = null;

    @Option(description = OUT_FILE_OR_STDOUT,
            names = {"--output"})
    private String outputFileName = null;

    @Option(description = FAIR_SORTING,
            names = {"--fair-sorting"})
    private boolean fairSorting = false;

    @Option(description = NUMBER_OF_READS,
            names = {"-n", "--number-of-reads"})
    private long inputReadsLimit = 0;

    @Option(description = "Number of threads for parsing reads.",
            names = {"--threads"})
    private int threads = DEFAULT_THREADS;

    @Option(description = REPORT,
            names = "--report")
    private String reportFileName = null;

    @Option(description = JSON_REPORT,
            names = "--json-report")
    private String jsonReportFileName = null;

    private ReadFilter parseFilterQuery(String filterQuery) {
        CodePointCharStream charStream = CharStreams.fromString(filterQuery);
        FilterGrammarLexer lexer = new FilterGrammarLexer(charStream);
        CommonTokenStream tokenStream = new CommonTokenStream(lexer);
        FilterGrammarParser parser = new FilterGrammarParser(tokenStream);
        ParseTreeWalker walker = new ParseTreeWalker();
        FilterListener listener = new FilterListener();
        walker.walk(listener, parser.filter());
        return listener.getFilter();
    }

    private class AntlrFilterListener extends FilterGrammarBaseListener {
        protected ReadFilter filter = null;

        ReadFilter getFilter() {
            return filter;
        }

        protected void setIfNotNull(ParserRuleContext ctx, AntlrFilterListener listener) {
            if (ctx != null) {
                ctx.enterRule(listener);
                ReadFilter parsedFilter = listener.getFilter();
                if (parsedFilter != null)
                    filter = parsedFilter;
            }
        }
    }

    private class MinConsensusReadsListener extends AntlrFilterListener {
        @Override
        public void enterMinConsensusReads(FilterGrammarParser.MinConsensusReadsContext ctx) {
            filter = new ConsensusReadsReadFilter(Integer.parseInt(ctx.minConsensusReadsNum().getText()));
        }
    }

    private class LenListener extends AntlrFilterListener {
        @Override
        public void enterLen(FilterGrammarParser.LenContext ctx) {
            filter = new LenReadFilter(ctx.groupNameOrAll().getText(), Integer.parseInt(ctx.groupLength().getText()));
        }
    }

    private class GroupNFractionListener extends AntlrFilterListener {
        @Override
        public void enterGroupNFraction(FilterGrammarParser.GroupNFractionContext ctx) {
            filter = new GroupNFractionFilter(ctx.groupNameOrAll().getText(),
                    Float.parseFloat(ctx.groupNFractionNum().getText()));
        }
    }

    private class GroupNCountListener extends AntlrFilterListener {
        @Override
        public void enterGroupNCount(FilterGrammarParser.GroupNCountContext ctx) {
            filter = new GroupNCountFilter(ctx.groupNameOrAll().getText(),
                    Integer.parseInt(ctx.groupNCountNum().getText()));
        }
    }

    private class AvgGroupQualityListener extends AntlrFilterListener {
        @Override
        public void enterAvgGroupQuality(FilterGrammarParser.AvgGroupQualityContext ctx) {
            filter = new AvgGroupQualityFilter(ctx.groupNameOrAll().getText(),
                    Byte.parseByte(ctx.avgGroupQualityNum().getText()));
        }
    }

    private class MinGroupQualityListener extends AntlrFilterListener {
        @Override
        public void enterMinGroupQuality(FilterGrammarParser.MinGroupQualityContext ctx) {
            filter = new MinGroupQualityFilter(ctx.groupNameOrAll().getText(),
                    Byte.parseByte(ctx.minGroupQualityNum().getText()));
        }
    }

    private class SimpleFilterListener extends AntlrFilterListener {
        @Override
        public void enterSimpleFilter(FilterGrammarParser.SimpleFilterContext ctx) {
            setIfNotNull(ctx.minGroupQuality(), new MinGroupQualityListener());
            setIfNotNull(ctx.avgGroupQuality(), new AvgGroupQualityListener());
            setIfNotNull(ctx.groupNCount(), new GroupNCountListener());
            setIfNotNull(ctx.groupNFraction(), new GroupNFractionListener());
            setIfNotNull(ctx.minConsensusReads(), new MinConsensusReadsListener());
            setIfNotNull(ctx.len(), new LenListener());
        }
    }

    private class PatternListener extends AntlrFilterListener {
        @Override
        public void enterPattern(FilterGrammarParser.PatternContext ctx) {
            String patternString = ctx.patternString().getText();
            if ((patternString.charAt(0) != '\'') && (patternString.charAt(patternString.length() - 1) != '\''))
                throw exitWithError("Missing single quotes in pattern query " + patternString);
            filter = new PatternReadFilter(ctx.groupName().getText(),
                    patternString.substring(1, patternString.length() - 1), fairSorting);
        }
    }

    private class AndOperandListener extends AntlrFilterListener {
        ArrayList<ReadFilter> readFilters = new ArrayList<>();

        @Override
        public void enterAndOperand(FilterGrammarParser.AndOperandContext ctx) {
            setIfNotNull(ctx.pattern(), new PatternListener());
            setIfNotNull(ctx.simpleFilter(), new SimpleFilterListener());
            setIfNotNull(ctx.filterInParentheses(), new FilterInParenthesesListener());
            readFilters.add(filter);
        }
    }

    private class AndListener extends AntlrFilterListener {
        @Override
        public void enterAnd(FilterGrammarParser.AndContext ctx) {
            AndOperandListener andOperandListener = new AndOperandListener();
            ctx.andOperand().forEach(andOperandContext -> andOperandContext.enterRule(andOperandListener));
            filter = new AndReadFilter(andOperandListener.readFilters);
        }
    }

    private class OrOperandListener extends AntlrFilterListener {
        ArrayList<ReadFilter> readFilters = new ArrayList<>();

        @Override
        public void enterOrOperand(FilterGrammarParser.OrOperandContext ctx) {
            setIfNotNull(ctx.pattern(), new PatternListener());
            setIfNotNull(ctx.simpleFilter(), new SimpleFilterListener());
            setIfNotNull(ctx.and(), new AndListener());
            setIfNotNull(ctx.filterInParentheses(), new FilterInParenthesesListener());
            readFilters.add(filter);
        }
    }

    private class OrListener extends AntlrFilterListener {
        @Override
        public void enterOr(FilterGrammarParser.OrContext ctx) {
            OrOperandListener orOperandListener = new OrOperandListener();
            ctx.orOperand().forEach(orOperandContext -> orOperandContext.enterRule(orOperandListener));
            filter = new OrReadFilter(orOperandListener.readFilters);
        }
    }

    private class AnySingleFilterListener extends AntlrFilterListener {
        @Override
        public void enterAnySingleFilter(FilterGrammarParser.AnySingleFilterContext ctx) {
            setIfNotNull(ctx.simpleFilter(), new SimpleFilterListener());
            setIfNotNull(ctx.pattern(), new PatternListener());
            setIfNotNull(ctx.and(), new AndListener());
            setIfNotNull(ctx.or(), new OrListener());
        }
    }

    private class FilterInParenthesesListener extends AntlrFilterListener {
        @Override
        public void enterFilterInParentheses(FilterGrammarParser.FilterInParenthesesContext ctx) {
            setIfNotNull(ctx.anySingleFilter(), new AnySingleFilterListener());
        }
    }

    private class FilterListener extends AntlrFilterListener {
        @Override
        public void enterFilter(FilterGrammarParser.FilterContext ctx) {
            setIfNotNull(ctx.filterInParentheses(), new FilterInParenthesesListener());
            setIfNotNull(ctx.anySingleFilter(), new AnySingleFilterListener());
        }
    }
}
