package nl.weeaboo.vn.impl.base;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Comparator;
import java.util.EmptyStackException;
import java.util.List;

import nl.weeaboo.common.Rect2D;
import nl.weeaboo.lua2.io.LuaSerializable;
import nl.weeaboo.vn.IDrawable;
import nl.weeaboo.vn.IInput;
import nl.weeaboo.vn.ILayer;
import nl.weeaboo.vn.IRenderer;

@LuaSerializable
public final class Layer implements ILayer {

	private static final long serialVersionUID = BaseImpl.serialVersionUID;
	
	public static final Comparator<IDrawable> zFrontToBackComparator = new Comparator<IDrawable>() {
		public int compare(IDrawable d1, IDrawable d2) {
			int d = (int)d1.getZ() - (int)d2.getZ();
			if (d == 0) d = (d1.getX() > d2.getX() ? -1 : 1);
			return d;
		}
	};
	public static final Comparator<IDrawable> zBackToFrontComparator = new Comparator<IDrawable>() {
		public int compare(IDrawable d1, IDrawable d2) {
			int d = (int)d2.getZ() - (int)d1.getZ();
			if (d == 0) d = (d1.getX() <= d2.getX() ? -1 : 1);
			return d;
		}
	};
	
	private boolean destroyed;
	private List<LayerState> sstack;
	private ScreenshotBuffer screenshotBuffer;
	private transient boolean changed;
	private transient IDrawable[] tempArray;

	public Layer(double w, double h) {
		sstack = new ArrayList<LayerState>();
		sstack.add(new LayerState(w, h));
		
		screenshotBuffer = new ScreenshotBuffer();		
		initTransients();
	}
	
	//Functions	
	private void initTransients() {
		changed = true;
	}
	
	private void readObject(ObjectInputStream in) throws IOException, ClassNotFoundException {
		in.defaultReadObject();
		
		initTransients();
	}	
	
	@Override
	public void add(IDrawable d) {
		if (destroyed) return;

		if (getState().add(d)) {
			markChanged();
		}
	}

	@Override
	public void clear() {
		if (destroyed) return;

		Collection<IDrawable> removed = getState().clear();
		if (!removed.isEmpty()) {
			for (IDrawable d : removed) {
				if (d != null && !containsRecursive(d)) {				
					d.destroy();
				}
			}
			markChanged();
		}
	}
	
	@Override
	public void destroy() {
		if (destroyed) return;
		
		destroyed = true;
		while (!sstack.isEmpty()) {
			pop();
		}
		markChanged();
	}
	
	public void push() {
		push(Short.MIN_VALUE);
	}

	@Override
	public void push(short z) {
		if (destroyed) return;
		
		sstack.add(new LayerState(getState(), z));
		markChanged();
	}
	
	@Override
	public void pop() throws EmptyStackException {
		if (!destroyed && sstack.size() <= 1) {
			throw new EmptyStackException();
		}
		
		if (!sstack.isEmpty()) {
			LayerState oldState = sstack.remove(sstack.size()-1);
			oldState.clear();
			markChanged();
		}
	}

	@Override
	public boolean update(IInput input, double effectSpeed) {
		LayerState state = getState();
		
		if (isVisible()) {
			tempArray = state.getDrawables(tempArray, 1);
			for (int n = 0; n < tempArray.length; n++) {
				IDrawable d = tempArray[n];
				if (d == null) break; //The array can only contain nulls at the end
				tempArray[n] = null; //Null the array indices to allow garbage collection
				
				if (!d.isDestroyed() && d.update(this, input, effectSpeed)) {
					markChanged();
				}
			}
		}
		
		if (!screenshotBuffer.isEmpty()) {
			markChanged();
		}
		
		return consumeChanged();
	}

	@Override
	public void draw(IRenderer r) {
		screenshotBuffer.flush(r);
		
		if (isVisible()) {
			LayerState state = getState();
			tempArray = state.getDrawables(tempArray, -1);
			for (int n = 0; n < tempArray.length; n++) {
				IDrawable d = tempArray[n];
				if (d == null) break; //The array can only contain nulls at the end
				tempArray[n] = null; //Null the array indices to allow garbage collection
				
				if (!d.isDestroyed()) {
					d.draw(r);
				}
			}
		}
	}
	
	protected void markChanged() {
		changed = true;
	}
	
	protected boolean consumeChanged() {
		boolean result = changed;
		changed = false;
		return result;
	}
	
	@Override
	public boolean contains(double x, double y) {
		return getState().getBounds().contains(x, y);
	}
	
	//Getters
	protected LayerState getState() {
		if (destroyed) return null;

		return sstack.get(sstack.size()-1);
	}
	
	@Override
	public final boolean isDestroyed() {
		return destroyed;
	}
	
	@Override
	public ScreenshotBuffer getScreenshotBuffer() {
		return screenshotBuffer;
	}
	
	@Override
	public IDrawable[] getDrawables() {
		return getDrawables(null);
	}

	@Override
	public IDrawable[] getDrawables(IDrawable[] out) {
		if (destroyed) return new IDrawable[0];
		return getState().getDrawables(out);
	}
	
	@Override
	public boolean contains(IDrawable d) {
		if (destroyed) return false;
		
		return getState().contains(d);
	}
	
	protected boolean containsRecursive(IDrawable d) {
		if (destroyed) return false;

		for (LayerState state : sstack) {
			if (state.contains(d)) return true;
		}
		return false;
	}
	
	@Override
	public double getX() {
		if (destroyed) return 0;

		return getState().getBounds().x;
	}

	@Override
	public double getY() {
		if (destroyed) return 0;

		return getState().getBounds().y;
	}

	@Override
	public double getWidth() {
		if (destroyed) return 0;

		return getState().getBounds().w;
	}

	@Override
	public double getHeight() {
		if (destroyed) return 0;

		return getState().getBounds().h;
	}
	
	@Override
	public Rect2D getBounds() {
		if (destroyed) return new Rect2D(0, 0, 0, 0);

		return getState().getBounds();
	}
	
	@Override
	public short getZ() {
		if (destroyed) return 0;

		return getState().getZ();
	}
	
	@Override
	public boolean isVisible() {
		if (destroyed) return false;

		return getState().isVisible();
	}
	
	//Setters
	@Override
	public void setPos(double x, double y) {
		if (destroyed) return;
		
		setBounds(x, y, getWidth(), getHeight());
	}

	@Override
	public void setSize(double w, double h) {
		if (destroyed) return;

		setBounds(getX(), getY(), w, h);
	}
	
	@Override
	public void setBounds(double x, double y, double w, double h) {
		if (destroyed) return;

		if (getX() != x || getY() != y || getWidth() != w || getHeight() != h) {
			getState().setBounds(new Rect2D(x, y, w, h));
			markChanged();
		}
	}

	@Override
	public void setZ(short z) {
		if (destroyed) return;

		if (getZ() != z) {
			getState().setZ(z);
			markChanged();
		}
	}

	@Override
	public void setVisible(boolean v) {
		if (destroyed) return;

		if (isVisible() != v) {
			getState().setVisible(v);
			markChanged();
		}
	}
	
}
