/*
 * Copyright (c) 2016-2018, MiLaboratory LLC
 * All Rights Reserved
 *
 * Permission to use, copy, modify and distribute any part of this program for
 * educational, research and non-profit purposes, by non-profit institutions
 * only, without fee, and without a written agreement is hereby granted,
 * provided that the above copyright notice, this paragraph and the following
 * three paragraphs appear in all copies.
 *
 * Those desiring to incorporate this work into commercial products or use for
 * commercial purposes should contact MiLaboratory LLC, which owns exclusive
 * rights for distribution of this program for commercial purposes, using the
 * following email address: licensing@milaboratory.com.
 *
 * IN NO EVENT SHALL THE INVENTORS BE LIABLE TO ANY PARTY FOR DIRECT, INDIRECT,
 * SPECIAL, INCIDENTAL, OR CONSEQUENTIAL DAMAGES, INCLUDING LOST PROFITS,
 * ARISING OUT OF THE USE OF THIS SOFTWARE, EVEN IF THE INVENTORS HAS BEEN
 * ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 *
 * THE SOFTWARE PROVIDED HEREIN IS ON AN "AS IS" BASIS, AND THE INVENTORS HAS
 * NO OBLIGATION TO PROVIDE MAINTENANCE, SUPPORT, UPDATES, ENHANCEMENTS, OR
 * MODIFICATIONS. THE INVENTORS MAKES NO REPRESENTATIONS AND EXTENDS NO
 * WARRANTIES OF ANY KIND, EITHER IMPLIED OR EXPRESS, INCLUDING, BUT NOT
 * LIMITED TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY OR FITNESS FOR A
 * PARTICULAR PURPOSE, OR THAT THE USE OF THE SOFTWARE WILL NOT INFRINGE ANY
 * PATENT, TRADEMARK OR OTHER RIGHTS.
 */

// Generated from DemultiplexGrammar.g4 by ANTLR 4.7
package com.milaboratory.mist.cli;
import org.antlr.v4.runtime.tree.ParseTreeListener;

/**
 * This interface defines a complete listener for a parse tree produced by
 * {@link DemultiplexGrammarParser}.
 */
public interface DemultiplexGrammarListener extends ParseTreeListener {
	/**
	 * Enter a parse tree produced by {@link DemultiplexGrammarParser#demultiplexArguments}.
	 * @param ctx the parse tree
	 */
	void enterDemultiplexArguments(DemultiplexGrammarParser.DemultiplexArgumentsContext ctx);
	/**
	 * Exit a parse tree produced by {@link DemultiplexGrammarParser#demultiplexArguments}.
	 * @param ctx the parse tree
	 */
	void exitDemultiplexArguments(DemultiplexGrammarParser.DemultiplexArgumentsContext ctx);
	/**
	 * Enter a parse tree produced by {@link DemultiplexGrammarParser#demultiplexArgument}.
	 * @param ctx the parse tree
	 */
	void enterDemultiplexArgument(DemultiplexGrammarParser.DemultiplexArgumentContext ctx);
	/**
	 * Exit a parse tree produced by {@link DemultiplexGrammarParser#demultiplexArgument}.
	 * @param ctx the parse tree
	 */
	void exitDemultiplexArgument(DemultiplexGrammarParser.DemultiplexArgumentContext ctx);
	/**
	 * Enter a parse tree produced by {@link DemultiplexGrammarParser#bySample}.
	 * @param ctx the parse tree
	 */
	void enterBySample(DemultiplexGrammarParser.BySampleContext ctx);
	/**
	 * Exit a parse tree produced by {@link DemultiplexGrammarParser#bySample}.
	 * @param ctx the parse tree
	 */
	void exitBySample(DemultiplexGrammarParser.BySampleContext ctx);
	/**
	 * Enter a parse tree produced by {@link DemultiplexGrammarParser#byBarcode}.
	 * @param ctx the parse tree
	 */
	void enterByBarcode(DemultiplexGrammarParser.ByBarcodeContext ctx);
	/**
	 * Exit a parse tree produced by {@link DemultiplexGrammarParser#byBarcode}.
	 * @param ctx the parse tree
	 */
	void exitByBarcode(DemultiplexGrammarParser.ByBarcodeContext ctx);
	/**
	 * Enter a parse tree produced by {@link DemultiplexGrammarParser#inputFileName}.
	 * @param ctx the parse tree
	 */
	void enterInputFileName(DemultiplexGrammarParser.InputFileNameContext ctx);
	/**
	 * Exit a parse tree produced by {@link DemultiplexGrammarParser#inputFileName}.
	 * @param ctx the parse tree
	 */
	void exitInputFileName(DemultiplexGrammarParser.InputFileNameContext ctx);
	/**
	 * Enter a parse tree produced by {@link DemultiplexGrammarParser#fileName}.
	 * @param ctx the parse tree
	 */
	void enterFileName(DemultiplexGrammarParser.FileNameContext ctx);
	/**
	 * Exit a parse tree produced by {@link DemultiplexGrammarParser#fileName}.
	 * @param ctx the parse tree
	 */
	void exitFileName(DemultiplexGrammarParser.FileNameContext ctx);
	/**
	 * Enter a parse tree produced by {@link DemultiplexGrammarParser#barcodeName}.
	 * @param ctx the parse tree
	 */
	void enterBarcodeName(DemultiplexGrammarParser.BarcodeNameContext ctx);
	/**
	 * Exit a parse tree produced by {@link DemultiplexGrammarParser#barcodeName}.
	 * @param ctx the parse tree
	 */
	void exitBarcodeName(DemultiplexGrammarParser.BarcodeNameContext ctx);
}