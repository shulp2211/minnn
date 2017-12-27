package com.milaboratory.mist.io;

import com.milaboratory.mist.outputconverter.ParsedRead;
import com.milaboratory.mist.pattern.*;
import com.milaboratory.primitivio.*;

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

    public static class MatchSerializer implements Serializer<Match> {
        @Override
        public void write(PrimitivO output, Match object) {
            Match.write(output, object);
        }

        @Override
        public Match read(PrimitivI input) {
            return Match.read(input);
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

    public static class MatchedGroupEdgeSerializer implements Serializer<MatchedGroupEdge> {
        @Override
        public void write(PrimitivO output, MatchedGroupEdge object) {
            MatchedGroupEdge.write(output, object);
        }

        @Override
        public MatchedGroupEdge read(PrimitivI input) {
            return MatchedGroupEdge.read(input);
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

    public static class GroupEdgeSerializer implements Serializer<GroupEdge> {
        @Override
        public void write(PrimitivO output, GroupEdge object) {
            GroupEdge.write(output, object);
        }

        @Override
        public GroupEdge read(PrimitivI input) {
            return GroupEdge.read(input);
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
