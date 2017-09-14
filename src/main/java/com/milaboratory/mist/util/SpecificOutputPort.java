package com.milaboratory.mist.util;

import cc.redberry.pipe.OutputPort;
import com.milaboratory.mist.pattern.Match;

import java.util.ArrayList;

import static com.milaboratory.mist.util.UnfairSorterConfiguration.specificPortLimit;

final class SpecificOutputPort implements OutputPort<Match> {
    private final OutputPort<Match> port;
    private final ArrayList<Match> cachedMatches = new ArrayList<>();
    private final int operandIndex;
    private final int from;
    private boolean finished = false;

    SpecificOutputPort(OutputPort<Match> port, int operandIndex, int from) {
        this.port = port;
        this.operandIndex = operandIndex;
        this.from = from;
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
            if ((from != -1) && (cachedMatches.size() == specificPortLimit))
                finished = true;
        }
        return match;
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
