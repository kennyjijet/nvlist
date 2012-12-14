package nl.weeaboo.vn.impl.lua;

import static nl.weeaboo.vn.impl.lua.SaveChunk.createId;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.OutputStream;
import java.io.Serializable;
import java.nio.ByteBuffer;
import java.util.HashSet;
import java.util.Set;

import nl.weeaboo.common.Dim;
import nl.weeaboo.io.ByteChunkOutputStream;
import nl.weeaboo.io.StreamUtil;
import nl.weeaboo.lua2.LuaException;
import nl.weeaboo.lua2.io.LuaSerializer;
import nl.weeaboo.lua2.io.ObjectDeserializer;
import nl.weeaboo.lua2.io.ObjectSerializer;
import nl.weeaboo.lua2.io.ObjectSerializer.PackageLimit;
import nl.weeaboo.settings.IConfig;
import nl.weeaboo.vn.INotifier;
import nl.weeaboo.vn.IProgressListener;
import nl.weeaboo.vn.ISaveHandler;
import nl.weeaboo.vn.IScreenshot;
import nl.weeaboo.vn.IStorage;
import nl.weeaboo.vn.impl.base.ProgressInputStream;
import nl.weeaboo.vn.impl.base.ProgressOutputStream;

public abstract class LuaSaveHandler implements ISaveHandler, Serializable {

	private static final long serialVersionUID = LuaImpl.serialVersionUID;
	
	static final int QUICK_SAVE_OFFSET = 800;
	static final int AUTO_SAVE_OFFSET = 900;

	protected static final long FILE_ID                  = createId('N','V','L','S','A','V','E',' ');
	protected static final int  FILE_VERSION             = 1;
	protected static final long CHUNK_META_DATA_ID       = createId('m','E','T','A','D','A','T','A');
	protected static final int  CHUNK_META_DATA_VERSION  = 1;
	protected static final long CHUNK_SCREENSHOT_ID      = createId('t','H','M','B','N','A','I','L');
	protected static final int  CHUNK_SCREENSHOT_VERSION = 1;
	protected static final long CHUNK_DATA_ID            = createId('D','A','T','A','S','N','A','P');
	protected static final long CHUNK_SAVEPOINT_ID       = createId('d','A','T','A','S','A','V','P');
	protected static final int  CHUNK_SAVEPOINT_VERSION  = 1;
		
	private final int dataVersion;
	private final IStorage savepoint;
	private LuaNovel novel;
	private LuaSerializer luaSerializer;
	private PackageLimit packageLimit;

	private String allowedPackages[] = { "nl.weeaboo.common", "nl.weeaboo.collections", "nl.weeaboo.styledtext" };

	protected Set<Long> chunkSaveIgnoreList;
	
	protected LuaSaveHandler(int dataVersion) {
		this.dataVersion = dataVersion;
		this.savepoint = new SavepointStorage();
		this.chunkSaveIgnoreList = new HashSet<Long>();
		this.packageLimit = PackageLimit.WARNING;
	}
	
	//Functions
	protected void addAllowedPackages(String... pkgs) {
		String result[] = new String[allowedPackages.length + pkgs.length];
		System.arraycopy(allowedPackages, 0, result, 0, allowedPackages.length);
		System.arraycopy(pkgs, 0, result, allowedPackages.length, pkgs.length);
		allowedPackages = result;
	}
		
	protected boolean tryDelete(int slot) {
		try {
			delete(slot);
		} catch (Exception e) {
			//Ignore
		}
		return !getSaveExists(slot);
	}
	
	private void checkNovelSet() {
		if (novel == null || luaSerializer == null) {
			throw new IllegalStateException("Must call setNovel() first");
		}
	}
	
	@Override
	public final void save(int slot, IScreenshot ss, Dim ssMaxSize, IStorage metaData, IProgressListener pl)
		throws IOException
	{
		checkNovelSet();
		
		try {
			novel.eval("onSave("+slot+")");
		} catch (LuaException e) {
			IOException ioe = new IOException("Error calling Lua save hook :: " + e);
			ioe.setStackTrace(e.getStackTrace());
			throw ioe;
		}
		novel.savePersistent();
		
		OutputStream raw = openSaveOutputStream(slot);
		try {
			raw = new BufferedOutputStream(raw, 16<<10);
			DataOutputStream dout = new DataOutputStream(raw);

			writeFileHeader(dout);
			if (!chunkSaveIgnoreList.contains(CHUNK_META_DATA_ID)) {
				if (metaData != null) {
					writeMetaDataChunk(dout, metaData);
				}
			}
			if (!chunkSaveIgnoreList.contains(CHUNK_SCREENSHOT_ID)) {
				writeScreenshotChunk(dout, ss, ssMaxSize);
			}
			if (!chunkSaveIgnoreList.contains(CHUNK_DATA_ID)) {
				writeDataChunk(dout, pl);
			}
			if (!chunkSaveIgnoreList.contains(CHUNK_SAVEPOINT_ID)) {
				writeSavepointChunk(dout);
			}
			
			onSaveSuccessful(slot);
		} catch (Throwable t) {
			// Yes, I know I'm catching throwable. Sometimes the loading can
			// cause stack overflows, and I really want to log any errors.
			IOException ioe = new IOException("Error saving :: " + t);
			ioe.setStackTrace(t.getStackTrace());
			throw ioe;
		} finally {
			raw.close();
		}
	}
	
