package nl.weeaboo.vn.script.lua;

import java.util.Collection;

import nl.weeaboo.lua2.link.LuaLink;
import nl.weeaboo.vn.script.IScriptContext;
import nl.weeaboo.vn.script.IScriptFunction;
import nl.weeaboo.vn.script.IScriptThread;
import nl.weeaboo.vn.script.ScriptException;
import nl.weeaboo.vn.script.ScriptLog;
import nl.weeaboo.vn.script.impl.ScriptThreadCollection;

import org.luaj.vm2.LuaClosure;
import org.luaj.vm2.LuaValue;
import org.luaj.vm2.Varargs;

public class LuaScriptContext implements IScriptContext {

    private static final long serialVersionUID = LuaImpl.serialVersionUID;

    private final LuaScriptThread mainThread;

    private final ScriptThreadCollection<LuaScriptThread> threads = new ScriptThreadCollection<LuaScriptThread>();

    public LuaScriptContext(LuaScriptEnv scriptEnv) {
        LuaLink luaLink = new LuaLink(scriptEnv.getRunState());
        luaLink.setPersistent(true);
        mainThread = new LuaScriptThread(this, luaLink);
        threads.add(mainThread);
    }

    public IScriptThread newThread(LuaClosure func) throws ScriptException {
        return newThread(func, LuaValue.NONE);
    }
    public IScriptThread newThread(LuaClosure func, Varargs args) throws ScriptException {
        return newThread(new LuaScriptFunction(func, args));
    }

    @Override
    public IScriptThread newThread(IScriptFunction func) throws ScriptException {
        LuaScriptFunction luaFunc = (LuaScriptFunction)func;

        LuaScriptThread thread = luaFunc.callInNewThread(this);
        threads.add(thread);
        return thread;
    }

    @Override
    public LuaScriptThread getMainThread() {
        return mainThread;
    }

    @Override
    public Collection<LuaScriptThread> getThreads() {
        return threads.getThreads();
    }

    @Override
    public void updateThreads() {
        for (LuaScriptThread thread : threads) {
            try {
                thread.update();
            } catch (ScriptException e) {
                ScriptLog.w("Exception while executing thread: " + thread, e);
            }
        }
    }

}
