package com.milaboratory.mist.output_converter;

import com.milaboratory.core.io.sequence.SequenceRead;
import com.milaboratory.mist.io.IO;
import com.milaboratory.mist.pattern.Match;
import com.milaboratory.primitivio.annotations.Serializable;

import java.io.DataInput;
import java.io.DataOutput;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import static com.milaboratory.mist.output_converter.GroupUtils.getGroupsFromMatch;

@Serializable(by = IO.ParsedReadSerializer.class)
public final class ParsedRead {
    private final SequenceRead originalRead;
    private final SequenceRead parsedRead;
    private final boolean reverseMatch;
    private final Match bestMatch;
    private final List<MatchedGroup> groups;
    private final long bestMatchScore;

    public ParsedRead(SequenceRead originalRead) {
        this(originalRead, null, false, null);
    }

    public ParsedRead(SequenceRead originalRead, SequenceRead parsedRead, boolean reverseMatch, Match bestMatch) {
        this.originalRead = originalRead;
        this.parsedRead = parsedRead;
        this.reverseMatch = reverseMatch;
        this.bestMatch = bestMatch;
        this.groups = (bestMatch == null) ? new ArrayList<>() : getGroupsFromMatch(bestMatch);
        this.bestMatchScore = (bestMatch == null) ? Long.MIN_VALUE : bestMatch.getScore();
    }

    public SequenceRead getOriginalRead() {
        return originalRead;
    }

    public SequenceRead getParsedRead() {
        return parsedRead;
    }

    public boolean isReverseMatch() {
        return reverseMatch;
    }

    public Match getBestMatch() {
        return bestMatch;
    }

    public List<MatchedGroup> getGroups() {
        return groups;
    }

    public long getBestMatchScore() {
        return bestMatchScore;
    }

    public static ParsedRead read(DataInput input) {
        try {
            int dummy = input.readInt();
            return new ParsedRead(null);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public static void write(DataOutput output, ParsedRead object) {
        try {
            output.writeInt(0);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
}
