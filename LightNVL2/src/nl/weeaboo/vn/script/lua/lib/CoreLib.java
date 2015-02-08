package nl.weeaboo.vn.script.lua.lib;

import nl.weeaboo.lua2.lib.LuajavaLib;
import nl.weeaboo.vn.IContext;
import nl.weeaboo.vn.IContextManager;
import nl.weeaboo.vn.script.IScriptFunction;
import nl.weeaboo.vn.script.ScriptException;
import nl.weeaboo.vn.script.ScriptFunction;
import nl.weeaboo.vn.script.lua.LuaScriptUtil;

import org.luaj.vm2.Varargs;

public class CoreLib extends LuaLib {

    private final IContextManager contextManager;

    public CoreLib(IContextManager contextManager) {
        super(null); // Register all as global functions

        this.contextManager = contextManager;
    }

    @ScriptFunction
    public Varargs createContext(Varargs args) {
        IContext context = contextManager.createContext();
        return LuajavaLib.toUserdata(context, IContext.class);
    }

    @ScriptFunction
    public Varargs newThread(Varargs args) throws ScriptException {
        IContext context = args.touserdata(1, IContext.class);

        IScriptFunction func = LuaScriptUtil.toScriptFunction(args, 2);
        context.getScriptContext().newThread(func);

        return LuajavaLib.toUserdata(context, IContext.class);
    }

}
