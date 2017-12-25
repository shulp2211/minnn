package com.milaboratory.mist.io;

import com.milaboratory.mist.outputconverter.ParsedRead;
import com.milaboratory.mist.pattern.MatchedGroupEdge;
import com.milaboratory.primitivio.PrimitivO;

import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.util.ArrayList;

final class MifWriter {
    private final PrimitivO output;

    MifWriter(OutputStream outputStream, ArrayList<MatchedGroupEdge> matchedGroupEdges) {
        output = new PrimitivO(outputStream);
        initKnownReferences(matchedGroupEdges);
    }

    MifWriter(String file, ArrayList<MatchedGroupEdge> matchedGroupEdges) throws IOException {
        output = new PrimitivO(new FileOutputStream(file));
        initKnownReferences(matchedGroupEdges);
    }

    private void initKnownReferences(ArrayList<MatchedGroupEdge> matchedGroupEdges) {
        output.writeInt(matchedGroupEdges.size());
        for (MatchedGroupEdge matchedGroupEdge : matchedGroupEdges) {
            output.writeObject(matchedGroupEdge);
            output.putKnownReference(matchedGroupEdge);
        }
    }

    void write(ParsedRead parsedRead) {
        output.writeObject(parsedRead);
    }

    void close() {
        output.close();
    }
}
