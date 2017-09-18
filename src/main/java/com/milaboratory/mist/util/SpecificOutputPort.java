package com.milaboratory.mist.util;

import cc.redberry.pipe.OutputPort;
import com.milaboratory.mist.pattern.Match;

import java.util.ArrayList;

final class SpecificOutputPort implements OutputPort<Match> {
    private final OutputPort<Match> port;
    private final ArrayList<Match> cachedMatches = new ArrayList<>();
    private final int operandIndex;
    private final int from;
    private final int portLimit;
    private boolean finished = false;

    SpecificOutputPort(OutputPort<Match> port, int operandIndex, int from, int portLimit) {
        this.port = port;
        this.operandIndex = operandIndex;
        this.from = from;
        this.portLimit = portLimit;
    }

    @Override
    public Match take() {
        if (finished)
            return null;
        Match match = port.take();
        if (match == null)
            finished = true;
        else {
            cachedMatches.add(match);
            if (cachedMatches.size() == portLimit)
                finished = true;
        }
        return match;
    }

    ArrayList<Match> takeAll(boolean nullMatchesAllowed) {
        ArrayList<Match> allMatches = new ArrayList<>();
        Match currentMatch;
        int index = 0;
        do {
            currentMatch = get(index);
            if ((currentMatch != null) || (nullMatchesAllowed && (index == 0)))
                allMatches.add(currentMatch);
            index++;
        } while (currentMatch != null);

        return allMatches;
    }

    Match get(int index) {
        if (index < cachedMatches.size())
            return cachedMatches.get(index);
        else if (index == cachedMatches.size())
            return take();
        else
            throw new IndexOutOfBoundsException("index: " + index + ", cachedMatches size: " + cachedMatches.size());
    }

    boolean paramsEqualTo(int operandIndex, int from) {
        return (operandIndex == this.operandIndex) && (from == this.from);
    }

    boolean isFinished() {
        return finished;
    }
}
