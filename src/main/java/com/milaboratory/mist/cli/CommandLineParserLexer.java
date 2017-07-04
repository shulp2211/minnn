// Generated from /home/user/ml/mist/src/main/java/com/milaboratory/mist/cli/CommandLineParser.g4 by ANTLR 4.7
package com.milaboratory.mist.cli;
import org.antlr.v4.runtime.Lexer;
import org.antlr.v4.runtime.CharStream;
import org.antlr.v4.runtime.Token;
import org.antlr.v4.runtime.TokenStream;
import org.antlr.v4.runtime.*;
import org.antlr.v4.runtime.atn.*;
import org.antlr.v4.runtime.dfa.DFA;
import org.antlr.v4.runtime.misc.*;

@SuppressWarnings({"all", "warnings", "unchecked", "unused", "cast"})
public class CommandLineParserLexer extends Lexer {
	static { RuntimeMetaData.checkVersion("4.7", RuntimeMetaData.VERSION); }

	protected static final DFA[] _decisionToDFA;
	protected static final PredictionContextCache _sharedContextCache =
		new PredictionContextCache();
	public static final int
		T__0=1, T__1=2, OPTION=3, STRING=4, FILE_NAME=5, EXPRESSION=6, ID=7, WS=8;
	public static String[] channelNames = {
		"DEFAULT_TOKEN_CHANNEL", "HIDDEN"
	};

	public static String[] modeNames = {
		"DEFAULT_MODE"
	};

	public static final String[] ruleNames = {
		"T__0", "T__1", "OPTION", "STRING", "FILE_NAME", "EXPRESSION", "ID", "WS"
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


	public CommandLineParserLexer(CharStream input) {
		super(input);
		_interp = new LexerATNSimulator(this,_ATN,_decisionToDFA,_sharedContextCache);
	}

	@Override
	public String getGrammarFileName() { return "CommandLineParser.g4"; }

	@Override
	public String[] getRuleNames() { return ruleNames; }

	@Override
	public String getSerializedATN() { return _serializedATN; }

	@Override
	public String[] getChannelNames() { return channelNames; }

	@Override
	public String[] getModeNames() { return modeNames; }

	@Override
	public ATN getATN() { return _ATN; }

	public static final String _serializedATN =
		"\3\u608b\ua72a\u8133\ub9ed\u417c\u3be7\u7786\u5964\2\nP\b\1\4\2\t\2\4"+
		"\3\t\3\4\4\t\4\4\5\t\5\4\6\t\6\4\7\t\7\4\b\t\b\4\t\t\t\3\2\3\2\3\2\3\2"+
		"\3\2\3\2\3\3\3\3\3\3\3\3\3\3\3\3\3\3\3\4\3\4\3\4\3\4\7\4%\n\4\f\4\16\4"+
		"(\13\4\3\5\3\5\3\5\3\5\7\5.\n\5\f\5\16\5\61\13\5\3\5\3\5\3\6\6\6\66\n"+
		"\6\r\6\16\6\67\3\7\6\7;\n\7\r\7\16\7<\3\7\3\7\6\7A\n\7\r\7\16\7B\3\b\6"+
		"\bF\n\b\r\b\16\bG\3\t\6\tK\n\t\r\t\16\tL\3\t\3\t\2\2\n\3\3\5\4\7\5\t\6"+
		"\13\7\r\b\17\t\21\n\3\2\b\3\2c|\3\2$$\7\2/\60\62;C\\aac|\4\2C\\c|\5\2"+
		"\62;C\\c|\5\2\13\f\17\17\"\"\2W\2\3\3\2\2\2\2\5\3\2\2\2\2\7\3\2\2\2\2"+
		"\t\3\2\2\2\2\13\3\2\2\2\2\r\3\2\2\2\2\17\3\2\2\2\2\21\3\2\2\2\3\23\3\2"+
		"\2\2\5\31\3\2\2\2\7 \3\2\2\2\t)\3\2\2\2\13\65\3\2\2\2\r:\3\2\2\2\17E\3"+
		"\2\2\2\21J\3\2\2\2\23\24\7r\2\2\24\25\7c\2\2\25\26\7t\2\2\26\27\7u\2\2"+
		"\27\30\7g\2\2\30\4\3\2\2\2\31\32\7h\2\2\32\33\7k\2\2\33\34\7n\2\2\34\35"+
		"\7v\2\2\35\36\7g\2\2\36\37\7t\2\2\37\6\3\2\2\2 !\7/\2\2!\"\7/\2\2\"&\3"+
		"\2\2\2#%\t\2\2\2$#\3\2\2\2%(\3\2\2\2&$\3\2\2\2&\'\3\2\2\2\'\b\3\2\2\2"+
		"(&\3\2\2\2)/\7$\2\2*+\7$\2\2+.\7$\2\2,.\n\3\2\2-*\3\2\2\2-,\3\2\2\2.\61"+
		"\3\2\2\2/-\3\2\2\2/\60\3\2\2\2\60\62\3\2\2\2\61/\3\2\2\2\62\63\7$\2\2"+
		"\63\n\3\2\2\2\64\66\t\4\2\2\65\64\3\2\2\2\66\67\3\2\2\2\67\65\3\2\2\2"+
		"\678\3\2\2\28\f\3\2\2\29;\t\5\2\2:9\3\2\2\2;<\3\2\2\2<:\3\2\2\2<=\3\2"+
		"\2\2=>\3\2\2\2>@\7?\2\2?A\t\5\2\2@?\3\2\2\2AB\3\2\2\2B@\3\2\2\2BC\3\2"+
		"\2\2C\16\3\2\2\2DF\t\6\2\2ED\3\2\2\2FG\3\2\2\2GE\3\2\2\2GH\3\2\2\2H\20"+
		"\3\2\2\2IK\t\7\2\2JI\3\2\2\2KL\3\2\2\2LJ\3\2\2\2LM\3\2\2\2MN\3\2\2\2N"+
		"O\b\t\2\2O\22\3\2\2\2\13\2&-/\67<BGL\3\b\2\2";
	public static final ATN _ATN =
		new ATNDeserializer().deserialize(_serializedATN.toCharArray());
	static {
		_decisionToDFA = new DFA[_ATN.getNumberOfDecisions()];
		for (int i = 0; i < _ATN.getNumberOfDecisions(); i++) {
			_decisionToDFA[i] = new DFA(_ATN.getDecisionState(i), i);
		}
	}
}