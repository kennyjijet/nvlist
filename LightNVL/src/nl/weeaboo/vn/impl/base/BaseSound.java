package nl.weeaboo.vn.impl.base;

import java.io.IOException;

import nl.weeaboo.vn.ISound;
import nl.weeaboo.vn.SoundType;

/**
 * Don't forget to implement a specialized version of <code>readObject</code> to
 * restart playback after deserialization.
 */
public abstract class BaseSound implements ISound {

	private static final long serialVersionUID = BaseImpl.serialVersionUID;
	
	private SoundType stype;
	private String filename;
	private double privateVolume;
	private double masterVolume;
	private int loopsLeft;
	
	private boolean destroyed;
	private boolean playing;
	private boolean paused;
	private volatile boolean stopped;
	
	protected BaseSound(SoundType st, String filename) {
		this.stype = st;
		this.filename = filename;
		
		privateVolume = masterVolume = 1.0;
		loopsLeft = 1;
		stopped = true;
	}
	
	//Functions
	@Override
	public void destroy() {
		if (!destroyed) {
			stop();

			destroyed = true;
		}
	}
	
	@Override
	public void start(int loops) throws IOException {
		if (destroyed) return;
		
		if (stopped) {
			setLoopsLeft(loops);
			
			_start();

			playing = true;
			paused = false;
			stopped = false;
		}
	}
	
	protected abstract void _start() throws IOException;
	
	@Override
	public void stop() {
		stop(0);
	}
	
	@Override
	public void stop(int fadeOutMillis) {
		if (destroyed) return;

		playing = false;
		paused = false;
		if (!stopped) {
			stopped = true;			
			_stop(fadeOutMillis);
		}
	}
	
	protected abstract void _stop(int fadeOutMillis);

	@Override
	public void pause() {
		if (destroyed) return;
		if (stopped) throw new IllegalStateException("Cannot pause while stopped");
		
		playing = false;
		if (!paused) {
			paused = true;
			try {
				_pause();
			} catch (InterruptedException e) {
				//Ignore exception
			}
		}
	}
	
	protected abstract void _pause() throws InterruptedException;

	@Override
	public void resume() {
		if (destroyed) return;
		if (stopped) throw new IllegalStateException("Cannot resume while stopped");
		if (!paused) throw new IllegalStateException("Cannot resume while unpaused");
		
		playing = true;
		if (paused) {
			paused = false;
			_resume();
		}
	}
	
	protected abstract void _resume();
	
	/**
	 * Call this function at the end of a loop to see if we're done.
	 * 
	 * @return True if the sound should continue looping
	 */
	protected boolean onLoopEnd() {
		if (loopsLeft == 0 || loopsLeft == 1) {
			return true;
		}
		if (loopsLeft > 0) {
			setLoopsLeft(loopsLeft-1);			
		}		
		return false;
	}
	
	protected abstract void onVolumeChanged();
		
	//Getters
	@Override
	public boolean isDestroyed() {
		return destroyed;
	}
	
	@Override
	public String getFilename() {
		return filename;
	}
	
	@Override
	public SoundType getSoundType() {
		return stype;
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
	public int getLoopsLeft() {
		return loopsLeft;
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
	
	protected void setLoopsLeft(int ll) {
		loopsLeft = ll;
	}
	
}
