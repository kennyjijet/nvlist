package nl.weeaboo.vn.impl.nvlist;

import nl.weeaboo.styledtext.layout.TextLayout;
import nl.weeaboo.vn.BlendMode;
import nl.weeaboo.vn.IDrawBuffer;
import nl.weeaboo.vn.RenderEnv;
import nl.weeaboo.vn.impl.base.BaseDrawBuffer;

public class DrawBuffer extends BaseDrawBuffer {

	public DrawBuffer(RenderEnv env) {
		super(env);
	}
	
	//Functions
	public static DrawBuffer cast(IDrawBuffer buf) {
		if (buf == null) return null;
		if (buf instanceof DrawBuffer) return (DrawBuffer)buf;
		throw new ClassCastException("Supplied draw buffer is of an invalid class: " + buf.getClass() + ", expected: " + DrawBuffer.class);
	}
	
	public void drawText(short z, boolean clipEnabled, BlendMode blendMode, int argb,
			TextLayout textLayout, int lineStart, int lineEnd, double visibleChars,
			double x, double y)
	{		
		draw(new RenderTextCommand(z, clipEnabled, blendMode, argb, textLayout,
				lineStart, lineEnd, visibleChars, x, y));
	}
	
	//Getters
	
	//Setters
	
}
