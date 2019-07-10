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

import cc.redberry.pipe.CUtils;
import com.milaboratory.cli.PipelineConfiguration;
import com.milaboratory.core.sequence.NucleotideSequence;
import com.milaboratory.minnn.cli.DemultiplexArgument;
import com.milaboratory.minnn.outputconverter.MatchedGroup;
import com.milaboratory.minnn.outputconverter.ParsedRead;
import com.milaboratory.util.SmartProgressReporter;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.PrintStream;
import java.util.*;
import java.util.stream.Collectors;

import static com.milaboratory.minnn.cli.CliUtils.floatFormat;
import static com.milaboratory.minnn.io.ReportWriter.*;
import static com.milaboratory.minnn.util.MinnnVersionInfo.getShortestVersionString;
import static com.milaboratory.minnn.util.SystemUtils.exitWithError;
import static com.milaboratory.util.FormatUtils.nanoTimeToString;

public final class DemultiplexIO {
    private final PipelineConfiguration pipelineConfiguration;
    private final String inputFileName;
    private final List<DemultiplexFilter> demultiplexFilters;
    private final String logFileName;
    private final long inputReadsLimit;
    private final String reportFileName;
    private final String jsonReportFileName;
    private final boolean debugMode;
    private final String prefix;
    private final LinkedHashMap<OutputFileIdentifier, OutputFileIdentifier> outputFileIdentifiers;
    private final HashSet<String> outputFileNames;
    private MifHeader header;
    private long originalNumberOfReads;

    public DemultiplexIO(PipelineConfiguration pipelineConfiguration, String inputFileName,
                         List<DemultiplexArgument> demultiplexArguments, String logFileName, long inputReadsLimit,
                         String reportFileName, String jsonReportFileName, boolean debugMode) {
        this.pipelineConfiguration = pipelineConfiguration;
        this.inputFileName = inputFileName;
        this.demultiplexFilters = demultiplexArguments.stream().map(this::parseFilter).collect(Collectors.toList());
        this.logFileName = logFileName;
        this.inputReadsLimit = inputReadsLimit;
        this.reportFileName = reportFileName;
        this.jsonReportFileName = jsonReportFileName;
        this.debugMode = debugMode;
        this.prefix = ((inputFileName.length() > 4)
                && inputFileName.substring(inputFileName.length() - 4).equals(".mif"))
                ? inputFileName.substring(0, inputFileName.length() - 4) : inputFileName;
        this.outputFileIdentifiers = new LinkedHashMap<>();
        this.outputFileNames = new HashSet<>();
    }

    public void go() {
        long startTime = System.currentTimeMillis();
        long totalReads = 0;
        long matchedReads = 0;
        String readerStats = null;
        try (MifReader reader = new MifReader(inputFileName);
             PrintStream logWriter = new PrintStream(new FileOutputStream(logFileName))) {
            header = new MifHeader(pipelineConfiguration, reader.getNumberOfTargets(), reader.getCorrectedGroups(),
                    reader.getSortedGroups(), reader.getGroupEdges());
            if (inputReadsLimit > 0)
                reader.setParsedReadsLimit(inputReadsLimit);
            SmartProgressReporter.startProgressReport("Demultiplexing reads", reader, System.err);
            for (ParsedRead parsedRead : CUtils.it(reader)) {
                DemultiplexResult demultiplexResult = demultiplex(parsedRead);
                if (demultiplexResult.mifWriter != null) {
                    demultiplexResult.mifWriter.write(demultiplexResult.parsedRead);
                    if (!outputFileNames.contains(demultiplexResult.outputFileName)) {
                        logWriter.println(demultiplexResult.outputFileName);
                        outputFileNames.add(demultiplexResult.outputFileName);
                    }
                    matchedReads++;
                }
                if (++totalReads == inputReadsLimit)
                    break;
            }
            if (debugMode)
                readerStats = reader.getStats().toString();
            reader.close();
            originalNumberOfReads = reader.getOriginalNumberOfReads();
            outputFileIdentifiers.keySet().forEach(OutputFileIdentifier::closeWriter);
        } catch (IOException e) {
            throw exitWithError(e.getMessage());
        }

        StringBuilder reportFileHeader = new StringBuilder();
        StringBuilder report = new StringBuilder();
        LinkedHashMap<String, Object> jsonReportData = new LinkedHashMap<>();

        reportFileHeader.append("MiNNN v").append(getShortestVersionString()).append('\n');
        reportFileHeader.append("Report for Demultiplex command:\n");
        if (inputFileName == null)
            reportFileHeader.append("Input is from stdin\n");
        else
            reportFileHeader.append("Input file name: ").append(inputFileName).append('\n');
        reportFileHeader.append("Output files prefix: ").append(prefix).append('\n');
        if (debugMode) {
            reportFileHeader.append("\n\nDebug information:\n\n");
            reportFileHeader.append("Reader stats:\n").append(readerStats).append("\n\n");
        }

        long elapsedTime = System.currentTimeMillis() - startTime;
        report.append("\nProcessing time: ").append(nanoTimeToString(elapsedTime * 1000000)).append('\n');
        float percent = (totalReads == 0) ? 0 : (float)matchedReads / totalReads * 100;
        report.append("Processed ").append(totalReads).append(" reads, matched ").append(matchedReads)
                .append(" reads (").append(floatFormat.format(percent)).append("%)\n");

        jsonReportData.put("version", getShortestVersionString());
        jsonReportData.put("inputFileName", inputFileName);
        jsonReportData.put("prefix", prefix);
        jsonReportData.put("outputFilesNum", outputFileNames.size());
        jsonReportData.put("elapsedTime", elapsedTime);
        jsonReportData.put("matchedReads", matchedReads);
        jsonReportData.put("totalReads", totalReads);

        humanReadableReport(reportFileName, reportFileHeader.toString(), report.toString());
        jsonReport(jsonReportFileName, jsonReportData);
    }

