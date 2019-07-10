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
package com.milaboratory.minnn.io;

import cc.redberry.pipe.*;
import cc.redberry.pipe.blocks.Merger;
import cc.redberry.pipe.blocks.ParallelProcessor;
import cc.redberry.pipe.util.Chunk;
import cc.redberry.pipe.util.OrderedOutputPort;
import com.milaboratory.cli.PipelineConfiguration;
import com.milaboratory.core.io.sequence.*;
import com.milaboratory.core.io.sequence.fasta.*;
import com.milaboratory.core.io.sequence.fastq.*;
import com.milaboratory.core.sequence.*;
import com.milaboratory.minnn.cli.DescriptionGroups;
import com.milaboratory.minnn.outputconverter.*;
import com.milaboratory.minnn.pattern.*;
import com.milaboratory.util.CanReportProgress;
import com.milaboratory.util.SmartProgressReporter;

import java.io.IOException;
import java.util.*;
import java.util.concurrent.atomic.AtomicLong;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;

import static com.milaboratory.minnn.cli.CliUtils.floatFormat;
import static com.milaboratory.minnn.io.MinnnDataFormat.*;
import static com.milaboratory.minnn.io.ReportWriter.*;
import static com.milaboratory.minnn.parser.Parser.BUILTIN_READ_GROUPS_NUM;
import static com.milaboratory.minnn.util.MinnnVersionInfo.getShortestVersionString;
import static com.milaboratory.minnn.util.SystemUtils.exitWithError;
import static com.milaboratory.util.FormatUtils.nanoTimeToString;
import static java.lang.Double.NaN;

public final class ReadProcessor {
    private final PipelineConfiguration pipelineConfiguration;
    private final List<String> inputFileNames;
    private final String outputFileName;
    private final String notMatchedOutputFileName;
    private final Pattern pattern;
    private final String patternQuery;
    private final int outputNumberOfTargets;
    private final boolean orientedReads;
    private final boolean fairSorting;
    private final long inputReadsLimit;
    private final int threads;
    private final String reportFileName;
    private final String jsonReportFileName;
    private final boolean debugMode;
    private final MinnnDataFormat inputFormat;
    private final DescriptionGroups descriptionGroups;
    private final AtomicLong totalReads = new AtomicLong(0);

    public ReadProcessor(PipelineConfiguration pipelineConfiguration, List<String> inputFileNames,
                         String outputFileName, String notMatchedOutputFileName, Pattern pattern, String patternQuery,
                         boolean orientedReads, boolean fairSorting, long inputReadsLimit, int threads,
                         String reportFileName, String jsonReportFileName, boolean debugMode,
                         MinnnDataFormat inputFormat, DescriptionGroups descriptionGroups) {
        if ((inputFormat == MIF) && (inputFileNames.size() > 1))
            throw exitWithError("Mif data format uses single file; specified " + inputFileNames.size()
                    + " input files!");
        this.pipelineConfiguration = pipelineConfiguration;
        this.inputFileNames = inputFileNames;
        this.outputFileName = outputFileName;
        this.notMatchedOutputFileName = notMatchedOutputFileName;
        this.pattern = pattern;
        this.patternQuery = patternQuery;
        this.outputNumberOfTargets = calculateOutputNumberOfTargets();
        this.orientedReads = orientedReads;
        this.fairSorting = fairSorting;
        this.inputReadsLimit = inputReadsLimit;
        this.threads = threads;
        this.reportFileName = reportFileName;
        this.jsonReportFileName = jsonReportFileName;
        this.debugMode = debugMode;
        this.inputFormat = inputFormat;
        this.descriptionGroups = descriptionGroups;
    }

