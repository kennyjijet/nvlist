package nl.weeaboo.vn;

public interface ISystemLib {

	// === Functions ===========================================================
	/**
	 * Completely restarts the game. 
	 */
	public void restart();
	
	/**
	 * Attempt to exit the game, if <code>force == true</code> the user is not
	 * shown a confirmation dialog.
	 */
	public void exit(boolean force);
	
	/**
	 * Optional operation, returns <code>null</code> if not supported.  
	 */
	public IChoice createChoice(String... options);

	/**
	 * Optional operation, returns <code>null</code> if not supported.  
	 */
	public ISaveLoadScreen createSaveScreen();

	/**
	 * Optional operation, returns <code>null</code> if not supported.  
	 */
	public ISaveLoadScreen createLoadScreen();
	
	/**
	 * Opens the website specified by <code>url</code> in an external web browser. 
	 * @param url The URL of the website
	 */
	public void openWebsite(String url);
	
	// === Getters =============================================================
	
	/**
	 * Returns <code>false</code> if {@link #exit(boolean)} is unavailable or
	 * disabled.
	 */
	public boolean canExit();
	
	/**
	 * Returns <code>true</code> when running on a device with a touchscreen.
	 */
	public boolean isTouchScreen();
	
	/**
	 * Returns <code>true</code> on devices with a slow CPU.
	 */
	public boolean isLowEnd();
	
	// === Setters =============================================================
	
	/**
	 * Changes the state of the system supplied textbox if it exists.
	 */
	public void setTextFullscreen(boolean fullscreen);
	
}
