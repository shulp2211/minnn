package com.milaboratory.mist.io;

import java.util.HashMap;

import static com.milaboratory.mist.io.MistDataFormat.*;

public final class MistDataFormatNames {
    public static final HashMap<String, MistDataFormat> parameterNames = new HashMap<>();
    static {
        parameterNames.put("fastq", FASTQ);
        parameterNames.put("mif", MIF);
    }
}