    public void processReadsParallel() {
        long startTime = System.currentTimeMillis();
        long matchedReads = 0;
        String readerStats = null;
        String writerStats = null;
        try (IndexedSequenceReader<?> reader = createReader();
             MifWriter writer = Objects.requireNonNull(createWriter(false));
             MifWriter mismatchedReadsWriter = createWriter(true)) {
            SmartProgressReporter.startProgressReport("Parsing", reader, System.err);
            Merger<Chunk<IndexedSequenceRead>> bufferedReaderPort = CUtils.buffered(CUtils.chunked(reader,
                    4 * 64), 4 * 16);
            OutputPort<Chunk<ParsedRead>> parsedReadsPort = new ParallelProcessor<>(bufferedReaderPort,
                    CUtils.chunked(new ReadParserProcessor(orientedReads)), threads);
            OrderedOutputPort<ParsedRead> orderedReadsPort = new OrderedOutputPort<>(CUtils.unchunked(parsedReadsPort),
                    ParsedRead::getOutputPortId);
            for (ParsedRead parsedRead : CUtils.it(orderedReadsPort)) {
                if (parsedRead.getBestMatch() != null) {
                    writer.write(parsedRead);
                    matchedReads++;
                } else if (mismatchedReadsWriter != null)
                    mismatchedReadsWriter.write(parsedRead);
            }
            if (debugMode) {
                if (inputFormat == MIF)
                    readerStats = reader.getMifReader().getStats().toString();
                writerStats = writer.getStats().toString();
            }
            reader.close();
            long originalNumberOfReads = (inputFormat == MIF) ? reader.getOriginalNumberOfReads() : totalReads.get();
            writer.setOriginalNumberOfReads(originalNumberOfReads);
            if (mismatchedReadsWriter != null)
                mismatchedReadsWriter.setOriginalNumberOfReads(originalNumberOfReads);
        } catch (IOException e) {
            throw exitWithError(e.getMessage());
        }

        StringBuilder reportFileHeader = new StringBuilder();
        StringBuilder report = new StringBuilder();
        LinkedHashMap<String, Object> jsonReportData = new LinkedHashMap<>();

        reportFileHeader.append("MiNNN v").append(getShortestVersionString()).append('\n');
        reportFileHeader.append("Report for Extract command:\n");
        switch (inputFileNames.size()) {
            case 0:
                reportFileHeader.append("Input is from stdin\n");
                break;
            case 1:
                reportFileHeader.append("Input file name: ").append(inputFileNames.get(0)).append('\n');
                break;
            default:
                reportFileHeader.append("Input files: ").append(inputFileNames).append('\n');
        }
        if (outputFileName == null)
            reportFileHeader.append("Output is to stdout\n");
        else
            reportFileHeader.append("Output file name: ").append(outputFileName).append('\n');
        if (notMatchedOutputFileName != null)
            reportFileHeader.append("Output file for not matched reads: ").append(notMatchedOutputFileName)
                    .append('\n');
        reportFileHeader.append("Pattern: ").append(patternQuery).append('\n');
        if (debugMode) {
            reportFileHeader.append("\n\nDebug information:\n\n");
            if (inputFormat == MIF)
                reportFileHeader.append("Reader stats:\n").append(readerStats).append('\n');
            reportFileHeader.append("Writer stats:\n").append(writerStats).append("\n\n");
        }

        long elapsedTime = System.currentTimeMillis() - startTime;
        report.append("\nProcessing time: ").append(nanoTimeToString(elapsedTime * 1000000)).append('\n');
        float percent = (totalReads.get() == 0) ? 0 : (float)matchedReads / totalReads.get() * 100;
        report.append("Processed ").append(totalReads).append(" reads, matched ").append(matchedReads)
                .append(" reads (").append(floatFormat.format(percent)).append("%)\n");

        jsonReportData.put("version", getShortestVersionString());
        jsonReportData.put("inputFileNames", inputFileNames);
        jsonReportData.put("outputFileName", outputFileName);
        jsonReportData.put("notMatchedOutputFileName", notMatchedOutputFileName);
        jsonReportData.put("patternQuery", patternQuery);
        jsonReportData.put("pattern", pattern.toString());
        jsonReportData.put("elapsedTime", elapsedTime);
        jsonReportData.put("matchedReads", matchedReads);
        jsonReportData.put("totalReads", totalReads.get());

        humanReadableReport(reportFileName, reportFileHeader.toString(), report.toString());
        jsonReport(jsonReportFileName, jsonReportData);
    }

