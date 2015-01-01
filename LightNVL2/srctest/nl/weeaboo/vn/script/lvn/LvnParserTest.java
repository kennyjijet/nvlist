package nl.weeaboo.vn.script.lvn;

import java.io.IOException;
import java.io.InputStream;

import nl.weeaboo.collections.IntMap;
import nl.weeaboo.common.StringUtil;
import nl.weeaboo.io.StreamUtil;
import nl.weeaboo.lua2.lib.LuajavaLib;
import nl.weeaboo.styledtext.StyledText;
import nl.weeaboo.styledtext.TextStyle;
import nl.weeaboo.vn.script.lvn.RuntimeTextParser.ParseResult;
import nl.weeaboo.vn.script.lvn.TextParser.Token;

import org.junit.Assert;
import org.junit.Test;
import org.luaj.vm2.LuaTable;
import org.luaj.vm2.LuaValue;
import org.luaj.vm2.Varargs;
import org.luaj.vm2.lib.OneArgFunction;
import org.luaj.vm2.lib.TwoArgFunction;
import org.luaj.vm2.lib.VarArgFunction;

public class LvnParserTest {

	@Test
	public void syntaxTest3() throws LvnParseException, IOException {
	    syntaxTest(3);
	}

    @Test
    public void syntaxTest4() throws LvnParseException, IOException {
        syntaxTest(4);
    }

	private void syntaxTest(int version) throws IOException, LvnParseException {
        String filename = "test";

        final String contents;
        InputStream in = LvnParserTest.class.getResourceAsStream(filename + ".lvn");
        try {
            ILvnParser parser = LvnParserFactory.getParser(Integer.toString(version));
            ICompiledLvnFile lvnFile = parser.parseFile(filename, in);
            contents = lvnFile.getCompiledContents();
        } finally {
            in.close();
        }

        final byte[] checkBytes;
        in = LvnParserTest.class.getResourceAsStream(filename + version + ".lua");
        try {
            checkBytes = StreamUtil.readFully(in);
        } finally {
            in.close();
        }

        System.out.println(contents);

        Assert.assertEquals(StringUtil.fromUTF8(checkBytes, 0, checkBytes.length), contents);
        Assert.assertArrayEquals(checkBytes, StringUtil.toUTF8(contents));
	}

	@SuppressWarnings("serial")
	@Test
	public void textParserTest() {
		String input = "Text with [embedded()] code and {tag a,b,c,d}embedded tags{/tag} and ${stringifiers} too.";

		TextParser parser = new TextParser();

		System.out.println("----------------------------------------");
		for (Token token : parser.tokenize(input)) {
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
