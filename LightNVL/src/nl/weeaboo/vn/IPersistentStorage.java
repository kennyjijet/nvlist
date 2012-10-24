package nl.weeaboo.vn;

import java.io.IOException;

public interface IPersistentStorage extends IStorage {

	// === Functions ===========================================================
	public void load() throws IOException;
	public void save() throws IOException;
	
	// === Getters =============================================================
	
	// === Setters =============================================================
	
}
