package nl.weeaboo.vn;

import nl.weeaboo.settings.IConfig;
import nl.weeaboo.vn.ISystemEventHandler;
import nl.weeaboo.vn.script.ScriptException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class TestSystemEventHandler implements ISystemEventHandler {

    private static final Logger LOG = LoggerFactory.getLogger(TestSystemEventHandler.class);

    @Override
    public void onExit() throws ScriptException {
        LOG.info("SystemEventHandler.onExit()");
    }

    @Override
    public void onPrefsChanged(IConfig config) {
        LOG.info("SystemEventHandler.onPrefsChanged()");
    }

}
