package nl.weeaboo.vn;

import java.io.Serializable;

import nl.weeaboo.lua2.io.LuaSerializable;

@LuaSerializable
public interface IScreenshotBuffer extends Serializable {
	
	//Functions
	/**
	 * Adds a screenshot to the buffer.
	 * 
	 * @param ss The screenshot object to fill with the pixel data later.
	 * @param clip Set to <code>false</code> to ignore the layer bounds.
	 */
	public void add(IScreenshot ss, boolean clip);

	/**
	 * Adds render commands to <code>d</code> that take the screenshots.
	 * 
	 * @param d The draw buffer to add the commands to.
	 */
	public void flush(IDrawBuffer d);

	/**
	 * Remove all buffered data.
	 */
	public void clear();
		
	//Getters
	/**
	 * @return <code>true</code> if no screenshots are currently buffered.
	 */
	public boolean isEmpty();
	
	//Setters
	
}
