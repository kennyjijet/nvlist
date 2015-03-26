package nl.weeaboo.vn.save.impl;

import java.io.FileNotFoundException;
import java.io.IOException;

import nl.weeaboo.common.Checks;
import nl.weeaboo.common.StringUtil;
import nl.weeaboo.filesystem.IFileSystem;
import nl.weeaboo.filesystem.SecureFileWriter;
import nl.weeaboo.vn.IEnvironment;
import nl.weeaboo.vn.IProgressListener;
import nl.weeaboo.vn.core.impl.Storage;
import nl.weeaboo.vn.image.IScreenshot;
import nl.weeaboo.vn.save.ISaveModule;
import nl.weeaboo.vn.save.IStorage;
import nl.weeaboo.vn.save.SaveParams;
import nl.weeaboo.vn.save.StorageIO;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class SaveModule implements ISaveModule {

    private static final Logger LOG = LoggerFactory.getLogger(SaveModule.class);
    private static final String SHARED_GLOBALS_PATH = "save-shared.bin";
    private static final int QUICK_SAVE_OFFSET = 800;
    private static final int AUTO_SAVE_OFFSET = 900;

    private final IEnvironment env;
    private final IStorage globals;
    private final IStorage sharedGlobals;

    public SaveModule(IEnvironment env) {
        this.env = Checks.checkNotNull(env);

        globals = new Storage();
        sharedGlobals = new Storage();
    }

    protected SecureFileWriter getSecureFileWriter() {
        return new SecureFileWriter(env.getFileSystem());
    }

    @Override
    public void loadPersistent() {
        try {
// TODO LVN-017
//            if (isVNDS()) {
//                sharedGlobals.set(VndsUtil.readVndsGlobalSav(fs));
//            }
            loadSharedGlobals();
        } catch (FileNotFoundException fnfe) {
            // Shared globals don't exist yet, not an error
        } catch (IOException ioe) {
            LOG.error("Error loading shared globals", ioe);
        }
    }

    protected void loadSharedGlobals() throws IOException {
        SecureFileWriter sfw = getSecureFileWriter();
        IStorage read = StorageIO.read(sfw, SHARED_GLOBALS_PATH);

        sharedGlobals.clear();
        sharedGlobals.addAll(read);
    }

    @Override
    public void savePersistent() {
        try {
            saveSharedGlobals();
        } catch (IOException e) {
            LOG.error("Unable to save shared globals", e);
        }
        generatePreloaderData();
    }

    protected void saveSharedGlobals() throws IOException {
        SecureFileWriter sfw = getSecureFileWriter();
        StorageIO.write(sharedGlobals, sfw, SHARED_GLOBALS_PATH);
    }

    protected void generatePreloaderData() {
// TODO LVN-011 Re-enable analytics
//      IAnalytics an = novel.getAnalytics();
//      if (an instanceof BaseLoggingAnalytics) {
//          BaseLoggingAnalytics ba = (BaseLoggingAnalytics)an;
//          try {
//              ba.optimizeLog(true);
//          } catch (IOException ioe) {
//              GameLog.w("Error dumping analytics", ioe);
//          }
//      }
    }

    @Override
    public IStorage getGlobals() {
        return globals;
    }

    @Override
    public IStorage getSharedGlobals() {
        return sharedGlobals;
    }

    @Override
    public void delete(int slot) throws IOException {
        try {
            IFileSystem fs = env.getFileSystem();
            fs.delete(getSaveFilename(slot));
        } catch (FileNotFoundException fnfe) {
            //Ignore
        }

        if (getSaveExists(slot)) {
            throw new IOException("Deletion of slot " + slot + " failed");
        }
    }

    @Override
    public void load(int slot, IProgressListener pl) throws IOException {
        // TODO LVN-018
    }

    @Override
    public void save(int slot, SaveParams params, IProgressListener pl) throws IOException {
        // TODO LVN-018
    }

    @Override
    public SaveFileHeader readSaveHeader(int slot) throws IOException {
        // TODO LVN-018
        return null;
    }

    @Override
    public IScreenshot readSaveThumbnail(int slot) throws IOException {
        // TODO LVN-018
        return null;
    }

    protected String getSaveFilename(int slot) {
        return String.format(StringUtil.LOCALE, "save-%03d.sav",  slot);
    }

    @Override
    public int getNextFreeSlot() {
        int slot = 1;
        while (getSaveExists(slot)) {
            slot++;
        }
        return slot;
    }

    @Override
    public boolean getSaveExists(int slot) {
        IFileSystem fs = env.getFileSystem();
        return fs.getFileExists(getSaveFilename(slot));
    }

    @Override
    public int getQuickSaveSlot(int slot) {
        int s = QUICK_SAVE_OFFSET + slot;
        if (!isQuickSaveSlot(s)) {
            throw new IllegalArgumentException("Slot outside valid range: " + slot);
        }
        return s;
    }

    static boolean isQuickSaveSlot(int slot) {
        return slot > QUICK_SAVE_OFFSET && slot < QUICK_SAVE_OFFSET + 100;
    }

    @Override
    public int getAutoSaveSlot(int slot) {
        int s = AUTO_SAVE_OFFSET + slot;
        if (!isAutoSaveSlot(s)) throw new IllegalArgumentException("Slot outside valid range: " + slot);
        return s;
    }

    static boolean isAutoSaveSlot(int slot) {
        return slot > AUTO_SAVE_OFFSET && slot < AUTO_SAVE_OFFSET + 100;
    }

}
