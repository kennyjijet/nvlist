package nl.weeaboo.vn.script.lua.lib;

import nl.weeaboo.lua2.lib.LuajavaLib;
import nl.weeaboo.vn.IContext;
import nl.weeaboo.vn.IContextManager;
import nl.weeaboo.vn.script.ScriptFunction;

import org.luaj.vm2.Varargs;

public class CoreLib extends LuaLib {

    private final IContextManager contextManager;

    public CoreLib(IContextManager contextManager) {
        super("Core");

        this.contextManager = contextManager;
    }

    @ScriptFunction
    public Varargs createContext(Varargs args) {
        IContext context = contextManager.createContext();
        return LuajavaLib.toUserdata(context, IContext.class);
    }

}
