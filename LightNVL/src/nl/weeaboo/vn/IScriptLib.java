package nl.weeaboo.vn;

import java.io.IOException;
import java.io.InputStream;
import java.util.List;

import nl.weeaboo.vn.parser.LVNParser;

public interface IScriptLib {

	// === Functions ===========================================================
	public InputStream openScriptFile(String filename) throws IOException;
	public String normalizeFilename(String filename);
	
	// === Getters =============================================================
	/**
	 * Returns the paths for all script files in the specified folder
	 */
	public List<String> getScriptFiles(String folder, boolean includeBuiltIn);
	
	public long getScriptModificationTime(String filename) throws IOException;
	
	public LVNParser getLVNParser();
	
	// === Setters =============================================================
	
}
