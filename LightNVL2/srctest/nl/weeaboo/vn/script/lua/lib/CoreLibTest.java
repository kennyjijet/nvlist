package nl.weeaboo.vn.script.lua.lib;

import java.io.IOException;

import nl.weeaboo.vn.IContext;
import nl.weeaboo.vn.script.ScriptException;
import nl.weeaboo.vn.script.lua.LuaScriptEnv;
import nl.weeaboo.vn.script.lua.LuaTestUtil;

import org.junit.Assert;
import org.junit.Test;

public class CoreLibTest extends AbstractLibTest {

    @Override
    protected void addInitializers(LuaScriptEnv scriptEnv) {
        scriptEnv.addInitializer(new CoreLib(contextManager));
    }

    @Test
    public void createContext() throws IOException, ScriptException {
        loadScript(LuaTestUtil.SCRIPT_SCRIPTLIB);

        IContext createdContext = LuaTestUtil.getGlobal("context", IContext.class);
        Assert.assertNotNull(createdContext);
        Assert.assertEquals(2, contextManager.getContexts().size());
    }

}
