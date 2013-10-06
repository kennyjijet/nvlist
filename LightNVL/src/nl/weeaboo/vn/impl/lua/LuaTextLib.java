package nl.weeaboo.vn.impl.lua;

import java.io.ByteArrayInputStream;
import java.io.IOException;

import nl.weeaboo.collections.IntMap;
import nl.weeaboo.common.StringUtil;
import nl.weeaboo.lua2.LuaUtil;
import nl.weeaboo.lua2.io.LuaSerializable;
import nl.weeaboo.lua2.lib.CoerceLuaToJava;
import nl.weeaboo.lua2.lib.LuaLibrary;
import nl.weeaboo.lua2.lib.LuajavaLib;
import nl.weeaboo.styledtext.MutableStyledText;
import nl.weeaboo.styledtext.MutableTextStyle;
import nl.weeaboo.styledtext.StyledText;
import nl.weeaboo.styledtext.TextAttribute;
import nl.weeaboo.styledtext.TextStyle;
import nl.weeaboo.vn.ITextDrawable;
import nl.weeaboo.vn.ITextState;
import nl.weeaboo.vn.parser.RuntimeTextParser;
import nl.weeaboo.vn.parser.RuntimeTextParser.ParseResult;

import org.luaj.vm2.LoadState;
import org.luaj.vm2.LuaError;
import org.luaj.vm2.LuaTable;
import org.luaj.vm2.LuaValue;
import org.luaj.vm2.Varargs;
import org.luaj.vm2.lib.VarArgFunction;

@LuaSerializable
public class LuaTextLib extends LuaLibrary {

	private static final long serialVersionUID = LuaImpl.serialVersionUID;
	
	private static final String[] NAMES = {
		"createStyle",
		"createStyledText",
		"parseText",
		"registerBasicTagHandlers",
		"resolveConstant",
		"rebaseTriggers"
	};

	private static final int INIT               = 0;
	private static final int CREATE_STYLE       = 1;
	private static final int CREATE_STYLED_TEXT = 2;
	private static final int PARSE_TEXT         = 3;
	private static final int REGISTER_BASIC_TAG_HANDLERS = 4;
	private static final int RESOLVE_CONSTANT   = 5;
	private static final int REBASE_TRIGGERS    = 6;
	
	private final ITextState ts;
	private final RuntimeTextParser runtimeParser;
	
	public LuaTextLib(ITextState ts, RuntimeTextParser runtimeParser) {
		this.ts = ts;
		this.runtimeParser = runtimeParser;
	}

	@Override
	protected LuaLibrary newInstance() {
		return new LuaTextLib(ts, runtimeParser);
	}
	
	@Override
	public Varargs invoke(Varargs args) {
		switch (opcode) {
		case INIT: return initLibrary("Text", NAMES, 1);
		case CREATE_STYLE: return createStyle(args);
		case CREATE_STYLED_TEXT: return createStyledText(args);
		case PARSE_TEXT: return parseText(args);
		case REGISTER_BASIC_TAG_HANDLERS: return registerBasicTagHandlers(args);
		case RESOLVE_CONSTANT: return resolveConstant(args);
		case REBASE_TRIGGERS: return rebaseTriggers(args);
		default: return super.invoke(args);
		}
	}

	protected TextStyle toTextStyle(LuaValue val) {
		TextStyle ts = val.touserdata(TextStyle.class);
		if (ts != null) {
			return ts;
		}
		
		if (val.isstring()) {
			return TextStyle.fromString(val.tojstring());
		} else if (val.istable()) {
			LuaTable table = val.checktable();
			
			MutableTextStyle mts = new MutableTextStyle();
			for (LuaValue lkey : table.keys()) {
				TextAttribute tkey = TextAttribute.valueOf(lkey.toString());
				if (tkey != null) {
					LuaValue lval = table.get(lkey);
					
					Object tval;
					try {					
						tval = CoerceLuaToJava.coerceArg(lval, tkey.getType());
					} catch (Exception e) {
						tval = CoerceLuaToJava.coerceArg(lval, Object.class);
					}
					
					if (tval != null) {
						mts.setProperty(tkey, tval);
					}
				}
			}
			return mts.immutableCopy();
		} else {		
			return TextStyle.defaultInstance();
		}
	}
	
	protected Varargs createStyle(Varargs args) {
		return LuajavaLib.toUserdata(toTextStyle(args.arg1()), TextStyle.class);
	}
	
	protected Varargs createStyledText(Varargs args) {
		StyledText st = args.touserdata(1, StyledText.class);
		if (st != null) {
			TextStyle style = args.touserdata(2, TextStyle.class);
			if (style != null) {
				MutableStyledText mts = st.mutableCopy();
				mts.setBaseStyle(style);
				st = mts.immutableCopy();
			}
		} else {
			String text = (args.isnil(1) ? "" : args.tojstring(1));
			TextStyle style = toTextStyle(args.arg(2));
			st = new StyledText(text, style);
		}
		return LuajavaLib.toUserdata(st, StyledText.class);
	}
	
