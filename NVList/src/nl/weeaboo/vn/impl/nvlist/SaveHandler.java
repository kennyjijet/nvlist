package nl.weeaboo.vn.impl.nvlist;

import java.awt.Image;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.io.InputStream;
import java.io.ObjectStreamException;
import java.io.OutputStream;
import java.io.Serializable;
import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import nl.weeaboo.awt.ImageUtil;
import nl.weeaboo.common.Dim;
import nl.weeaboo.filemanager.FileManager;
import nl.weeaboo.io.ByteChunkOutputStream;
import nl.weeaboo.io.EnvironmentSerializable;
import nl.weeaboo.lua2.io.ObjectSerializer;
import nl.weeaboo.nvlist.Game;
import nl.weeaboo.vn.INotifier;
import nl.weeaboo.vn.IScreenshot;
import nl.weeaboo.vn.impl.lua.LuaSaveHandler;
import nl.weeaboo.vn.impl.lua.SaveInfo;

public class SaveHandler extends LuaSaveHandler implements Serializable {

	private static final Dim screenshotSaveSize = new Dim(224, 126);
	private static final Dim screenshotLoadSize = new Dim(224, 126);
	
	private final EnvironmentSerializable es;
	
	private static final String pathPrefix = "";
	private final FileManager fm;
	private final INotifier notifier;
	
	public SaveHandler(FileManager fm, INotifier n) {
		super(Game.VERSION);
		
		this.fm = fm;
		this.notifier = n;

		es = new EnvironmentSerializable(this);
				
		addAllowedPackages("nl.weeaboo.gl", "nl.weeaboo.gl.capture", "nl.weeaboo.gl.texture");
	}

	//Functions
	@Override
	public void delete(int slot) throws IOException {
		if (!fm.delete(getFilename(slot))) {
			if (getSaveExists(slot)) {
				throw new IOException("Deletion of slot " + slot + " failed");
			}
		}		
	}
	
	@Override
	protected byte[] encodeScreenshot(IScreenshot ss) {
		if (ss == null || ss.isCancelled() || !ss.isAvailable()) {
			return new byte[0];
		}
		
		int argb[] = ss.getPixels();
		int w = ss.getPixelsWidth();
		int h = ss.getPixelsHeight();
		if (argb == null || w <= 0 || h <= 0) {
			return new byte[0];
		}
		
		BufferedImage image = ImageUtil.createBufferedImage(w, h, argb, false);		
		if (screenshotSaveSize != null) {
			image = ImageUtil.getScaledImageProp(image,
					screenshotSaveSize.w, screenshotSaveSize.h,
					Image.SCALE_AREA_AVERAGING);
		}
		
		ByteChunkOutputStream bout = new ByteChunkOutputStream(32 << 10);
		try {
			ImageUtil.writeJPEG(bout, image, 0.95f);
			
			//ImageUtil.getPixels(image, argb, 0, image.getWidth());
			//TGAUtil.writeTGA(bout, argb, image.getWidth(), image.getHeight(), false);
	        
			return bout.toByteArray();
		} catch (IOException ioe) {
			notifier.w("Error while encoding screenshot", ioe);
			return new byte[0];
		}
	}
	
	private Object writeReplace() throws ObjectStreamException {	
		return es.writeReplace();
	}
	
	//Getters
	protected String getFilename(int slot) {
		return String.format("%ssave-%03d.sav", pathPrefix, slot);		
	}
	
	protected int getSlot(String filename) {
		if (filename.endsWith(".sav")) {
			int index = filename.lastIndexOf('-');
			String part = filename.substring(index+1, filename.length()-4);
			try {
				return Integer.parseInt(part);
			} catch (NumberFormatException nfe) {
				//Ignore
			}
		}
		return 0;
	}
	
	@Override
	public boolean getSaveExists(int slot) {
		return fm.getFileExists(getFilename(slot));
	}

	@Override
	public SaveInfo[] getSaves(int start, int end) {
		List<SaveInfo> result = new ArrayList<SaveInfo>();
		try {
			//for (String filename : fm.getFolderContents(pathPrefix, false)) {
			//    int slot = getSlot(filename);
			
			if (end - (long)start > 1000L) {
				start = Math.max(-1000, start);
				end = Math.min(1000, end);
			}
			
			for (int slot = start; slot < end; slot++) {
				String filename = getFilename(slot);
				if (fm.getFileExists(filename)) {
					try {
						result.add(loadSaveInfo(slot));
					} catch (IOException e) {
						notifier.v("Unreadable save slot: " + filename, e);
						delete(slot);
					}
				}
			}
		} catch (IOException e) {
			//Ignore
		}
		return result.toArray(new SaveInfo[result.size()]);
	}
	
	@Override
	protected IScreenshot decodeScreenshot(ByteBuffer b) {
		return new ImageDecodingScreenshot(b, screenshotLoadSize.w, screenshotLoadSize.h);
	}

	@Override
	protected InputStream openSaveInputStream(int slot) throws IOException {
		return fm.getInputStream(getFilename(slot));
	}

	@Override
	protected OutputStream openSaveOutputStream(int slot) throws IOException {
		return fm.getOutputStream(getFilename(slot));
	}
	
	@Override
	protected void onSaveWarnings(String[] warnings) {
		notifier.w(ObjectSerializer.toErrorString(Arrays.asList(warnings)));
	}
	
}
