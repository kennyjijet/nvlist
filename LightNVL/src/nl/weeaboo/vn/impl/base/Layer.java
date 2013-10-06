package nl.weeaboo.vn.impl.base;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Comparator;
import java.util.EmptyStackException;
import java.util.List;

import nl.weeaboo.common.Rect2D;
import nl.weeaboo.lua2.io.LuaSerializable;
import nl.weeaboo.vn.IDrawBuffer;
import nl.weeaboo.vn.IDrawable;
import nl.weeaboo.vn.IInput;
import nl.weeaboo.vn.ILayer;
import nl.weeaboo.vn.RenderEnv;

@LuaSerializable
public final class Layer extends BaseDrawable implements ILayer {

	private static final long serialVersionUID = BaseImpl.serialVersionUID;
	
	public static final Comparator<IDrawable> zFrontToBackComparator = new Comparator<IDrawable>() {
		public int compare(IDrawable d1, IDrawable d2) {
			int d = (int)d1.getZ() - (int)d2.getZ();
			if (d == 0) d = Double.compare(d2.getX(), d1.getX());
			return d;
		}
	};
	public static final Comparator<IDrawable> zBackToFrontComparator = new Comparator<IDrawable>() {
		public int compare(IDrawable d1, IDrawable d2) {
			int d = (int)d2.getZ() - (int)d1.getZ();
			if (d == 0) d = Double.compare(d1.getX(), d2.getX());
			return d;
		}
	};
	
	private final DrawableRegistry registry;
	
	private List<LayerContents> sstack;
	private ScreenshotBuffer screenshotBuffer;
	private double width, height;

	private transient IDrawable[] tempArray;
	private transient Rect2D _bounds;

	public Layer(DrawableRegistry registry) {
		this.registry = registry;
		
		sstack = new ArrayList<LayerContents>();
		sstack.add(new LayerContents());
		
		screenshotBuffer = new ScreenshotBuffer();		
	}
	
	//Functions
	protected void invalidateBounds() {
		_bounds = null;
	}
	
	@Override
	protected void invalidateTransform() {
		super.invalidateTransform();
		
		invalidateBounds();
	}	
	
	@Override
	public void add(IDrawable d) {
		if (isDestroyed() || d.isDestroyed()) return;
				
		if (getState().add(d)) {
			registry.addReference(d);
			
			//Change drawable's current parent to this layer and remove it from its previous layer if any
			Layer oldLayer = registry.setParentLayer(d, this);
			if (oldLayer != null && oldLayer != this) {
				oldLayer.remove(d);
			}
			
			d.setRenderEnv(getRenderEnv());
			
			markChanged();
		}		
	}

	private void onRemoved(IDrawable d) {
		registry.removeReference(d);
	}
	
	/**
	 * This remove operation removes the given drawable from this layer
	 * completely, including from any pushed states.
	 */
	protected void remove(IDrawable d) {
		if (getState().remove(d)) {
			onRemoved(d);
			markChanged();
		}
	}

	@Override
	public void clearContents() {
		if (isDestroyed()) return;

		Collection<IDrawable> removed = getState().clear();
		for (IDrawable d : removed) {
			onRemoved(d);
		}
		markChanged();
	}
	
	@Override
	public void destroy() {
		if (isDestroyed()) {
			return;
		}
		
		super.destroy();
		
		//We need to be marked destroyed before we're allowed to pop the final stack entry
		
		while (!sstack.isEmpty()) {
			popContents();
		}
	}
	
	@Override
	public void pushContents() {
		pushContents(Short.MIN_VALUE);
	}

	@Override
	public void pushContents(short z) {
		if (isDestroyed()) return;
		
		LayerContents oldState = getState();
		sstack.add(new LayerContents());
		
		tempArray = oldState.getDrawables(tempArray, 0);
		for (int n = 0; n < tempArray.length; n++) {
			IDrawable d = tempArray[n];
			if (d == null) break; //The array can only contain nulls at the end
			tempArray[n] = null; //Null the array indices to allow garbage collection
			
			if (d.getZ() <= z) {
				add(d); //Re-add and increase reference count
				if (d instanceof ILayer) {
					//We need to also recursively push the contents of nested layers
					ILayer l = (ILayer)d;
					l.pushContents();
				}
			}
		}
		
		markChanged();
	}
	
	@Override
	public void popContents() throws EmptyStackException {
		if (!isDestroyed() && sstack.size() <= 1) {
			throw new EmptyStackException();
		}
		
		if (!sstack.isEmpty()) {
			LayerContents oldState = sstack.remove(sstack.size()-1);
			
			Collection<IDrawable> removed = oldState.clear();
			for (IDrawable d : removed) {
				onRemoved(d); //Decrease reference count
				
				if (d instanceof ILayer) {
					//Pop the recursively pushed layers
					ILayer l = (ILayer)d;
					l.popContents();
				}
			}

			if (!sstack.isEmpty()) {
				//Set the drawables's parent back to this layer
				for (IDrawable d : getContents()) {
					Layer oldLayer = registry.setParentLayer(d, this);
					if (oldLayer != null && oldLayer != this) {
						oldLayer.remove(d);
					}		
				}
			}
			
			markChanged();
		}
	}
	
