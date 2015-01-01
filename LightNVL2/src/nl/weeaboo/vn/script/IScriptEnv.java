package nl.weeaboo.vn.script;

import java.io.Serializable;

/**
 * Container for scripting-related state.
 */
public interface IScriptEnv extends Serializable {

	public IScriptEventDispatcher getEventDispatcher();

	public IScriptLoader getScriptLoader();

}
