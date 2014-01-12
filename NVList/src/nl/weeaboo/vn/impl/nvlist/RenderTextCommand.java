package nl.weeaboo.vn.impl.nvlist;

import nl.weeaboo.textlayout.TextLayout;
import nl.weeaboo.vn.BlendMode;
import nl.weeaboo.vn.impl.base.BaseRenderCommand;

public class RenderTextCommand extends BaseRenderCommand {

	public static final byte id = 1;
	
	public final TextLayout textLayout;
	public final int lineStart, lineEnd;
	public final double visibleChars;
	public final double x, y;
	
	protected RenderTextCommand(short z, boolean clipEnabled, BlendMode blendMode,
		int argb, TextLayout textLayout, int lineStart, int lineEnd, double visibleChars,
		double x, double y)
	{
		super(id, z, clipEnabled, blendMode, argb, (byte)textLayout.hashCode());
		
		this.textLayout = textLayout;
		this.lineStart = lineStart;
		this.lineEnd = lineEnd;
		this.visibleChars = visibleChars;
		this.x = x;
		this.y = y;
	}

}
