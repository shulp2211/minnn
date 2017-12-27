package com.milaboratory.mist.io;

import cc.redberry.pipe.OutputPortCloseable;
import com.milaboratory.mist.outputconverter.ParsedRead;
import com.milaboratory.mist.pattern.GroupEdge;
import com.milaboratory.primitivio.PrimitivI;
import com.milaboratory.util.CanReportProgress;

import java.io.EOFException;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;

final class MifReader implements OutputPortCloseable<ParsedRead>, CanReportProgress {
    private final PrimitivI input;
    private boolean finished = false;

    MifReader(InputStream stream) {
        input = new PrimitivI(stream);
        initKnownReferences();
    }

    MifReader(String file) throws IOException {
        input = new PrimitivI(new FileInputStream(file));
        initKnownReferences();
    }

    private void initKnownReferences() {
        int groupEdgesNum = input.readInt();
        for (int i = 0; i < groupEdgesNum; i++)
            input.putKnownReference(input.readObject(GroupEdge.class));
    }

    @Override
    public void close() {
        input.close();
    }

    @Override
    public double getProgress() {
        return finished ? 1 : 0;
    }

    @Override
    public boolean isFinished() {
        return finished;
    }

    @Override
    public ParsedRead take() {
        ParsedRead parsedRead;
        try {
            parsedRead = input.readObject(ParsedRead.class);
        } catch (RuntimeException e) {
            if (e.getCause().getClass().equals(EOFException.class))
                parsedRead = null;
            else
                throw(e);
        }
        if (parsedRead == null)
            finished = true;
        return parsedRead;
    }
}
