package nl.weeaboo.vn.impl.lua;

import nl.weeaboo.lua2.io.LuaSerializable;
import nl.weeaboo.lua2.lib.CoerceLuaToJava;
import nl.weeaboo.lua2.lib.LuaLibrary;
import nl.weeaboo.lua2.lib.LuajavaLib;
import nl.weeaboo.styledtext.MutableStyledText;
import nl.weeaboo.styledtext.MutableTextStyle;
import nl.weeaboo.styledtext.StyledText;
import nl.weeaboo.styledtext.TextAttribute;
import nl.weeaboo.styledtext.TextStyle;
import nl.weeaboo.vn.ITextState;

import org.luaj.vm2.LuaTable;
import org.luaj.vm2.LuaValue;
import org.luaj.vm2.Varargs;

@LuaSerializable
public class LuaTextLib extends LuaLibrary {

	private static final long serialVersionUID = LuaImpl.serialVersionUID;
	
	private static final String[] NAMES = {
		"createStyle",
		"createStyledText",
	};

	private static final int INIT               = 0;
	private static final int CREATE_STYLE       = 1;
	private static final int CREATE_STYLED_TEXT = 2;
	
	private final ITextState ts;
	
	public LuaTextLib(ITextState ts) {
		this.ts = ts;
	}

	@Override
	protected LuaLibrary newInstance() {
		return new LuaTextLib(ts);
	}
	
	@Override
	public Varargs invoke(Varargs args) {
		switch (opcode) {
		case INIT: return initLibrary("Text", NAMES, 1);
		case CREATE_STYLE: return createStyle(args);
		case CREATE_STYLED_TEXT: return createStyledText(args);
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

}
