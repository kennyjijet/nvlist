package nl.weeaboo.vn.layout;

import java.util.Collection;

public final class LayoutUtil2 {

	private LayoutUtil2() {
	}

	public static double getMaxComponentWidth(Collection<LayoutComponent> components) {
		double w = 0;
		for (LayoutComponent lc : components) w = Math.max(w, lc.component.getWidth());
		return w;
	}
	
	public static double getMaxComponentHeight(Collection<LayoutComponent> components) {
		double h = 0;
		for (LayoutComponent lc : components) h = Math.max(h, lc.component.getHeight());
		return h;
	}
	
}
