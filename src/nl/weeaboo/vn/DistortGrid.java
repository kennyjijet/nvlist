package nl.weeaboo.vn;

import java.io.Serializable;

import nl.weeaboo.lua2.io.LuaSerializable;

@LuaSerializable
public final class DistortGrid implements Serializable {

	private static final long serialVersionUID = 1L;

	private final int width, height;
	private final int scansize;

	private boolean sharedBuffer;
	private float[] data;

	public DistortGrid(int w, int h) {
		if (w < 1 || h < 1) {
			throw new IllegalArgumentException("Invalid size ("+w+"x"+h+"), must be at least 2x2");
		}

		width = w;
		height = h;
		scansize = (w+1) * 2;
		data = new float[getRequiredElements(w, h)];
	}

	private DistortGrid(DistortGrid other) {
		other.sharedBuffer = true;
		data = other.data;
		width = other.width;
		height = other.height;
		scansize = other.scansize;
	}

	//Functions
	public DistortGrid copy() {
		return new DistortGrid(this);
	}

	private static int getRequiredElements(int w, int h) {
		return (w+1) * (h+1) * 2;
	}

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
		if (sharedBuffer) {
			float[] oldData = data;
			data = new float[getRequiredElements(width, height)];
			System.arraycopy(oldData, 0, data, 0, data.length);
			sharedBuffer = false;
		}

		data[y * scansize + x * 2    ] = dx;
		data[y * scansize + x * 2 + 1] = dy;
	}

}
