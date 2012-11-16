package nl.weeaboo.vn;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;

import nl.weeaboo.settings.IConfig;

public interface INovel {

	// === Functions ===========================================================
	
	public void reset();	
	public void savePersistent();
	public void onPrefsChanged(IConfig config);
	
	/**
	 * @return <code>true</code> if the state was changed.
	 */
	public boolean update();

	public void readAttributes(ObjectInputStream in) throws ClassNotFoundException, IOException;
	public void writeAttributes(ObjectOutputStream out) throws IOException;
	
	// === Getters =============================================================
	public INovelConfig getNovelConfig();
	public IImageFactory getImageFactory();	
	public IImageState getImageState();
	public IImageFxLib getImageFxLib();
	public ISoundFactory getSoundFactory();
	public ISoundState getSoundState();
	public IVideoFactory getVideoFactory();
	public IVideoState getVideoState();
	public IGUIFactory getGUIFactory();
	public ITextState getTextState();
	public INotifier getNotifier();
	public IInput getInput();
	public IShaderFactory getShaderFactory();
	public ISystemLib getSystemLib();
	public ISaveHandler getSaveHandler();
	public IScriptLib getScriptLib();
	public IStorage getGlobals();
	public IPersistentStorage getSharedGlobals();
	public ISeenLog getSeenLog();
	public IAnalytics getAnalytics();
	public ITimer getTimer();
	
	// === Setters =============================================================	
	
}
