package com.milaboratory.mist.io;

import com.milaboratory.core.io.sequence.SequenceRead;
import com.milaboratory.core.io.sequence.SequenceReaderCloseable;

import java.io.IOException;
import java.io.InputStream;

import static com.milaboratory.mist.io.ReadsNumber.*;

final class MifReader implements SequenceReaderCloseable<SequenceRead> {
    MifReader(InputStream stream) throws IOException {

    }

    MifReader(String file) throws IOException {
        this(file, false);
    }

    MifReader(String file, boolean swappedReads) throws IOException {

    }

    @Override
    public void close() {

    }

    @Override
    public long getNumberOfReads() {
        return 0;
    }

    @Override
    public SequenceRead take() {
        return null;
    }

    static ReadsNumber detectReadsNumber() {
        return SINGLE;
    }
}
