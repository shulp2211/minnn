package com.milaboratory.mist.input;

import com.milaboratory.core.io.sequence.PairedRead;
import com.milaboratory.core.io.sequence.SingleRead;
import com.milaboratory.core.io.sequence.fastq.PairedFastqReader;
import com.milaboratory.core.io.sequence.fastq.SingleFastqReader;
import com.milaboratory.core.sequence.MultiNSequenceWithQuality;
import com.milaboratory.core.sequence.NSequenceWithQuality;
import com.milaboratory.mist.pattern.MatchingResult;
import com.milaboratory.mist.pattern.Pattern;
import com.milaboratory.mist.pattern.SinglePattern;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import static com.milaboratory.mist.util.SystemUtils.exitWithError;

public class TargetReader {
    private final Pattern pattern;
    private final boolean orientedReads;

    public TargetReader(Pattern pattern, boolean orientedReads) {
        this.pattern = pattern;
        this.orientedReads = orientedReads;
    }

    /**
     * Read target from file or stdin, apply pattern to it and return MatchingResult.
     * MatchingResult is lazy object, so actual matching is not performed on this stage.
     *
     * @param fileNames list of file names: single file = one read or multi-read file;
     *                  multiple files = 1 file for each read; empty list = use stdin
     * @return MatchingResult object
     */
    public MatchingResult getMatchingResult(List<String> fileNames) {
        if (fileNames.size() <= 1) {
            SingleFastqReader reader = null;
            try {
                reader = (fileNames.size() == 0) ? new SingleFastqReader(System.in)
                        : new SingleFastqReader(fileNames.get(0));
            } catch (IOException e) {
                exitWithError(e.getMessage());
            }
            if (reader != null) {
                SingleRead read = reader.take();
                if (read != null) {
                    NSequenceWithQuality target = read.getData();
                    if (SinglePattern.class.isAssignableFrom(pattern.getClass())) {
                        SinglePattern singlePattern = (SinglePattern)pattern;
                        return singlePattern.match(target);
                    } else
                        exitWithError("Trying to use pattern for multiple reads with single read!");
                }
            }
        } else if (fileNames.size() == 2) {
            PairedFastqReader reader = null;
            try {
                reader = new PairedFastqReader(fileNames.get(0), fileNames.get(1));
            } catch (IOException e) {
                exitWithError(e.getMessage());
            }
            if (reader != null) {
                PairedRead read = reader.take();
                if (read != null) {
                    MultiNSequenceWithQuality target = new MultiNSequenceWithQuality() {
                        @Override
                        public int numberOfSequences() {
                            return 2;
                        }

                        @Override
                        public NSequenceWithQuality get(int id) {
                            return ((id == 0) ? read.getR1() : read.getR2()).getData();
                        }
                    };
                    return pattern.match(target);
                }
            }
        } else {
            ArrayList<SingleFastqReader> readers = new ArrayList<>();
            ArrayList<NSequenceWithQuality> sequences = new ArrayList<>();
            try {
                for (String fileName : fileNames)
                    readers.add(new SingleFastqReader(fileName));
            } catch (IOException e) {
                exitWithError(e.getMessage());
            }
            for (int i = 0; i < readers.size(); i++) {
                SingleRead read = readers.get(i).take();
                if (read != null)
                    sequences.add(read.getData());
                else
                    exitWithError("Target " + i + " was not read!");
            }
            MultiNSequenceWithQuality target = new MultiNSequenceWithQuality() {
                @Override
                public int numberOfSequences() {
                    return sequences.size();
                }

                @Override
                public NSequenceWithQuality get(int id) {
                    return sequences.get(id);
                }
            };
            return pattern.match(target);
        }

        exitWithError("Target was not read!");
        return null;
    }
}
