package nl.weeaboo.vn.impl.base;

import nl.weeaboo.vn.IScreenshot;

public class ScreenshotRenderCommand extends BaseRenderCommand {

	public static final byte id = ID_SCREENSHOT_RENDER_COMMAND;
	
	public final IScreenshot ss;
	
	public ScreenshotRenderCommand(IScreenshot ss, boolean clip) {
		super(id, ss.getZ(), clip, (byte)255);
		
		this.ss = ss;
	}
	
}
