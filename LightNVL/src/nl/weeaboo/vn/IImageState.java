package nl.weeaboo.vn;

import java.io.Serializable;

public interface IImageState extends Serializable {

	// === Functions ===========================================================
	
	/**
	 * Resets the entire state, including the drawables stack.
	 */
	public void reset();

	/**
	 * Stores a copy of the current internal state onto a stack, then replaces
	 * the current state with an empty copy.
	 */
	public void push();
	
	/**
	 * Restores to a state previously stored with {@link #push()}/
	 */
	public void pop();
	
	/**
	 * Creates a new layer object and adds it to <code>parentLayer</code>.
	 */
	public ILayer createLayer(ILayer parentLayer);
	
	/**
	 * Updates the image state and all layers it contains
	 * 
	 * @param input The current user input
	 * @param effectSpeed The suggested relative animation speed
	 * @return <code>true</code> if the state changed since the last call to
	 *         this function.
	 */
	public boolean update(IInput input, double effectSpeed);
	
	// === Getters =============================================================
	
	/**
	 * Returns the root layer.
	 */
	public ILayer getRootLayer();
	
	/**
	 * Returns the default layer.
	 */
	public ILayer getDefaultLayer();
	
	/**
	 * Returns the overlay layer.
	 */
	public ILayer getOverlayLayer();
	
	/**
	 * @return The virtual screen width
	 */
	public int getWidth();
	
	/**
	 * @return The virtual screen height
	 */
	public int getHeight();
				
	// === Setters =============================================================
	
}
