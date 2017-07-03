package com.milaboratory.mist.cli;

import org.antlr.v4.runtime.CharStreams;
import org.antlr.v4.runtime.CodePointCharStream;
import org.antlr.v4.runtime.CommonTokenStream;
import org.antlr.v4.runtime.tree.ParseTree;

public class CommandLineParser {
    private final String commandLine;

    public CommandLineParser(String[] args) {
        this.commandLine = String.join(" ", args);
    }

    public void parseAndExecute() {
        CodePointCharStream charStream = CharStreams.fromString(commandLine);
        CommandLineParserLexer lexer = new CommandLineParserLexer(charStream);
        CommonTokenStream tokenStream = new CommonTokenStream(lexer);
        CommandLineParserParser parser = new CommandLineParserParser(tokenStream);
        ParseTree tree = parser.commandLine();
        System.out.println(tree.toStringTree(parser));
    }
}
