package nl.weeaboo.vn.impl.base;

import nl.weeaboo.common.Rect;

public final class RenderEnv {

	public final int vw, vh;
	public final int rx, ry, rw, rh;
	public final int sw, sh;
	public final double scale;
	public final Rect screenClip;
	
	public RenderEnv(int vw, int vh, int rx, int ry, int rw, int rh, int sw, int sh) {
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
	}
	
	/**
	 * @return The OpenGL clipping rectangle for the virtual screen.
	 */
	public Rect getScreenClip() {
		return screenClip;
	}
	
	/**
	 * @return The virtual width of the screen (usually taken from
	 *         {@link nl.weeaboo.vn.IImageState})
	 */
	public int getWidth() {
		return vw;
	}

	/**
	 * @return The virtual height of the screen (usually taken from
	 *         {@link nl.weeaboo.vn.IImageState})
	 */
	public int getHeight() {
		return vh;
	}

	/**
	 * @return The X-offset in physical screen pixels
	 */
	public int getRealX() {
		return rx;
	}

	/**
	 * @return The Y-offset in physical screen pixels
	 */
	public int getRealY() {
		return ry;
	}

	/**
	 * @return The width in physical screen pixels
	 */
	public int getRealWidth() {
		return rw;
	}

	/**
	 * @return The height in physical screen pixels
	 */
	public int getRealHeight() {
		return rh;
	}

	/**
	 * @return The width of the entire physical screen pixels
	 */
	public int getScreenWidth() {
		return sw;
	}

	/**
	 * @return The height of the entire physical screen pixels
	 */
	public int getScreenHeight() {
		return sh;
	}

	/**
	 * @return The scale factor from virtual coords to real coords.
	 */
	public double getScale() {
		return scale;
	}
	
}
