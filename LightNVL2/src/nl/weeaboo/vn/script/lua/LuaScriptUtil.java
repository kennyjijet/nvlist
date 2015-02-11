package nl.weeaboo.vn.script.lua;

import nl.weeaboo.lua2.LuaException;
import nl.weeaboo.vn.script.IScriptContext;
import nl.weeaboo.vn.script.ScriptException;

import org.luaj.vm2.Varargs;

public final class LuaScriptUtil {

    private static final ThreadLocal<IScriptContext> currentContext = new ThreadLocal<IScriptContext>();

    private LuaScriptUtil() {
    }

    public static IScriptContext getScriptContext() {
        return currentContext.get();
    }

    public static IScriptContext setScriptContext(IScriptContext context) {
        IScriptContext oldContext = getScriptContext();
        currentContext.set(context);
        return oldContext;
    }

    public static LuaScriptFunction toScriptFunction(Varargs args, int offset) {
        return new LuaScriptFunction(args.checkclosure(offset), args.subargs(offset+1));
    }

    public static ScriptException toScriptException(String message, LuaException e) {
        ScriptException se = new ScriptException(message + ": " + e.getMessage());
        se.setStackTrace(e.getStackTrace());
        return se;
    }

}