    private DemultiplexFilter parseFilter(DemultiplexArgument demultiplexArgument) {
        if (demultiplexArgument.isBarcode)
            return new BarcodeFilter(demultiplexArgument);
        else
            return new SampleFilter(demultiplexArgument);
    }

    private MifWriter getMifWriter(OutputFileIdentifier outputFileIdentifier) {
        if (outputFileIdentifiers.containsKey(outputFileIdentifier))
            return outputFileIdentifiers.get(outputFileIdentifier).getWriter();
        else {
            outputFileIdentifiers.put(outputFileIdentifier, outputFileIdentifier);
            return outputFileIdentifier.getWriter();
        }
    }

    private DemultiplexResult demultiplex(ParsedRead parsedRead) {
        List<DemultiplexParameterValue> parameterValues = new ArrayList<>();
        for (DemultiplexFilter demultiplexFilter : demultiplexFilters) {
            DemultiplexParameterValue parameterValue = demultiplexFilter.filter(parsedRead);
            if (parameterValue == null)
                return new DemultiplexResult(parsedRead, null, null);
            else
                parameterValues.add(parameterValue);
        }
        OutputFileIdentifier outputFileIdentifier = new OutputFileIdentifier(parameterValues);
        return new DemultiplexResult(parsedRead, outputFileIdentifier.toString(), getMifWriter(outputFileIdentifier));
    }

    private interface DemultiplexFilter {
        DemultiplexParameterValue filter(ParsedRead parsedRead);
    }

    private class BarcodeFilter implements DemultiplexFilter {
        private final String name;

        BarcodeFilter(DemultiplexArgument argument) {
            if (!argument.isBarcode)
                throw new IllegalArgumentException("Non-barcode argument " + argument.argument
                        + " passed to BarcodeFilter!");
            name = argument.argument;
        }

        @Override
        public DemultiplexParameterValue filter(ParsedRead parsedRead) {
            for (MatchedGroup matchedGroup : parsedRead.getGroups())
                if (matchedGroup.getGroupName().equals(name))
                    return new Barcode(matchedGroup.getValue().getSequence());
            return null;
        }
    }

    private class SampleFilter implements DemultiplexFilter {
        private final List<Sample> samples;

        SampleFilter(DemultiplexArgument argument) {
            if (argument.isBarcode)
                throw new IllegalArgumentException("Non-sample argument " + argument.argument
                        + " passed to SampleFilter!");
            samples = new ArrayList<>();
            File sampleFile = new File(argument.argument);
            try (Scanner sampleScanner = new Scanner(sampleFile)) {
                String[] barcodeNames;
                if (sampleScanner.hasNextLine()) {
                    String[] header = getTokens(sampleScanner.nextLine());
                    if ((header.length < 2) || !header[0].equals("Sample"))
                        throw exitWithError("Wrong sample file " + argument.argument + ": first line is expected " +
                                "to start with Sample keyword and contain at least 1 barcode name!");
                    barcodeNames = new String[header.length - 1];
                    System.arraycopy(header, 1, barcodeNames, 0, barcodeNames.length);
                } else
                    throw exitWithError("Missing header in sample file " + argument.argument);
                if (!sampleScanner.hasNextLine())
                    throw exitWithError("Expected at least 1 sample in sample file " + argument.argument);
                while (sampleScanner.hasNextLine()) {
                    String[] sampleTokens = getTokens(sampleScanner.nextLine());
                    if (sampleTokens.length == 0)
                        break;
                    else if (sampleTokens.length == 1)
                        throw exitWithError("Wrong line in " + argument.argument + ": " + sampleTokens[0]);
                    else {
                        NucleotideSequence[] barcodeSequences = new NucleotideSequence[sampleTokens.length - 1];
                        for (int i = 0; i < barcodeSequences.length; i++)
                            barcodeSequences[i] = new NucleotideSequence(sampleTokens[i + 1]);
                        samples.add(new Sample(sampleTokens[0], barcodeNames, barcodeSequences));
                    }
                }
            } catch (IOException e) {
                throw exitWithError(e.getMessage());
            }
        }

