package com.milaboratory.mist.io;

import com.milaboratory.mist.output_converter.ParsedRead;
import com.milaboratory.primitivio.PrimitivI;
import com.milaboratory.primitivio.PrimitivO;
import com.milaboratory.primitivio.Serializer;

public final class IO {
    public static class ParsedReadSerializer implements Serializer<ParsedRead> {
        @Override
        public void write(PrimitivO output, ParsedRead object) {
            ParsedRead.write(output, object);
        }

        @Override
        public ParsedRead read(PrimitivI input) {
            return ParsedRead.read(input);
        }

        @Override
        public boolean isReference() {
            return true;
        }

        @Override
        public boolean handlesReference() {
            return false;
        }
    }
}
