package com.milaboratory.mist.output;

import com.milaboratory.mist.output_converter.ParsedRead;
import com.milaboratory.mist.output_converter.ParsedReadsPort;

import java.util.ArrayList;

public class ResultWriter {
    /**
     * Write results to file or stdout.
     *
     * @param fileNames list of file names: single file = one read or multi-read file;
     *                  multiple files = 1 file for each read; empty list = use stdout
     * @param port port to take parsed reads
     */
    public static void writeResultsFromPort(ArrayList<String> fileNames, ParsedReadsPort port) {
        ParsedRead bestParsedRead = port.take();
        if (bestParsedRead != null)
            writeResult(fileNames, bestParsedRead);
    }

    private static void writeResult(ArrayList<String> fileNames, ParsedRead parsedRead) {
    }
}
