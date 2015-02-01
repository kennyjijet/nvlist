package nl.weeaboo.vn.script.lua;

import org.luaj.vm2.LuaClosure;
import org.luaj.vm2.LuaValue;

import nl.weeaboo.lua2.LuaException;
import nl.weeaboo.lua2.link.LuaLink;
import nl.weeaboo.vn.script.IScriptThread;
import nl.weeaboo.vn.script.ScriptException;

public class LuaScriptThread implements IScriptThread {

    private static final long serialVersionUID = LuaImpl.serialVersionUID;

    private final LuaLink thread;

    public LuaScriptThread(LuaLink thread) {
        this.thread = thread;
    }

    @Override
    public void destroy() {
        thread.destroy();
    }

    @Override
    public boolean isDestroyed() {
        return thread.isFinished();
    }

    @Deprecated
    @Override
    public boolean isFinished() {
        return thread.isFinished();
    }

    public void call(LuaClosure func) throws ScriptException {
        try {
            thread.call(func, LuaValue.NONE);
        } catch (LuaException e) {
            throw new ScriptException("Error in thread: " + this, e);
        }
    }

    @Override
    public void update() throws ScriptException {
        try {
            thread.update();
        } catch (LuaException e) {
            throw new ScriptException("Error in thread: " + this, e);
        }
    }

    @Override
    public boolean isRunnable() {
        return thread.isRunnable();
    }

}
