package nl.weeaboo.vn;

import java.io.Serializable;

import nl.weeaboo.common.Rect;
import nl.weeaboo.common.Rect2D;

public interface IRenderEnv extends Serializable {

	/**
	 * @return The OpenGL clipping rectangle for the virtual screen.
	 */
	public Rect getGLClip();

	/**
	 * @return The virtual width of the screen.
	 */
	public int getWidth();

	/**
	 * @return The virtual height of the screen.
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

	/**
	 * @return The bounds of the full OpenGL rendering bounds in virtual
	 *         coordinates, ignoring any offset or clipping.
	 */
	public Rect2D getGLScreenVirtualBounds();

	/**
	 * @return {@code true} when running on a touchscreen device.
	 */
	public boolean isTouchScreen();

}
