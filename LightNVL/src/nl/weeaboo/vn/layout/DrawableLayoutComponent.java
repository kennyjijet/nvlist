package nl.weeaboo.vn.layout;

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
	@Override
	public double getX() {
		return d.getX();
	}

	@Override
	public double getY() {
		return d.getY();
	}

	@Override
	public double getWidth() {
		return d.getWidth();
	}

	@Override
	public double getHeight() {
		return d.getHeight();
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
