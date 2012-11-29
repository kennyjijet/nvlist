package nl.weeaboo.vn.layout;

import java.util.Collection;

import nl.weeaboo.common.Rect2D;
import nl.weeaboo.vn.ITextRenderer;
import nl.weeaboo.vn.ITexture;
import nl.weeaboo.vn.math.Vec2;

public final class LayoutUtil {

	private LayoutUtil() {		
	}

	public static Vec2 getImageOffset(ITexture tex, double alignX, double alignY) {
		if (tex == null) {
			return new Vec2(0, 0);
		}
		return new Vec2(getOffset(tex.getWidth(), alignX),
				getOffset(tex.getHeight(), alignY));
	}
	public static double getOffset(double size, double align) {
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
		return new Rect2D(getOffset(w, alignX), getOffset(h, alignY), w, h);
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

	public static double alignAnchorX(double outer, double inner, int anchor) {		
		if (anchor == 2 || anchor == 5 || anchor == 8) {
			return (outer-inner) / 2;
		} else if (anchor == 3 || anchor == 6 || anchor == 9) {
			return (outer-inner);
		}
		return 0;
	}
	
	public static double alignAnchorY(double outer, double inner, int anchor) {		
		if (anchor >= 4 && anchor <= 6) {
			return (outer-inner) / 2;
		} else if (anchor >= 1 && anchor <= 3) {
			return (outer-inner);
		}
		return 0;
	}
	
	public static Vec2 alignSubRect(Rect2D base, double w, double h, int anchor) {
		Vec2 p = new Vec2(alignAnchorX(base.w, 0, anchor), alignAnchorY(base.h, 0, anchor));
		Vec2 offset = new Vec2(alignAnchorX(w, 0, anchor), alignAnchorY(h, 0, anchor));
		
		//System.out.println(base + " " + p + " " + offset);
		
		offset.x -= p.x + base.x;
		if (w != 0) offset.x /= w;
		
		offset.y -= p.y + base.y;
		if (h != 0) offset.y /= h;
		
		return offset;
	}
	
	public static double getMaxComponentWidth(Collection<ILayoutComponent> components) {
		double w = 0;
		for (ILayoutComponent lc : components) w = Math.max(w, lc.getWidth());
		return w;
	}
	
	public static double getMaxComponentHeight(Collection<ILayoutComponent> components) {
		double h = 0;
		for (ILayoutComponent lc : components) h = Math.max(h, lc.getHeight());
		return h;
	}
	
	public static void getTextRendererXY(Vec2 out, double outerW, double outerH, ITextRenderer tr, int anchor) {
		out.x = LayoutUtil.alignAnchorX(outerW, tr.getTextWidth(), anchor);
		if (tr.isRightToLeft()) {
			out.x -= tr.getTextTrailing();
		} else {
			out.x -= tr.getTextLeading();
		}
		out.y = LayoutUtil.alignAnchorY(outerH, tr.getTextHeight(), anchor);
	}
	
}
