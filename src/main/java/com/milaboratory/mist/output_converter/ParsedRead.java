package com.milaboratory.mist.output_converter;

import com.milaboratory.core.io.sequence.MultiRead;

import java.util.List;

public interface ParsedRead {
    MultiRead read();
    List<MatchedGroup> groups();
}
