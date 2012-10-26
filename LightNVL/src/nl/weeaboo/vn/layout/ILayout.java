package nl.weeaboo.vn.layout;

import java.io.Serializable;
import java.util.Collection;

import nl.weeaboo.common.Rect2D;

public interface ILayout extends Serializable {

	public void layout(Rect2D bounds, Collection<LayoutComponent> components);
	
}
