package nl.weeaboo.vn;

import java.io.Serializable;

public interface ISoundState extends Serializable {

	// === Functions ===========================================================
		
	/**
	 * Must be called regularly (preferably every frame).
	 */
	public void update();
	
	/**
	 * Stops all sounds
	 */
	public void stopAll();
	
	/**
	 * Stops the sound playing in the specified channel.
	 */
	public void stop(int channel);
	
	// === Getters =============================================================
	
	/**
	 * @param channel The channel to request the contents of.
	 * @return The sound in the specified channel, or <code>null</code> if none
	 *         exists.
	 */
	public ISound get(int channel);
	
	/**
	 * @return The master volume for the specified type.
	 * @see #setMasterVolume(SoundType, double)
	 */
	public double getMasterVolume(SoundType type);
	
	public boolean isPaused();
	
	// === Setters =============================================================
	
	/**
	 * @param channel The channel to store the sound in. If a sound is already
	 *        in that slot, it is stopped first.
	 * @param sound The sound object to store.
	 */
	public void set(int channel, ISound sound);
	
	/**
	 * Changes the master volume for the specified type. The master volume
	 * (between <code>0.0</code> and <code>1.0</code>) is multiplied together
	 * with the sound's private volume to get the true playback volume.
	 */
	public void setMasterVolume(SoundType type, double vol);
	
	/**
	 * Pauses or unpauses this sound state. Any sounds that are/were already
	 * paused are left alone.
	 */
	public void setPaused(boolean p);
}
