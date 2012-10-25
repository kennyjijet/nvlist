package nl.weeaboo.vn.impl.base;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collection;

import nl.weeaboo.lua2.io.LuaSerializable;
import nl.weeaboo.vn.IDrawBuffer;
import nl.weeaboo.vn.IScreenshot;

@LuaSerializable
public class ScreenshotBuffer implements Serializable {

	private static final long serialVersionUID = BaseImpl.serialVersionUID;
	
	private Collection<ScreenshotEntry> screenshots;
	
	public ScreenshotBuffer() {
		screenshots = new ArrayList<ScreenshotEntry>();
	}
	
	//Functions
	public void add(IScreenshot ss, boolean clip) {
		screenshots.add(new ScreenshotEntry(ss, clip));
	}
	
	public void clear() {
		for (ScreenshotEntry entry : screenshots) {
			entry.screenshot.cancel();
		}
		screenshots.clear();
	}
	
	public void flush(IDrawBuffer d) {
		for (ScreenshotEntry entry : screenshots) {
			d.screenshot(entry.screenshot, entry.clip);
		}
		screenshots.clear();
	}
	
	//Getters
	public boolean isEmpty() {
		return screenshots.isEmpty();
	}
	
	//Setters
	
	//Inner Classes
	@LuaSerializable
	private static class ScreenshotEntry implements Serializable {
		
		private static final long serialVersionUID = 1L;
		
		final IScreenshot screenshot;
		final boolean clip;
		
		public ScreenshotEntry(IScreenshot ss, boolean clip) {
			this.screenshot = ss;
			this.clip = clip;
		}
		
	}
	
}
