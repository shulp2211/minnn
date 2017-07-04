// Generated from /home/user/ml/mist/src/main/java/com/milaboratory/mist/cli/CommandLineParser.g4 by ANTLR 4.7
package com.milaboratory.mist.cli;
import org.antlr.v4.runtime.atn.*;
import org.antlr.v4.runtime.dfa.DFA;
import org.antlr.v4.runtime.*;
import org.antlr.v4.runtime.misc.*;
import org.antlr.v4.runtime.tree.*;
import java.util.List;
import java.util.Iterator;
import java.util.ArrayList;

@SuppressWarnings({"all", "warnings", "unchecked", "unused", "cast"})
public class CommandLineParserParser extends Parser {
	static { RuntimeMetaData.checkVersion("4.7", RuntimeMetaData.VERSION); }

	protected static final DFA[] _decisionToDFA;
	protected static final PredictionContextCache _sharedContextCache =
		new PredictionContextCache();
	public static final int
		T__0=1, T__1=2, OPTION=3, STRING=4, FILE_NAME=5, EXPRESSION=6, ID=7, WS=8;
	public static final int
		RULE_commandLine = 0, RULE_parseOption = 1, RULE_filterOutput = 2;
	public static final String[] ruleNames = {
		"commandLine", "parseOption", "filterOutput"
	};

	private static final String[] _LITERAL_NAMES = {
		null, "'parse'", "'filter'"
	};
	private static final String[] _SYMBOLIC_NAMES = {
		null, null, null, "OPTION", "STRING", "FILE_NAME", "EXPRESSION", "ID", 
		"WS"
	};
	public static final Vocabulary VOCABULARY = new VocabularyImpl(_LITERAL_NAMES, _SYMBOLIC_NAMES);

	/**
	 * @deprecated Use {@link #VOCABULARY} instead.
	 */
	@Deprecated
	public static final String[] tokenNames;
	static {
		tokenNames = new String[_SYMBOLIC_NAMES.length];
		for (int i = 0; i < tokenNames.length; i++) {
			tokenNames[i] = VOCABULARY.getLiteralName(i);
			if (tokenNames[i] == null) {
				tokenNames[i] = VOCABULARY.getSymbolicName(i);
			}

			if (tokenNames[i] == null) {
				tokenNames[i] = "<INVALID>";
			}
		}
	}

	@Override
	@Deprecated
	public String[] getTokenNames() {
		return tokenNames;
	}

	@Override

	public Vocabulary getVocabulary() {
		return VOCABULARY;
	}

	@Override
	public String getGrammarFileName() { return "CommandLineParser.g4"; }

	@Override
	public String[] getRuleNames() { return ruleNames; }

	@Override
	public String getSerializedATN() { return _serializedATN; }

	@Override
	public ATN getATN() { return _ATN; }

