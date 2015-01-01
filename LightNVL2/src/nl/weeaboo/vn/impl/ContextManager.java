package nl.weeaboo.vn.impl;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

import nl.weeaboo.lua2.io.LuaSerializable;
import nl.weeaboo.vn.IContext;
import nl.weeaboo.vn.IContextManager;

@LuaSerializable
public class ContextManager implements IContextManager {

    private static final long serialVersionUID = BaseImpl.serialVersionUID;

    private final ContextArgs contextArgs;

    private final List<Context> contexts = new ArrayList<Context>();

    public ContextManager(ContextArgs contextArgs) {
        this.contextArgs = contextArgs.clone();
    }

    //Functions
    @Override
    public final Context createContext() {
        Context context = doCreateContext();
        register(context);
        return context;
    }

    private void register(Context context) {
        if (contexts.contains(context)) {
            return;
        }

        contexts.add(context);
    }

    protected Context doCreateContext() {
        return new Context(contextArgs);
    }

    private Context checkContains(IContext ctxt) {
        for (Context context : contexts) {
            if (context == ctxt) {
                return context;
            }
        }
        throw new IllegalArgumentException("Context (" + ctxt + ") is not contained by this contextmanager.");
    }

    //Getters
    @Override
    public Collection<Context> getContexts() {
        return Collections.unmodifiableCollection(contexts);
    }

    @Override
    public Collection<Context> getActiveContexts() {
        List<Context> active = new ArrayList<Context>();
        for (Context context : contexts) {
            if (context.isActive()) {
                active.add(context);
            }
        }
        return Collections.unmodifiableCollection(active);
    }

    @Override
    public boolean isContextActive(IContext ctxt) {
        Context context = checkContains(ctxt);
        return context.isActive();
    }

    //Setters
    @Override
    public void setContextActive(IContext ctxt, boolean active) {
        Context context = checkContains(ctxt);
        context.setActive(active);
    }

}
