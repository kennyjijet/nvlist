package nl.weeaboo.vn.layout;

import nl.weeaboo.lua2.io.LuaSerializable;

@LuaSerializable
public class MockLayoutComponent extends AbstractLayoutComponent {

	private static final long serialVersionUID = 1L;
	
	private double x, y, w, h;
	
	public MockLayoutComponent(double x, double y, double w, double h, ILayoutConstraints c) {
		super(c);
		
		this.x = x;
		this.y = y;
		this.w = w;
		this.h = h;
	}

	//Functions
	
	//Getters
	@Override
	public double getX() {
		return x;
	}

	@Override
	public double getY() {
		return y;
	}

	@Override
	public double getWidth() {
		return w;
	}

	@Override
	public double getHeight() {
		return h;
	}
	
	//Setters
	@Override
	public void setPos(double x, double y) {
		this.x = x;
		this.y = y;
	}

	@Override
	public void setSize(double w, double h) {
		this.w = w;
		this.h = h;
	}
	
}
