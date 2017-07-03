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
		OPTION=1, COMMAND=2, STRING=3, WS=4;
	public static final int
		RULE_commandLine = 0, RULE_option = 1;
	public static final String[] ruleNames = {
		"commandLine", "option"
	};

	private static final String[] _LITERAL_NAMES = {
	};
	private static final String[] _SYMBOLIC_NAMES = {
		null, "OPTION", "COMMAND", "STRING", "WS"
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
		public TerminalNode COMMAND() { return getToken(CommandLineParserParser.COMMAND, 0); }
		public List<OptionContext> option() {
			return getRuleContexts(OptionContext.class);
		}
		public OptionContext option(int i) {
			return getRuleContext(OptionContext.class,i);
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
			enterOuterAlt(_localctx, 1);
			{
			setState(4);
			match(COMMAND);
			setState(6); 
			_errHandler.sync(this);
			_la = _input.LA(1);
			do {
				{
				{
				setState(5);
				option();
				}
				}
				setState(8); 
				_errHandler.sync(this);
				_la = _input.LA(1);
			} while ( _la==OPTION );
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

	public static class OptionContext extends ParserRuleContext {
		public TerminalNode OPTION() { return getToken(CommandLineParserParser.OPTION, 0); }
		public TerminalNode STRING() { return getToken(CommandLineParserParser.STRING, 0); }
		public OptionContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_option; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof CommandLineParserListener ) ((CommandLineParserListener)listener).enterOption(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof CommandLineParserListener ) ((CommandLineParserListener)listener).exitOption(this);
		}
	}

	public final OptionContext option() throws RecognitionException {
		OptionContext _localctx = new OptionContext(_ctx, getState());
		enterRule(_localctx, 2, RULE_option);
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(10);
			match(OPTION);
			setState(11);
			match(STRING);
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
		"\3\u608b\ua72a\u8133\ub9ed\u417c\u3be7\u7786\u5964\3\6\20\4\2\t\2\4\3"+
		"\t\3\3\2\3\2\6\2\t\n\2\r\2\16\2\n\3\3\3\3\3\3\3\3\2\2\4\2\4\2\2\2\16\2"+
		"\6\3\2\2\2\4\f\3\2\2\2\6\b\7\4\2\2\7\t\5\4\3\2\b\7\3\2\2\2\t\n\3\2\2\2"+
		"\n\b\3\2\2\2\n\13\3\2\2\2\13\3\3\2\2\2\f\r\7\3\2\2\r\16\7\5\2\2\16\5\3"+
		"\2\2\2\3\n";
	public static final ATN _ATN =
		new ATNDeserializer().deserialize(_serializedATN.toCharArray());
	static {
		_decisionToDFA = new DFA[_ATN.getNumberOfDecisions()];
		for (int i = 0; i < _ATN.getNumberOfDecisions(); i++) {
			_decisionToDFA[i] = new DFA(_ATN.getDecisionState(i), i);
		}
	}
}