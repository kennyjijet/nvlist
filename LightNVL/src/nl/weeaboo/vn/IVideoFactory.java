package nl.weeaboo.vn;

import java.io.IOException;

public interface IVideoFactory {

	// === Functions ===========================================================

	/**
	 * Starts a full-screen video.
	 * 
	 * @param filename Path to the video file that should be played, use
	 *        <code>null</code> to stop the currently playing cutscene.
	 * @return An IVideo object that can be used to control playback. 
	 */
	public IVideo movie(String filename) throws IOException;
	
	// === Getters =============================================================
	
	// === Setters =============================================================
	
}
