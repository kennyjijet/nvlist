package nl.weeaboo.vn.impl.base;

import nl.weeaboo.common.Rect2D;
import nl.weeaboo.vn.ITexture;
import nl.weeaboo.vn.math.Vec2;

public final class LayoutUtil {

	private LayoutUtil() {		
	}

	public static Vec2 getImageOffset(ITexture tex, double alignX, double alignY) {
		if (tex == null) {
			return new Vec2(0, 0);
		}
		return new Vec2(getImageOffset(tex.getWidth(), alignX),
				getImageOffset(tex.getHeight(), alignY));
	}
	public static double getImageOffset(double size, double align) {
		return -align * (size == 0 ? 1 : size);
	}
	
	public static Vec2 getRelativeOffset(ITexture base, double baseAlignX, double baseAlignY,
			ITexture rel, double relAlignX, double relAlignY)
	{
		Vec2 offset0 = getImageOffset(base, baseAlignX, baseAlignY);
		Vec2 offset1 = getImageOffset(rel, relAlignX, relAlignY);
		
		offset1.sub(offset0);
		return offset1;
	}
	
	public static Rect2D getBounds(ITexture tex, double alignX, double alignY) {
		if (tex == null) {
			return getBounds(0, 0, alignX, alignY);
		} else {
			return getBounds(tex.getWidth(), tex.getHeight(), alignX, alignY);
		}
	}
	public static Rect2D getBounds(double w, double h, double alignX, double alignY) {
		return new Rect2D(getImageOffset(w, alignX), getImageOffset(h, alignY), w, h);
	}
	
	public static Rect2D getBounds(ITexture tex0, double alignX0, double alignY0,
			ITexture tex1, double alignX1, double alignY1)
	{
		Rect2D r0 = getBounds(tex0, alignX0, alignY0);
		Rect2D r1 = getBounds(tex1, alignX1, alignY1);
		double x = Math.min(r0.x, r1.x);
		double y = Math.min(r0.y, r1.y);
		double w = Math.max(r0.x+r0.w-x, r1.x+r1.w-x);
		double h = Math.max(r0.y+r0.h-y, r1.y+r1.h-y);
		return new Rect2D(x, y, Math.max(0, w), Math.max(0, h));
	}

	public static Vec2 layoutSubRect(double boundsW, double boundsH,
			double subW, double subH, int anchor)
	{
		double dx = 0, dy = 0;			
		if (anchor == 2 || anchor == 5 || anchor == 8) {
			dx = (boundsW-subW) / 2;
		} else if (anchor == 3 || anchor == 6 || anchor == 9) {
			dx = (boundsW-subW);
		}			
		if (anchor >= 4 && anchor <= 6) {
			dy = (boundsH-subH) / 2;
		} else if (anchor >= 1 && anchor <= 3) {
			dy = (boundsH-subH);
		}
		
		return new Vec2(dx, dy);
	}

	public static Vec2 alignSubRect(Rect2D base, double w, double h, int anchor) {
		Vec2 p = layoutSubRect(base.w, base.h, 0, 0, anchor);
		Vec2 offset = layoutSubRect(w, h, 0, 0, anchor);
		
		//System.out.println(base + " " + p + " " + offset);
		
		offset.x -= p.x + base.x;
		if (w != 0) offset.x /= w;
		offset.y -= p.y + base.y;
		if (h != 0) offset.y /= h;
		
		return offset;
	}	
}
