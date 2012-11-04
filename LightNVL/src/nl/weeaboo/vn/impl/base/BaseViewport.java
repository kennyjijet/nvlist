package nl.weeaboo.vn.impl.base;

import java.io.Serializable;
import java.util.LinkedList;

import nl.weeaboo.common.Insets2D;
import nl.weeaboo.common.Rect2D;
import nl.weeaboo.common.StringUtil;
import nl.weeaboo.lua2.io.LuaSerializable;
import nl.weeaboo.vn.BlendMode;
import nl.weeaboo.vn.IButtonDrawable;
import nl.weeaboo.vn.IContainer;
import nl.weeaboo.vn.IDrawBuffer;
import nl.weeaboo.vn.IDrawable;
import nl.weeaboo.vn.IInput;
import nl.weeaboo.vn.ILayer;
import nl.weeaboo.vn.ITexture;
import nl.weeaboo.vn.IViewport;
import nl.weeaboo.vn.layout.ILayoutComponent;
import nl.weeaboo.vn.math.Matrix;
import nl.weeaboo.vn.math.Vec2;

@LuaSerializable
public abstract class BaseViewport extends BaseContainer implements IViewport {

	private static final long serialVersionUID = BaseImpl.serialVersionUID;
	
	private final ILayer layer;
	private double fadeSize;
	private int fadeColorARGB;
	private ITexture fadeTop, fadeBottom;
	private Rect2D generatedVirtualBounds, explicitVirtualBounds;	
	
	private ScrollBar scrollBarX, scrollBarY;
	private ScrollInfo scrollX, scrollY;
	private double scrollWheelSpeed;
	private double snapInertia;
	private double dragSnap;
	
	private Vec2 mousePos;
	private double snap;
		
	public BaseViewport(ILayer layer) {
		this.layer = layer;
		
		fadeColorARGB = 0xFFFFFFFF;
		scrollX = new ScrollInfo();
		scrollY = new ScrollInfo();
		scrollWheelSpeed = 8;
		snapInertia = .8;
		dragSnap = .2;
		
		double w = layer.getWidth();
		double h = layer.getHeight();
		
		super.setPos(layer.getX(), layer.getY());
		super.setSize(w, h);
		super.setZ((short)(layer.getZ()+1));
	}
	
	//Functions
	@Override
	public void destroy() {
		if (!isDestroyed()) {
			layer.destroy();
			super.destroy();
		}
	}

	@Override
	public void add(ILayoutComponent lc) {
		if (!contains(lc)) {
			IDrawable d = tryGetDrawable(lc);
			if (d != null) {
				layer.add(d);				
			}			
			
			Insets2D padding = getPadding();
			lc.setPos(lc.getX()+padding.left, lc.getY()+padding.top);
			
			super.add(lc);
		}
	}

	@Override
	public void remove(ILayoutComponent lc) {
		if (contains(lc)) {
			Insets2D padding = getPadding();
			lc.setPos(lc.getX()-padding.left, lc.getY()-padding.top);
			
			super.remove(lc);
		}
	}
	
	protected void onResized() {
		layer.setBounds(getX(), getY(), getInnerWidth(), getInnerHeight());
		
		invalidateLayout();
		calculateScrollLimits();
	}

	@Override
	public boolean update(ILayer layer, IInput input, double effectSpeed) {
		if (super.update(layer, input, effectSpeed)) {
			markChanged();
		}
		
		double oldxpos = scrollX.pos;
		double oldypos = scrollY.pos;
		
		Vec2 mold = (mousePos != null ? mousePos.clone() : null);
		Vec2 mnew = new Vec2(input.getMouseX(), input.getMouseY());
		boolean mouseContained = (isClipEnabled() || layer.containsRel(mnew.x, mnew.y))
				&& contains(mnew.x, mnew.y) && isVisible();
		
		boolean dragging = false;
		if (input.isMouseHeld()) {
			if (mold != null) {
				dragging = true;
			} else if (mouseContained && /*input.consumeMouse()*/input.isMousePressed()) {
				dragging = true;
				onStartDrag();
			}
		}
		
		//Calculate mouse drag
		if (dragging) {		    
		    scrollX.spd = (scrollX.min < scrollX.max && mold != null ? mold.x-mnew.x : 0);
		    scrollY.spd = (scrollY.min < scrollY.max && mold != null ? mold.y-mnew.y : 0);
			
		    snap = dragSnap;
			mousePos = new Vec2(mnew.x, mnew.y);
		} else {		    
	    	//Scrolling with the scroll wheel
		    int mscroll = input.getMouseScroll();
		    if (mouseContained && mscroll != 0) {
		        snap = 1.0;
		        scrollY.spd = scrollWheelSpeed * mscroll;
		    }

			mousePos = null;
		}
		
		//Limit speed and update pos
		double scrollInertia = (dragging ? 1 : snapInertia);
		double scrollSnap = (dragging && dragSnap < 1 ? 0 : snap);
		scrollX.update(scrollInertia, scrollSnap);
		scrollY.update(scrollInertia, scrollSnap);
		
		//Update component positions
		if (scrollX.pos != oldxpos || scrollY.pos != oldypos) {
			translateComponents(oldxpos-scrollX.pos, oldypos-scrollY.pos);
			markChanged();
		}
				
		return consumeChanged();
	}
	
