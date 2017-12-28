package com.milaboratory.mist.io;

import cc.redberry.pipe.*;
import cc.redberry.pipe.blocks.ParallelProcessor;
import cc.redberry.pipe.util.OrderedOutputPort;
import com.milaboratory.core.io.sequence.*;
import com.milaboratory.core.io.sequence.fasta.*;
import com.milaboratory.core.io.sequence.fastq.*;
import com.milaboratory.core.sequence.*;
import com.milaboratory.mist.outputconverter.*;
import com.milaboratory.mist.pattern.*;
import com.milaboratory.util.CanReportProgress;
import com.milaboratory.util.SmartProgressReporter;

import java.io.IOException;
import java.util.*;
import java.util.stream.StreamSupport;

import static com.milaboratory.mist.io.MistDataFormat.*;
import static com.milaboratory.mist.util.SystemUtils.exitWithError;
import static com.milaboratory.util.TimeUtils.nanoTimeToString;

public final class ReadProcessor {
    private final List<String> inputFileNames;
    private final String outputFileName;
    private final Pattern pattern;
    private final boolean orientedReads;
    private final boolean fairSorting;
    private final int threads;
    private final MistDataFormat inputFormat;
    private final boolean testIOSpeed;
    private boolean readsNumberChecked = false;

    public ReadProcessor(List<String> inputFileNames, String outputFileName, Pattern pattern,
            boolean orientedReads, boolean fairSorting, int threads, MistDataFormat inputFormat, boolean testIOSpeed) {
        if ((inputFormat == MIF) && (inputFileNames.size() > 1))
            throw exitWithError("Mif data format uses single file; specified " + inputFileNames.size()
                    + " input files!");
        this.inputFileNames = inputFileNames;
        this.outputFileName = outputFileName;
        this.pattern = pattern;
        this.orientedReads = orientedReads;
        this.fairSorting = fairSorting;
        this.threads = threads;
        this.inputFormat = inputFormat;
        this.testIOSpeed = testIOSpeed;
    }

    public void processReadsParallel() {
        long startTime = System.currentTimeMillis();
        OutputPortCloseable<IndexedSequenceRead> reader;
        try {
            reader = createReader();
        } catch (IOException e) {
            throw exitWithError(e.getMessage());
        }
        CanReportProgress progress = (CanReportProgress)reader;
        SmartProgressReporter.startProgressReport("Parsing", progress);
        OutputPort<IndexedSequenceRead> bufferedReaderPort = CUtils.buffered(reader, 2048);
        OutputPort<ProcessorInput> processorInputs = () -> readToProcessorInput(bufferedReaderPort.take());
        OutputPort<ProcessorOutput> parsedReadsPort = new ParallelProcessor<>(processorInputs,
                testIOSpeed ? new TestIOSpeedProcessor() : new ReadParserProcessor(), threads);
        OrderedOutputPort<ProcessorOutput> orderedReadsPort = new OrderedOutputPort<>(parsedReadsPort,
                object -> object.index);

        MifWriter writer = null;
        long totalReads = 0;
        long matchedReads = 0;
        for (ProcessorOutput processorOutput : CUtils.it(orderedReadsPort)) {
            ParsedRead parsedRead = processorOutput.parsedRead;
            totalReads++;
            if (parsedRead.getBestMatch() != null) {
                if (writer == null)
                    writer = createWriter(pattern.getGroupEdges());
                writer.write(parsedRead);
                matchedReads++;
            }
        }
        if (writer == null)
            writer = createWriter(new ArrayList<>());
        reader.close();
        writer.close();

        long elapsedTime = System.currentTimeMillis() - startTime;
        System.out.println("\nProcessing time: " + nanoTimeToString(elapsedTime * 1000000));
        System.out.println(String.format("Matched reads: %.1f%%\n",
                totalReads == 0 ? 0.0 : matchedReads * 100.0 / totalReads));
    }

    private OutputPortCloseable<IndexedSequenceRead> createReader() throws IOException {
        switch (inputFormat) {
            case FASTQ:
                switch (inputFileNames.size()) {
                    case 0:
                        return new FastqSequenceReader(new SingleFastqReader(System.in));
                    case 1:
                        String[] s = inputFileNames.get(0).split("\\.");
                        if (s[s.length - 1].equals("fasta") || s[s.length - 1].equals("fa"))
                            return new FastqSequenceReader(new FastaSequenceReaderWrapper(new FastaReader<>(
                                    inputFileNames.get(0), NucleotideSequence.ALPHABET), true));
                        else
                            return new FastqSequenceReader(new SingleFastqReader(inputFileNames.get(0),
                                    true));
                    case 2:
                        return new FastqSequenceReader(new PairedFastqReader(inputFileNames.get(0),
                                inputFileNames.get(1), true));
                    default:
                        SingleFastqReader readers[] = new SingleFastqReader[inputFileNames.size()];
                        for (int i = 0; i < inputFileNames.size(); i++)
                            readers[i] = new SingleFastqReader(inputFileNames.get(i), true);
                        return new FastqSequenceReader(new MultiReader(readers));
                }
            case MIF:
                if (inputFileNames.size() == 0)
                    return new MifSequenceReader(new MifReader(System.in));
                else
                    return new MifSequenceReader(new MifReader(inputFileNames.get(0)));
            default:
                throw new IllegalStateException("Unknown input format: " + inputFormat);
        }
    }

