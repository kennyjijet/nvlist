package nl.weeaboo.vn;

import java.io.Serializable;
import java.util.Collection;

/** Manages the active {@link IContext} and context lifetimes. */
public interface IContextManager extends Serializable {

    public IContext createContext();

    public Collection<? extends IContext> getContexts();

    public Collection<? extends IContext> getActiveContexts();

    public boolean isContextActive(IContext context);

    public void setContextActive(IContext context, boolean active);

}
