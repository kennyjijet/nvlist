package nl.weeaboo.vn.impl.base;

import java.io.Serializable;
import java.util.Collection;

import nl.weeaboo.common.Rect2D;
import nl.weeaboo.lua2.io.LuaSerializable;
import nl.weeaboo.vn.IDrawable;

@LuaSerializable
class LayerState implements Serializable {

	private static final long serialVersionUID = BaseImpl.serialVersionUID;
	
	private Rect2D bounds;
	private short z;
	private boolean visible;
	private LayerContents contents;
	
	public LayerState(double w, double h) {
		bounds = new Rect2D(0, 0, w, h);
		visible = true;
		contents = new LayerContents();
	}
	public LayerState(LayerState ls, short maxDrawableZ) {
		bounds = ls.bounds;
		z = ls.z;
		visible = ls.visible;
		
		contents = new LayerContents();
		for (IDrawable d : ls.getDrawables()) {
			if (d.getZ() <= maxDrawableZ) {
				contents.add(d);
			}
		}
	}
	
	//Functions
	public boolean add(IDrawable d) {
		return contents.add(d);
	}
	public Collection<IDrawable> clear() {
		return contents.clear();
	}
	
	//Getters
	public Rect2D getBounds() { return bounds; }
	public short getZ() { return z; }
	public boolean isVisible() { return visible; }
	
	public IDrawable[] getDrawables() {
		return getDrawables(null);
	}
	public IDrawable[] getDrawables(IDrawable[] out) {
		return getDrawables(out, 0);
	}
	IDrawable[] getDrawables(IDrawable[] out, int zDirection) {
		return contents.getDrawables(out, zDirection);
	}
	
	public boolean contains(IDrawable d) {
		return contents.contains(d);
	}
	
	//Setters
	public void setBounds(Rect2D r) { this.bounds = r; }
	public void setZ(short z) { this.z = z; }
	public void setVisible(boolean v) { visible = v; }
	
}
