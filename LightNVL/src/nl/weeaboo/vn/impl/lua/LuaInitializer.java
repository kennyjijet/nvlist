package nl.weeaboo.vn.impl.lua;

import java.io.Serializable;

import nl.weeaboo.lua2.LuaRunState;

public interface LuaInitializer extends Serializable {

	public void init(LuaRunState lrs);
	
}
