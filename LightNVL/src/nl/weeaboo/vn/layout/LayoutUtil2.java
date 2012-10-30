package nl.weeaboo.vn.layout;

import java.util.Collection;

public final class LayoutUtil2 {

	private LayoutUtil2() {
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
	
}
