package nl.weeaboo.vn.render;

import nl.weeaboo.common.Area2D;
import nl.weeaboo.common.Rect;
import nl.weeaboo.common.Rect2D;

public final class RenderUtil {

	private RenderUtil() {
	}

	public static Rect roundClipRect(Rect2D clip2D) {
		//Rounded to ints, resulting clip rect should be no bigger than the non-rounded version.
		int x0 = (int)Math.ceil(clip2D.x);
		int y0 = (int)Math.ceil(clip2D.y);

		//We can't just floor() the w/h because the ceil() of the x/y would skew the result.
		int x1 = (int)Math.floor(clip2D.x+clip2D.w);
		int y1 = (int)Math.floor(clip2D.y+clip2D.h);

		return new Rect(x0, y0, Math.max(0, x1-x0), Math.max(0, y1-y0));
	}

	public static Area2D combineUV(Area2D uv, Area2D texUV) {
		return new Area2D(texUV.x + uv.x * texUV.w, texUV.y + uv.y * texUV.h, texUV.w * uv.w, texUV.h * uv.h);
	}

}
