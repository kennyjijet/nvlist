package nl.weeaboo.vn.render.impl;

import nl.weeaboo.vn.ITexture;
import nl.weeaboo.vn.IWritableScreenshot;
import nl.weeaboo.vn.impl.AbstractScreenshot;

public class WritableScreenshot extends AbstractScreenshot implements IWritableScreenshot {

	private static final long serialVersionUID = RenderImpl.serialVersionUID;

	public WritableScreenshot(short z, boolean isVolatile) {
		super(z, isVolatile);
	}

	@Override
	public void setPixels(int[] argb, int w, int h, int screenWidth, int screenHeight) {
		super.setPixels(argb, w, h, screenWidth, screenHeight);
	}

	@Override
	public void setVolatilePixels(ITexture tex, int screenWidth, int screenHeight) {
		super.setVolatilePixels(tex, screenWidth, screenHeight);
	}

}
