package nl.weeaboo.vn;

import java.io.IOException;
import java.io.InputStream;

public interface IScriptLib {

	// === Functions ===========================================================
	public InputStream openScriptFile(String filename) throws IOException;
	public String normalizeFilename(String filename);
	
	// === Getters =============================================================
	public long getScriptModificationTime(String filename) throws IOException;
	
	// === Setters =============================================================
	
}
