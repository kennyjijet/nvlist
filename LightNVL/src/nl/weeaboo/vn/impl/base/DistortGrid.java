package nl.weeaboo.vn.impl.base;

import java.io.Serializable;

import nl.weeaboo.lua2.io.LuaSerializable;

@LuaSerializable
public final class DistortGrid implements Serializable {

	private static final long serialVersionUID = 1L;
	
	private final float[] data;
	private final int width, height;
	private final int scansize;
	
	public DistortGrid() {
		this(2, 2);
	}
	public DistortGrid(int w, int h) {
		if (w < 1 || h < 1) {
			throw new IllegalArgumentException("Invalid size ("+w+"x"+h+"), must be at least 2x2");
		}
		
		data = new float[(w+1) * (h+1) * 2];
		width = w;
		height = h;
		scansize = (w+1) * 2;
	}
	
	//Functions
	
	//Getters
	public float getDistortX(int x, int y) {
		if (x < 0 || y < 0 || x > width || y > height) {
			return 0;
		}
		return data[y * scansize + x * 2];
	}
	public float getDistortY(int x, int y) {
		if (x < 0 || y < 0 || x > width || y > height) {
			return 0;
		}
		return data[y * scansize + x * 2 + 1];
	}
	
	public int getWidth() {
		return width;
	}
	public int getHeight() {
		return height;
	}
	
	//Setters
	public void setDistort(int x, int y, float dx, float dy) {
		data[y * scansize + x * 2    ] = dx;
		data[y * scansize + x * 2 + 1] = dy;
	}
	
}
