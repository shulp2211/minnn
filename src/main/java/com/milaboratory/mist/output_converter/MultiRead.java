package com.milaboratory.mist.output_converter;

import java.util.List;

public class MultiRead {
    private final List<SingleRead> reads;

    public MultiRead(List<SingleRead> reads) {
        this.reads = reads;
    }

    public List<SingleRead> getReads() {
        return reads;
    }
}
