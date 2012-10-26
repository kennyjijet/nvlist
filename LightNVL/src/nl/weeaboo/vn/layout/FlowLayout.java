package nl.weeaboo.vn.layout;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import nl.weeaboo.common.Rect2D;
import nl.weeaboo.lua2.io.LuaSerializable;
import nl.weeaboo.vn.IDrawable;
import nl.weeaboo.vn.impl.base.LayoutUtil;

@LuaSerializable
public class FlowLayout implements ILayout {

	private static final long serialVersionUID = LayoutImpl.serialVersionUID;

	private int anchor;
	private double ipad;
	private double maxWidth;
	private int cols;
	
	public FlowLayout() {
		anchor = 7;
		ipad = 0;
		maxWidth = -1;
		cols = -1;
	}
	
	//Functions
	@Override
	public void layout(Rect2D bounds, Collection<LayoutComponent> components) {
		if (components.isEmpty()) {
			return;
		}
		
		final double maxLineWidth = maxWidth;
		double lineWidth = 0;
				
		//Separate into lines
		List<List<LayoutComponent>> lines = new ArrayList<List<LayoutComponent>>();
		List<LayoutComponent> currentLine = new ArrayList<LayoutComponent>();
		for (LayoutComponent lc : components) {
			IDrawable d = lc.component;
			double width = d.getWidth();
			
			final boolean fitsWidth = (maxLineWidth < 0 || lineWidth + ipad + width <= maxLineWidth);
			final boolean fitsCols  = (cols < 0 || lines.size() <= cols);
			if (!fitsWidth || !fitsCols) {
				if (!currentLine.isEmpty()) {
					lines.add(currentLine);
					currentLine = new ArrayList<LayoutComponent>();					
				}
				lineWidth = 0;
			}
			
			currentLine.add(lc);
		}
		if (!currentLine.isEmpty()) {
			lines.add(currentLine);
		}
		
		//Calculate minimum required width
		double w = 0;
		double[] lws = new double[lines.size()];
		double[] lhs = new double[lws.length];
		int t = 0;
		for (List<LayoutComponent> line : lines) {				
			lws[t] = (line.size()-1) * ipad + LayoutUtil2.getMaxComponentWidth(line);
			lws[t] = LayoutUtil2.getMaxComponentHeight(line);
			w = Math.max(w, lws[t]);
			t++;
		}
		
		//Do line layout
		double x = 0;
		double y = 0;
		t = 0;
		for (List<LayoutComponent> line : lines) {
			double lw = lws[t];
			double lh = lhs[t];
			t++;
			
			double cx = x + LayoutUtil.alignAnchorX(w, lw, anchor);
			for (LayoutComponent lc : line) {
				IDrawable d = lc.component;
				d.setPos(cx, y + LayoutUtil.alignAnchorY(lh, d.getHeight(), anchor));
				cx += ipad + d.getWidth();
			}			
			y += ipad + lh;
		}
	}
	
	//Getters
	
	//Setters
	
}
