package nl.weeaboo.vn.layout;

import java.util.Collection;

import nl.weeaboo.common.Rect2D;
import nl.weeaboo.lua2.io.LuaSerializable;

@LuaSerializable
public final class NullLayout extends AbstractLayout {

	private static final long serialVersionUID = LayoutImpl.serialVersionUID;

	public static NullLayout INSTANCE = new NullLayout();
	
	private NullLayout() {		
	}
	
	//Functions
	@Override
	public void layout(Rect2D bounds, Collection<ILayoutComponent> components) {
	}

	//Getters
	
	//Setters
	
}
