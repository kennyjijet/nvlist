package nl.weeaboo.vn.script.impl;

import nl.weeaboo.lua2.LuaRunState;
import nl.weeaboo.vn.script.ScriptException;

final class ScriptImpl {

	static final long serialVersionUID = 1L;

	private ScriptImpl() {
	}

	public static LuaRunState getRunState() throws ScriptException {
	    LuaRunState runState = LuaRunState.getCurrent();
	    if (runState == null) {
	        throw new ScriptException("No LuaRunState is current");
	    }
	    return runState;
	}

}
