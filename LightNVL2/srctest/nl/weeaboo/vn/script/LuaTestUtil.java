package nl.weeaboo.vn.script;

import nl.weeaboo.lua2.LuaRunState;

public final class LuaTestUtil {

    private LuaTestUtil() {
    }

    public static LuaRunState newRunState() {
        LuaRunState runState = new LuaRunState();
        runState.registerOnThread();
        return runState;
    }

}