    private MifWriter createWriter(ArrayList<GroupEdge> groupEdges) {
        if (outputFileName == null)
            return new MifWriter(System.out, groupEdges);
        else
            try {
                return new MifWriter(outputFileName, groupEdges);
            } catch (IOException e) {
                throw exitWithError(e.getMessage());
            }
    }

    private ProcessorInput readToProcessorInput(IndexedSequenceRead indexedRead) {
        if (indexedRead == null)
            return null;
        if (!readsNumberChecked) {
            int readsNumberInPattern = pattern instanceof SinglePattern ? 1
                    : ((MultipleReadsOperator)pattern).getNumberOfPatterns();
            int readsNumberInInput = indexedRead.sequenceRead.numberOfReads();
            if (readsNumberInPattern != readsNumberInInput)
                throw exitWithError("Mismatched number of patterns (" + readsNumberInPattern
                        + ") and reads (" + readsNumberInInput + ")!");
            readsNumberChecked = true;
        }

        return new ProcessorInput(indexedRead.sequenceRead, orientedReads, indexedRead.index);
    }

    private class ProcessorInput {
        final SequenceRead read;
        final boolean orientedReads;
        final long index;

        ProcessorInput(SequenceRead read, boolean orientedReads, long index) {
            this.read = read;
            this.orientedReads = orientedReads;
            this.index = index;
        }
    }

    private class ProcessorOutput {
        final ParsedRead parsedRead;
        final long index;

        ProcessorOutput(ParsedRead parsedRead, long index) {
            this.parsedRead = parsedRead;
            this.index = index;
        }
    }

    private class IndexedSequenceRead {
        final SequenceRead sequenceRead;
        final long index;

        IndexedSequenceRead(SequenceRead sequenceRead, long index) {
            this.sequenceRead = sequenceRead;
            this.index = index;
        }
    }

    private class FastqSequenceReader implements OutputPortCloseable<IndexedSequenceRead>, CanReportProgress {
        private final SequenceReaderCloseable fastqReader;

        FastqSequenceReader(SequenceReaderCloseable fastqReader) {
            this.fastqReader = fastqReader;
        }

        @Override
        public void close() {
            fastqReader.close();
        }

        @Override
        public IndexedSequenceRead take() {
            SequenceRead sequenceRead = (SequenceRead)(fastqReader.take());
            return sequenceRead == null ? null : new IndexedSequenceRead(sequenceRead, sequenceRead.getId());
        }

        @Override
        public double getProgress() {
            return ((CanReportProgress)fastqReader).getProgress();
        }

        @Override
        public boolean isFinished() {
            return ((CanReportProgress)fastqReader).isFinished();
        }
    }

    private class MifSequenceReader implements OutputPortCloseable<IndexedSequenceRead>, CanReportProgress {
        private final MifReader mifReader;
        private long index = 0;

        MifSequenceReader(MifReader mifReader) {
            this.mifReader = mifReader;
        }

        @Override
        public void close() {
            mifReader.close();
        }

        @Override
        public IndexedSequenceRead take() {
            ParsedRead parsedRead = mifReader.take();
            return parsedRead == null ? null : new IndexedSequenceRead(parsedRead.getOriginalRead(), index++);
        }

        @Override
        public double getProgress() {
            return mifReader.getProgress();
        }

        @Override
        public boolean isFinished() {
            return mifReader.isFinished();
        }
    }

    private class ReadParserProcessor implements Processor<ProcessorInput, ProcessorOutput> {
        @Override
        public ProcessorOutput process(ProcessorInput input) {
            Match bestMatch = null;
            boolean reverseMatch = false;
            if (input.orientedReads) {
                MultiNSequenceWithQualityImpl target = new MultiNSequenceWithQualityImpl(StreamSupport.stream(
                        input.read.spliterator(), false).map(SingleRead::getData)
                        .toArray(NSequenceWithQuality[]::new));
                bestMatch = pattern.match(target).getBestMatch(fairSorting);
            } else {
                NSequenceWithQuality[] sequences = StreamSupport.stream(input.read.spliterator(), false)
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

            return new ProcessorOutput(new ParsedRead(input.read, reverseMatch, bestMatch), input.index);
        }
    }

    private class TestIOSpeedProcessor implements Processor<ProcessorInput, ProcessorOutput> {
        @Override
        public ProcessorOutput process(ProcessorInput input) {
            return new ProcessorOutput(new ParsedRead(input.read, false, null), input.index);
        }
    }
}
