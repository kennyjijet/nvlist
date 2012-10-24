package nl.weeaboo.nvlist.build.android;

import java.io.File;
import java.io.IOException;

import nl.weeaboo.settings.INIFile;

public class TemplateVersion implements Comparable<TemplateVersion> {

	private static final String KEY_SIZE = "size";
	private static final String KEY_MODIFIED = "modified";
	
	private long size;
	private long modifiedTime;	
	
	public TemplateVersion() {		
	}
	
	//Functions
	public void initFromTemplateFile(File file) {
		size = file.length();
		modifiedTime = file.lastModified();
	}
	
	public void load(File file) throws IOException {
		INIFile ini = new INIFile();
		ini.read(file);
		
		size = ini.getLong(KEY_SIZE, 0);
		modifiedTime = ini.getLong(KEY_MODIFIED, 0);
	}
	
	public void save(File file) throws IOException {
		INIFile ini = new INIFile();
		ini.putLong(KEY_SIZE, size);
		ini.putLong(KEY_MODIFIED, modifiedTime);
		
		ini.write(file);
	}

	@Override
	public int compareTo(TemplateVersion tv) {
		if (modifiedTime != tv.modifiedTime) {
			return Long.valueOf(modifiedTime).compareTo(tv.modifiedTime);
		}
		if (size != tv.size) {
			return Long.valueOf(size).compareTo(tv.size);
		}
		return 0;
	}
	
	//Getters
	
	//Setters
	
}
