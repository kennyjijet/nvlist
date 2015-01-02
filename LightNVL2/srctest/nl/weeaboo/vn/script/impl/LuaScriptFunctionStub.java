package nl.weeaboo.vn.script.impl;

import nl.weeaboo.vn.script.IScriptThread;
import nl.weeaboo.vn.script.ScriptException;

import org.luaj.vm2.LuaValue;
import org.luaj.vm2.Varargs;

public class LuaScriptFunctionStub extends LuaScriptFunction {

    private static final long serialVersionUID = 1L;

    private int callCount;

    public LuaScriptFunctionStub() {
        this(LuaValue.NONE);
    }
    public LuaScriptFunctionStub(Varargs args) {
        super(null, args);
    }

    @Override
    public void call() throws ScriptException {
        callCount++;
    }

    @Override
    public IScriptThread callInNewThread() throws ScriptException {
        return new ThreadStub(this);
    }

    public int getCallCount() {
        return callCount;
    }

    private static class ThreadStub implements IScriptThread {

        private static final long serialVersionUID = 1L;

        private final LuaScriptFunctionStub function;
        private boolean destroyed;

        public ThreadStub(LuaScriptFunctionStub f) {
            function = f;
        }

        @Override
        public void destroy() {
            destroyed = true;
        }

        @Override
        public boolean isDestroyed() {
            return destroyed;
        }

        @Override
        public void update() throws ScriptException {
            function.call();
        }

        @Override
        public boolean isAlive() {
            return !destroyed && function.getCallCount() < 1;
        }

    }
}
