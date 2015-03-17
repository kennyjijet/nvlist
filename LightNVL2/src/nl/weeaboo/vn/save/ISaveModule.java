package nl.weeaboo.vn.save;

import java.io.IOException;

import nl.weeaboo.vn.IProgressListener;
import nl.weeaboo.vn.IScreenshot;
import nl.weeaboo.vn.IStorage;

public interface ISaveModule {

    public void savePersistent();

    public void delete(int slot) throws IOException;

    public void save(int slot, SaveParams params, IProgressListener pl) throws IOException;

    public void load(int slot, IProgressListener pl) throws IOException;

    public int getNextFreeSlot();

    public boolean getSaveExists(int slot);

    /**
     * Reads the header of a save file.
     *
     * @param slot The save slot to load.
     * @throws IOException If the specified save file can't be read.
     * @see #getQuickSaveSlot(int)
     * @see #getAutoSaveSlot(int)
     */
    public SaveFileHeader readSaveHeader(int slot) throws IOException;

    public IScreenshot readSaveThumbnail(int slot) throws IOException;

    /**
     * @param slot The quicksave slot index in the range {@code (1, 99)}.
     * @return The general purpose save slot index.
     */
    public int getQuickSaveSlot(int slot);

    /**
     * @param slot The autosave slot index in the range {@code (1, 99)}.
     * @return The general purpose save slot index.
     */
    public int getAutoSaveSlot(int slot);

    public IStorage getGlobals();

    public IStorage getSharedGlobals();

}
