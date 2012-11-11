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
import nl.weeaboo.vn.RenderEnv;

public class BaseImageState implements IImageState {

	private static final long serialVersionUID = BaseImpl.serialVersionUID;
	
	private final int width, height;	
	private final List<State> sstack;
	private final DrawableRegistry registry;
	
	private /*transient*/ RenderEnv renderEnv;
	
	private transient boolean changed;
	
	protected BaseImageState(IImageFactory fac, int w, int h) {
		width = w;
		height = h;
		sstack = new ArrayList<State>();
		registry = new DrawableRegistry();
		
		renderEnv = new RenderEnv(w, h, 0, 0, w, h, w, h, false);
		
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
		ILayer rootLayer = createLayer(null);
		
		ILayer defaultLayer = createLayer(rootLayer);
		
		ILayer overlayLayer = createLayer(rootLayer);
		overlayLayer.setZ((short)(-30000));
		
		return new State(rootLayer, defaultLayer, overlayLayer);
	}
	
	@Override
	public ILayer createLayer(ILayer parentLayer) {
		ILayer layer = new Layer(registry);
		if (parentLayer != null) {
			layer.setBounds(parentLayer.getX(), parentLayer.getY(), parentLayer.getWidth(), parentLayer.getHeight());
			parentLayer.add(layer);
		} else {
			layer.setBounds(0, 0, width, height);
			layer.setRenderEnv(renderEnv);
		}
		return layer;
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
			
	@Override
	public RenderEnv getRenderEnv() {
		return renderEnv;
	}
	
	//Setters
	@Override
	public void setRenderEnv(RenderEnv env) {
		if (renderEnv != env) {
			renderEnv = env;
			for (State state : sstack) {
				state.getRootLayer().setRenderEnv(env);
			}		
		}
	}

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
			rootLayer.destroy();
			defaultLayer.destroy();
			overlayLayer.destroy();
		}
		
		public ILayer getRootLayer() {
			if (rootLayer == null || rootLayer.isDestroyed()) {
				rootLayer = createLayer(null);
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
