package nl.weeaboo.vn.impl.nvlist;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;

import javax.sound.sampled.UnsupportedAudioFileException;

import nl.weeaboo.common.StringUtil;
import nl.weeaboo.lua2.io.LuaSerializable;
import nl.weeaboo.sound.ISound;
import nl.weeaboo.sound.SoundDesc;
import nl.weeaboo.sound.SoundManager;
import nl.weeaboo.sound.SoundManager.SoundInput;
import nl.weeaboo.vn.SoundType;
import nl.weeaboo.vn.impl.base.BaseSound;

@LuaSerializable
public final class NovelSound extends BaseSound {

	private static final long serialVersionUID = NVListImpl.serialVersionUID;

	private final SoundFactory soundFactory;
	
	private transient ISound sound;
	
	public NovelSound(SoundFactory sfac, SoundType type, String filename) {
		super(type, filename);
		
		soundFactory = sfac;
	}
	
	//Functions
	@Override
	protected void _start() throws IOException {
		SoundManager sm = soundFactory.getSoundManager();
		int fadeTime = soundFactory.getFadeTimeMillis(getSoundType(), true);
		String filename = getFilename();
		SoundDesc desc = sm.getSoundDesc(filename);
		long loopStart = -1, loopEnd = -1;
		if (desc != null) {
			if (desc.getLoopStart() != null) loopStart = desc.getLoopStart().getTimeNanos();
			if (desc.getLoopEnd() != null)   loopEnd   = desc.getLoopEnd().getTimeNanos();
		}
		
		boolean ok = false;
		SoundInput sin = sm.getSoundInput(filename);
		try {
			int ch = sm.findFreeChannel(100, 999);
			sound = sm.play(ch, SoundFactory.convertSoundType(getSoundType()),
					sin.in, sin.length, 0, getLoopsLeft(),
					loopStart, loopEnd);
			
			sound.setPrivateVolume(getVolume(), fadeTime);
			ok = true;
		} finally {			
			if (!ok) sin.close();
		}
	}

	@Override
	protected void _stop() {
		if (sound != null) {
			sound.stop(soundFactory.getFadeTimeMillis(getSoundType(), false));
			checkSoundException();
		}
	}

	@Override
	protected void _pause() throws InterruptedException {
		if (sound != null) {
			sound.pause();
		}
	}

	@Override
	protected void _resume() {
		if (sound != null) {
			sound.resume();
		}
	}

	@Override
	protected void onVolumeChanged() {
		if (sound != null) {
			sound.setPrivateVolume(getVolume(), 0);
		}
	}

	private void writeObject(ObjectOutputStream out) throws IOException {
		if (isStopped()) {
			sound = null;
		}
		
		out.defaultWriteObject();
	}
	
	private void readObject(ObjectInputStream in) throws IOException, ClassNotFoundException {
		in.defaultReadObject();

		//Restart automatically when loaded
		if (!isStopped()) {
			_start();
			if (isPaused()) {
				try {
					_pause();
				} catch (InterruptedException e) { }
			}
		}
	}	
	
	private void checkSoundException() {
		if (sound == null) return;
		
		Exception ex = sound.getException();
		if (ex instanceof UnsupportedAudioFileException) {
			soundFactory.addInvalidExt(StringUtil.getExtension(getFilename()));
		}
	}
	
	//Getters
	@Override
	public boolean isStopped() {
		if (sound != null) {
			checkSoundException();
			return sound.isStopping() || sound.isStopped();
		}
		return super.isStopped();
	}
	
	//Setters
	
}
