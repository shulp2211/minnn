package com.milaboratory.mist.output_converter;

import java.util.List;

public interface ParsedRead {
    MultiRead read();
    List<MatchedGroup> groups();
}
