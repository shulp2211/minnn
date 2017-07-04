// Generated from /home/user/ml/mist/src/main/java/com/milaboratory/mist/cli/CommandLineParser.g4 by ANTLR 4.7
package com.milaboratory.mist.cli;
import org.antlr.v4.runtime.tree.ParseTreeListener;

/**
 * This interface defines a complete listener for a parse tree produced by
 * {@link CommandLineParserParser}.
 */
public interface CommandLineParserListener extends ParseTreeListener {
	/**
	 * Enter a parse tree produced by {@link CommandLineParserParser#commandLine}.
	 * @param ctx the parse tree
	 */
	void enterCommandLine(CommandLineParserParser.CommandLineContext ctx);
	/**
	 * Exit a parse tree produced by {@link CommandLineParserParser#commandLine}.
	 * @param ctx the parse tree
	 */
	void exitCommandLine(CommandLineParserParser.CommandLineContext ctx);
	/**
	 * Enter a parse tree produced by {@link CommandLineParserParser#parseOption}.
	 * @param ctx the parse tree
	 */
	void enterParseOption(CommandLineParserParser.ParseOptionContext ctx);
	/**
	 * Exit a parse tree produced by {@link CommandLineParserParser#parseOption}.
	 * @param ctx the parse tree
	 */
	void exitParseOption(CommandLineParserParser.ParseOptionContext ctx);
	/**
	 * Enter a parse tree produced by {@link CommandLineParserParser#filterOutput}.
	 * @param ctx the parse tree
	 */
	void enterFilterOutput(CommandLineParserParser.FilterOutputContext ctx);
	/**
	 * Exit a parse tree produced by {@link CommandLineParserParser#filterOutput}.
	 * @param ctx the parse tree
	 */
	void exitFilterOutput(CommandLineParserParser.FilterOutputContext ctx);
}