package com.milaboratory.mist.cli;

import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;

import static org.junit.Assert.*;

public class CommandLineParserTest {
    @Rule
    public final ExpectedException exception = ExpectedException.none();

    @Test
    public void simpleTest() throws Exception {
        String[] args = {"parse", "--pattern", "\"^N{0:5} ATTAGACA (ID:N{4}) W (UMI:N{7}) / ^GACATAT (R2:N{:})\"",
                "input_R1.fastq.gz", "input_R2.fastq.gz", "output_R1.fastq.gz", "output_R2.fastq.gz"};
        CommandLineParser commandLineParser = new CommandLineParser(args);
        commandLineParser.parseAndExecute();
    }
}