	private void writeFileHeader(DataOutputStream dout) throws IOException {
		dout.writeLong(FILE_ID);
		dout.writeInt(FILE_VERSION);
		dout.writeLong(System.currentTimeMillis());
	}
	
	private void writeMetaDataChunk(DataOutputStream dout, IStorage metaData) throws IOException {
		byte[] b = serializeStorageObject(metaData);
		SaveChunk.write(dout, CHUNK_META_DATA_ID, CHUNK_META_DATA_VERSION, false, b, 0, b.length);
	}
	private void writeScreenshotChunk(DataOutputStream dout, IScreenshot ss, Dim maxSize) throws IOException {
		writeScreenshotChunk(dout, encodeScreenshot(ss, maxSize));
	}
	private void writeScreenshotChunk(DataOutputStream dout, byte[] b) throws IOException {
		SaveChunk.write(dout, CHUNK_SCREENSHOT_ID, CHUNK_SCREENSHOT_VERSION, false, b, 0, b.length);		
	}	
	private void writeDataChunk(DataOutputStream dout, IProgressListener pl) throws IOException {
		ByteChunkOutputStream bco = new ByteChunkOutputStream();
		
		OutputStream raw = bco;
		if (pl != null) {
			raw = new ProgressOutputStream(raw, 4096, pl);			
		}
		
		ObjectSerializer os = luaSerializer.openSerializer(raw);		
		try {
			os.setPackageLimit(packageLimit);
			os.setAllowedPackages(allowedPackages);
			novel.writeAttributes(os);
			os.flush();
			
			String warnings[] = os.checkErrors();
			if (warnings != null && warnings.length > 0) {
				onSaveWarnings(warnings);
			}
		} finally {
			os.close();
		}
		
		byte[] b = bco.toByteArray();
		SaveChunk.write(dout, CHUNK_DATA_ID, dataVersion, true, b, 0, b.length);
	}
	private void writeSavepointChunk(DataOutputStream dout) throws IOException {
		byte[] b = serializeStorageObject(savepoint);
		SaveChunk.write(dout, CHUNK_SAVEPOINT_ID, CHUNK_SAVEPOINT_VERSION, false, b, 0, b.length);		
	}
	
	private static byte[] serializeStorageObject(IStorage storage) throws IOException {
		ByteChunkOutputStream bout = new ByteChunkOutputStream();
		ObjectOutputStream oout = new ObjectOutputStream(bout);
		try {
			storage.save(oout);
		} finally {
			oout.flush();
			oout.close();
		}				
		return bout.toByteArray();		
	}

	@Override
	public final void load(int slot, IProgressListener pl) throws IOException {
		LuaSaveInfo info = newSaveInfo(slot);
		load(info, false, true, pl);		
	}
	
	private void load(LuaSaveInfo info, boolean loadScreenshot, boolean loadData,
			IProgressListener pl) throws IOException
	{
		checkNovelSet();
		
		IConfig prefs = novel.getPrefs();
		if (loadData) {
			savepoint.clear();
		}

		int slot = info.getSlot();
		InputStream raw = openSaveInputStream(slot);						
		try {
			raw = new BufferedInputStream(raw, 16<<10);
			DataInputStream din = new DataInputStream(raw);
			
			readFileHeader(din, info);
			
			boolean dataChunkRead = false;
			Throwable dataChunkException = null;
			
			SaveChunk chunk;
			while ((chunk = SaveChunk.read(din)) != null) {
				final long cid = chunk.getId();
				if (cid == CHUNK_DATA_ID && loadData) {
					try {
						readDataChunk(chunk, pl);
						if (loadData) {
							novel.onPrefsChanged(prefs);
							novel.update();
							onLoadSuccessful(info);
						}
						dataChunkRead = true;
					} catch (Throwable t) {
						dataChunkException = t;
					}
				} else if (cid == CHUNK_META_DATA_ID) {
					readMetaDataChunk(chunk, info);
				} else if (cid == CHUNK_SCREENSHOT_ID && loadScreenshot) {
					readScreenshotChunk(chunk, info);
				} else if (cid == CHUNK_SAVEPOINT_ID && loadData) {
					readSavepointChunk(chunk);
				}
			}
			
			raw.close();
			raw = null;
			
			if (loadData && !dataChunkRead) {
				try {
					loadFromSavepoint(savepoint, prefs, pl);
				} catch (RuntimeException e) {
					if (dataChunkException != null) {
						throw dataChunkException;
					} else {
						throw e;
					}
				}
				if (dataChunkException != null) {
					novel.getNotifier().d("Recovered from exception while loading data chunk", dataChunkException);
				} else {
					onLoadSuccessful(info);
				}
			}			
		} catch (Throwable t) {
			// Yes, I know I'm catching throwable. Sometimes the loading can
			// cause stack overflows, and I really want to log any errors.
			IOException ioe = new IOException("Error loading :: " + t);
			ioe.setStackTrace(t.getStackTrace());
			throw ioe;
		} finally {
			if (raw != null) raw.close();
		}
	}
		
