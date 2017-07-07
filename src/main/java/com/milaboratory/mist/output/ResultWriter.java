package com.milaboratory.mist.output;

import com.milaboratory.core.io.sequence.MultiRead;
import com.milaboratory.core.io.sequence.fastq.SingleFastqWriter;
import com.milaboratory.mist.output_converter.ParsedRead;
import com.milaboratory.mist.output_converter.ParsedReadsPort;

import java.io.IOException;
import java.util.List;

import static com.milaboratory.mist.util.SystemUtils.exitWithError;

public class ResultWriter {
    /**
     * Write results to file or stdout.
     *
     * @param fileNames list of file names: single file = one read or multi-read file;
     *                  multiple files = 1 file for each read; empty list = use stdout
     * @param port port to take parsed reads
     */
    public static void writeResultsFromPort(List<String> fileNames, ParsedReadsPort port) {
        ParsedRead bestParsedRead = port.take();
        if (bestParsedRead != null)
            try {
                writeResult(fileNames, bestParsedRead);
            } catch (IOException e) {
                System.err.println("I/O exception!");
                exitWithError(e.getMessage());
            }
    }

    private static void writeResult(List<String> fileNames, ParsedRead parsedRead) throws IOException {
        MultiRead multiRead = parsedRead.read();
        int numberOfReads = multiRead.numberOfReads();
        if (fileNames.size() != numberOfReads)
            exitWithError("Mismatched number of reads (" + numberOfReads + ") and number of output files ("
                    + fileNames.size() + ")!");
        for (int i = 0; i < numberOfReads; i++) {
            SingleFastqWriter writer = new SingleFastqWriter(fileNames.get(i));
            writer.write(multiRead.getRead(i));
            writer.close();
        }
    }
}
