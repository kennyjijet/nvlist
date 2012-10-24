package nl.weeaboo.vn;

import java.io.IOException;

public interface IAnalytics {

	//Functions
	public void load() throws IOException;
	public void save() throws IOException;

	public void logScriptLine(String callSite);
	public void logScriptCompile(String scriptFilename, long modificationTime);
	public void logImageLoad(String callSite, String imageFilename, long loadTimeNanos);
	public void logSoundLoad(String callSite, String soundFilename, long loadTimeNanos);
	
	//Getters

	//Setters
	
}
