package nl.weeaboo.vn.script;

import nl.weeaboo.filesystem.IFileSystem;
import nl.weeaboo.vn.TestUtil;
import nl.weeaboo.vn.script.impl.LuaScriptLoader;
import nl.weeaboo.vn.script.impl.ScriptEnv;

import org.junit.Test;

public class BaseScriptTest {

    @Test
    public void createEnv() {
        IFileSystem fileSystem = TestUtil.newReadOnlyFileSystem();

        LuaScriptLoader scriptLoader = new LuaScriptLoader(fileSystem);
        new ScriptEnv(scriptLoader);
    }

}