	protected void onStartDrag() {
		//Recursively traverses this container and its children and calls cancelMouseArmed() on each drawable.
		
		LinkedList<IContainer> todo = new LinkedList<IContainer>();
		todo.add(this);
		
		IDrawable[] tempArray = new IDrawable[16];
		while (!todo.isEmpty()) {
			IContainer c = todo.removeFirst();
			tempArray = c.getDrawableComponents(tempArray);
			for (IDrawable d : tempArray) {
				if (d == null) break;

				if (d instanceof IContainer) {
					todo.addLast((IContainer)d);
				}
				
				if (d instanceof IButtonDrawable) {
					IButtonDrawable b = (IButtonDrawable)d;
					b.cancelMouseArmed();
				}
			}
		}
	}
	
	@Override
	public void draw(IDrawBuffer d) {
		super.draw(d);
		
		short z = getZ();
		boolean clipEnabled = isClipEnabled();
		Matrix transform = getTransform();
		double w = getWidth();
		double h = getHeight();
		double iw = getInnerWidth();
		double ih = getInnerHeight();

		short overlayZ = (short)Math.max(Short.MIN_VALUE, z - 100);
		
		//Draw fading edges
		if (fadeTop != null) {
			double fh = Math.min(fadeSize, scrollY.pos-scrollY.min);
			if (fh >= 1) {
				d.drawQuad(overlayZ, clipEnabled, BlendMode.DEFAULT, fadeColorARGB, fadeTop, transform,
						0, 0, iw, fh, null);
			}
		}
		if (fadeBottom != null) {
			double fh = Math.min(fadeSize, scrollY.max-scrollY.pos);
			if (fh >= 1) {
				d.drawQuad(overlayZ, clipEnabled, BlendMode.DEFAULT, fadeColorARGB, fadeBottom, transform,
						0, ih-fh, iw, fh, null);
			}
		}
		
		//Draw scrollbars
		if (scrollBarX != null) {
			double bh = scrollBarX.thickness;
			Insets2D m = scrollBarX.margin;
			scrollBarX.draw(d, overlayZ, clipEnabled, transform, m.left, h-bh-m.bottom,
					iw-m.left-m.right, bh, scrollX.frac());
		}
		if (scrollBarY != null) {
			double bw = scrollBarY.thickness;
			Insets2D m = scrollBarY.margin;
			scrollBarY.draw(d, overlayZ, clipEnabled, transform, w-bw-m.right, m.top,
					bw, h-m.top-m.bottom, scrollY.frac());
		}
	}

	@Override
	public void layout() {
		super.layout();
		
		invalidateGeneratedVirtualBounds();

		calculateScrollLimits();
	}
	
	protected void invalidateGeneratedVirtualBounds() {
		generatedVirtualBounds = null;
	}

	protected Rect2D generateVirtualBounds() {
		validateLayout();
		
		//Calculate bounds
		double minX = Double.MAX_VALUE;
		double maxX = Double.MIN_VALUE;
		double minY = Double.MAX_VALUE;
		double maxY = Double.MIN_VALUE;
		for (ILayoutComponent lc : getLayoutComponents()) {
			double x = lc.getX();
			double y = lc.getY();
			double w = lc.getWidth();
			double h = lc.getHeight();
			minX = Math.min(minX, x);
			maxX = Math.max(maxX, x+w);
			minY = Math.min(minY, y);
			maxY = Math.max(maxY, y+h);
		}
		
		Insets2D pad = getPadding();
		double w = (maxX >= minX ? maxX-minX : getInnerWidth());
		double h = (maxY >= minY ? maxY-minY : getInnerHeight());
		
		return new Rect2D(minX-pad.left, minY-pad.top, w+pad.left+pad.right, h+pad.top+pad.bottom);
	}
	
	protected void calculateScrollLimits() {
		validateLayout();
		
		Rect2D ibounds = getInnerBounds();
		Rect2D vbounds = getVirtualBounds();
		
		scrollX.min = 0;
		scrollX.max = Math.max(scrollX.min, vbounds.w - ibounds.w);
		
		scrollY.min = 0;
		scrollY.max = Math.max(scrollY.min, vbounds.h - ibounds.h);
		
		//System.out.println(ibounds + " ** " + vbounds);
		//System.out.println(scrollX + " :: " + scrollY);		
	}
	
