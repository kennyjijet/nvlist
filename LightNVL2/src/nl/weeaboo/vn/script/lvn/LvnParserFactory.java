package nl.weeaboo.vn.script.lvn;

import nl.weeaboo.common.StringUtil;

public final class LvnParserFactory {

    public static ILvnParser getParser(String engineVersion) {
        if (StringUtil.compareVersion(engineVersion, "4.0") < 0) {
            return new LvnParser3();
        }
        return new LvnParser4();
    }

}
