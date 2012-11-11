package nl.weeaboo.vn;

import java.io.Serializable;

import nl.weeaboo.common.Rect;
import nl.weeaboo.common.Rect2D;
import nl.weeaboo.lua2.io.LuaSerializable;

@LuaSerializable
public class RenderEnv implements Serializable {

	private static final long serialVersionUID = 1L;
	
	public final int rx, ry, rw, rh;
	public final int sw, sh;
	public final int vw, vh;
	public final double scale;
	public final Rect screenClip;
	private final Rect2D glScreenVirtualBounds;
	public final boolean isTouchScreen;
	
	public RenderEnv(int vw, int vh, int rx, int ry, int rw, int rh, int sw, int sh, boolean isTouchScreen) {
		this.vw = vw;
		this.vh = vh;
		this.rx = rx;
		this.ry = ry;
		this.rw = rw;
		this.rh = rh;
		this.sw = sw;
		this.sh = sh;
		this.scale = Math.min(rw / (double)vw, rh / (double)vh);
		this.screenClip = new Rect(rx, sh - ry - rh, rw, rh);
		this.glScreenVirtualBounds = calculateGLScreenVirtualBounds(rx, ry, sw, sh, scale);
		this.isTouchScreen = isTouchScreen;
	}
	
	private static Rect2D calculateGLScreenVirtualBounds(double rx, double ry, double sw, double sh, double scale) {
		double s = 1.0 / scale;
		double x = s * -rx;
		double y = s * -ry;		
		double w = s * sw;
		double h = s * sh;
		
		w = Double.isNaN(w) ? 0 : Math.max(0, w);
		h = Double.isNaN(h) ? 0 : Math.max(0, h);
		
		return new Rect2D(x, y, w, h);		
	}
	
	/**
	 * @return The OpenGL clipping rectangle for the virtual screen.
	 */
	public final Rect getScreenClip() {
		return screenClip;
	}
	
	/**
	 * @return The virtual width of the screen (usually taken from
	 *         {@link nl.weeaboo.vn.IImageState})
	 */
	public final int getWidth() {
		return vw;
	}

	/**
	 * @return The virtual height of the screen (usually taken from
	 *         {@link nl.weeaboo.vn.IImageState})
	 */
	public final int getHeight() {
		return vh;
	}

	/**
	 * @return The X-offset in physical screen pixels
	 */
	public final int getRealX() {
		return rx;
	}

	/**
	 * @return The Y-offset in physical screen pixels
	 */
	public final int getRealY() {
		return ry;
	}

	/**
	 * @return The width in physical screen pixels
	 */
	public final int getRealWidth() {
		return rw;
	}

	/**
	 * @return The height in physical screen pixels
	 */
	public final int getRealHeight() {
		return rh;
	}

	/**
	 * @return The width of the entire physical screen pixels
	 */
	public final int getScreenWidth() {
		return sw;
	}

	/**
	 * @return The height of the entire physical screen pixels
	 */
	public final int getScreenHeight() {
		return sh;
	}

	/**
	 * @return The scale factor from virtual coords to real coords.
	 */
	public final double getScale() {
		return scale;
	}
	
	/**
	 * @return The bounds of the full OpenGL rendering bounds in virtual
	 *         coordinates, ignoring any offset or clipping.
	 */
	public final Rect2D getGLScreenVirtualBounds() {
		return glScreenVirtualBounds;
	}
	
	/**
	 * @return <code>true</code> when running on a touchscreen device.
	 */
	public final boolean isTouchScreen() {
		return isTouchScreen;
	}
	
}
