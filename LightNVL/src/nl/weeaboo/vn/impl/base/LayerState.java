package nl.weeaboo.vn.impl.base;

import java.io.Serializable;
import java.util.Collection;

import nl.weeaboo.lua2.io.LuaSerializable;
import nl.weeaboo.vn.IDrawable;
import nl.weeaboo.vn.ILayer;

@LuaSerializable
class LayerState implements Serializable {

	private static final long serialVersionUID = BaseImpl.serialVersionUID;
	
	private LayerContents contents;
	
	public LayerState() {
		contents = new LayerContents();
	}
	public LayerState(LayerState ls, short maxDrawableZ) {
		contents = new LayerContents();
		for (IDrawable d : ls.getContents()) {
			if (d.getZ() <= maxDrawableZ) {
				contents.add(d);
				if (d instanceof ILayer) {
					//We need to also recursively push the contents of nested layers
					ILayer l = (ILayer)d;
					l.pushContents();
				}
			}
		}
	}
	
	//Functions
	public Collection<IDrawable> destroy() {
		Collection<IDrawable> c = contents.clear();
		for (IDrawable d : c) {
			if (d instanceof ILayer) {
				//Pop the recursively pushed layers
				ILayer l = (ILayer)d;
				l.popContents();
			}
		}
		return c;
	}
	public boolean add(IDrawable d) {
		return contents.add(d);
	}
	
	//Getters
	public IDrawable[] getContents() {
		return getContents(null);
	}
	public IDrawable[] getContents(IDrawable[] out) {
		return getContents(out, 0);
	}
	IDrawable[] getContents(IDrawable[] out, int zDirection) {
		return contents.getDrawables(out, zDirection);
	}
	
	public boolean contains(IDrawable d) {
		return contents.contains(d);
	}
	
	//Setters
	
}
