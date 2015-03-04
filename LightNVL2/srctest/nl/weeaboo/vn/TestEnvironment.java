package nl.weeaboo.vn;

import nl.weeaboo.common.Checks;
import nl.weeaboo.lua2.LuaRunState;
import nl.weeaboo.vn.impl.DefaultEnvironment;
import nl.weeaboo.vn.impl.ContextManager;
import nl.weeaboo.vn.impl.EnvironmentBuilder;
import nl.weeaboo.vn.script.IScriptLoader;
import nl.weeaboo.vn.script.lua.LuaScriptEnv;
import nl.weeaboo.vn.script.lua.LuaScriptLoader;
import nl.weeaboo.vn.script.lua.LuaTestUtil;

public class TestEnvironment extends DefaultEnvironment {

    public final LuaScriptEnv scriptEnv;
    public final IScriptLoader scriptLoader;

    private TestEnvironment(EnvironmentBuilder b, LuaScriptEnv scriptEnv) {
        super(b);

        Checks.checkArgument(b.contextManager instanceof ContextManager,
                "ContextManager must be an instance of " + ContextManager.class.getName());

        this.scriptEnv = scriptEnv;
        this.scriptLoader = scriptEnv.getScriptLoader();
    }

    public static TestEnvironment newInstance() {
        EnvironmentBuilder b = new EnvironmentBuilder();

        b.partRegistry = new BasicPartRegistry();
        b.notifier = new TestNotifier();
        b.renderEnv = TestUtil.BASIC_ENV;

        LuaRunState runState = LuaTestUtil.newRunState();
        LuaScriptLoader scriptLoader = LuaTestUtil.newScriptLoader(TestFileSystem.newInstance());

        LuaScriptEnv scriptEnv = new LuaScriptEnv(runState, scriptLoader);

        TestContextBuilder contextBuilder = new TestContextBuilder(scriptEnv);
        b.contextManager = new ContextManager(contextBuilder);

        return new TestEnvironment(b, scriptEnv);
    }

    @Override
    public void destroy() {
        if (!isDestroyed()) {
            super.destroy();

            scriptEnv.getRunState().destroy();
        }
    }

    @Override
    public ContextManager getContextManager() {
        return (ContextManager)super.getContextManager();
    }

}
