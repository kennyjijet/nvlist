package nl.weeaboo.vn.impl.base;

import java.io.IOException;

import nl.weeaboo.vn.IVideo;

/**
 * Don't forget to implement a specialized version of <code>readObject</code> to
 * restart playback after deserialization.
 */
public abstract class BaseVideo implements IVideo {

	private static final long serialVersionUID = BaseImpl.serialVersionUID;
	
	private String videoPath;
	private double privateVolume;
	private double masterVolume;
	
	private boolean prepared;
	private boolean playing;
	private boolean paused;
	private volatile boolean stopped;	
	
	protected BaseVideo() {
		privateVolume = masterVolume = 1.0;
		stopped = true;
	}
	
	//Functions
	@Override
	public void prepare() throws IOException {
		if (!prepared) {
			prepared = true;
			_prepare();
		}
	}
	
	protected abstract void _prepare() throws IOException;
	
	@Override
	public void start() throws IOException {
		if (stopped) {
			if (!isPrepared()) {
				prepare();
			}
			
			_start();

			playing = true;
			paused = false;
			stopped = false;
		}
	}
	
	protected abstract void _start() throws IOException;
	
	@Override
	public void stop() {
		playing = false;
		paused = false;
		if (!stopped) {
			stopped = true;
			_stop();
		}
	}
	
	protected abstract void _stop();
	
	@Override
	public void pause() {
		if (stopped) throw new IllegalStateException("Cannot pause while stopped");
		
		playing = false;
		if (!paused) {
			paused = true;
			_pause();
		}
	}
	
	protected abstract void _pause();

	@Override
	public void resume() {
		if (stopped) throw new IllegalStateException("Cannot resume while stopped");
		if (!paused) throw new IllegalStateException("Cannot resume while unpaused");
		
		playing = true;
		if (paused) {
			paused = false;
			_resume();
		}
	}
	
	protected abstract void _resume();
	
	protected abstract void onVolumeChanged();
			
	//Getters
	@Override
	public String getVideoPath() {
		return videoPath;
	}
	
	@Override
	public boolean isPrepared() {
		return prepared;
	}
	
	@Override
	public boolean isPlaying() {
		return playing;
	}

	@Override
	public boolean isPaused() {
		return paused;
	}
	
	@Override
	public boolean isStopped() {
		return stopped;
	}

	@Override
	public double getPrivateVolume() {
		return privateVolume;
	}

	@Override
	public double getMasterVolume() {
		return masterVolume;
	}
	
	@Override
	public double getVolume() {
		return masterVolume * privateVolume;
	}
	
	//Setters
	@Override
	public void setVideoPath(String path) {
		stop();
		
		videoPath = path;
		prepared = false;
	}
	
	@Override
	public void setPrivateVolume(double v) {
		if (privateVolume != v) {
			privateVolume = v;
			onVolumeChanged();
		}
	}

	@Override
	public void setMasterVolume(double v) {
		if (masterVolume != v) {
			masterVolume = v;
			onVolumeChanged();
		}
	}
	
}
