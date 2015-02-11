package nl.weeaboo.vn.script.lua;

import org.luaj.vm2.LuaClosure;
import org.luaj.vm2.LuaValue;

import nl.weeaboo.lua2.LuaException;
import nl.weeaboo.lua2.link.LuaLink;
import nl.weeaboo.vn.script.IScriptContext;
import nl.weeaboo.vn.script.IScriptThread;
import nl.weeaboo.vn.script.ScriptException;

public class LuaScriptThread implements IScriptThread {

    private static final long serialVersionUID = LuaImpl.serialVersionUID;

    private final IScriptContext scriptContext;
    private final LuaLink thread;

    LuaScriptThread(IScriptContext scriptContext, LuaLink thread) {
        this.scriptContext = scriptContext;
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
        IScriptContext oldContext = LuaScriptUtil.setScriptContext(scriptContext);
        try {
            thread.call(func, LuaValue.NONE);
        } catch (LuaException e) {
            throw LuaScriptUtil.toScriptException("Error in thread: " + this, e);
        } finally {
            LuaScriptUtil.setScriptContext(oldContext);
        }
    }

    @Override
    public void update() throws ScriptException {
        IScriptContext oldContext = LuaScriptUtil.setScriptContext(scriptContext);
        try {
            thread.update();
        } catch (LuaException e) {
            throw LuaScriptUtil.toScriptException("Error in thread: " + this, e);
        } finally {
            LuaScriptUtil.setScriptContext(oldContext);
        }
    }

    @Override
    public boolean isRunnable() {
        return thread.isRunnable();
    }

}
