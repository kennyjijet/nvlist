package nl.weeaboo.vn.layout;

import nl.weeaboo.lua2.io.LuaSerializable;
import nl.weeaboo.vn.IDrawable;

@LuaSerializable
public class LayoutComponent {

	public final IDrawable component;
	public final ILayoutConstraints constraints;
	
	public LayoutComponent(IDrawable component, ILayoutConstraints constraints) {
		this.component = component;
		this.constraints = constraints;
	}
	
	//Functions
	
	//Getters
	
	//Setters
	
}
