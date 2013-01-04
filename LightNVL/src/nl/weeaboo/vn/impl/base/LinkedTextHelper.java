package nl.weeaboo.vn.impl.base;

import nl.weeaboo.lua2.io.LuaSerializable;
import nl.weeaboo.vn.IInput;
import nl.weeaboo.vn.ITextRenderer;

@LuaSerializable
public class LinkedTextHelper extends AbstractLinked {

	private static final long serialVersionUID = BaseImpl.serialVersionUID;

	private final ITextRenderer tr;
	
	public LinkedTextHelper(ITextRenderer tr) {
		this.tr = tr;
	}
	
	//Functions
	public void update(IInput input, double x, double y) {
		double cx = input.getMouseX() - x;
		double cy = input.getMouseY() - y;
		int[] hits = tr.getHitTags((float)cx, (float)cy);
		if (hits != null && hits.length > 0) {
			boolean clicked = false;
			for (int tag : hits) {
				String link = getLink(tag);
				if (link != null) {
					clicked |= input.consumeMouse(); //Only read clicks for tags with associated links.
				}
				
				if (clicked) {
					onLinkPressed(tag);
				} else {
					//onLinkHovered(hit); Future addition?
				}
			}
		}
	}
	
	//Getters
	
	//Setters
	
}
