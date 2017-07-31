package com.milaboratory.mist.util;

import cc.redberry.pipe.OutputPort;
import com.milaboratory.mist.pattern.Match;

public class ApproximateSorterOperandPort {
    final OutputPort<Match> outputPort;
    final int unfairSorterPortLimit;

    /**
     * Output port for operand matches; we assume that they are already sorted, maybe approximately.
     *
     * @param outputPort operand output port
     * @param unfairSorterPortLimit limit for number of values that will be taken from this port, for unfair sorter
     */
    public ApproximateSorterOperandPort(OutputPort<Match> outputPort, int unfairSorterPortLimit) {
        this.outputPort = outputPort;
        this.unfairSorterPortLimit = unfairSorterPortLimit;
    }
}
