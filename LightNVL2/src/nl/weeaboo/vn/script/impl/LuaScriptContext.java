package nl.weeaboo.vn.script.impl;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;

import nl.weeaboo.lua2.io.LuaSerializable;
import nl.weeaboo.vn.script.IScriptContext;
import nl.weeaboo.vn.script.IScriptFunction;
import nl.weeaboo.vn.script.IScriptThread;
import nl.weeaboo.vn.script.ScriptException;

@LuaSerializable
public class LuaScriptContext implements IScriptContext {

    private static final long serialVersionUID = ScriptImpl.serialVersionUID;

    private final List<IScriptThread> threads = new ArrayList<IScriptThread>();

    @Override
    public IScriptThread newThread(IScriptFunction func) throws ScriptException {
        LuaScriptFunction luaFunc = (LuaScriptFunction)func;
        LuaScriptThread thread = luaFunc.callInNewThread();
        threads.add(thread);
        return thread;
    }

    private void cleanupDeadThreads() {
        for (Iterator<IScriptThread> itr = threads.iterator(); itr.hasNext(); ) {
            IScriptThread thread = itr.next();
            if (!thread.isAlive()) {
                itr.remove();
            }
        }
    }

    @Override
    public Collection<IScriptThread> getThreads() {
        cleanupDeadThreads();
        return Collections.unmodifiableCollection(threads);
    }

}
