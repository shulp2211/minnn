package com.milaboratory.mist.util;

import cc.redberry.pipe.OutputPort;
import com.milaboratory.mist.pattern.MatchIntermediate;

import java.util.ArrayList;

final class SpecificOutputPort implements OutputPort<MatchIntermediate> {
    private final OutputPort<MatchIntermediate> port;
    private final ArrayList<MatchIntermediate> cachedMatches = new ArrayList<>();
    private final int operandIndex;
    private final int from;
    private final int to;
    private final int portLimit;
    private boolean finished = false;

    SpecificOutputPort(OutputPort<MatchIntermediate> port, int operandIndex, int from, int to, int portLimit) {
        this.port = port;
        this.operandIndex = operandIndex;
        this.from = from;
        this.to = to;
        this.portLimit = portLimit;
    }

    @Override
    public MatchIntermediate take() {
        if (finished)
            return null;
        MatchIntermediate match = port.take();
        if (match == null)
            finished = true;
        else {
            cachedMatches.add(match);
            if (cachedMatches.size() == portLimit)
                finished = true;
        }
        return match;
    }

    ArrayList<MatchIntermediate> takeAll(boolean nullMatchesAllowed) {
        ArrayList<MatchIntermediate> allMatches = new ArrayList<>();
        MatchIntermediate currentMatch;
        int index = 0;
        do {
            currentMatch = get(index);
            if ((currentMatch != null) || (nullMatchesAllowed && (index == 0)))
                allMatches.add(currentMatch);
            index++;
        } while (currentMatch != null);

        return allMatches;
    }

    MatchIntermediate get(int index) {
        if (index < cachedMatches.size())
            return cachedMatches.get(index);
        else if (index == cachedMatches.size())
            return take();
        else
            throw new IndexOutOfBoundsException("index: " + index + ", cachedMatches size: " + cachedMatches.size());
    }

    boolean paramsEqualTo(int operandIndex, int from, int to) {
        return (operandIndex == this.operandIndex) && (from == this.from) && (to == this.to);
    }
}
