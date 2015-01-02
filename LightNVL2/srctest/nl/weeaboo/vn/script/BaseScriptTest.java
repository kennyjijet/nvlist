package nl.weeaboo.vn.script;

import nl.weeaboo.filesystem.IFileSystem;
import nl.weeaboo.vn.TestUtil;
import nl.weeaboo.vn.script.impl.LuaScriptContext;
import nl.weeaboo.vn.script.impl.LuaScriptFunctionStub;
import nl.weeaboo.vn.script.impl.LuaScriptLoader;
import nl.weeaboo.vn.script.impl.ScriptEnv;

import org.junit.Assert;
import org.junit.Test;

public class BaseScriptTest {

    @Test
    public void createEnv() {
        IFileSystem fileSystem = TestUtil.newReadOnlyFileSystem();

        LuaScriptLoader scriptLoader = new LuaScriptLoader(fileSystem);
        new ScriptEnv(scriptLoader);
    }

    @Test
    public void threads() throws ScriptException {
        LuaTestUtil.newRunState();

        LuaScriptContext scriptContext = new LuaScriptContext();

        LuaScriptFunctionStub function = new LuaScriptFunctionStub();
        IScriptThread thread = scriptContext.newThread(function);
        thread.update();

        Assert.assertEquals(1, function.getCallCount());
    }

}
