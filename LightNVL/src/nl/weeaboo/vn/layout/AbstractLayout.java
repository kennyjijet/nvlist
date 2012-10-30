package nl.weeaboo.vn.layout;

import java.util.Arrays;

import nl.weeaboo.common.Rect2D;

public abstract class AbstractLayout implements ILayout {

	private static final long serialVersionUID = 1L;

	//Functions
	@Override
	public void layout(double x, double y, double w, double h, ILayoutComponent[] components) {
		layout(new Rect2D(x, y, w, h), Arrays.asList(components));
	}
	
	//Getters
	
	//Setters
	
}
