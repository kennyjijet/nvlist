package nl.weeaboo.vn.script;

import java.io.Serializable;
import java.util.Collection;

/**
 * Context-specific part of a script environment. A context includes a set of threads and a way of storing
 * context-specific data.
 */
public interface IScriptContext extends Serializable {

    public IScriptThread newThread(IScriptFunction func) throws ScriptException;

	public Collection<IScriptThread> getThreads();

}
