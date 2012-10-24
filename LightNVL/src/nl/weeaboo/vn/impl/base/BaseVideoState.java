package nl.weeaboo.vn.impl.base;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import nl.weeaboo.vn.IInput;
import nl.weeaboo.vn.IVideo;
import nl.weeaboo.vn.IVideoState;

public class BaseVideoState implements IVideoState {

	private static final long serialVersionUID = BaseImpl.serialVersionUID;

	private IVideo fullscreenVideo;
	private List<IVideo> videos;
	private List<IVideo> pausedList;
	private boolean paused;
	private double masterVolume;
	
	protected BaseVideoState() {
		videos = new ArrayList<IVideo>();
		pausedList = new ArrayList<IVideo>();
		masterVolume = 1.0;
	}
	
	//Functions
	@Override
	public void add(IVideo v, boolean fullscreen) {
		videos.add(v);
		v.setMasterVolume(masterVolume);
		if (fullscreen) {
			fullscreenVideo = v;
		}
	}

	@Override
	public void stopAll() {
		IVideo vs[] = videos.toArray(new IVideo[videos.size()]);
		IVideo pvs[] = pausedList.toArray(new IVideo[pausedList.size()]);
		videos.clear();
		pausedList.clear();
		fullscreenVideo = null;

		for (IVideo v : vs) v.stop();
		for (IVideo v : pvs) v.stop();
	}

	@Override
	public boolean update(IInput input) {
		boolean active = false;
		
		if (fullscreenVideo != null) {
			if (fullscreenVideo.isStopped()) {
				fullscreenVideo = null;
			} else if (input.consumeEffectSkip()) {
				fullscreenVideo.stop();
				fullscreenVideo = null;
			}
		}
		
		if (videos.size() > 0) {
			active = true;

			Iterator<IVideo> itr = videos.iterator();
			while (itr.hasNext()) {
				IVideo v = itr.next();
				if (v.isStopped()) {
					pausedList.remove(v);
					itr.remove();
				}
			}
		}		
		
		return active;
	}

	//Getters
	@Override
	public boolean isPaused() {
		return paused;
	}
	
	@Override
	public boolean isBlocking() {
		return getBlocking() != null;
	}
	
	@Override
	public IVideo getBlocking() {
		return fullscreenVideo;
	}
	
	//Setters
	@Override
	public void setMasterVolume(double vol) {
		masterVolume = vol;
		
		for (IVideo video : videos) {
			video.setMasterVolume(vol);
		}
	}
	
	@Override
	public void setPaused(boolean p) {
		paused = p;
		
		if (paused) {
			for (IVideo video : videos.toArray(new IVideo[videos.size()])) {
				if (!video.isPaused()) {
					video.pause();
					pausedList.add(video);
				}
			}
		} else {
			for (IVideo video : pausedList.toArray(new IVideo[pausedList.size()])) {
				video.resume();
			}
			pausedList.clear();
		}
	}
	
}
