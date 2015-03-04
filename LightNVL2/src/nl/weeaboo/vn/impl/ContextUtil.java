package nl.weeaboo.vn.impl;

import nl.weeaboo.vn.IContext;

public final class ContextUtil {

    private static final ThreadLocal<IContext> currentContext = new ThreadLocal<IContext>();

    private ContextUtil() {
    }

    public static IContext getCurrentContext() {
        return currentContext.get();
    }

    public static IContext setCurrentContext(IContext context) {
        IContext oldContext = getCurrentContext();
        currentContext.set(context);
        return oldContext;
    }

}