	@Override
	public LuaSaveInfo loadSaveInfo(int slot) throws IOException {
		LuaSaveInfo info = newSaveInfo(slot);
		load(info, true, false, null);
		return info;
	}
	
	protected abstract LuaSaveInfo newSaveInfo(int slot);
	
	protected void onSaveSuccessful(int slot) {
		INotifier ntf = novel.getNotifier();
		if (!isAutoSaveSlot(slot)) {
			if (isQuickSaveSlot(slot)) {
				ntf.message("Quicksave successful");
			} else {
				ntf.message("Save successful");
			}
		}
	}
	
	protected void onLoadSuccessful(LuaSaveInfo info) {
		INotifier ntf = novel.getNotifier();
		ntf.message(String.format("Save slot loaded (%s)", info.getTitle()));
	}
	
	private void readFileHeader(DataInputStream din, LuaSaveInfo info) throws IOException {
		long fileId = din.readLong();
		if (fileId != FILE_ID) throw new IOException("Invalid fileId: " + fileId + ", expected: " + FILE_ID); 
		
		int fileVersion = din.readInt();
		if (fileVersion != FILE_VERSION) throw new IOException("Invalid fileVersion: " + fileId + ", expected: " + FILE_ID); 

		info.timestamp = din.readLong();
	}
	private void readScreenshotChunk(SaveChunk chunk, LuaSaveInfo info) throws IOException {
		SaveChunk.checkId(chunk, CHUNK_SCREENSHOT_ID);
		SaveChunk.checkVersion(chunk, CHUNK_SCREENSHOT_VERSION);

		InputStream in = chunk.getData();
		try {			
			info.screenshotBytes = ByteBuffer.wrap(StreamUtil.readFully(in));
		} finally {
			in.close();
		}
	}
	private void readDataChunk(SaveChunk chunk, IProgressListener pl) throws IOException, ClassNotFoundException {
		SaveChunk.checkId(chunk, CHUNK_DATA_ID);
		SaveChunk.checkVersion(chunk, dataVersion);
				
		InputStream in = chunk.getData();
		if (pl != null) {
			in = new ProgressInputStream(in, 2048, chunk.getDataLength(), pl);
		}
		
		try {
			ObjectDeserializer is = luaSerializer.openDeserializer(in);
			try {
				novel.reset();
				novel.readAttributes(is);
			} finally {
				is.close();
			}
		} finally {
			in.close();
		}
	}
	private void readMetaDataChunk(SaveChunk chunk, LuaSaveInfo info) throws IOException, ClassNotFoundException {
		SaveChunk.checkId(chunk, CHUNK_META_DATA_ID);
		SaveChunk.checkVersion(chunk, CHUNK_META_DATA_VERSION);
		
		deserializeStorageObject(info.metaData, chunk);
	}
	private void readSavepointChunk(SaveChunk chunk) throws IOException, ClassNotFoundException {
		SaveChunk.checkId(chunk, CHUNK_SAVEPOINT_ID);
		SaveChunk.checkVersion(chunk, CHUNK_SAVEPOINT_VERSION);

		deserializeStorageObject(savepoint, chunk);
	}
	private static void deserializeStorageObject(IStorage storage, SaveChunk chunk) throws IOException, ClassNotFoundException {
		ObjectInputStream oin = new ObjectInputStream(chunk.getData());
		try {
			storage.clear();
			storage.load(oin);
		} finally {
			oin.close();
		}		
	}
	
	protected void loadFromSavepoint(IStorage savepoint, IConfig prefs, IProgressListener pl) {
		if (pl != null) pl.onProgressChanged(.1f);
		novel.restart(luaSerializer, prefs, "onLoad");
		if (pl != null) pl.onProgressChanged(1.0f);
	}
			
	/**
	 * @param ss The screenshot object to serialize.
	 * @param maxSize Size hint for rescaling the screenshot prior to saving (to
	 *        prevent memory use getting out of hand).
	 */
	protected abstract byte[] encodeScreenshot(IScreenshot ss, Dim maxSize);
	
	protected abstract InputStream openSaveInputStream(int slot) throws IOException;
	
	protected abstract OutputStream openSaveOutputStream(int slot) throws IOException;
	
	protected abstract void onSaveWarnings(String warnings[]);
	
	//Getters
	@Override
	public IStorage getSavepointStorage() {
		return savepoint;
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
	public int getQuickSaveSlot(int slot) {
		int s = QUICK_SAVE_OFFSET + slot;
		if (!isQuickSaveSlot(s)) throw new IllegalArgumentException("Slot outside valid range: " + slot);		
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
	
	protected PackageLimit getPackageLimit() {
		return packageLimit;
	}
		
	//Setters
	public void setNovel(LuaNovel novel, LuaSerializer ls) {
		this.novel = novel;
		this.luaSerializer = ls;
	}	
	
	protected void setPackageLimit(PackageLimit pl) {
		if (pl == null) throw new NullPointerException();
		
		packageLimit = pl;
	}
	
}
