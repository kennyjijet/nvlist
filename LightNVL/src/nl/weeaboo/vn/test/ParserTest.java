package nl.weeaboo.vn.test;

import java.io.FileInputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import nl.weeaboo.collections.IntMap;
import nl.weeaboo.io.StreamUtil;
import nl.weeaboo.lua2.lib.LuajavaLib;
import nl.weeaboo.styledtext.StyledText;
import nl.weeaboo.styledtext.TextStyle;
import nl.weeaboo.vn.parser.LVNParser;
import nl.weeaboo.vn.parser.ParseException;
import nl.weeaboo.vn.parser.ParserUtil;
import nl.weeaboo.vn.parser.RuntimeTextParser;
import nl.weeaboo.vn.parser.RuntimeTextParser.ParseResult;
import nl.weeaboo.vn.parser.TextParser;
import nl.weeaboo.vn.parser.TextParser.Token;

import org.junit.Assert;
import org.junit.Test;
import org.luaj.vm2.LuaTable;
import org.luaj.vm2.LuaValue;
import org.luaj.vm2.Varargs;
import org.luaj.vm2.lib.OneArgFunction;
import org.luaj.vm2.lib.TwoArgFunction;
import org.luaj.vm2.lib.VarArgFunction;

public class ParserTest {

	@Test
	public void syntaxTest() throws ParseException, IOException {		
		String filename = "test/test";
		String contents;
		byte checkBytes[];
		
		for (int version = 3; version <= 4; version++) {
			FileInputStream in = new FileInputStream(filename + ".lvn");
			try {
				LVNParser parser;
				switch (version) {
				case 3: {
					parser = ParserUtil.getParser("3.3");
				} break;
				default:
					parser = ParserUtil.getParser("4.0");
				}
				contents = parser.parseFile(filename, in).compile();				
			} finally {
				in.close();
			}
	
			//System.err.println(contents);
			//System.err.println("----------------------------------------");
			
			in = new FileInputStream(filename + version + ".lua");		
			try {
				checkBytes = StreamUtil.readFully(in);
			} finally {
				in.close();
			}
			
			/*
			String check = new String(checkBytes, "UTF-8");
			System.out.println(check);
			
			for (int n = 0; n < Math.max(check.length(), contents.length()); n++) {
				char c0 = (n < check.length() ? check.charAt(n) : ' ');
				char c1 = (n < contents.length() ? contents.charAt(n) : ' ');
				System.out.println(c0 + " " + c1 + " " + (c0==c1));
			}
			*/
			
			Assert.assertArrayEquals(checkBytes, contents.getBytes("UTF-8"));
		}
	}
	
	@SuppressWarnings("serial")
	@Test
	public void textParserTest() {
		String input = "Text with [embedded()] code and {tag a,b,c,d}embedded tags{/tag} and ${stringifiers} too.";
		
		TextParser parser = new TextParser();
		parser.setInput(input);
		parser.tokenize();
		
		List<Token> tokens = new ArrayList<Token>();
		parser.getTokens(tokens);
		System.out.println("----------------------------------------");
		for (Token token : tokens) {
			System.out.println(token);
		}
		System.out.println("----------------------------------------");
		
		LuaTable debugTable = new LuaTable();
		debugTable.set("stringify", new OneArgFunction() {
			@Override
			public LuaValue call(LuaValue arg) {
				System.out.println("stringify: " + arg);
				return arg;
			}
		});
		debugTable.set("tagOpen", new VarArgFunction() {
			@Override
			public Varargs invoke(Varargs args) {
				System.out.println("tagOpen: " + args.arg1() + " " + args.arg(2));
				return varargsOf(valueOf(""), LuajavaLib.toUserdata(TextStyle.withTags(1337), TextStyle.class));
			}
		});
		debugTable.set("tagClose", new TwoArgFunction() {
			@Override
			public LuaValue call(LuaValue name, LuaValue args) {
				System.out.println("tagClose: " + name + " " + args);
				return valueOf("");
			}
		});
		RuntimeTextParser runtimeParser = new RuntimeTextParser(debugTable);
		ParseResult parseResult = runtimeParser.parse(input);
		StyledText stext = parseResult.getText();
		System.out.println(stext);
		for (int n = 0; n < stext.length(); n++) {
			TextStyle style = stext.getStyle(n);
			if (style != null && style.getTags().length > 0) {
				System.out.print('*');
			} else {
				System.out.print(' ');
			}
		}
		System.out.println();
		IntMap<String> commandMap = parseResult.getCommands();
		for (int n = 0; n < commandMap.size(); n++) {
			System.out.println(commandMap.keyAt(n) + ": " + commandMap.valueAt(n));
		}
		System.out.println("----------------------------------------");
		
	}
	
}