    private IndexedSequenceReader createReader() throws IOException {
        IndexedSequenceReader reader;
        int numberOfTargets;
        switch (inputFormat) {
            case FASTQ:
                switch (inputFileNames.size()) {
                    case 0:
                        numberOfTargets = 1;
                        reader = new IndexedSequenceReader<>(new SingleFastqReader(System.in), t -> t);
                        break;
                    case 1:
                        numberOfTargets = 1;
                        String[] s = inputFileNames.get(0).split("\\.");
                        if (s[s.length - 1].equals("fasta") || s[s.length - 1].equals("fa")
                                || ((s.length > 2) && s[s.length - 1].equals("gz")
                                    && (s[s.length - 2].equals("fasta") || s[s.length - 2].equals("fa"))))
                            reader = new IndexedSequenceReader<>(new FastaSequenceReaderWrapper(new FastaReader<>(
                                    inputFileNames.get(0), NucleotideSequence.ALPHABET)), t -> t);
                        else
                            reader = new IndexedSequenceReader<>(new SingleFastqReader(inputFileNames.get(0)), t -> t);
                        break;
                    case 2:
                        numberOfTargets = 2;
                        reader = new IndexedSequenceReader<>(new PairedFastqReader(inputFileNames.get(0),
                                inputFileNames.get(1)), t -> t);
                        break;
                    default:
                        numberOfTargets = inputFileNames.size();
                        SingleFastqReader[] readers = new SingleFastqReader[inputFileNames.size()];
                        for (int i = 0; i < inputFileNames.size(); i++)
                            readers[i] = new SingleFastqReader(inputFileNames.get(i));
                        reader = new IndexedSequenceReader<>(new MultiReader(readers), t -> t);
                }
                break;
            case MIF:
                MifReader mifReader = (inputFileNames.size() == 0) ? new MifReader(System.in)
                        : new MifReader(inputFileNames.get(0));
                if (inputReadsLimit > 0)
                    mifReader.setParsedReadsLimit(inputReadsLimit);
                numberOfTargets = mifReader.getNumberOfTargets();
                reader = new IndexedSequenceReader<>(mifReader, ParsedRead::getOriginalRead);
                break;
            default:
                throw new IllegalStateException("Unknown input format: " + inputFormat);
        }
        int targetsInPattern = pattern instanceof SinglePattern ? 1
                : ((MultipleReadsOperator)pattern).getNumberOfPatterns();
        if (numberOfTargets != targetsInPattern)
            throw exitWithError("Mismatched number of patterns (" + targetsInPattern + ") and target reads ("
                    + numberOfTargets + ")!");
        return reader;
    }

    private MifWriter createWriter(boolean mismatchedReads) throws IOException {
        ArrayList<GroupEdge> outputGroupEdges = new ArrayList<>(pattern.getGroupEdges());
        descriptionGroups.getGroupNames().forEach(groupName -> {
            outputGroupEdges.add(new GroupEdge(groupName, true));
            outputGroupEdges.add(new GroupEdge(groupName, false));
        });
        MifHeader mifHeader = new MifHeader(pipelineConfiguration, outputNumberOfTargets, new ArrayList<>(),
                new ArrayList<>(), outputGroupEdges);
        if (mismatchedReads)
            return (notMatchedOutputFileName == null) ? null : new MifWriter(notMatchedOutputFileName, mifHeader);
        else
            return (outputFileName == null) ? new MifWriter(new SystemOutStream(), mifHeader)
                    : new MifWriter(outputFileName, mifHeader);
    }

    private int calculateOutputNumberOfTargets() {
        Set<String> outputGroupNames = pattern.getGroupEdges().stream().map(GroupEdge::getGroupName)
                .collect(Collectors.toSet());
        if (!outputGroupNames.contains("R1"))
            throw exitWithError("Default groups overriding requires all output default groups to be specified; "
                    + "group R1 not found in the pattern!");
        int foundDefaultGroups = 1;
        boolean lastGroupFound = false;
        for (int i = 2; i < BUILTIN_READ_GROUPS_NUM; i++) {
            String expectedGroupName = "R" + i;
            if (lastGroupFound && outputGroupNames.contains(expectedGroupName))
                throw exitWithError("Default groups overriding requires all output default groups to be specified; "
                        + "group " + expectedGroupName + " is found, but group R" + (i - 1) + " is missing!");
            if (outputGroupNames.contains(expectedGroupName))
                foundDefaultGroups++;
            else
                lastGroupFound = true;
        }
        return foundDefaultGroups;
    }

    private class IndexedSequenceRead {
        final SequenceRead sequenceRead;
        final long index;

        IndexedSequenceRead(SequenceRead sequenceRead, long index) {
            this.sequenceRead = sequenceRead;
            this.index = index;
        }
    }

    private class IndexedSequenceReader<T> implements OutputPortCloseable<IndexedSequenceRead>, CanReportProgress {
        private final OutputPortCloseable<? extends T> innerReader;
        private final Function<T, SequenceRead> toSequenceRead;
        private final CanReportProgress progress;
        private boolean finished = false;