	public void scrollToVisible(IDrawable d) {
		Rect2D r = d.getBounds();
		scrollToVisible(r.x+getScrollX(), r.y+getScrollY(), r.w, r.h);
	}
	
	public void scrollToVisible(ILayoutComponent lc) {
		Rect2D r = lc.getBounds();
		scrollToVisible(r.x+getScrollX(), r.y+getScrollY(), r.w, r.h);
	}
	
	public void scrollToVisible(double rx, double ry, double rw, double rh) {
		validateLayout();
		
		Insets2D pad = getPadding();
		Rect2D ibounds = getInnerBounds();
		Rect2D visible = new Rect2D(getScrollX()+pad.left, getScrollY()+pad.top,
				ibounds.w-pad.left-pad.right, ibounds.h-pad.top-pad.bottom);
		
		if (visible.contains(rx, ry, rw, rh)) {
			//Nothing to do
			return;
		}

		double sx = Math.min(0, rx - visible.x) + Math.max(0, (rx+rw) - (visible.x+visible.w));
		double sy = Math.min(0, ry - visible.y) + Math.max(0, (ry+rh) - (visible.y+visible.h));
		
		//System.out.println(ry + "-" + rh + " " + sy + " " + visible.y);
		
		setScroll(getScrollX() + sx, getScrollY() + sy);
	}
	
	//Getters
	@Override
	public ILayer getLayer() {
		return layer;
	}
	
	@Override
	public double getFadeSize() {
		return fadeSize;
	}
	
	@Override
	public int getFadeColorRGB() {
		return fadeColorARGB & 0xFFFFFF;
	}
	
	@Override
	public ITexture getFadeTop() {
		return fadeTop;
	}
	
	@Override
	public ITexture getFadeBottom() {
		return fadeBottom;
	}
	
	@Override
	public double getInnerWidth() {
		double scrollBarSize = 0;
		if (scrollBarY != null) {
			scrollBarSize = scrollBarY.thickness + scrollBarY.margin.left + scrollBarY.margin.right;
		}
		return Math.max(0, getWidth() - scrollBarSize);
	}

	@Override
	public double getInnerHeight() {
		double scrollBarSize = 0;
		if (scrollBarX != null) {
			scrollBarSize = scrollBarX.thickness + scrollBarX.margin.top + scrollBarX.margin.bottom;
		}
		return Math.max(0, getHeight() - scrollBarSize);
	}
	
	@Override
	public Rect2D getInnerBounds() {
		return new Rect2D(getX(), getY(), Math.max(0, getInnerWidth()), Math.max(0, getInnerHeight()));
	}
	
	@Override
	public Rect2D getLayoutBounds() {
		Insets2D padding = getPadding();
		return new Rect2D(padding.left-scrollX.pos, padding.top-scrollY.pos,
				Math.max(0, getInnerWidth()-padding.left-padding.right),
				Math.max(0, getInnerHeight()-padding.top-padding.bottom));
	}
	
	@Override
	public Rect2D getVirtualBounds() {
		if (explicitVirtualBounds != null) {
			return explicitVirtualBounds;
		}
		
		if (generatedVirtualBounds == null) {
			generatedVirtualBounds = generateVirtualBounds();
			calculateScrollLimits();
		}
		return generatedVirtualBounds;
	}
	
	@Override
	public double getScrollX() {
		return scrollX.pos;
	}
	
	@Override
	public double getScrollY() {
		return scrollY.pos;
	}
	
	@Override
	public double getDragSnap() {
		return dragSnap;
	}
	
	@Override
	public boolean canScrollX() {
		calculateScrollLimits();
		return scrollX.max > scrollX.min;
	}
	
	@Override
	public boolean canScrollY() {
		calculateScrollLimits();
		return scrollY.max > scrollY.min;
	}
	
	@Override
	public boolean hasScrollBarX() {
		return scrollBarX != null;
	}
	
	@Override
	public boolean hasScrollBarY() {
		return scrollBarY != null;
	}
	
	//Setters
	@Override
	public void setVirtualBounds(double x, double y, double w, double h) {
		Rect2D r = explicitVirtualBounds;
		if (r == null || r.x != x || r.y != y || r.w != w || r.h != h) {
			explicitVirtualBounds = new Rect2D(x, y, w, h);
			markChanged();
			calculateScrollLimits();
		}
	}
	
	@Override
	public void setScroll(double sx, double sy) {
		double oldSx = scrollX.pos;
		double oldSy = scrollY.pos;
		if (sx != oldSx || sy != oldSy) {
			scrollX.pos = sx;
			scrollY.pos = sy;
			
			translateComponents(oldSx-sx, oldSy-sy);
		}
	}
	
