package nl.weeaboo.vn.impl.base;

import java.io.IOException;
import java.io.Serializable;

import nl.weeaboo.lua2.io.LuaSerializable;
import nl.weeaboo.vn.IAnalytics;

@LuaSerializable
public class NullAnalytics implements IAnalytics, Serializable {

	private static final long serialVersionUID = 1L;

	//Functions
	@Override
	public void load() throws IOException {
	}

	@Override
	public void save() throws IOException {
	}

	@Override
	public void logScriptLine(String callSite) {
	}
	
	@Override
	public void logScriptCompile(String scriptFilename, long modificationTime) {
	}
	
	@Override
	public void logImageLoad(String callSite, String imageFilename, long loadTimeNanos) {
	}

	@Override
	public void logSoundLoad(String callSite, String soundFilename, long loadTimeNanos) {
	}
	
	//Getters
	
	//Setters
	
}
