package nl.weeaboo.vn.impl.base;

import java.util.ArrayList;
import java.util.Collection;
import java.util.EnumMap;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import nl.weeaboo.vn.ISound;
import nl.weeaboo.vn.ISoundState;
import nl.weeaboo.vn.SoundType;

public class BaseSoundState implements ISoundState {

	private static final long serialVersionUID = BaseImpl.serialVersionUID;
	
	private Map<SoundType, Double> masterVolume;
	private Map<Integer, ISound> sounds;
	private List<ISound> pausedList;
	private boolean paused;
	
	protected BaseSoundState() {
		masterVolume = new EnumMap<SoundType, Double>(SoundType.class);
		for (SoundType type : SoundType.values()) {
			masterVolume.put(type, 1.0);
		}
		
		sounds = new HashMap<Integer, ISound>();
		pausedList = new ArrayList<ISound>();
	}
	
	//Functions
	@Override
	public void update() {
		Iterator<Entry<Integer, ISound>> itr = sounds.entrySet().iterator();
		while (itr.hasNext()) {
			Entry<Integer, ISound> entry = itr.next();
			ISound s = entry.getValue();
			if (s.isStopped()) {
				pausedList.remove(s);
				itr.remove();
			}
		}
	}

	@Override
	public void stopAll() {
		Integer channels[] = sounds.keySet().toArray(new Integer[sounds.size()]);
		for (int channel : channels) {
			stop(channel);
		}
		
		ISound ps[] = pausedList.toArray(new ISound[pausedList.size()]);
		pausedList.clear();
		for (ISound sound : ps) {
			sound.stop();
		}
	}

	@Override
	public void stop(int channel) {
		ISound sound = sounds.remove(channel);
		if (sound != null) {
			pausedList.remove(sound);
			sound.stop();
		}
	}

	//Getters
	
	@Override
	public ISound get(int channel) {
		return sounds.get(channel);
	}
	
	@Override
	public double getMasterVolume(SoundType type) {
		if (type == null) throw new IllegalArgumentException("Type must be non-null");
		return masterVolume.get(type);
	}
	
	protected Iterable<ISound> getSounds(SoundType type) {
		Collection<ISound> result = new ArrayList<ISound>();
		for (ISound sound : sounds.values()) {
			if (sound.getSoundType() == type) {
				result.add(sound);
			}
		}
		return result;
	}
	
	@Override
	public boolean isPaused() {
		return paused;
	}
	
	//Setters
	
	@Override
	public void set(int channel, ISound sound) {
		stop(channel);
		
		if (sounds.containsKey(channel)) {
			throw new IllegalStateException("Attempt to overwrite an existing sound entry.");
		}

		double mvol = getMasterVolume(sound.getSoundType());
		sound.setMasterVolume(mvol);
		sounds.put(channel, sound);		
	}

	@Override
	public void setMasterVolume(SoundType type, double vol) {
		masterVolume.put(type, vol);
		
		for (ISound sound : getSounds(type)) {
			sound.setMasterVolume(vol);
		}
	}
	
	@Override
	public void setPaused(boolean p) {
		paused = p;
		
		if (paused) {
			for (ISound sound : sounds.values()) {
				if (!sound.isPaused()) {
					sound.pause();
					pausedList.add(sound);
				}
			}
		} else {
			for (ISound sound : pausedList) {
				sound.resume();
			}
			pausedList.clear();
		}
	}
	
}
