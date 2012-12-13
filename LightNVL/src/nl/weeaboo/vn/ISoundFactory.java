package nl.weeaboo.vn;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.Collection;

public interface ISoundFactory {

	// === Functions ===========================================================
	
	/**
	 * Creates a new sound object that can be used for playing the audio file
	 * specified by <code>filename</code>.
	 * 
	 * @param stype The kind of sound to start playing (voice, sfx, music, ...)
	 * @param filename The file to load
	 * @param callStack The Lua callstack from which the preload function was
	 *        called.
	 *        
	 * @throws FileNotFoundException If no sound data could be found for the
	 *         specified filename.
	 */
	public ISound createSound(SoundType stype, String filename, String[] callStack) throws IOException;
	
	/**
	 * Tells the runtime that the sound specified by the given filename will be
	 * used in the near future.
	 * 
	 * @param filename The file to load
	 */
	public void preload(String filename);
	
	// === Getters =============================================================

	/**
	 * @return The human-readable name of the sound specified by
	 *         <code>filename</code>, or <code>null</code> if none is defined
	 *         in <code>snd.xml</code>.
	 */
	public String getName(String filename);
	
	/**
	 * Returns the paths for all sound files in the specified folder
	 */
	public Collection<String> getSoundFiles(String folder);
	
	/**
	 * @param fadeIn <code>true</code> to get the fade-in time (sound becomes
	 *        louder), <code>false</code> for the fade-out time (sound becomes
	 *        quieter).
	 * @return The default fade-in/fade-out time for the specified sound type.
	 */
	public int getFadeTimeMillis(SoundType type, boolean fadeIn);
	
	// === Setters =============================================================
	
}
