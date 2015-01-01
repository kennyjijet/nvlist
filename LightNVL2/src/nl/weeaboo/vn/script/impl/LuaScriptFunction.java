package nl.weeaboo.vn.script.impl;

import nl.weeaboo.lua2.LuaRunState;
import nl.weeaboo.lua2.link.LuaFunctionLink;
import nl.weeaboo.vn.script.IScriptFunction;
import nl.weeaboo.vn.script.ScriptException;

import org.luaj.vm2.LuaClosure;
import org.luaj.vm2.LuaThread;
import org.luaj.vm2.Varargs;

class LuaScriptFunction implements IScriptFunction {

    private static final long serialVersionUID = ScriptImpl.serialVersionUID;

    private final LuaClosure func;
    private final Varargs args;

    public LuaScriptFunction(LuaClosure func, Varargs args) {
        this.func = func;
        this.args = args;
    }

    @Override
    public void call() throws ScriptException {
        LuaRunState runState = ScriptImpl.getRunState();
        LuaThread currentThread = runState.getRunningThread();
        if (currentThread == null) {
            throw new ScriptException("Unable to call Lua function -- no thread is current");
        }
    }

    LuaScriptThread callInNewThread() throws ScriptException {
        LuaRunState runState = ScriptImpl.getRunState();
        LuaFunctionLink link = new LuaFunctionLink(runState, func, args);
        return new LuaScriptThread(link);
    }

}
