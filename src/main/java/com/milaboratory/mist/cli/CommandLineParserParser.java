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
		COMMAND=1, OPTION=2, STRING=3, WS=4;
	public static final int
		RULE_commandLine = 0;
	public static final String[] ruleNames = {
		"commandLine"
	};

	private static final String[] _LITERAL_NAMES = {
	};
	private static final String[] _SYMBOLIC_NAMES = {
		null, "COMMAND", "OPTION", "STRING", "WS"
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
		public List<TerminalNode> OPTION() { return getTokens(CommandLineParserParser.OPTION); }
		public TerminalNode OPTION(int i) {
			return getToken(CommandLineParserParser.OPTION, i);
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
			setState(2);
			match(COMMAND);
			setState(4); 
			_errHandler.sync(this);
			_la = _input.LA(1);
			do {
				{
				{
				setState(3);
				match(OPTION);
				}
				}
				setState(6); 
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

	public static final String _serializedATN =
		"\3\u608b\ua72a\u8133\ub9ed\u417c\u3be7\u7786\u5964\3\6\13\4\2\t\2\3\2"+
		"\3\2\6\2\7\n\2\r\2\16\2\b\3\2\2\2\3\2\2\2\2\n\2\4\3\2\2\2\4\6\7\3\2\2"+
		"\5\7\7\4\2\2\6\5\3\2\2\2\7\b\3\2\2\2\b\6\3\2\2\2\b\t\3\2\2\2\t\3\3\2\2"+
		"\2\3\b";
	public static final ATN _ATN =
		new ATNDeserializer().deserialize(_serializedATN.toCharArray());
	static {
		_decisionToDFA = new DFA[_ATN.getNumberOfDecisions()];
		for (int i = 0; i < _ATN.getNumberOfDecisions(); i++) {
			_decisionToDFA[i] = new DFA(_ATN.getDecisionState(i), i);
		}
	}
}