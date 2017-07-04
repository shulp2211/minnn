package com.milaboratory.mist.cli;

import org.junit.Test;

import static org.junit.Assert.*;

public class ActionParseTest {
    @Test
    public void simpleTest() throws Exception {
        String[] args1 = {"parse", "--pattern", "\"^N{0:5} ATTAGACA (ID:N{4}) W (UMI:N{7}) / ^GACATAT (R2:N{:})\"",
                "--input", "input_R1.fastq.gz", "input_R2.fastq.gz", "--output", "output_R1.fastq.gz", "output_R2.fastq.gz"};
        CommandLineParser commandLineParser1 = new CommandLineParser(args1);
        commandLineParser1.parseAndExecute();

        String[] args2 = {"parse", "--scoring", "0", "--oriented", "--pattern", "ATTAGACA", "--input", "input",
                "--output", "output"};
        CommandLineParser commandLineParser2 = new CommandLineParser(args2);
        commandLineParser2.parseAndExecute();
    }
}
