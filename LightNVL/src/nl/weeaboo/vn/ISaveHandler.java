package nl.weeaboo.vn;

import java.io.IOException;

import nl.weeaboo.common.Dim;

public interface ISaveHandler {

	public void delete(int slot) throws IOException;
	public void save(int slot, IScreenshot ss, Dim screenshotEncodeSize, IStorage meta, IProgressListener pl) throws IOException;
	public void load(int slot, IProgressListener pl) throws IOException;
	public ISaveInfo loadSaveInfo(int slot) throws IOException;
	
	public int getNextFreeSlot();
	public boolean getSaveExists(int slot);
	
	/**
	 * Reads and returns all save file headers from start to end (exclusive).
	 */
	public ISaveInfo[] getSaves(int start, int end);
	
	/**
	 * Returns a storage object that can be used by the application to provide a
	 * manual fallback save for when the main save chunk is broken or missing.
	 */
	public IStorage getSavepointStorage();

	/**
	 * @param slot The quick save slot index between <code>1 and 99</code>.
	 * @return The general purpose save slot index.
	 */
	public int getQuickSaveSlot(int slot);

	/**
	 * @param slot The autosave slot index between <code>1 and 99</code>.
	 * @return The general purpose save slot index.
	 */
	public int getAutoSaveSlot(int slot);
	
}
