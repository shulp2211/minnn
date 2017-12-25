package com.milaboratory.mist.outputconverter;

import com.milaboratory.core.io.sequence.SequenceRead;
import com.milaboratory.mist.io.IO;
import com.milaboratory.mist.pattern.Match;
import com.milaboratory.primitivio.PrimitivI;
import com.milaboratory.primitivio.PrimitivO;
import com.milaboratory.primitivio.annotations.Serializable;

import java.util.ArrayList;

@Serializable(by = IO.ParsedReadSerializer.class)
public final class ParsedRead {
    private final SequenceRead originalRead;
    private final boolean reverseMatch;
    private final Match bestMatch;

    public ParsedRead(SequenceRead originalRead, boolean reverseMatch, Match bestMatch) {
        this.originalRead = originalRead;
        this.reverseMatch = reverseMatch;
        this.bestMatch = bestMatch;
    }

    public SequenceRead getOriginalRead() {
        return originalRead;
    }

    public boolean isReverseMatch() {
        return reverseMatch;
    }

    public Match getBestMatch() {
        return bestMatch;
    }

    public ArrayList<MatchedGroup> getGroups() {
        if (bestMatch == null)
            return new ArrayList<>();
        else
            return bestMatch.getGroups();
    }

    public long getBestMatchScore() {
        return (bestMatch == null) ? Long.MIN_VALUE : bestMatch.getScore();
    }

    public static ParsedRead read(PrimitivI input) {
        SequenceRead originalRead = input.readObject(SequenceRead.class);
        boolean reverseMatch = input.readBoolean();
        Match bestMatch = input.readObject(Match.class);
        return new ParsedRead(originalRead, reverseMatch, bestMatch);
    }

    public static void write(PrimitivO output, ParsedRead object) {
        output.writeObject(object.getOriginalRead());
        output.writeObject(object.isReverseMatch());
        output.writeObject(object.getBestMatch());
    }
}
