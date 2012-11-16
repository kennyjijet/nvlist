package nl.weeaboo.vn.impl.base;

import static nl.weeaboo.vn.NovelPrefs.EFFECT_SPEED;
import static nl.weeaboo.vn.NovelPrefs.MUSIC_VOLUME;
import static nl.weeaboo.vn.NovelPrefs.SOUND_VOLUME;
import static nl.weeaboo.vn.NovelPrefs.TEXTLOG_PAGE_LIMIT;
import static nl.weeaboo.vn.NovelPrefs.TEXT_SPEED;
import static nl.weeaboo.vn.NovelPrefs.TIMER_IDLE_TIMEOUT;
import static nl.weeaboo.vn.NovelPrefs.VOICE_VOLUME;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;

import nl.weeaboo.settings.IConfig;
import nl.weeaboo.vn.IAnalytics;
import nl.weeaboo.vn.IImageState;
import nl.weeaboo.vn.IInput;
import nl.weeaboo.vn.INovel;
import nl.weeaboo.vn.INovelConfig;
import nl.weeaboo.vn.IPersistentStorage;
import nl.weeaboo.vn.ISaveHandler;
import nl.weeaboo.vn.ISeenLog;
import nl.weeaboo.vn.ISoundState;
import nl.weeaboo.vn.IStorage;
import nl.weeaboo.vn.ITextState;
import nl.weeaboo.vn.ITimer;
import nl.weeaboo.vn.ITweenLib;
import nl.weeaboo.vn.IVideoState;
import nl.weeaboo.vn.SoundType;
import nl.weeaboo.vn.impl.lua.BaseScriptLib;

public abstract class BaseNovel implements INovel {

	private transient final INovelConfig novelConfig;
	private transient final BaseImageFactory imageFactory;
	private transient final BaseImageFxLib imageFxLib;
	private transient final BaseSoundFactory soundFactory;
	private transient final BaseVideoFactory videoFactory;
	private transient final BaseGUIFactory guiFactory;
	private transient final BaseShaderFactory shaderFactory;
	private transient final BaseSystemLib systemLib;
	private transient final BaseNotifier notifier;
	private transient final IInput input;
	private transient final ISaveHandler saveHandler;
	private transient final BaseScriptLib scriptLib;
	private transient final ITweenLib tweenLib;
	private transient final IPersistentStorage sharedGlobals;
	private transient final ISeenLog seenLog;
	private transient final IAnalytics analytics;
	private transient final ITimer timer;
	private transient double normalEffectSpeed;
	private transient double fastEffectSpeed;
	
	// !!WARNING!! Do not add properties without adding code for saving/loading

	private IImageState imageState;
	private ISoundState soundState;
	private IVideoState videoState;
	private ITextState textState;
	private IStorage globals;
	
	protected BaseNovel(INovelConfig gc, BaseImageFactory imgfac, IImageState is, BaseImageFxLib imgfxlib,
			BaseSoundFactory sndfac, ISoundState ss, BaseVideoFactory vf, IVideoState vs, BaseGUIFactory gf,
			ITextState ts, BaseNotifier n, IInput in, BaseShaderFactory sf, BaseSystemLib syslib, ISaveHandler sh,
			BaseScriptLib scrlib, ITweenLib tl, IPersistentStorage sglobs, IStorage glob, ISeenLog sl,
			IAnalytics an, ITimer tmr)
	{
		novelConfig = gc;
		imageFactory = imgfac;
		imageState = is;
		imageFxLib = imgfxlib;
		soundFactory = sndfac;
		soundState = ss;
		videoFactory = vf;
		videoState = vs;
		guiFactory = gf;
		textState = ts;
		notifier = n;
		input = in;
		shaderFactory = sf;
		systemLib = syslib;
		saveHandler = sh;
		scriptLib = scrlib;
		tweenLib = tl;
		sharedGlobals = sglobs;
		globals = glob;
		seenLog = sl;
		analytics = an;
		timer = tmr;

		normalEffectSpeed = 1;
		fastEffectSpeed = 8;
	}
	
	//Functions
	@Override
	public void reset() {
		savePersistent();
		
		globals.clear();
		imageState.reset();
		soundState.stopAll();
		videoState.stopAll();
		textState.reset();
	}
	
	@Override
	public boolean update() {
		boolean changed = false;
		
		IInput input = getInput();
		double effectSpeed = getEffectSpeed(input);		
		
		timer.update(input);
		soundState.update();
		changed |= videoState.update(input);		
		changed |= imageState.update(input, effectSpeed);
		updateScript(input); //Script changes aren't display changes, so the changed flag remains the same 
		
		return changed;
	}
	
