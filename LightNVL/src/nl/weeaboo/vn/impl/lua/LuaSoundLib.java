package nl.weeaboo.vn.impl.lua;

import java.io.FileNotFoundException;
import java.io.IOException;

import nl.weeaboo.lua2.io.LuaSerializable;
import nl.weeaboo.lua2.lib.LuaLibrary;
import nl.weeaboo.vn.ISound;
import nl.weeaboo.vn.ISoundState;
import nl.weeaboo.vn.SoundType;
import nl.weeaboo.vn.impl.base.BaseNotifier;
import nl.weeaboo.vn.impl.base.BaseSoundFactory;

import org.luaj.vm2.LuaError;
import org.luaj.vm2.LuaTable;
import org.luaj.vm2.Varargs;

@LuaSerializable
public class LuaSoundLib extends LuaLibrary {

	private static final long serialVersionUID = LuaImpl.serialVersionUID;

	private static final String[] NAMES = {
		"start",
		"stop",
		"stopAll",
		"setVolume",
		"getName",
		"getSoundFiles"
	};

	private static final int INIT            = 0;
	private static final int START           = 1;
	private static final int STOP            = 2;
	private static final int STOP_ALL        = 3;
	private static final int SET_VOLUME      = 4;
	private static final int GET_NAME        = 5;
	private static final int GET_SOUND_FILES = 6;
	
	private final BaseNotifier notifier;
	private final BaseSoundFactory soundFactory;
	private final ISoundState soundState;

	public LuaSoundLib(BaseNotifier ntf, BaseSoundFactory fac, ISoundState ss) {
		this.notifier = ntf;
		this.soundFactory = fac;
		this.soundState = ss;
	}

	@Override
	protected LuaLibrary newInstance() {
		return new LuaSoundLib(notifier, soundFactory, soundState);
	}
	
	@Override
	public Varargs invoke(Varargs args) {
		switch (opcode) {
		case INIT:            return initLibrary("Sound", NAMES, 1);
		case START:           return start(args);
		case STOP:            return stop(args);
		case STOP_ALL:        return stopAll(args);
		case SET_VOLUME:      return setVolume(args);
		case GET_NAME:        return getName(args);
		case GET_SOUND_FILES: return getSoundFiles(args);
		default: return super.invoke(args);
		}
	}
	
	protected Varargs start(Varargs args) {
		int channel = args.optint(1, 0);
		String filename = args.checkjstring(2);		
		SoundType stype = args.optuserdata(3, SoundType.class, SoundType.SOUND);		
		int loops = args.optint(4, 1);		
		double volume = args.optdouble(5, 1.0);
		
		try {
			ISound sound = soundFactory.createSound(stype, filename, LuaNovelUtil.getLuaStack());
			if (sound != null) {
				sound.setPrivateVolume(volume);
				soundState.set(channel, sound);
				sound.start(loops);
			}
		} catch (FileNotFoundException fnfe) {
			notifier.d("Audio file not found ("+filename+")", fnfe);
		} catch (IOException e) {
			throw new LuaError(e);
		}
		return NONE;
	}
	
	protected Varargs stop(Varargs args) {		
		int channel = args.optint(1, 0);
		soundState.stop(channel);
		return NONE;
	}
	
	protected Varargs stopAll(Varargs args) {		
		soundState.stopAll();
		return NONE;
	}
	
	protected Varargs setVolume(Varargs args) {
		int channel = args.optint(1, 0);
		double volume = args.optdouble(2, 1);
		ISound sound = soundState.get(channel);
		if (sound != null) {
			sound.setPrivateVolume(volume);
		}
		return NONE;
	}

	protected Varargs getName(Varargs args) {
		String filename = args.checkjstring(1);
		String name = soundFactory.getName(filename);
		return name != null ? valueOf(name) : NIL;
	}
		
	protected Varargs getSoundFiles(Varargs args) {
		String folder = args.optjstring(1, "");
		
		LuaTable table = new LuaTable();
		int t = 1;
		for (String filename : soundFactory.getSoundFiles(folder)) {
			table.rawset(t++, filename);
		}
		return table;
	}
	
}
