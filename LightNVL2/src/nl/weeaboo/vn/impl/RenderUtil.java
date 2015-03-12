package nl.weeaboo.vn.impl;

import nl.weeaboo.common.Rect2D;
import nl.weeaboo.styledtext.TextStyle;

final class RenderUtil {

	private RenderUtil() {
	}

	public static TextStyle CENTERED_STYLE = TextStyle.fromString("anchor=5");

	/**
	 * @param scale The scale factor from virtual coordinates to real coordinates.
	 */
	public static Rect2D calculateGLScreenVirtualBounds(int clipX, int clipY,
			int screenWidth, int screenHeight, double scale)
	{
		double s = 1.0 / scale;
		double x = s * -clipX;
		double y = s * -clipY;
		double w = s * screenWidth;
		double h = s * screenHeight;

		w = Double.isNaN(w) ? 0 : Math.max(0, w);
		h = Double.isNaN(h) ? 0 : Math.max(0, h);

		return new Rect2D(x, y, w, h);
	}

}
