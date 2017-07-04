package com.milaboratory.mist.cli;

import org.antlr.v4.runtime.CharStreams;
import org.antlr.v4.runtime.CodePointCharStream;
import org.antlr.v4.runtime.CommonTokenStream;
import org.antlr.v4.runtime.tree.ParseTree;

import java.util.ArrayList;

import static com.milaboratory.mist.cli.ActionFilter.executeActionFilter;
import static com.milaboratory.mist.cli.ActionParse.executeActionParse;

public final class CommandLineParser {
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
        int treeChildCount = tree.getChildCount();

        if (treeChildCount < 1)
            exitWithError("No action specified in command line!");
        String action = tree.getChild(0).getText();
        switch (action) {
            case "parse":
                ArrayList<ParseTree> actionParseOptions = new ArrayList<>();
                for (int i = 1; i < treeChildCount; i++)
                    if (tree.getChild(i).getChildCount() > 0)
                        actionParseOptions.add(tree.getChild(i));
                    else
                        exitWithError("Unexpected token: " + tree.getChild(i).getText());
                executeActionParse(actionParseOptions);
                break;
            case "filter":
                ArrayList<String> actionFilterInputFiles = new ArrayList<>();
                ArrayList<ParseTree> actionFilterOutputs = new ArrayList<>();
                boolean parsingInputFiles = true;
                for (int i = 1; i < treeChildCount; i++) {
                    if (parsingInputFiles)
                        if (tree.getChild(i).getChildCount() == 0)
                            actionFilterInputFiles.add(tree.getChild(i).getText());
                        else
                            parsingInputFiles = false;
                    if (!parsingInputFiles)
                        if (tree.getChild(i).getChildCount() > 0)
                            actionFilterOutputs.add(tree.getChild(i));
                        else
                            exitWithError("Unexpected token: " + tree.getChild(i).getText());
                }
                executeActionFilter(actionFilterInputFiles, actionFilterOutputs);
                break;
            default:
                exitWithError("Action not recognized: " + action);
        }
    }

    static void exitWithError(String message) {
        System.err.println(message);
        System.exit(1);
        throw new IllegalStateException();
    }
}
