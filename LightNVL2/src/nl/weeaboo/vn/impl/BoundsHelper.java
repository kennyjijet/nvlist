package nl.weeaboo.vn.impl;

import java.io.Serializable;

import nl.weeaboo.common.Rect2D;
import nl.weeaboo.lua2.io.LuaSerializable;

@LuaSerializable
class BoundsHelper implements Serializable {

	private static final long serialVersionUID = 1L;

	private double x, y, w, h;

	private transient IChangeListener changeListener;

	// Functions
	protected final void fireChanged() {
		if (changeListener != null) {
			changeListener.onChanged();
		}
	}

	// Getters
	public double getX() { return x; }
	public double getY() { return y; }
	public double getWidth() { return w; }
	public double getHeight() { return h; }

	public Rect2D getBounds() {
		double w = getWidth();
		double h = getHeight();
		return new Rect2D(x, y, Double.isNaN(w) ? 0 : w, Double.isNaN(h) ? 0 : h);
	}

	public boolean contains(double px, double py) {
		return getBounds().contains(px, py);
	}

	// Setters
	public void setPos(double x, double y) {
		BaseImpl.checkRange(x, "x");
		BaseImpl.checkRange(y, "y");

		if (this.x != x || this.y != y) {
			this.x = x;
			this.y = y;

			fireChanged();
		}
	}

	public void setSize(double w, double h) {
		BaseImpl.checkRange(w, "w", 0);
		BaseImpl.checkRange(h, "h", 0);

		if (this.w != w || this.h != h) {
			this.w = w;
			this.h = h;

			fireChanged();
		}
	}

	/**
	 * Warning: The change listener is internally marked transient and will therefore be lost upon
	 * serialization.
	 * <p>
	 * The given change listener will be called whenever a property of this bounds helper changes.
	 */
	public void setChangeListener(IChangeListener cl) {
		changeListener = cl;
	}

}