	public CommandLineParserParser(TokenStream input) {
		super(input);
		_interp = new ParserATNSimulator(this,_ATN,_decisionToDFA,_sharedContextCache);
	}
	public static class CommandLineContext extends ParserRuleContext {
		public List<ParseOptionContext> parseOption() {
			return getRuleContexts(ParseOptionContext.class);
		}
		public ParseOptionContext parseOption(int i) {
			return getRuleContext(ParseOptionContext.class,i);
		}
		public List<TerminalNode> FILE_NAME() { return getTokens(CommandLineParserParser.FILE_NAME); }
		public TerminalNode FILE_NAME(int i) {
			return getToken(CommandLineParserParser.FILE_NAME, i);
		}
		public List<FilterOutputContext> filterOutput() {
			return getRuleContexts(FilterOutputContext.class);
		}
		public FilterOutputContext filterOutput(int i) {
			return getRuleContext(FilterOutputContext.class,i);
		}
		public CommandLineContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_commandLine; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof CommandLineParserListener ) ((CommandLineParserListener)listener).enterCommandLine(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof CommandLineParserListener ) ((CommandLineParserListener)listener).exitCommandLine(this);
		}
	}

	public final CommandLineContext commandLine() throws RecognitionException {
		CommandLineContext _localctx = new CommandLineContext(_ctx, getState());
		enterRule(_localctx, 0, RULE_commandLine);
		int _la;
		try {
			setState(23);
			_errHandler.sync(this);
			switch (_input.LA(1)) {
			case T__0:
				enterOuterAlt(_localctx, 1);
				{
				{
				setState(6);
				match(T__0);
				setState(8); 
				_errHandler.sync(this);
				_la = _input.LA(1);
				do {
					{
					{
					setState(7);
					parseOption();
					}
					}
					setState(10); 
					_errHandler.sync(this);
					_la = _input.LA(1);
				} while ( _la==OPTION );
				}
				}
				break;
			case T__1:
				enterOuterAlt(_localctx, 2);
				{
				{
				setState(12);
				match(T__1);
				setState(14); 
				_errHandler.sync(this);
				_la = _input.LA(1);
				do {
					{
					{
					setState(13);
					match(FILE_NAME);
					}
					}
					setState(16); 
					_errHandler.sync(this);
					_la = _input.LA(1);
				} while ( _la==FILE_NAME );
				setState(19); 
				_errHandler.sync(this);
				_la = _input.LA(1);
				do {
					{
					{
					setState(18);
					filterOutput();
					}
					}
					setState(21); 
					_errHandler.sync(this);
					_la = _input.LA(1);
				} while ( _la==OPTION );
				}
				}
				break;
			default:
				throw new NoViableAltException(this);
			}
		}
		catch (RecognitionException re) {
			_localctx.exception = re;
			_errHandler.reportError(this, re);
			_errHandler.recover(this, re);
		}
		finally {
			exitRule();
		}
		return _localctx;
	}

	public static class ParseOptionContext extends ParserRuleContext {
		public TerminalNode OPTION() { return getToken(CommandLineParserParser.OPTION, 0); }
		public List<TerminalNode> ID() { return getTokens(CommandLineParserParser.ID); }
		public TerminalNode ID(int i) {
			return getToken(CommandLineParserParser.ID, i);
		}
		public List<TerminalNode> STRING() { return getTokens(CommandLineParserParser.STRING); }
		public TerminalNode STRING(int i) {
			return getToken(CommandLineParserParser.STRING, i);
		}
		public List<TerminalNode> FILE_NAME() { return getTokens(CommandLineParserParser.FILE_NAME); }
		public TerminalNode FILE_NAME(int i) {
			return getToken(CommandLineParserParser.FILE_NAME, i);
		}
		public ParseOptionContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_parseOption; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof CommandLineParserListener ) ((CommandLineParserListener)listener).enterParseOption(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof CommandLineParserListener ) ((CommandLineParserListener)listener).exitParseOption(this);
		}
	}

	public final ParseOptionContext parseOption() throws RecognitionException {
		ParseOptionContext _localctx = new ParseOptionContext(_ctx, getState());
		enterRule(_localctx, 2, RULE_parseOption);
		int _la;
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(25);
			match(OPTION);
			setState(29);
			_errHandler.sync(this);
			_la = _input.LA(1);
			while ((((_la) & ~0x3f) == 0 && ((1L << _la) & ((1L << STRING) | (1L << FILE_NAME) | (1L << ID))) != 0)) {
				{
				{
				setState(26);
				_la = _input.LA(1);
				if ( !((((_la) & ~0x3f) == 0 && ((1L << _la) & ((1L << STRING) | (1L << FILE_NAME) | (1L << ID))) != 0)) ) {
				_errHandler.recoverInline(this);
				}
				else {
					if ( _input.LA(1)==Token.EOF ) matchedEOF = true;
					_errHandler.reportMatch(this);
					consume();
				}
				}
				}
				setState(31);
				_errHandler.sync(this);
				_la = _input.LA(1);
			}
			}
		}
		catch (RecognitionException re) {
			_localctx.exception = re;
			_errHandler.reportError(this, re);
			_errHandler.recover(this, re);
		}
		finally {
			exitRule();
		}
		return _localctx;
	}

	public static class FilterOutputContext extends ParserRuleContext {
		public TerminalNode OPTION() { return getToken(CommandLineParserParser.OPTION, 0); }
		public TerminalNode EXPRESSION() { return getToken(CommandLineParserParser.EXPRESSION, 0); }
		public TerminalNode STRING() { return getToken(CommandLineParserParser.STRING, 0); }
		public List<TerminalNode> FILE_NAME() { return getTokens(CommandLineParserParser.FILE_NAME); }
		public TerminalNode FILE_NAME(int i) {
			return getToken(CommandLineParserParser.FILE_NAME, i);
		}
		public FilterOutputContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_filterOutput; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof CommandLineParserListener ) ((CommandLineParserListener)listener).enterFilterOutput(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof CommandLineParserListener ) ((CommandLineParserListener)listener).exitFilterOutput(this);
		}
	}

	public final FilterOutputContext filterOutput() throws RecognitionException {
		FilterOutputContext _localctx = new FilterOutputContext(_ctx, getState());
		enterRule(_localctx, 4, RULE_filterOutput);
		int _la;
		try {
			int _alt;
			enterOuterAlt(_localctx, 1);
			{
			setState(32);
			match(OPTION);
			setState(33);
			_la = _input.LA(1);
			if ( !(_la==STRING || _la==EXPRESSION) ) {
			_errHandler.recoverInline(this);
			}
			else {
				if ( _input.LA(1)==Token.EOF ) matchedEOF = true;
				_errHandler.reportMatch(this);
				consume();
			}
			setState(35); 
			_errHandler.sync(this);
			_alt = 1+1;
			do {
				switch (_alt) {
				case 1+1:
					{
					{
					setState(34);
					match(FILE_NAME);
					}
					}
					break;
				default:
					throw new NoViableAltException(this);
				}
				setState(37); 
				_errHandler.sync(this);
				_alt = getInterpreter().adaptivePredict(_input,5,_ctx);
			} while ( _alt!=1 && _alt!=org.antlr.v4.runtime.atn.ATN.INVALID_ALT_NUMBER );
			}
		}
		catch (RecognitionException re) {
			_localctx.exception = re;
			_errHandler.reportError(this, re);
			_errHandler.recover(this, re);
		}
		finally {
			exitRule();
		}
		return _localctx;
	}

	public static final String _serializedATN =
		"\3\u608b\ua72a\u8133\ub9ed\u417c\u3be7\u7786\u5964\3\n*\4\2\t\2\4\3\t"+
		"\3\4\4\t\4\3\2\3\2\6\2\13\n\2\r\2\16\2\f\3\2\3\2\6\2\21\n\2\r\2\16\2\22"+
		"\3\2\6\2\26\n\2\r\2\16\2\27\5\2\32\n\2\3\3\3\3\7\3\36\n\3\f\3\16\3!\13"+
		"\3\3\4\3\4\3\4\6\4&\n\4\r\4\16\4\'\3\4\3\'\2\5\2\4\6\2\4\4\2\6\7\t\t\4"+
		"\2\6\6\b\b\2,\2\31\3\2\2\2\4\33\3\2\2\2\6\"\3\2\2\2\b\n\7\3\2\2\t\13\5"+
		"\4\3\2\n\t\3\2\2\2\13\f\3\2\2\2\f\n\3\2\2\2\f\r\3\2\2\2\r\32\3\2\2\2\16"+
		"\20\7\4\2\2\17\21\7\7\2\2\20\17\3\2\2\2\21\22\3\2\2\2\22\20\3\2\2\2\22"+
		"\23\3\2\2\2\23\25\3\2\2\2\24\26\5\6\4\2\25\24\3\2\2\2\26\27\3\2\2\2\27"+
		"\25\3\2\2\2\27\30\3\2\2\2\30\32\3\2\2\2\31\b\3\2\2\2\31\16\3\2\2\2\32"+
		"\3\3\2\2\2\33\37\7\5\2\2\34\36\t\2\2\2\35\34\3\2\2\2\36!\3\2\2\2\37\35"+
		"\3\2\2\2\37 \3\2\2\2 \5\3\2\2\2!\37\3\2\2\2\"#\7\5\2\2#%\t\3\2\2$&\7\7"+
		"\2\2%$\3\2\2\2&\'\3\2\2\2\'(\3\2\2\2\'%\3\2\2\2(\7\3\2\2\2\b\f\22\27\31"+
		"\37\'";
	public static final ATN _ATN =
		new ATNDeserializer().deserialize(_serializedATN.toCharArray());
	static {
		_decisionToDFA = new DFA[_ATN.getNumberOfDecisions()];
		for (int i = 0; i < _ATN.getNumberOfDecisions(); i++) {
			_decisionToDFA[i] = new DFA(_ATN.getDecisionState(i), i);
		}
	}
}