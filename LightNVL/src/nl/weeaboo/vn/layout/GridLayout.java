package nl.weeaboo.vn.layout;

import java.util.Collection;

import nl.weeaboo.common.Rect2D;
import nl.weeaboo.lua2.io.LuaSerializable;

@LuaSerializable
public class GridLayout extends AbstractLayout {

	private static final long serialVersionUID = LayoutImpl.serialVersionUID;

	private int pack;
	private int anchor;
	private double ipad;
	private int cols;
	private boolean leftToRight;
	private boolean stretchW, stretchH;
	private boolean shrinkW, shrinkH;
	
	public GridLayout() {
		pack = 0;
		anchor = 5;
		ipad = 0;
		cols = -1;
		leftToRight = true;
	}
	
	//Functions
	@Override
	public void layout(Rect2D bounds, Collection<ILayoutComponent> components) {
		if (components.isEmpty()) {
			return;
		}
		
		int cols = this.cols;
		int rows = 1;
		if (cols >= 0) {
			rows = (int)Math.ceil(components.size() / (double)cols);
		} else {
			cols = components.size();
		}

		if (rows <= 0 || cols <= 0) {
			return;
		}
		
		double startX = bounds.x;
		double startY = bounds.y;
		double colW = (bounds.w - (cols-1) * ipad) / cols;
		double rowH = (bounds.h - (rows-1) * ipad) / rows;
		double gridW = bounds.w;
		double gridH = bounds.h;
		
		if (pack > 0) {
			colW = Math.min(colW, LayoutUtil.getMaxComponentWidth(components));
			rowH = Math.min(rowH, LayoutUtil.getMaxComponentHeight(components));
			gridW = cols*(colW+ipad)-ipad;
			gridH = rows*(rowH+ipad)-ipad;
			if (bounds.w > gridW) startX += LayoutUtil.alignAnchorX(bounds.w, gridW, pack);
			if (bounds.h > gridH) startY += LayoutUtil.alignAnchorY(bounds.h, gridH, pack);
		}
		
		if (!leftToRight) {
			startX += gridW - colW;
		}
		
		int col = 0;
		double x = startX;
		double y = startY;
		for (ILayoutComponent lc : components) {
			if (shrinkW || shrinkH || stretchW || stretchH) {
				double lcw = lc.getWidth();
				double lch = lc.getHeight();
				
				double cellW = lcw;
				double cellH = lch;
				
				if (stretchW) {
					cellW = colW;
				} else if (shrinkW) {
					cellW = Math.min(lcw, colW);
				}
				
				if (stretchH) {
					cellH = rowH;
				} else if (shrinkH) {
					cellH = Math.min(lch, rowH);
				}
				
				lc.setSize(cellW, cellH);
			}
			
			double cx = x + LayoutUtil.alignAnchorX(colW, lc.getWidth(), anchor);
			double cy = y + LayoutUtil.alignAnchorY(rowH, lc.getHeight(), anchor);
			lc.setPos(cx, cy);
			
			if (leftToRight) {
				x += colW + ipad;
			} else {
				x -= colW + ipad;
			}
			
			col++;
			if (cols >= 0 && col >= cols) {
				col = 0;
				x = startX;
				y += rowH + ipad;
			}
		}
	}
	
	//Getters
	public int getCols() {
		return cols;
	}
	
	public int getPack() {
		return pack;
	}
	
	public int getAnchor() {
		return anchor;
	}
	
	public double getPadding() {
		return ipad;
	}
	
	public boolean isLeftToRight() {
		return leftToRight;
	}
	
	//Setters	
	/**
	 * Sets the maximum number of components per row. Use <code>-1</code> for no limit. 
	 */
	public void setCols(int c) {
		cols = c;
	}
	
	/**
	 * Setting <code>pack</code> to non-zero uses grid cell sizes just large
	 * enough to hold the largest component instead of filling the entire
	 * allotted area. The specific value of <code>pack</code> determines the
	 * alignment of the packed grid inside the bounding rectangle, with the
	 * direction corresponding to the directions of the number keys on a
	 * keyboard's numpad.
	 */
	public void setPack(int p) {
		pack = p;
	}
	
	/**
	 * Changes the alignment of the components if there's space left. The anchor
	 * values correspond to the directions of the number keys on a keyboard's
	 * numpad.
	 */
	public void setAnchor(int a) {
		anchor = a;
	}
	
	/**
	 * Changes the amount of padding between each component.
	 */
	public void setPadding(double p) {
		ipad = p;
	}
	
	/**
	 * Toggles the order of items per line between left-to-right and right-to-left. 
	 */
	public void setLeftToRight(boolean ltr) {
		leftToRight = ltr;
	}
	
	/**
	 * @see #setStretch(boolean, boolean)
	 */
	public void setStretch(boolean stretch) {
		setStretch(stretch, stretch);
	}
	
	/**
	 * Changes in which directions the contents of a cell are stretched.
	 * 
	 * @param horizontal Stretch in horizontal direction.
	 * @param vertical Stretch in vertical direction.
	 */
	public void setStretch(boolean horizontal, boolean vertical) {
		stretchW = horizontal;
		stretchH = vertical;
	}
	
	/**
	 * @see #setShrink(boolean, boolean)
	 */
	public void setShrink(boolean stretch) {
		setShrink(stretch, stretch);
	}
	
	/**
	 * Changes in which directions the contents of a cell may be shrunk.
	 * 
	 * @param horizontal Shrink in horizontal direction.
	 * @param vertical Shrink in vertical direction.
	 */
	public void setShrink(boolean horizontal, boolean vertical) {
		shrinkW = horizontal;
		shrinkH = vertical;
	}
	
}
