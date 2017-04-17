package com.milaboratory.mist.util;

import cc.redberry.pipe.OutputPort;
import com.milaboratory.mist.pattern.Match;

public class SorterByScore extends ApproximateSorter {
    public SorterByScore(boolean multipleReads, boolean combineScoresBySum, OutputPort<Match>[] inputPorts) {
        super(multipleReads, combineScoresBySum, inputPorts);
    }

    @Override
    public OutputPort<Match> getOutputPort() {
        return null;
    }
}
