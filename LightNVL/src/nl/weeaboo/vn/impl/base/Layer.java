package nl.weeaboo.vn.impl.base;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Comparator;
import java.util.EmptyStackException;
import java.util.LinkedList;
import java.util.List;

import nl.weeaboo.lua2.io.LuaSerializable;
import nl.weeaboo.vn.IDrawBuffer;
import nl.weeaboo.vn.IDrawable;
import nl.weeaboo.vn.IInput;
import nl.weeaboo.vn.ILayer;

@LuaSerializable
public final class Layer extends BaseDrawable implements ILayer {

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
	
	private List<LayerState> sstack;
	private ScreenshotBuffer screenshotBuffer;
	private double width, height;
	private transient boolean changed;
	private transient IDrawable[] tempArray;

	public Layer() {
		sstack = new ArrayList<LayerState>();
		sstack.add(new LayerState());
		
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
		if (isDestroyed()) return;

		if (getState().add(d)) {
			markChanged();
		}
	}

	@Override
	public void clearContents() {
		if (isDestroyed()) return;

		Collection<IDrawable> removed = getState().destroy();
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
		if (isDestroyed()) return;
		
		while (!sstack.isEmpty()) {
			popContents();
		}
		
		super.destroy();
	}
	
	@Override
	public void pushContents() {
		pushContents(Short.MIN_VALUE);
	}

	@Override
	public void pushContents(short z) {
		if (isDestroyed()) return;
		
		sstack.add(new LayerState(getState(), z));
		markChanged();
	}
	
	@Override
	public void popContents() throws EmptyStackException {
		if (!isDestroyed() && sstack.size() <= 1) {
			throw new EmptyStackException();
		}
		
		if (!sstack.isEmpty()) {
			LayerState oldState = sstack.remove(sstack.size()-1);
			oldState.destroy();
			markChanged();
		}
	}

	@Override
	public boolean update(ILayer parent, IInput input, double effectSpeed) {
		LayerState state = getState();
		
		if (isVisible()) {
			final double x = getX();
			final double y = getY();
			input.translate(-x, -y);
			try {
				tempArray = state.getContents(tempArray, 1);
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
		
		return consumeChanged();
	}

	@Override
	public void draw(IDrawBuffer buf) {		
		if (isVisible()) {
			BaseDrawBuffer baseBuf = BaseDrawBuffer.cast(buf);
			baseBuf.startLayer(this);
			
			LayerState state = getState();
			tempArray = state.getContents(tempArray, -1);
			
			int t = 0;
			for (int pass = 0; pass < 2; pass++) {
				for (t = 0; t < tempArray.length; t++) {
					IDrawable d = tempArray[t];
					if (d == null) break; //The array can only contain nulls at the end
					
					if (!d.isDestroyed() && d.isVisible(.001)) {
						if (pass == 0) {
							if (d instanceof ILayer) {
								ILayer l = (ILayer)d;
								baseBuf.draw(new LayerRenderCommand(l));
							} else {
								d.draw(baseBuf);
							}
						} else {
							if (d instanceof ILayer) {
								d.draw(baseBuf);
							}
						}
						
					}
				}
			}
			Arrays.fill(tempArray, 0, t, null);
		}
		
		screenshotBuffer.flush(buf);		
	}
	
	protected void markChanged() {
		changed = true;
	}
	
	protected boolean consumeChanged() {
		boolean result = changed;
		changed = false;
		return result;
	}
	
	//Getters
	protected LayerState getState() {
		if (isDestroyed()) return null;

		return sstack.get(sstack.size()-1);
	}
	
	@Override
	public ScreenshotBuffer getScreenshotBuffer() {
		return screenshotBuffer;
	}
	
	@Override
	public Collection<ILayer> getSubLayers(boolean recursive) {
		List<ILayer> result = new ArrayList<ILayer>();
		IDrawable[] temp = new IDrawable[16];
		
		//Add ourselves to the work list
		LinkedList<ILayer> workQ = new LinkedList<ILayer>();
		workQ.add(this);
		
		//Keep processing layers
		while (!workQ.isEmpty()) {
			ILayer layer = workQ.removeFirst();
			temp = layer.getContents(temp);
			for (IDrawable d : temp) {
				if (d == null) break;
				
				if (!d.isDestroyed() && d instanceof ILayer) {
					ILayer sub = (ILayer)d;
					
					result.add(sub);
					if (recursive) {
						workQ.add(sub);
					}
				}
			}
		}
		return result;
	}
	
	@Override
	public IDrawable[] getContents() {
		return getContents(null);
	}

	@Override
	public IDrawable[] getContents(IDrawable[] out) {
		if (isDestroyed()) return new IDrawable[0];
		return getState().getContents(out);
	}
	
	@Override
	public boolean contains(IDrawable d) {
		if (isDestroyed()) return false;
		
		return getState().contains(d);
	}
	
	protected boolean containsRecursive(IDrawable d) {
		if (isDestroyed()) return false;

		for (LayerState state : sstack) {
			if (state.contains(d)) return true;
		}
		return false;
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
		}
	}

	@Override
	public void setBounds(double x, double y, double w, double h) {
		setPos(x, y);
		setSize(w, h);
	}
	
}