	protected abstract boolean updateScript(IInput input);
	
	@Override
	public void readAttributes(ObjectInputStream in) throws ClassNotFoundException, IOException {
		imageState = (IImageState)in.readObject();
		soundState = (ISoundState)in.readObject();
		videoState = (IVideoState)in.readObject();
		textState = (ITextState)in.readObject();
		globals = (IStorage)in.readObject();
	}
	
	@Override
	public void writeAttributes(ObjectOutputStream out) throws IOException {
		out.writeObject(imageState);
		out.writeObject(soundState);
		out.writeObject(videoState);
		out.writeObject(textState);
		out.writeObject(globals);
	}
	
	@Override
	public void savePersistent() {
		IPersistentStorage sharedGlobals = getSharedGlobals();

		try {
			timer.save(sharedGlobals);
		} catch (IOException e) {
			notifier.v("Error saving timer", e);
		}
		
		try {			
			sharedGlobals.save();
		} catch (IOException e) {
			notifier.v("Error saving shared globals", e);
		}
		try {
			getSeenLog().save();
		} catch (IOException e) {
			notifier.v("Error saving seen log", e);
		}
		try {
			getAnalytics().save();
		} catch (IOException e) {
			notifier.v("Error saving analytics", e);
		}
	}
	
	@Override
	public void onPrefsChanged(IConfig config) {
		double effectSpeed = config.get(EFFECT_SPEED);
		setEffectSpeed(effectSpeed, 8 * effectSpeed);

		ITextState ts = getTextState();
		if (ts != null) {
			ts.setBaseTextSpeed(config.get(TEXT_SPEED));
			ts.getTextLog().setPageLimit(config.get(TEXTLOG_PAGE_LIMIT));
		}
		
		ISoundState ss = getSoundState();
		if (ss != null) {
			ss.setMasterVolume(SoundType.MUSIC, config.get(MUSIC_VOLUME));
			ss.setMasterVolume(SoundType.SOUND, config.get(SOUND_VOLUME));
			ss.setMasterVolume(SoundType.VOICE, config.get(VOICE_VOLUME));
		}
		
		ITimer timer = getTimer();
		if (timer != null) {
			timer.setIdleTimeout(config.get(TIMER_IDLE_TIMEOUT));
		}		
	}
	
	//Getters
	@Override
	public INovelConfig getNovelConfig() {
		return novelConfig;
	}
	
	@Override
	public BaseImageFactory getImageFactory() {
		return imageFactory;
	}

	@Override
	public IImageState getImageState() {
		return imageState;
	}
	
	@Override
	public BaseImageFxLib getImageFxLib() {
		return imageFxLib;
	}

	@Override
	public BaseSoundFactory getSoundFactory() {
		return soundFactory;
	}

	@Override
	public ISoundState getSoundState() {
		return soundState;
	}
	
	@Override
	public BaseVideoFactory getVideoFactory() {
		return videoFactory;
	}
		
	@Override
	public IVideoState getVideoState() {
		return videoState;
	}

	@Override
	public BaseGUIFactory getGUIFactory() {
		return guiFactory;
	}
	
	@Override
	public ITextState getTextState() {
		return textState;
	}
	
	@Override
	public BaseNotifier getNotifier() {
		return notifier;
	}

	@Override
	public IInput getInput() {
		return input;
	}
	
	@Override
	public BaseShaderFactory getShaderFactory() {
		return shaderFactory;
	}
	
	@Override
	public BaseSystemLib getSystemLib() {
		return systemLib;
	}
	
	@Override
	public ISaveHandler getSaveHandler() {
		return saveHandler;
	}
	
	@Override
	public BaseScriptLib getScriptLib() {
		return scriptLib;
	}
	
	@Override
	public IStorage getGlobals() {
		return globals;
	}
	
	@Override
	public IPersistentStorage getSharedGlobals() {
		return sharedGlobals;
	}
	
	@Override
	public ISeenLog getSeenLog() {
		return seenLog;
	}
	
	@Override
	public IAnalytics getAnalytics() {
		return analytics;
	}
	
	@Override
	public ITimer getTimer() {
		return timer;
	}
		
	protected ITweenLib getTweenLib() {
		return tweenLib;
	}
	
	protected double getEffectSpeed(IInput input) {
		return input.isQuickRead() ? fastEffectSpeed : normalEffectSpeed;
	}
		
	//Setters
	public void setEffectSpeed(double normal, double quickread) {
		normalEffectSpeed = normal;
		fastEffectSpeed = quickread;
	}
	
}