	@Override
	public void setScrollFrac(double fracX, double fracY) {
		validateLayout();
		
		setScroll(scrollX.min + fracX * (scrollX.max-scrollX.min),
				scrollY.min + fracY * (scrollY.max-scrollY.min));
	}
	
	@Override
	public void setPos(double x, double y) {
		if (x != getX() || y != getY()) {
			super.setPos(x, y);
			onResized();
		}
	}
	
	@Override
	public void setSize(double w, double h) {
		if (w != getWidth() || h != getHeight()) {
			super.setSize(w, h);
			onResized();
		}
	}
	
	@Override
	public void setZ(short z) {
		if (z != getZ()) {
			super.setZ(z);
			layer.setZ((short)(z-1));
		}
	}
		
	@Override
	public void setFadingEdges(double size, int colorRGB, ITexture top, ITexture bottom) {
		int color = (fadeColorARGB&0xFF000000) | (colorRGB&0xFFFFFF);
		
		if (fadeSize != size || fadeColorARGB != color || fadeTop != top || fadeBottom != bottom) {
			fadeSize = size;
			fadeColorARGB = color;
			fadeTop = top;
			fadeBottom = bottom;
			markChanged();
		}
	}
	
	@Override
	public void setDragSnap(double ds) {
		dragSnap = ds;
	}
	
	public void setScrollBarX(double height, ITexture bar, ITexture thumb,
			double marginTop, double marginRight, double marginBottom, double marginLeft)
	{
		Insets2D margin = new Insets2D(marginTop, marginRight, marginBottom, marginLeft);
		scrollBarX = new ScrollBar(height, bar, thumb, margin, true);
		markChanged();
		onResized();
	}
	
	public void setScrollBarY(double width, ITexture bar, ITexture thumb,
			double marginTop, double marginRight, double marginBottom, double marginLeft)
	{
		Insets2D margin = new Insets2D(marginTop, marginRight, marginBottom, marginLeft);
		scrollBarY = new ScrollBar(width, bar, thumb, margin, false);
		markChanged();
		onResized();
	}
	
	//Inner Classes
	@LuaSerializable
	protected static class ScrollInfo implements Serializable {
		
		private static final long serialVersionUID = 1L;
		
		public double min, max, pos, spd;
		
		public void update(double inertia, double snap) {
			//Update speed/pos
			if (Math.abs(spd) < 1) {
				spd = 0;
			} else {
				spd *= inertia;
				pos += spd;
			}
			
			//Update snapback
			if (Math.abs(snap) > .001) {
				if (pos < min) {
					double dist = min - pos;
					if (Math.abs(dist) < 1) {
						pos = min;
					} else {
						pos = Math.min(min, pos + dist * snap);
					}
				} else if (pos > max) {
					double dist = max - pos;
					if (Math.abs(dist) < 1) {
						pos = max;
					} else {
						pos = Math.max(max, pos + dist * snap);
					}
				}
			}
		}
		
		public double frac() {
			if (max <= min) {
				return Double.NaN;
			}
			return Math.max(0, Math.min(1, (pos-min)/(max-min)));
		}
		
		@Override
		public String toString() {
			return String.format(StringUtil.LOCALE, "[min=%.2f, max=%.2f, pos=%.2f, spd=%.2f]", min, max, pos, spd);
		}	
	}
	
	@LuaSerializable
	protected static class ScrollBar implements Serializable {
		
		private static final long serialVersionUID = 1L;

		public double thickness;
		public ITexture barTex, thumbTex;
		public Insets2D margin;
		public boolean horizontal;
		
		public ScrollBar(double thickness, ITexture barTex, ITexture thumbTex, Insets2D margin, boolean horizontal) {
			this.thickness = thickness;
			this.barTex = barTex;
			this.thumbTex = thumbTex;
			this.margin = margin;
			this.horizontal = horizontal;
		}
		
		public void draw(IDrawBuffer d, short z, boolean clipEnabled, Matrix transform,
				double x, double y, double w, double h, double scrollFrac)
		{
			if (barTex != null) {
				d.drawQuad(z, clipEnabled, BlendMode.DEFAULT, 0xFFFFFFFF, barTex, transform, x, y, w, h, null);
			}
			if (thumbTex != null && !Double.isNaN(scrollFrac)) {
				double tx = x, ty = y, tw = w, th = h;
				if (horizontal) {
					tw = thumbTex.getWidth() * (th / thumbTex.getHeight());
					tx += scrollFrac * (w - tw);
				} else {
					th = thumbTex.getHeight() * (tw / thumbTex.getWidth());
					ty += scrollFrac * (h - th);
				}
				d.drawQuad((short)(z-1), clipEnabled, BlendMode.DEFAULT, 0xFFFFFFFF, thumbTex, transform,
						tx, ty, tw, th, null);
			}
		}
		
	}
	
}
