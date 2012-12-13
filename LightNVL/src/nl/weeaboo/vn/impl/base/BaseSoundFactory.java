package nl.weeaboo.vn.impl.base;

import java.io.IOException;
import java.io.Serializable;
import java.util.Collection;

import nl.weeaboo.vn.INotifier;
import nl.weeaboo.vn.ISeenLog;
import nl.weeaboo.vn.ISound;
import nl.weeaboo.vn.ISoundFactory;
import nl.weeaboo.vn.SoundType;

public abstract class BaseSoundFactory extends BaseMediaFactory implements ISoundFactory, Serializable {

	private static final long serialVersionUID = BaseImpl.serialVersionUID;
	
	public BaseSoundFactory(ISeenLog sl, INotifier ntf) {
		super(new String[] {"ogg"}, sl, ntf);
		
		if (sl instanceof BaseSeenLog) {
			((BaseSeenLog)sl).setSoundFactory(this);
		}
	}
	
	//Functions	
	@Override
	public final ISound createSound(SoundType stype, String filename, String[] callStack)
		throws IOException
	{
		String normalized = normalizeFilename(filename);
		if (normalized == null) {
			notifier.d("Unable to find sound file: " + filename);
			return null;
		}
		seenLog.addSound(normalized);		
		return createSoundNormalized(stype, normalized, callStack);
	}
	
	protected abstract ISound createSoundNormalized(SoundType stype, String filename,
			String[] callStack) throws IOException;
	
	//Getters
	@Override
	public final String getName(String filename) {
		String normalized = normalizeFilename(filename);
		if (normalized == null) {
			notifier.d("Unable to find sound file: " + filename);
			return null;
		}
		return getNameNormalized(normalized);
	}
	
	protected abstract String getNameNormalized(String filename);
	
	@Override
	public Collection<String> getSoundFiles(String folder) {
		return getMediaFiles(folder);
	}
	
	@Override
	public int getFadeTimeMillis(SoundType type, boolean fadeIn) {
		if (type == SoundType.MUSIC) {
			return (fadeIn ? 500 : 250);
		}
		return 0;
	}
	
	//Setters
	
}
