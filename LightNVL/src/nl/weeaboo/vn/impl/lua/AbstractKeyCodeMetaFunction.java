package nl.weeaboo.vn.impl.lua;

import java.io.Serializable;

import nl.weeaboo.lua2.io.LuaSerializable;

import org.luaj.vm2.LuaTable;
import org.luaj.vm2.LuaValue;
import org.luaj.vm2.Varargs;
import org.luaj.vm2.lib.VarArgFunction;

@LuaSerializable
public abstract class AbstractKeyCodeMetaFunction extends VarArgFunction implements Serializable {

	private static final long serialVersionUID = LuaImpl.serialVersionUID;
	
	private final LuaTable table;
	
	public AbstractKeyCodeMetaFunction(LuaTable t) {
		table = t;
	}
	
	protected abstract LuaValue getKeyCode(String name);
	
	@Override
	public Varargs invoke(Varargs args) {
		LuaValue retval = NIL;
		LuaValue nameArg = args.arg(2);
		if (nameArg.isstring()) {
			retval = getKeyCode(nameArg.tojstring());
			table.rawset(nameArg, retval); //Cache return value in table
		}
		return retval;
	}
	
}
