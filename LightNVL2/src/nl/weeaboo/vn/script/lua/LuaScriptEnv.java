package nl.weeaboo.vn.script.lua;

import java.util.ArrayList;
import java.util.List;

import nl.weeaboo.common.Checks;
import nl.weeaboo.lua2.LuaException;
import nl.weeaboo.lua2.LuaRunState;
import nl.weeaboo.vn.script.IScriptEnv;
import nl.weeaboo.vn.script.IScriptEventDispatcher;
import nl.weeaboo.vn.script.IScriptLoader;
import nl.weeaboo.vn.script.impl.ScriptEventDispatcher;

import org.luaj.vm2.LuaTable;

public class LuaScriptEnv implements IScriptEnv {

	private static final long serialVersionUID = LuaImpl.serialVersionUID;

	private final LuaRunState runState;
	private final LuaScriptLoader loader;
	private final IScriptEventDispatcher eventDispatcher;
	private final List<ILuaScriptEnvInitializer> initializers = new ArrayList<ILuaScriptEnvInitializer>();

	private boolean initialized;

	public LuaScriptEnv(LuaRunState runState, LuaScriptLoader loader) {
	    this.runState = runState;
		this.loader = loader;

		eventDispatcher = new ScriptEventDispatcher();
	}

	public void initEnv() throws LuaException {
	    initialized = true;

	    runState.registerOnThread();
        loader.initEnv();

	    for (ILuaScriptEnvInitializer init : initializers) {
	        init.initEnv(this);
	    }
	}

	public void addInitializer(ILuaScriptEnvInitializer init) {
	    Checks.checkState(!initialized, "Can't change initializers after initEnv() has been called.");

	    initializers.add(init);
	}

	public LuaRunState getRunState() {
	    return runState;
	}

	public LuaTable getGlobals() {
	    return runState.getGlobalEnvironment();
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