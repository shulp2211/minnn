package com.milaboratory.mist.input;

import com.milaboratory.mist.pattern.MatchingResult;
import com.milaboratory.mist.pattern.Pattern;

import java.util.ArrayList;

public class TargetReader {
    private final Pattern pattern;

    public TargetReader(Pattern pattern) {
        this.pattern = pattern;
    }

    /**
     * Read target from file or stdin, apply pattern to it and return MatchingResult.
     * MatchingResult is lazy object, so actual matching is not performed on this stage.
     *
     * @param fileNames list of file names: single file = one read or multi-read file;
     *                  multiple files = 1 file for each read; empty list = use stdin
     * @return MatchingResult object
     */
    public MatchingResult getMatchingResult(ArrayList<String> fileNames) {
        return null;
    }
}
