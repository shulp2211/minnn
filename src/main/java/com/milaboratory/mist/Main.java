package com.milaboratory.mist;

import com.milaboratory.mist.cli.CommandLineParser;

import java.io.IOException;

public class Main {
    public static void main(String[] args) {
        CommandLineParser commandLineParser = new CommandLineParser(args);
        commandLineParser.parseAndExecute();
    }
}