	protected void removeDestroyed() {
		Collection<IDrawable> removed = getState().removeDestroyed();
		if (!removed.isEmpty()) {
			for (IDrawable d : removed) {
				onRemoved(d);
			}
			markChanged();
		}
	}

	@Override
	public boolean update(ILayer parent, IInput input, double effectSpeed) {
		LayerContents state = getState();
				
		if (isVisible()) {
			final double x = getX();
			final double y = getY();
			input.translate(-x, -y);
			try {
				tempArray = state.getDrawables(tempArray, 1);
				for (int n = 0; n < tempArray.length; n++) {
					IDrawable d = tempArray[n];
					if (d == null) break; //The array can only contain nulls at the end
					tempArray[n] = null; //Null the array indices to allow garbage collection
					
					if (!d.isDestroyed() && d.update(this, input, effectSpeed)) {
						markChanged();
					}
				}
			} finally {
				input.translate(x, y);
			}			
		}
		
		if (!screenshotBuffer.isEmpty()) {
			markChanged();
		}
		
		removeDestroyed();
		
		return consumeChanged();
	}

	@Override
	public void draw(IDrawBuffer buf) {		
		if (isVisible()) {
			BaseDrawBuffer baseBuf = BaseDrawBuffer.cast(buf);
			baseBuf.startLayer(this);
			
			LayerContents state = getState();
			tempArray = state.getDrawables(tempArray, -1);
			
			final Rect2D r = getBounds();
			final double lw = r.w;
			final double lh = r.h;
			
			//Render drawables (insert layer render commands for the layers)
			for (int t = 0; t < tempArray.length; t++) {
				IDrawable d = tempArray[t];
				if (d == null) break; //The array can only contain nulls at the end
								
				if (!d.isDestroyed() && d.isVisible(.001)) {
					if (d instanceof ILayer) {
						ILayer l = (ILayer)d;
						baseBuf.draw(new LayerRenderCommand(l));
					} else {
						if (!d.isClipEnabled() || d.getBounds().intersects(0, 0, lw, lh)) {
							d.draw(baseBuf);
						}
					}							
				}
			}
			
			//Add screenshot render commands to the end of the list
			screenshotBuffer.flush(buf);		
			
			//Recursively draw other layers (implicitly ends the current layer)
			for (int t = 0; t < tempArray.length; t++) {
				IDrawable d = tempArray[t];
				if (d == null) break; //The array can only contain nulls at the end
				tempArray[t] = null;
				
				if (!d.isDestroyed() && d.isVisible(.001)) {
					if (d instanceof ILayer) {
						d.draw(baseBuf);
					}
				}
			}
		}		
	}
	
	@Override
	protected void onRenderEnvChanged() {
		super.onRenderEnvChanged();
		
		//Push the render env change through to all contained drawables, even the ones not currently active
		RenderEnv env = getRenderEnv();
		for (LayerContents state : sstack) {
			tempArray = state.getDrawables(tempArray, 0);
			for (int n = 0; n < tempArray.length; n++) {
				IDrawable d = tempArray[n];
				if (d == null) break; //The array can only contain nulls at the end
				tempArray[n] = null; //Null the array indices to allow garbage collection

				d.setRenderEnv(env);
			}
		}
	}
	
	//Getters
	@Override
	public Rect2D getBounds() {
		if (_bounds == null) {
			_bounds = super.getBounds();
		}
		return _bounds;
	}
	
	protected LayerContents getState() {
		if (isDestroyed()) return null;

		return sstack.get(sstack.size()-1);
	}
	
	@Override
	public ScreenshotBuffer getScreenshotBuffer() {
		return screenshotBuffer;
	}
	
	@Override
	public IDrawable[] getContents() {
		return getContents(null);
	}

	@Override
	public IDrawable[] getContents(IDrawable[] out) {
		if (isDestroyed()) {
			return new IDrawable[0];
		}
		
		removeDestroyed();
		return getState().getDrawables(out, 0);
	}
	
	@Override
	public boolean contains(IDrawable d) {
		if (isDestroyed() || d == null || d.isDestroyed()) return false;
		
		return getState().contains(d);
	}
	
	protected boolean containsRecursive(IDrawable d) {
		if (isDestroyed() || d == null || d.isDestroyed()) return false;

		for (LayerContents state : sstack) {
			if (state.contains(d)) return true;
		}
		return false;
	}
	
	@Override
	public boolean containsRel(double x, double y) {
		return contains(getX()+x, getY()+y);
	}	

	@Override
	public double getWidth() {
		return width;
	}

	@Override
	public double getHeight() {
		return height;
	}

	//Setters
	@Override
	public void setSize(double w, double h) {
		if (width != w || height != h) {
			width = w;
			height = h;
			
			markChanged();
			invalidateBounds();
		}
	}

	@Override
	public void setBounds(double x, double y, double w, double h) {
		setPos(x, y);
		setSize(w, h);
	}
	
}
