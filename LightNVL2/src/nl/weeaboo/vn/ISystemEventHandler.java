package nl.weeaboo.vn;

import nl.weeaboo.vn.script.ScriptException;

/** Interface for handling events triggered from outside {@link INovel}. */
public interface ISystemEventHandler {

    public void onExit() throws ScriptException;

}
