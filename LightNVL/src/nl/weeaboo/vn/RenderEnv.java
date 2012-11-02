package nl.weeaboo.vn;

import nl.weeaboo.common.Rect;

public final class RenderEnv {

	//--- Don't add properties without also comparing them in equals() ---
	public final double scale;
	public final int rx, ry, rw, rh;
	public final int sw, sh;
	public final int vw, vh;
	public final Rect screenClip;
	//--- Don't add properties without also comparing them in equals() ---
	
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
	
	@Override
	public int hashCode() {
		return vw ^ vh ^ (int)Double.doubleToLongBits(scale);
	}
	
	@Override
	public boolean equals(Object obj) {
		if (obj instanceof RenderEnv) {
			RenderEnv env = (RenderEnv)obj;
			return scale == env.scale
				&& vw == env.vw && vh == env.vh && sw == env.sw && sh == env.sh
				&& rx == env.rx && ry == env.ry && rw == env.rw && rh == env.rh;
			//No need to compare screenClip, generated from other attributes
		}
		return false;
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
