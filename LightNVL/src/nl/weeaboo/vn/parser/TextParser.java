package nl.weeaboo.vn.parser;

import static nl.weeaboo.vn.parser.ParserUtil.findBlockEnd;
import static nl.weeaboo.lua2.LuaUtil.unescape;

import java.text.CharacterIterator;
import java.text.StringCharacterIterator;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

public class TextParser {

	public static final int TOKEN_TEXT = 1;
	public static final int TOKEN_STRINGIFIER = 2;
	public static final int TOKEN_TAG = 3;
	public static final int TOKEN_COMMAND = 4;
	public static final int TOKEN_ANSI_COLOR = 5;
	
	private List<Token> tokens;
	private CharacterIterator input;
	
	public TextParser() {
		tokens = new ArrayList<Token>();
		input = new StringCharacterIterator("");
	}
	
	//Functions
	public void tokenize() {
		StringBuilder sb = new StringBuilder(input.getEndIndex() - input.getIndex());
		for (char c = input.current(); c != CharacterIterator.DONE; c = input.next()) {
			switch (c) {
			case '\\': {
				c = input.next();
				if (c != CharacterIterator.DONE) {
					sb.append(unescape(c));
				} else {
					sb.append('\\');
				}				
			} break;
			case '[': {
				flushBuffered(tokens, sb);
				input.next();
				tokenizeBlock(TOKEN_COMMAND, ']');
			} break;
			case '$': {
				flushBuffered(tokens, sb);
				char next = input.next();
				if (next == '{') {
					input.next();
					tokenizeBlock(TOKEN_STRINGIFIER, '}');
				} else {
					tokenizeBlock(TOKEN_STRINGIFIER, ' ');
					if (input.current() == ' ') {
						input.previous(); //Don't consume trailing space
					}
				}
			} break;
			case '{': {
				flushBuffered(tokens, sb);
				input.next();
				tokenizeBlock(TOKEN_TAG, '}');
			} break;
			default:
				sb.append(c);
			}
		}
		flushBuffered(tokens, sb);
	}
	
	private void tokenizeBlock(int tokenType, char endChar) {
		StringBuilder sb = new StringBuilder();
		findBlockEnd(input, endChar, sb);
		tokens.add(newToken(tokenType, sb));
	}
	
	/**
	 * Adds the contents of the buffered text as a text token, then clears the string buffer.
	 */
	private void flushBuffered(List<? super Token> out, StringBuilder sb) {
		if (sb.length() == 0) {
			return; //Don't add empty tokens
		}
		Token token = newToken(TOKEN_TEXT, sb);
		sb.delete(0, sb.length());
		out.add(token);
	}
	
	protected Token newToken(int tokenType, CharSequence cs) {
		switch (tokenType) {
		default:
			return new Token(tokenType, cs.toString());
		}
	}
	
	public Collection<Token> tokens() {
		return Collections.unmodifiableList(tokens);
	}
	
	//Getters
	public void getTokens(List<? super Token> out) {
		for (Token token : tokens) {
			out.add(token);
		}
	}
	
	//Setters
	public void setInput(String str) {
		setInput(new StringCharacterIterator(str));
	}
	public void setInput(CharacterIterator itr) {
		input = itr;
		
		tokens.clear();
	}
	
	//Inner Classes
	public static class Token {
		
		protected final int type;
		protected final String text;
		
		public Token(int type, String text) {
			this.type = type;
			this.text = text;
		}
		
		public int getType() { return type; }
		public String getText() { return text; }
		
		@Override
		public String toString() {
			return String.format("TK(%d:%s)", type, text);
		}
	}
		
}
