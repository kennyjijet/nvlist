package nl.weeaboo.vn;

import java.io.Serializable;
import java.util.Map;

public interface IImageState extends Serializable {

	// === Functions ===========================================================
		
	/**
	 * Creates and adds a new layer to this imagestate.
	 */
	public ILayer createLayer(String id);
	
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
	 * Updates the image state and all layers it contains
	 * 
	 * @param input The current user input
	 * @param effectSpeed The suggested relative animation speed
	 * @return <code>true</code> if the state changed since the last call to
	 *         this function.
	 */
	public boolean update(IInput input, double effectSpeed);
	
	/**
	 * Draws the image state and all layers it contains
	 * 
	 * @param renderer The renderer to use for drawing
	 */
	public void draw(IRenderer renderer);
	
	// === Getters =============================================================
	
	/**
	 * Returns all layers.
	 */
	public Map<String, ILayer> getLayers();
	
	/**
	 * Returns the top layer.
	 */
	public ILayer getTopLayer();
	
	/**
	 * Returns a previously created layer.
	 */
	public ILayer getLayer(String id);
	
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
