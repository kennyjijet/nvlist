package nl.weeaboo.vn.script.impl;

import nl.weeaboo.lua2.io.LuaSerializable;
import nl.weeaboo.vn.script.IScriptEnv;
import nl.weeaboo.vn.script.IScriptEventDispatcher;
import nl.weeaboo.vn.script.IScriptLoader;

@LuaSerializable
public class ScriptEnv implements IScriptEnv {

	private static final long serialVersionUID = ScriptImpl.serialVersionUID;

	private final IScriptLoader loader;
	private final IScriptEventDispatcher eventDispatcher;

	public ScriptEnv(IScriptLoader loader) {
		this.loader = loader;

		eventDispatcher = new ScriptEventDispatcher();
	}

	@Override
	public IScriptEventDispatcher getEventDispatcher() {
		return eventDispatcher;
	}

	@Override
	public IScriptLoader getScriptLoader() {
		return loader;
	}

}
