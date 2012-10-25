package nl.weeaboo.vn.impl.base;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.EmptyStackException;
import java.util.List;

import nl.weeaboo.lua2.io.LuaSerializable;
import nl.weeaboo.vn.IImageFactory;
import nl.weeaboo.vn.IImageState;
import nl.weeaboo.vn.IInput;
import nl.weeaboo.vn.ILayer;

public class BaseImageState implements IImageState {

	private static final long serialVersionUID = BaseImpl.serialVersionUID;
	
	private final IImageFactory imgfac;
	private final int width, height;
	
	private List<State> sstack;
	private transient boolean changed;
	
	protected BaseImageState(IImageFactory fac, int w, int h) {
		imgfac = fac;
		width = w;
		height = h;
		sstack = new ArrayList<State>();
		
		initTransients();
		reset0();
	}
	
	//Functions
	private void initTransients() {
		changed = true;		
	}

	private void readObject(ObjectInputStream in) throws IOException, ClassNotFoundException {
		in.defaultReadObject();
		
		initTransients();
	}	
	
	private void reset0() {
		while (!sstack.isEmpty()) {
			State state = sstack.remove(sstack.size()-1);
			state.destroy();
		}
		sstack.add(newState());
		markChanged();
	}
	
	private State newState() {
		ILayer rootLayer = imgfac.createLayer(0, 0, width, height);
		
		ILayer defaultLayer = imgfac.createLayer(0, 0, width, height);
		rootLayer.add(defaultLayer);
		
		ILayer overlayLayer = imgfac.createLayer(0, 0, width, height);
		overlayLayer.setZ((short)(-30000));
		rootLayer.add(overlayLayer);
		
		return new State(rootLayer, defaultLayer, overlayLayer);
	}
	
	@Override
	public void reset() {
		reset0();
		
		markChanged();
	}
		
	@Override
	public boolean update(IInput input, double effectSpeed) {
		ILayer rootLayer = getRootLayer();		
		if (!rootLayer.isDestroyed() && rootLayer.update(null, input, effectSpeed)) {
			markChanged();
		}
				
		return consumeChanged();
	}
		
	@Override
	public void push() {
		sstack.add(newState());
		markChanged();
	}
	
	@Override
	public void pop() {
		if (sstack.size() <= 1) {
			throw new EmptyStackException();
		}
		
		State oldState = sstack.remove(sstack.size()-1);
		oldState.destroy();
		markChanged();
	}
	
	protected final void markChanged() {
		changed = true;
	}
	
	protected boolean consumeChanged() {
		boolean result = changed;
		changed = false;
		return result;
	}
	
	//Getters
	@Override
	public int getWidth() {
		return width;
	}

	@Override
	public int getHeight() {
		return height;
	}

	protected State getState() {
		return sstack.isEmpty() ? null : sstack.get(sstack.size()-1);
	}
	
	@Override
	public ILayer getRootLayer() {
		return getState().getRootLayer();
	}
	
	@Override
	public ILayer getDefaultLayer() {
		return getState().getDefaultLayer();
	}
	
	@Override
	public ILayer getOverlayLayer() {
		return getState().getOverlayLayer();		
	}
		
	//Setters

	//Inner Classes
	@LuaSerializable
	private class State implements Serializable {

		private static final long serialVersionUID = BaseImageState.serialVersionUID;
				
		private ILayer rootLayer;
		private ILayer defaultLayer;
		private ILayer overlayLayer;
		
		public State(ILayer rootLayer, ILayer defaultLayer, ILayer overlayLayer) {
			this.rootLayer = rootLayer;
			this.defaultLayer = defaultLayer;
			this.overlayLayer = overlayLayer;
		}

		public void destroy() {			
		}
		
		public ILayer getRootLayer() {
			if (rootLayer == null || rootLayer.isDestroyed()) {
				rootLayer = imgfac.createLayer(0, 0, width, height);
			}
			return rootLayer;
		}
		
		public ILayer getDefaultLayer() {
			return defaultLayer;
		}
		
		public ILayer getOverlayLayer() {
			return overlayLayer;
		}
		
	}
	
}
