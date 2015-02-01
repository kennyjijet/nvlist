package nl.weeaboo.vn.script.lua.lib;

import java.io.IOException;

import nl.weeaboo.lua2.LuaException;
import nl.weeaboo.lua2.LuaRunState;
import nl.weeaboo.vn.TestContextBuilder;
import nl.weeaboo.vn.TestFileSystem;
import nl.weeaboo.vn.impl.Context;
import nl.weeaboo.vn.impl.ContextManager;
import nl.weeaboo.vn.script.IScriptContext;
import nl.weeaboo.vn.script.IScriptThread;
import nl.weeaboo.vn.script.ScriptException;
import nl.weeaboo.vn.script.lua.LuaScriptEnv;
import nl.weeaboo.vn.script.lua.LuaScriptLoader;
import nl.weeaboo.vn.script.lua.LuaTestUtil;

import org.junit.After;
import org.junit.Before;

public abstract class AbstractLibTest {

    protected LuaScriptLoader scriptLoader;
    protected LuaScriptEnv scriptEnv;
    protected ContextManager contextManager;
    protected Context mainContext;
    protected IScriptThread mainThread;

    @Before
    public void init() throws LuaException {
        LuaRunState runState = LuaTestUtil.newRunState();
        scriptLoader = LuaTestUtil.newScriptLoader(TestFileSystem.newInstance());

        scriptEnv = new LuaScriptEnv(runState, scriptLoader);

        TestContextBuilder contextBuilder = new TestContextBuilder(scriptEnv);
        contextManager = new ContextManager(contextBuilder);

        addInitializers(scriptEnv);

        scriptEnv.initEnv();

        // Create an initial context and activate it
        mainContext = contextManager.createContext();
        contextManager.setContextActive(mainContext, true);

        IScriptContext mainScriptContext = mainContext.getScriptContext();
        mainThread = mainScriptContext.getMainThread();
    }

    protected abstract void addInitializers(LuaScriptEnv scriptEvent);

    @After
    public void deinit() {
        scriptEnv.getRunState().destroy();
    }

    protected void loadScript(String path) throws IOException, ScriptException {
        scriptLoader.loadScript(mainThread, path);
    }

}
