package nl.weeaboo.vn.layout;

import nl.weeaboo.common.Rect2D;
import nl.weeaboo.lua2.io.LuaSerializable;
import nl.weeaboo.vn.IDrawable;

@LuaSerializable
public class DrawableLayoutComponent extends AbstractLayoutComponent {

	private static final long serialVersionUID = 1L;

	private final IDrawable d;
	
	public DrawableLayoutComponent(IDrawable d, ILayoutConstraints c) {
		super(c);
		
		this.d = d;
	}
	
	//Functions
	
	//Getters
	public IDrawable getDrawable() {
		return d;
	}
	
	@Override
	public double getX() {
		return getBounds().x;
	}

	@Override
	public double getY() {
		return getBounds().y;
	}

	@Override
	public double getWidth() {
		return getBounds().w;
	}

	@Override
	public double getHeight() {
		return getBounds().h;
	}
	
	@Override
	public Rect2D getBounds() {
		return d.getBounds();
	}
	
	//Setters
	@Override
	public void setPos(double x, double y) {
		d.setPos(x, y);
	}

	@Override
	public void setSize(double w, double h) {
		d.setSize(w, h);
	}
	
}
