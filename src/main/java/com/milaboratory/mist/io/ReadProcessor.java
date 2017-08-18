package com.milaboratory.mist.io;

import cc.redberry.pipe.CUtils;
import cc.redberry.pipe.OutputPort;
import cc.redberry.pipe.Processor;
import cc.redberry.pipe.blocks.ParallelProcessor;
import com.milaboratory.core.io.sequence.*;
import com.milaboratory.core.io.sequence.fasta.*;
import com.milaboratory.core.io.sequence.fastq.*;
import com.milaboratory.core.sequence.NucleotideSequence;
import com.milaboratory.mist.output_converter.ParsedRead;
import com.milaboratory.util.CanReportProgress;
import com.milaboratory.util.SmartProgressReporter;

import java.io.IOException;
import java.util.*;
import java.util.stream.Collectors;

import static com.milaboratory.mist.util.SystemUtils.exitWithError;

public final class ReadProcessor {
    private final List<String> inputFileNames;
    private final List<String> outputFileNames;
    private final boolean orientedReads;
    private final int threads;
    private final boolean addOldComment;

    public ReadProcessor(List<String> inputFileNames, List<String> outputFileNames, boolean orientedReads,
                         int threads, boolean addOldComment) {
        if (((inputFileNames.size() > 1) || (outputFileNames.size() > 1))
                && (inputFileNames.size() != outputFileNames.size()))
            throw exitWithError("Not equal numbers of input and output file names!");
        this.inputFileNames = inputFileNames;
        this.outputFileNames = outputFileNames;
        this.orientedReads = orientedReads;
        this.threads = threads;
        this.addOldComment = addOldComment;
    }

    public void processReadsParallel() {
        List<SequenceReaderCloseable<? extends SequenceRead>> readers = new ArrayList<>();
        SequenceWriter writer;
        try {
            readers.add(createReader(false));
            if (!orientedReads && (inputFileNames.size() >= 2))
                readers.add(createReader(true));
            writer = createWriter();
        } catch (IOException e) {
            throw exitWithError(e.getMessage());
        }
        List<OutputPort<? extends SequenceRead>> readerPorts = new ArrayList<>(readers);
        List<CanReportProgress> progress = readers.stream().map(r -> (CanReportProgress)r).collect(Collectors.toList());
        SmartProgressReporter.startProgressReport("Parsing", progress.get(0));
        if (progress.size() == 2)
            SmartProgressReporter.startProgressReport("Parsing with swapped reads", progress.get(1));
        List<OutputPort<? extends SequenceRead>> bufferedReaderPorts = readerPorts.stream()
                .map(rp -> CUtils.buffered(rp, 16)).collect(Collectors.toList());
        List<OutputPort<ParsedRead>> parsedReads = new ArrayList<>();
        for (int i = 0; i < readers.size(); i++) {
            ReadParserProcessor readParserProcessor = new ReadParserProcessor();
            parsedReads.add(new ParallelProcessor(bufferedReaderPorts.get(i), readParserProcessor, threads));
        }
    }

    private SequenceReaderCloseable<? extends SequenceRead> createReader(boolean swapped) throws IOException {
        switch (inputFileNames.size()) {
            case 0:
                return new SingleFastqReader(System.in);
            case 1:
                String[] s = inputFileNames.get(0).split("\\.");
                if (s[s.length - 1].equals("fasta") || s[s.length - 1].equals("fa"))
                    return new FastaSequenceReaderWrapper(new FastaReader<>(inputFileNames.get(0),
                            NucleotideSequence.ALPHABET), true);
                else
                    return new SingleFastqReader(inputFileNames.get(0), true);
            case 2:
                if (swapped)
                    return new PairedFastqReader(inputFileNames.get(1), inputFileNames.get(0), true);
                else
                    return new PairedFastqReader(inputFileNames.get(0), inputFileNames.get(1), true);
            default:
                List<SingleFastqReader> readers = new ArrayList<>();
                if (swapped) {
                    for (int i = 0; i < inputFileNames.size(); i++) {
                        if (i < inputFileNames.size() - 2)
                            readers.add(new SingleFastqReader(inputFileNames.get(i), true));
                        else if (i == inputFileNames.size() - 2)
                            readers.add(new SingleFastqReader(inputFileNames.get(i + 1), true));
                        else
                            readers.add(new SingleFastqReader(inputFileNames.get(i - 1), true));
                    }
                } else
                    for (String fileName : inputFileNames)
                        readers.add(new SingleFastqReader(fileName, true));
                return new MultiReader(readers.toArray(new SingleFastqReader[readers.size()]));
        }
    }

    private SequenceWriter createWriter() throws IOException {
        switch (outputFileNames.size()) {
            case 0:
                return new SingleFastqWriter(System.out);
            case 1:
                return new SingleFastqWriter(outputFileNames.get(0));
            case 2:
                return new PairedFastqWriter(outputFileNames.get(0), outputFileNames.get(1));
            default:
                throw new IllegalStateException("MultiWriter not yet implemented!");
        }
    }

    private class ReadParserProcessor implements Processor<SequenceRead, ParsedRead> {
        @Override
        public ParsedRead process(SequenceRead input) {
            return new ParsedRead(input, null, new ArrayList<>());
        }
    }
}
