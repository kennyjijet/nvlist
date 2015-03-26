package nl.weeaboo.vn.core.impl;

import nl.weeaboo.vn.IEnvironment;
import nl.weeaboo.vn.NovelPrefs;

abstract class AbstractEnvironment implements IEnvironment {

    @Override
    public boolean isDebug() {
        return getPreference(NovelPrefs.SCRIPT_DEBUG);
    }

}
