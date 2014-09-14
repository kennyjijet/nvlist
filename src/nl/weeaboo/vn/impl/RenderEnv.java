package nl.weeaboo.vn.impl;

import nl.weeaboo.common.Dim;
import nl.weeaboo.common.Rect;
import nl.weeaboo.common.Rect2D;
import nl.weeaboo.lua2.io.LuaSerializable;
import nl.weeaboo.vn.IRenderEnv;

@LuaSerializable
public class RenderEnv implements IRenderEnv {

	private static final long serialVersionUID = BaseImpl.serialVersionUID;

	private final Dim virtualSize;
	private final Rect realClip;
	private final Dim realScreenSize;
	private final boolean isTouchScreen;

	private final double scale;
	private final Rect glClip;
	private final Rect2D glScreenVirtualBounds;

	/**
	 * @param vsize Virtual screen size.
	 * @param rclip Clipping rectangle into which the virtual screen is projected.
	 * @param rscreen Physical size of the entire rendering viewport in which the clipping rectangle is
	 *        contained.
	 */
	public RenderEnv(Dim vsize, Rect rclip, Dim rscreen, boolean isTouchScreen) {
		this.virtualSize = vsize;
		this.realClip = rclip;
		this.realScreenSize = rscreen;
		this.isTouchScreen = isTouchScreen;

		this.scale = Math.min(rclip.w / (double)vsize.w, rclip.h / (double)vsize.h);
		this.glClip = new Rect(rclip.x, rscreen.h - rclip.y - rclip.h, rclip.w, rclip.h);
		this.glScreenVirtualBounds = RenderUtil.calculateGLScreenVirtualBounds(rclip.x, rclip.y,
				rscreen.w, rscreen.h, scale);
	}

	@Override
	public int getWidth() {
		return virtualSize.w;
	}

	@Override
	public int getHeight() {
		return virtualSize.h;
	}

	@Override
	public int getRealX() {
		return realClip.x;
	}

	@Override
	public int getRealY() {
		return realClip.y;
	}

	@Override
	public int getRealWidth() {
		return realClip.w;
	}

	@Override
	public int getRealHeight() {
		return realClip.h;
	}

	@Override
	public int getScreenWidth() {
		return realScreenSize.w;
	}

	@Override
	public int getScreenHeight() {
		return realScreenSize.h;
	}

	@Override
	public double getScale() {
		return scale;
	}

	@Override
	public Rect getGLClip() {
		return glClip;
	}

	@Override
	public Rect2D getGLScreenVirtualBounds() {
		return glScreenVirtualBounds;
	}

	@Override
	public boolean isTouchScreen() {
		return isTouchScreen;
	}

}
