package nl.weeaboo.vn.layout;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

import nl.weeaboo.common.Rect2D;
import nl.weeaboo.lua2.io.LuaSerializable;
import nl.weeaboo.vn.impl.base.LayoutUtil;

@LuaSerializable
public class FlowLayout extends AbstractLayout {

	private static final long serialVersionUID = LayoutImpl.serialVersionUID;

	private int pack;
	private int anchor;
	private double ipad;
	private int cols;
	private boolean leftToRight;
	
	public FlowLayout() {
		pack = 0;
		anchor = 7;
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
		
		final double maxLineWidth = bounds.w;
		double lineWidth = 0;
				
		//Separate into lines
		List<List<ILayoutComponent>> lines = new ArrayList<List<ILayoutComponent>>();
		List<ILayoutComponent> currentLine = new ArrayList<ILayoutComponent>();
		for (ILayoutComponent lc : components) {
			double width = lc.getWidth();
			if (!currentLine.isEmpty()) {
				width += ipad;
			}
			
			final boolean fitsWidth = (maxLineWidth < 0 || lineWidth + width <= maxLineWidth);
			final boolean fitsCols  = (cols < 0 || currentLine.size() < cols);
			if (!fitsWidth || !fitsCols) {
				if (!currentLine.isEmpty()) {
					lines.add(currentLine);
					currentLine = new ArrayList<ILayoutComponent>();					
				}
				lineWidth = 0;
			}
			
			currentLine.add(lc);
			lineWidth += width;
		}
		if (!currentLine.isEmpty()) {
			lines.add(currentLine);
		}
		
		//Flip components if right-to-left
		if (!leftToRight) {
			for (List<ILayoutComponent> line : lines) {
				Collections.reverse(line);
			}
		}
		
		//Calculate minimum required width
		double w = (pack > 0 || maxLineWidth < 0 ? 0 : maxLineWidth);
		double h = (lines.size()-1) * ipad;
		double[] lws = new double[lines.size()];
		double[] lhs = new double[lws.length];
		int t = 0;
		for (List<ILayoutComponent> line : lines) {				
			lws[t] = (line.size()-1) * ipad;
			for (ILayoutComponent lc : line) {
				lws[t] += lc.getWidth();
				lhs[t] = Math.max(lhs[t], lc.getHeight());
			}
			w = Math.max(w, lws[t]);
			h += lhs[t];
			t++;
		}
		
		//Do line layout
		double x = bounds.x;
		double y = bounds.y;
		if (pack > 0) {
			if (bounds.w > w) x += LayoutUtil.alignAnchorX(bounds.w, w, pack);
			if (bounds.h > h) y += LayoutUtil.alignAnchorY(bounds.h, h, pack);
		}
		
		t = 0;
		for (List<ILayoutComponent> line : lines) {
			double lw = lws[t];
			double lh = lhs[t];
			t++;
			
			double cx = x + LayoutUtil.alignAnchorX(w, lw, anchor);
			for (ILayoutComponent lc : line) {
				lc.setPos(cx, y + LayoutUtil.alignAnchorY(lh, lc.getHeight(), anchor));
				cx += ipad + lc.getWidth();
				
				//System.out.println(d.getX()+"x"+d.getY() + " " + w + " " + lw + "x" + lh);
			}			
			y += ipad + lh;
		}
	}
	
	//Getters
	public int getPack() {
		return pack;
	}
	
	public int getCols() {
		return cols;
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
	 * The value of <code>pack</code> determines the relative alignment of the
	 * flow layout's rows within its container.
	 */
	public void setPack(int p) {
		pack = p;
	}
	
	/**
	 * Sets the maximum number of components per row. Use <code>-1</code> for no limit. 
	 */
	public void setCols(int c) {
		cols = c;
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
	
}
