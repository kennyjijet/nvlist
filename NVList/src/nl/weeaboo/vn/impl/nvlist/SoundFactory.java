package nl.weeaboo.vn.impl.nvlist;

import java.io.IOException;
import java.io.ObjectStreamException;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import nl.weeaboo.io.EnvironmentSerializable;
import nl.weeaboo.lua2.io.LuaSerializable;
import nl.weeaboo.media.sound.SoundDesc;
import nl.weeaboo.sound.SoundManager;
import nl.weeaboo.sound.SoundManager.SoundInput;
import nl.weeaboo.vn.IAnalytics;
import nl.weeaboo.vn.INotifier;
import nl.weeaboo.vn.ISeenLog;
import nl.weeaboo.vn.ISound;
import nl.weeaboo.vn.SoundType;
import nl.weeaboo.vn.impl.base.BaseSoundFactory;
import nl.weeaboo.vn.impl.lua.LuaNovelUtil;

@LuaSerializable
public class SoundFactory extends BaseSoundFactory implements Serializable {

	private final IAnalytics analytics;
	private final SoundManager sm;
	private final EnvironmentSerializable es;
	
	public SoundFactory(SoundManager sm, IAnalytics an, ISeenLog sl, INotifier ntf) {
		super(sl, ntf);
		
		this.analytics = an;
		this.sm = sm;

		this.es = new EnvironmentSerializable(this);
	}
	
	//Functions	
	private Object writeReplace() throws ObjectStreamException {	
		return es.writeReplace();
	}
	
	@Override
	protected void preloadNormalized(String filename) {
		sm.preload(filename);
	}
	
	@Override
	public ISound createSoundNormalized(SoundType stype, String filename,
			String[] luaStack) throws IOException
	{
		long loadNanos = 0L;
		boolean cacheable = sm.isSoundCacheable(filename);
		if (cacheable) {
			if (!sm.isSoundLoaded(filename)) {
				long t0 = System.nanoTime();
				SoundInput sin = sm.getSoundInput(filename);
				loadNanos = System.nanoTime() - t0;			
				if (sin != null) sin.close();
			}
		
			String callSite = LuaNovelUtil.getNearestLVNSrcloc(luaStack);
			if (callSite != null) {
				analytics.logSoundLoad(callSite, filename, loadNanos);
				//System.out.println("Sound Load: " + filename);
			}
		}
		
		return new NovelSound(this, stype, filename);
	}
	
	public static nl.weeaboo.sound.SoundType convertSoundType(SoundType type) {
		switch (type) {
		case MUSIC: return nl.weeaboo.sound.SoundType.MUSIC;
		case SOUND: return nl.weeaboo.sound.SoundType.SOUND;
		case VOICE: return nl.weeaboo.sound.SoundType.VOICE;
		default: return nl.weeaboo.sound.SoundType.SOUND;
		}
	}
	
	//Getters		
	public SoundManager getSoundManager() {
		return sm;
	}

	@Override
	public String getNameNormalized(String filename) {
		SoundDesc desc = sm.getSoundDesc(filename);		
		return (desc != null ? desc.getName() : null);
	}

	@Override
	protected boolean isValidFilename(String filename) {
		if (filename == null) return false;

		return sm.getSoundFileExists(filename);
	}
	
	@Override
	protected List<String> getFiles(String folder) {
		List<String> out = new ArrayList<String>();
		try {
			sm.getSoundFiles(out, folder, true);
		} catch (IOException e) {
			notifier.d("Folder doesn't exist or can't be read: " + folder, e);
		}
		return out;
	}
	
	//Setters
	
}
