package nl.weeaboo.vn;

public interface IGUIFactory {

	// === Functions ===========================================================
	
	/**
	 * @return A newly created panel, added to the current layer.
	 */
	public IPanel createPanel();
	
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
	
	// === Getters =============================================================
	
	// === Setters =============================================================
	
}