        private String[] getTokens(String string) {
            return string.split("[ \\t]");
        }

        @Override
        public DemultiplexParameterValue filter(ParsedRead parsedRead) {
            for (Sample sample : samples) {
                boolean allMatch = true;
                for (int i = 0; i < sample.numBarcodes(); i++) {
                    String currentName = sample.barcodeNames[i];
                    NucleotideSequence currentSequence = sample.barcodeSequences[i];
                    boolean groupFound = false;
                    for (MatchedGroup matchedGroup : parsedRead.getGroups())
                        if (matchedGroup.getGroupName().equals(currentName)) {
                            groupFound = true;
                            if (!matchedGroup.getValue().getSequence().equals(currentSequence))
                                allMatch = false;
                            break;
                        }
                    if (!groupFound)
                        allMatch = false;
                    if (!allMatch)
                        break;
                }
                if (allMatch)
                    return sample;
            }
            return null;
        }
    }

    private interface DemultiplexParameterValue {}

    private class Barcode implements DemultiplexParameterValue {
        final NucleotideSequence barcode;

        Barcode(NucleotideSequence barcode) {
            this.barcode = barcode;
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;
            Barcode that = (Barcode)o;
            return barcode.equals(that.barcode);
        }

        @Override
        public int hashCode() {
            return barcode.hashCode();
        }

        @Override
        public String toString() {
            return barcode.toString();
        }
    }

    private class Sample implements DemultiplexParameterValue {
        final String name;
        final String[] barcodeNames;
        final NucleotideSequence[] barcodeSequences;

        Sample(String name, String[] barcodeNames, NucleotideSequence[] barcodeSequences) {
            if (barcodeNames.length == 0)
                throw exitWithError("Invalid sample file: missing barcode names!");
            if (barcodeNames.length != barcodeSequences.length)
                throw exitWithError("Invalid sample: mismatched number of barcode names "
                        + Arrays.toString(barcodeNames) + " and barcodes " + Arrays.toString(barcodeSequences));
            this.name = name;
            this.barcodeNames = barcodeNames;
            this.barcodeSequences = barcodeSequences;
        }

        int numBarcodes() {
            return barcodeNames.length;
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;
            Sample sample = (Sample)o;
            return Arrays.equals(barcodeSequences, sample.barcodeSequences);
        }

        @Override
        public int hashCode() {
            return Arrays.hashCode(barcodeSequences);
        }

        @Override
        public String toString() {
            return name;
        }
    }

    private class OutputFileIdentifier {
        final List<DemultiplexParameterValue> parameterValues;
        MifWriter writer = null;

        OutputFileIdentifier(List<DemultiplexParameterValue> parameterValues) {
            this.parameterValues = parameterValues;
        }

        MifWriter getWriter() {
            if (writer == null) {
                try {
                    writer = new MifWriter(toString(), header);
                } catch (IOException e) {
                    throw exitWithError(e.toString());
                }
            }
            return writer;
        }

        void closeWriter() {
            if (writer != null) {
                writer.setOriginalNumberOfReads(originalNumberOfReads);
                try {
                    writer.close();
                } catch (IOException e) {
                    throw exitWithError(e.toString());
                }
            }
            writer = null;
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;
            OutputFileIdentifier that = (OutputFileIdentifier)o;
            return parameterValues.equals(that.parameterValues);
        }

        @Override
        public int hashCode() {
            return parameterValues.hashCode();
        }

        @Override
        public String toString() {
            StringBuilder builder = new StringBuilder(prefix);
            for (DemultiplexParameterValue parameterValue : parameterValues) {
                builder.append('_');
                builder.append(parameterValue.toString());
            }
            builder.append(".mif");
            return builder.toString();
        }
    }

    private class DemultiplexResult {
        final ParsedRead parsedRead;
        final String outputFileName;
        final MifWriter mifWriter;

        DemultiplexResult(ParsedRead parsedRead, String outputFileName, MifWriter mifWriter) {
            this.parsedRead = parsedRead;
            this.outputFileName = outputFileName;
            this.mifWriter = mifWriter;
        }
    }
}
