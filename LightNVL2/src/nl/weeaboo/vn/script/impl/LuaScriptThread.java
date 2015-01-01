package nl.weeaboo.vn.script.impl;

import nl.weeaboo.lua2.LuaException;
import nl.weeaboo.lua2.link.LuaLink;
import nl.weeaboo.vn.script.IScriptThread;
import nl.weeaboo.vn.script.ScriptException;

class LuaScriptThread implements IScriptThread {

    private static final long serialVersionUID = ScriptImpl.serialVersionUID;

    private final LuaLink thread;

    private boolean destroyed;

    public LuaScriptThread(LuaLink thread) {
        this.thread = thread;
    }

    @Override
    public void destroy() {
        destroyed = true;

        thread.destroy();
    }

    @Override
    public boolean isDestroyed() {
        return destroyed;
    }

    @Override
    public void update() throws ScriptException {
        try {
            thread.update();
        } catch (LuaException e) {
            throw new ScriptException("Error in thread " + thread, e);
        }
    }

    @Override
    public boolean isAlive() {
        return !thread.isFinished();
    }

}
