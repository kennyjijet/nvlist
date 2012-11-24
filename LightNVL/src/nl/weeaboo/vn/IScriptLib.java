package nl.weeaboo.vn;

import java.io.IOException;
import java.io.InputStream;
import java.util.Collection;

public interface IScriptLib {

	// === Functions ===========================================================
	public InputStream openScriptFile(String filename) throws IOException;
	public String normalizeFilename(String filename);
	
	// === Getters =============================================================
	/**
	 * Returns the paths for all script files in the specified folder
	 */
	public Collection<String> getScriptFiles(String folder, boolean includeBuiltIn);
	
	public long getScriptModificationTime(String filename) throws IOException;
	
	// === Setters =============================================================
	
}
