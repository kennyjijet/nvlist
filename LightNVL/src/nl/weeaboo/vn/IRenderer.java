package nl.weeaboo.vn;

import nl.weeaboo.common.Rect2D;

public interface IRenderer {

	// === Functions ===========================================================
	/**
	 * Clears all buffered commands from the renderer and resets all properties
	 */
	public void reset();
	
	/**
	 * Schedules a screenshot to be taken during rendering.
	 * 
	 * @param ss The screenshot object to fill with pixels.
	 * @param clip If <code>true</code>, takes a screenshot of just the current
	 *        clipped area. Otherwise, takes a screenshot of the entire render
	 *        area.
	 */
	public void screenshot(IScreenshot ss, boolean clip);
	
	public void draw(IImageDrawable img);
	
	/**
	 * Renders all buffered draw commands.
	 * @param bounds Optional drawing bounds in virtual coordinates. 
	 */
	public void render(Rect2D bounds);

	// === Getters =============================================================
	/**
	 * @return The virtual width of the screen (usually taken from
	 *         {@link nl.weeaboo.vn.IImageState})
	 */
	public int getWidth();
	
	/**
	 * @return The virtual height of the screen (usually taken from
	 *         {@link nl.weeaboo.vn.IImageState})
	 */
	public int getHeight();
	
	/**
	 * @return The X-offset in physical screen pixels
	 */
	public int getRealX();
	
	/**
	 * @return The Y-offset in physical screen pixels
	 */
	public int getRealY();

	/**
	 * @return The width in physical screen pixels
	 */
	public int getRealWidth();
	
	/**
	 * @return The height in physical screen pixels
	 */
	public int getRealHeight();

	/**
	 * @return The width of the entire physical screen pixels
	 */
	public int getScreenWidth();
	
	/**
	 * @return The height of the entire physical screen pixels
	 */
	public int getScreenHeight();
	
	/**
	 * @return The scale factor from virtual coords to real coords.
	 */
	public double getScale();
	
	// === Setters =============================================================
	
}
