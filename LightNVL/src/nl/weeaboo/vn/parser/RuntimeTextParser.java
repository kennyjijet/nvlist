package nl.weeaboo.vn.parser;

import static nl.weeaboo.vn.parser.ParserUtil.findBlockEnd;
import static nl.weeaboo.vn.parser.ParserUtil.isCollapsibleSpace;
import static nl.weeaboo.lua2.LuaUtil.unescape;
import static nl.weeaboo.vn.parser.TextParser.TOKEN_COMMAND;
import static nl.weeaboo.vn.parser.TextParser.TOKEN_STRINGIFIER;
import static nl.weeaboo.vn.parser.TextParser.TOKEN_TAG;
import static nl.weeaboo.vn.parser.TextParser.TOKEN_TEXT;
import static org.luaj.vm2.LuaValue.valueOf;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.Serializable;
import java.text.CharacterIterator;
import java.text.StringCharacterIterator;
import java.util.ArrayList;
import java.util.List;

import nl.weeaboo.collections.IntMap;
import nl.weeaboo.lua2.io.LuaSerializable;
import nl.weeaboo.styledtext.MutableStyledText;
import nl.weeaboo.styledtext.StyledText;
import nl.weeaboo.styledtext.TextStyle;
import nl.weeaboo.vn.parser.TextParser.Token;

import org.luaj.vm2.LuaTable;
import org.luaj.vm2.LuaValue;
import org.luaj.vm2.Varargs;

@LuaSerializable
public class RuntimeTextParser implements Serializable {
	
	private static final long serialVersionUID = 1L;
	
	private static final String F_STRINGIFY = "stringify";
	private static final String F_TAG_OPEN  = "textTagOpen";
	private static final String F_TAG_CLOSE = "textTagClose";
	
	private final LuaValue globals;
	
	private transient TextParser parser;
	
	public RuntimeTextParser(LuaValue globs) {
		globals = globs;
		
		initTransients();
	}

	private void initTransients() {
		parser = new TextParser();
	}
	
	private void readObject(ObjectInputStream in) throws IOException, ClassNotFoundException {
		in.defaultReadObject();

		initTransients();
	}
	
	public ParseResult parse(String line) {
		parser.setInput(line);
		parser.tokenize();
		
		List<Token> tokens = new ArrayList<Token>();
		parser.getTokens(tokens);
		
		//Process non-command tokens into StyledText objects.
		List<Object> processed = new ArrayList<Object>();
		StyleStack styleStack = new StyleStack();		
		for (Token token : tokens) {
			processToken(processed, token, styleStack);
		}
		
		//Collapse whitespace and process command tokens
		IntMap<String> commandMap = new IntMap<String>();
		MutableStyledText mts = new MutableStyledText();
		boolean lastCharCollapsible = true;
		for (Object obj : processed) {
			if (obj instanceof StyledText) {
				StyledText stext = (StyledText)obj;
				
				//Append to result string
				for (int n = 0; n < stext.length(); n++) {
					char c = stext.charAt(n);
					if (isCollapsibleSpace(c)) {
						if (lastCharCollapsible) {
							continue; //Skip
						} else {
							lastCharCollapsible = true;
						}
					} else {
						lastCharCollapsible = false;
					}
					mts.append(c, stext.getStyle(n));
				}
			} else if (obj instanceof Token) {
				Token token = (Token)obj;
				if (token.type == TOKEN_COMMAND) {
					commandMap.put(mts.length(), token.getText());
				}
			}
		}
		
		return new ParseResult(mts.immutableCopy(), commandMap);
	}
	
	private void processToken(List<Object> out, Token token, StyleStack styleStack) {
		int type = token.getType();
		switch (type) {
		case TOKEN_COMMAND: {
			out.add(token);
		} break;
		case TOKEN_TEXT: {
			out.add(new StyledText(token.getText(), styleStack.getCalculatedStyle()));
		} break;
		case TOKEN_STRINGIFIER: {
			StyledText stext = processStringifier(token.getText(), styleStack);
			stext = changeBaseStyle(stext, styleStack.getCalculatedStyle());
			out.add(stext);
		} break;
		case TOKEN_TAG: {
			StringBuilder sb = new StringBuilder();
			CharacterIterator itr = new StringCharacterIterator(token.getText());
			findBlockEnd(itr, ' ', sb);
			
			String tag = sb.toString().trim();
			sb.delete(0, sb.length());

			boolean isOpenTag = true;
			if (tag.startsWith("/")) {
				tag = tag.substring(1);
				isOpenTag = false;
			}
			
			List<String> args = new ArrayList<String>();
			if (isOpenTag) {
				//Parse list of values aaa,bbb,ccc
				int start = itr.getIndex();
				while (start < itr.getEndIndex()) {
					int end = findBlockEnd(itr, ',', sb);
					if (end <= start) {
						break;
					}
					
					String arg = unescape(sb.toString().trim());
					sb.delete(0, sb.length());
					args.add(arg);
					
					start = end + 1;
				}
			}

			StyledText stext = processTextTag(tag, isOpenTag, args.toArray(new String[args.size()]), styleStack);
			stext = changeBaseStyle(stext, styleStack.getCalculatedStyle());
			out.add(stext);
		} break;
		default:
			//Ignore
		}
	}
	
	private static StyledText changeBaseStyle(StyledText stext, TextStyle style) {
		if (style == TextStyle.defaultInstance()) {
			return stext;
		}
		MutableStyledText mts = stext.mutableCopy();
		mts.setBaseStyle(style);
		return mts.immutableCopy();
	}
	
	private StyledText processStringifier(String str, StyleStack styleStack) {
		LuaValue func = globals.get(F_STRINGIFY);
		if (func.isnil()) {
			return StyledText.EMPTY_STRING;
		}
		
		Varargs result = func.invoke(valueOf(str));
		return optStyledText(result, 1);
	}
	
	private StyledText processTextTag(String tag, boolean isOpenTag, String[] args, StyleStack styleStack) {
		LuaValue func = globals.get(isOpenTag ? F_TAG_OPEN : F_TAG_CLOSE);
		if (func.isnil()) {
			return StyledText.EMPTY_STRING;
		}
		
		LuaTable argsTable = new LuaTable();
		for (int n = 0; n < args.length; n++) {
			argsTable.rawset(n+1, args[n]);
		}

		Varargs result = func.invoke(valueOf(tag), argsTable);
		if (isOpenTag) {
			TextStyle style = TextStyle.defaultInstance();
			if (result.narg() >= 2) {
				style = result.touserdata(2, TextStyle.class);
			}
			styleStack.pushWithTag(tag, style);			
		} else {
			styleStack.popWithTag(tag);			
		}

		return optStyledText(result, 1);
	}
	
	private StyledText optStyledText(Varargs args, int index) {
		LuaValue raw = args.arg(index);
		if (raw.isnil()) {
			return StyledText.EMPTY_STRING;
		}
		
		if (raw.isuserdata(StyledText.class)) {
			return raw.touserdata(StyledText.class);
		}
		String str = raw.tojstring();
		return (str.equals("") ? StyledText.EMPTY_STRING : new StyledText(str));		
	}
	
	public class ParseResult {
		
		private StyledText stext;
		private IntMap<String> commands;
		
		public ParseResult(StyledText stext, IntMap<String> commands) {
			this.stext = stext;
			this.commands = commands;
		}
		
		public StyledText getText() { return stext; }
		public IntMap<String> getCommands() { return commands; }
		
	}
	
}
