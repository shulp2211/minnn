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

// Generated from FilterGrammar.g4 by ANTLR 4.7
package com.milaboratory.mist.cli;
import org.antlr.v4.runtime.tree.ParseTreeListener;

/**
 * This interface defines a complete listener for a parse tree produced by
 * {@link FilterGrammarParser}.
 */
public interface FilterGrammarListener extends ParseTreeListener {
	/**
	 * Enter a parse tree produced by {@link FilterGrammarParser#filter}.
	 * @param ctx the parse tree
	 */
	void enterFilter(FilterGrammarParser.FilterContext ctx);
	/**
	 * Exit a parse tree produced by {@link FilterGrammarParser#filter}.
	 * @param ctx the parse tree
	 */
	void exitFilter(FilterGrammarParser.FilterContext ctx);
	/**
	 * Enter a parse tree produced by {@link FilterGrammarParser#filterInParentheses}.
	 * @param ctx the parse tree
	 */
	void enterFilterInParentheses(FilterGrammarParser.FilterInParenthesesContext ctx);
	/**
	 * Exit a parse tree produced by {@link FilterGrammarParser#filterInParentheses}.
	 * @param ctx the parse tree
	 */
	void exitFilterInParentheses(FilterGrammarParser.FilterInParenthesesContext ctx);
	/**
	 * Enter a parse tree produced by {@link FilterGrammarParser#anySingleFilter}.
	 * @param ctx the parse tree
	 */
	void enterAnySingleFilter(FilterGrammarParser.AnySingleFilterContext ctx);
	/**
	 * Exit a parse tree produced by {@link FilterGrammarParser#anySingleFilter}.
	 * @param ctx the parse tree
	 */
	void exitAnySingleFilter(FilterGrammarParser.AnySingleFilterContext ctx);
	/**
	 * Enter a parse tree produced by {@link FilterGrammarParser#or}.
	 * @param ctx the parse tree
	 */
	void enterOr(FilterGrammarParser.OrContext ctx);
	/**
	 * Exit a parse tree produced by {@link FilterGrammarParser#or}.
	 * @param ctx the parse tree
	 */
	void exitOr(FilterGrammarParser.OrContext ctx);
	/**
	 * Enter a parse tree produced by {@link FilterGrammarParser#orOperand}.
	 * @param ctx the parse tree
	 */
	void enterOrOperand(FilterGrammarParser.OrOperandContext ctx);
	/**
	 * Exit a parse tree produced by {@link FilterGrammarParser#orOperand}.
	 * @param ctx the parse tree
	 */
	void exitOrOperand(FilterGrammarParser.OrOperandContext ctx);
	/**
	 * Enter a parse tree produced by {@link FilterGrammarParser#and}.
	 * @param ctx the parse tree
	 */
	void enterAnd(FilterGrammarParser.AndContext ctx);
	/**
	 * Exit a parse tree produced by {@link FilterGrammarParser#and}.
	 * @param ctx the parse tree
	 */
	void exitAnd(FilterGrammarParser.AndContext ctx);
	/**
	 * Enter a parse tree produced by {@link FilterGrammarParser#andOperand}.
	 * @param ctx the parse tree
	 */
	void enterAndOperand(FilterGrammarParser.AndOperandContext ctx);
	/**
	 * Exit a parse tree produced by {@link FilterGrammarParser#andOperand}.
	 * @param ctx the parse tree
	 */
	void exitAndOperand(FilterGrammarParser.AndOperandContext ctx);
	/**
	 * Enter a parse tree produced by {@link FilterGrammarParser#pattern}.
	 * @param ctx the parse tree
	 */
	void enterPattern(FilterGrammarParser.PatternContext ctx);
	/**
	 * Exit a parse tree produced by {@link FilterGrammarParser#pattern}.
	 * @param ctx the parse tree
	 */
	void exitPattern(FilterGrammarParser.PatternContext ctx);
	/**
	 * Enter a parse tree produced by {@link FilterGrammarParser#minConsensusReads}.
	 * @param ctx the parse tree
	 */
	void enterMinConsensusReads(FilterGrammarParser.MinConsensusReadsContext ctx);
	/**
	 * Exit a parse tree produced by {@link FilterGrammarParser#minConsensusReads}.
	 * @param ctx the parse tree
	 */
	void exitMinConsensusReads(FilterGrammarParser.MinConsensusReadsContext ctx);
	/**
	 * Enter a parse tree produced by {@link FilterGrammarParser#len}.
	 * @param ctx the parse tree
	 */
	void enterLen(FilterGrammarParser.LenContext ctx);
	/**
	 * Exit a parse tree produced by {@link FilterGrammarParser#len}.
	 * @param ctx the parse tree
	 */
	void exitLen(FilterGrammarParser.LenContext ctx);
	/**
	 * Enter a parse tree produced by {@link FilterGrammarParser#patternString}.
	 * @param ctx the parse tree
	 */
	void enterPatternString(FilterGrammarParser.PatternStringContext ctx);
	/**
	 * Exit a parse tree produced by {@link FilterGrammarParser#patternString}.
	 * @param ctx the parse tree
	 */
	void exitPatternString(FilterGrammarParser.PatternStringContext ctx);
	/**
	 * Enter a parse tree produced by {@link FilterGrammarParser#groupName}.
	 * @param ctx the parse tree
	 */
	void enterGroupName(FilterGrammarParser.GroupNameContext ctx);
	/**
	 * Exit a parse tree produced by {@link FilterGrammarParser#groupName}.
	 * @param ctx the parse tree
	 */
	void exitGroupName(FilterGrammarParser.GroupNameContext ctx);
	/**
	 * Enter a parse tree produced by {@link FilterGrammarParser#minConsensusReadsNum}.
	 * @param ctx the parse tree
	 */
	void enterMinConsensusReadsNum(FilterGrammarParser.MinConsensusReadsNumContext ctx);
	/**
	 * Exit a parse tree produced by {@link FilterGrammarParser#minConsensusReadsNum}.
	 * @param ctx the parse tree
	 */
	void exitMinConsensusReadsNum(FilterGrammarParser.MinConsensusReadsNumContext ctx);
	/**
	 * Enter a parse tree produced by {@link FilterGrammarParser#groupLength}.
	 * @param ctx the parse tree
	 */
	void enterGroupLength(FilterGrammarParser.GroupLengthContext ctx);
	/**
	 * Exit a parse tree produced by {@link FilterGrammarParser#groupLength}.
	 * @param ctx the parse tree
	 */
	void exitGroupLength(FilterGrammarParser.GroupLengthContext ctx);
}