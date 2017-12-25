package com.milaboratory.mist.io;

import cc.redberry.pipe.OutputPortCloseable;
import com.milaboratory.mist.outputconverter.ParsedRead;
import com.milaboratory.mist.pattern.MatchedGroupEdge;
import com.milaboratory.primitivio.PrimitivI;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;

final class MifReader implements OutputPortCloseable<ParsedRead> {
    private final PrimitivI input;

    MifReader(InputStream stream) {
        input = new PrimitivI(stream);
        initKnownReferences();
    }

    MifReader(String file) throws IOException {
        input = new PrimitivI(new FileInputStream(file));
        initKnownReferences();
    }

    private void initKnownReferences() {
        int matchedGroupEdgesNum = input.readInt();
        for (int i = 0; i < matchedGroupEdgesNum; i++)
            input.putKnownReference(input.readObject(MatchedGroupEdge.class));
    }

    @Override
    public void close() {
        input.close();
    }

    @Override
    public ParsedRead take() {
        return input.readObject(ParsedRead.class);
    }
}