        IndexedSequenceReader(OutputPortCloseable<? extends T> innerReader, Function<T, SequenceRead> toSequenceRead) {
            this.innerReader = innerReader;
            this.toSequenceRead = toSequenceRead;
            this.progress = innerReader instanceof CanReportProgress ? (CanReportProgress)innerReader : null;
        }

        @Override
        public synchronized void close() {
            innerReader.close();
            finished = true;
        }

        @Override
        public synchronized IndexedSequenceRead take() {
            if (finished)
                return null;
            T t = innerReader.take();
            if (t == null) {
                finished = true;
                return null;
            } else {
                if (totalReads.incrementAndGet() == inputReadsLimit)
                    finished = true;
                return new IndexedSequenceRead(toSequenceRead.apply(t), totalReads.get() - 1);
            }
        }

        @Override
        public double getProgress() {
            if (inputReadsLimit < 1) {
                if (progress != null)
                    return progress.getProgress();
                else
                    return NaN;
            } else {
                double estimationByTakenReads = (double)totalReads.get() / inputReadsLimit;
                if (progress != null)
                    return Math.max(estimationByTakenReads, progress.getProgress());
                else
                    return estimationByTakenReads;
            }
        }

        @Override
        public synchronized boolean isFinished() {
            return finished;
        }

        MifReader getMifReader() {
            if (inputFormat == MIF)
                return (MifReader)innerReader;
            else
                throw new IllegalStateException("getMifReader() must be used only with MIF input!");
        }

        long getOriginalNumberOfReads() {
            return getMifReader().getOriginalNumberOfReads();
        }
    }

    private class ReadParserProcessor implements Processor<IndexedSequenceRead, ParsedRead> {
        private final boolean orientedReads;

        ReadParserProcessor(boolean orientedReads) {
            this.orientedReads = orientedReads;
        }

        @Override
        public ParsedRead process(IndexedSequenceRead input) {
            Match bestMatch = null;
            boolean reverseMatch = false;
            if (orientedReads) {
                MultiNSequenceWithQualityImpl target = new MultiNSequenceWithQualityImpl(StreamSupport.stream(
                        input.sequenceRead.spliterator(), false).map(SingleRead::getData)
                        .toArray(NSequenceWithQuality[]::new));
                bestMatch = pattern.match(target).getBestMatch(fairSorting);
            } else {
                NSequenceWithQuality[] sequences = StreamSupport.stream(input.sequenceRead.spliterator(), false)
                        .map(SingleRead::getData).toArray(NSequenceWithQuality[]::new);
                int numberOfReads = sequences.length;
                if (numberOfReads == 1)
                    bestMatch = pattern.match(sequences[0]).getBestMatch(fairSorting);
                else {
                    NSequenceWithQuality[] sequencesWithSwap = sequences.clone();
                    sequencesWithSwap[0] = sequences[1];
                    sequencesWithSwap[1] = sequences[0];
                    MultiNSequenceWithQualityImpl notSwappedTarget = new MultiNSequenceWithQualityImpl(sequences);
                    MultiNSequenceWithQualityImpl swappedTarget = new MultiNSequenceWithQualityImpl(sequencesWithSwap);
                    Match notSwappedMatch = pattern.match(notSwappedTarget).getBestMatch(fairSorting);
                    Match swappedMatch = pattern.match(swappedTarget).getBestMatch(fairSorting);
                    if (notSwappedMatch == null) {
                        if (swappedMatch != null) {
                            bestMatch = swappedMatch;
                            reverseMatch = true;
                        }
                    } else {
                        if (swappedMatch != null) {
                            if (swappedMatch.getScore() > notSwappedMatch.getScore()) {
                                bestMatch = swappedMatch;
                                reverseMatch = true;
                            } else
                                bestMatch = notSwappedMatch;
                        } else
                            bestMatch = notSwappedMatch;
                    }
                }
            }

            int numberOfTargetsOverride = (pattern.isDefaultGroupsOverride()) ? outputNumberOfTargets : -1;
            return new ParsedRead(input.sequenceRead, reverseMatch, numberOfTargetsOverride,
                    (bestMatch == null) ? null : descriptionGroups.addDescriptionGroups(bestMatch, input.sequenceRead),
                    0, input.index);
        }
    }
}