	protected Varargs parseText(Varargs args) {
		ParseResult res = runtimeParser.parse(args.arg(1).tojstring());
		
		LuaTable oldTriggers = args.opttable(2, null);
		LuaTable newTriggers = null;
		if (oldTriggers != null) {
			LuaValue oldTableIndex = ZERO;
			newTriggers = new LuaTable();		
			IntMap<String> commandMap = res.getCommands();
			for (int n = 0; n < commandMap.size(); n++) {
				LuaValue func = null;
				if (oldTriggers != null) {
					Varargs ipair = oldTriggers.inext(oldTableIndex);
					oldTableIndex = ipair.arg(1);
					func = ipair.arg(2);
				}
				
				if (func == null) {
					//If no compiled trigger function given, compile it here.
					//This means we lose access to any local variables as the compilation happens in the global scope.
					String str = commandMap.valueAt(n);
					ByteArrayInputStream bin = new ByteArrayInputStream(StringUtil.toUTF8(str));
					try {	
						func = LoadState.load(bin, "~trigger" + n, env);
					} catch (IOException ioe) {
						throw new LuaError("IOException", ioe);
					}
				}
				
				newTriggers.rawset(commandMap.keyAt(n), func);			
			}
		}
		
		LuaValue resultText = LuajavaLib.toUserdata(res.getText(), StyledText.class);
		if (newTriggers == null) {
			return resultText;
		} else {
			return varargsOf(resultText, newTriggers);
		}
	}
	
	protected Varargs registerBasicTagHandlers(Varargs args) {
		LuaTable table = args.checktable(1);
		BasicTagHandler h = new BasicTagHandler();
		for (String tag : BasicTagHandler.TAGS) {
			table.rawset(tag, h);
		}
		return table;
	}
	
	protected Varargs resolveConstant(Varargs args) {
		LuaValue val = args.arg1();
		if (val.isnil()) {
			return val;
		}
		
		return LuaUtil.resolveCodeConstant(val.tojstring());
	}
	
	protected Varargs rebaseTriggers(Varargs args) {
		LuaTable oldTriggers = args.opttable(1, null);
		int offset = 0;
		if (args.isnumber(2)) {
			offset = args.toint(2);
		} else {
			ITextDrawable textBox = args.touserdata(2, ITextDrawable.class);
			offset = textBox.getCharOffset(textBox.getStartLine());
		}
		
		if (oldTriggers == null || offset == 0) {
			return args.arg1();
		}
		
		LuaTable newTriggers = new LuaTable();
		LuaValue oldTableIndex = ZERO;
		while (!oldTableIndex.isnil()) {
			Varargs ipair = oldTriggers.inext(oldTableIndex);
			oldTableIndex = ipair.arg(1);
			LuaValue func = ipair.arg(2);
			newTriggers.rawset(offset + oldTableIndex.toint(), func);
		}
		return newTriggers;
	}

	//Inner Classes
	@LuaSerializable
	private static class BasicTagHandler extends VarArgFunction {

		private static final long serialVersionUID = 1L;

		static final String[] TAGS = {
			"b",
			"i",
			"u",
			"font",
			"color",
			"size",
			"speed",
			"align",
			"center"
		};
		
		public BasicTagHandler() {
		}
		
		@Override
		public Varargs invoke(Varargs args) {
			String tag = args.tojstring(1);
			
			LuaTable table = args.opttable(2, new LuaTable());
			int n = 1;
			
			TextStyle style = null;
			if ("b".equals(tag)) {
				style = newStyle(TextAttribute.fontStyle, valueOf("bold"));
			} else if ("i".equals(tag)) {
				style = newStyle(TextAttribute.fontStyle, valueOf("italic"));
			} else if ("u".equals(tag)) {
				style = newStyle(TextAttribute.underline, valueOf(true));
			} else if ("font".equals(tag)) {
				style = newStyle(TextAttribute.fontName, table.rawget(n));
			} else if ("color".equals(tag)) {
				style = newStyle(TextAttribute.color, table.rawget(n));
			} else if ("size".equals(tag)) {
				style = newStyle(TextAttribute.fontSize, table.rawget(n));				
			} else if ("speed".equals(tag)) {
				style = newStyle(TextAttribute.speed, table.rawget(n));
			} else if ("align".equals(tag)) {
				style = newStyle(TextAttribute.anchor, table.rawget(n));
			} else if ("center".equals(tag)) {
				style = newStyle(TextAttribute.anchor, valueOf("center"));
			}
			
			if (style == null) {
				return NONE;
			} else {
				return varargsOf(NIL, LuajavaLib.toUserdata(style, TextStyle.class));
			}
		}

		private static TextStyle newStyle(TextAttribute attr, LuaValue lval) {			
			Object tval;
			try {					
				tval = CoerceLuaToJava.coerceArg(lval, attr.getType());
			} catch (Exception e) {
				tval = CoerceLuaToJava.coerceArg(lval, Object.class);
			}	
			
			MutableTextStyle mts = new MutableTextStyle();			
			mts.setProperty(attr, tval);		
			return mts.immutableCopy();
		}
		
	}
}
