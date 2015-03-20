package nl.weeaboo.vn.render.impl;

import nl.weeaboo.vn.image.IScreenshot;

public final class ScreenshotRenderCommand extends BaseRenderCommand {

	public static final byte ID = ID_SCREENSHOT_RENDER_COMMAND;

	public final IScreenshot ss;

	public ScreenshotRenderCommand(IScreenshot ss, boolean clip) {
		super(ID, ss.getZ(), clip, (byte)255);

		this.ss = ss;
	}

}
