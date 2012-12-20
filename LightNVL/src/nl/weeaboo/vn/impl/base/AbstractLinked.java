package nl.weeaboo.vn.impl.base;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import nl.weeaboo.lua2.io.LuaSerializable;
import nl.weeaboo.vn.ILinked;

@LuaSerializable
public abstract class AbstractLinked implements ILinked, Serializable {

	private static final long serialVersionUID = BaseImpl.serialVersionUID;

	private static final int[] EMPTY = new int[0];
	
	private Map<Integer, String> links;
	private List<Integer> pressed;
	
	public AbstractLinked() {		
		links = new HashMap<Integer, String>();
		pressed = new ArrayList<Integer>(2);
	}
	
	//Functions
	@Override
	public void clearLinks() {
		links.clear();
		pressed.clear();
	}

	@Override
	public int[] consumePressedLinks() {
		if (pressed.isEmpty()) {
			return EMPTY;
		}
		
		int[] result = new int[pressed.size()];
		int t = 0;
		for (Integer p : pressed) {
			result[t++] = p.intValue();
		}
		pressed.clear();
		return result;
	}
	
	//Getters
	@Override
	public String getLink(int tag) {
		return links.get(tag);
	}

	//Setters
	@Override
	public String setLink(int tag, String link) {
		return links.put(tag, link);
	}

	public void onLinkPressed(int tag) {
		pressed.add(tag);
	}
	
}
