package nl.weeaboo.vn.impl.base;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.EmptyStackException;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import nl.weeaboo.lua2.io.LuaSerializable;
import nl.weeaboo.vn.IImageState;
import nl.weeaboo.vn.IInput;
import nl.weeaboo.vn.ILayer;
import nl.weeaboo.vn.IRenderer;

public class BaseImageState implements IImageState {

	private static final long serialVersionUID = BaseImpl.serialVersionUID;
	
	public static final String DEFAULT_LAYER_ID = "default";
	public static final String OVERLAY_LAYER_ID = "overlay";
	
	private final int width, height;
	private List<State> sstack;
	private transient boolean changed;
	private transient ILayer[] tempArray;
	
	protected BaseImageState(int w, int h) {
		width = w;
		height = h;
		sstack = new ArrayList<State>();
		
		initTransients();
		reset0();
	}
	
	//Functions
	private void initTransients() {
		changed = true;		
		tempArray = new ILayer[0];
	}

	private void readObject(ObjectInputStream in) throws IOException, ClassNotFoundException {
		in.defaultReadObject();
		
		initTransients();
	}	
	
	private void reset0() {
		while (!sstack.isEmpty()) {
			State state = sstack.remove(sstack.size()-1);
			state.clear();
		}
		sstack.add(newState());
		markChanged();
	}
	
	private State newState() {
		State state = new State();
		
		createLayer(state, DEFAULT_LAYER_ID);
		
		ILayer overlay = createLayer(state, OVERLAY_LAYER_ID);
		overlay.setZ((short)(-30000));
		
		return state;
	}
	
	@Override
	public void reset() {
		reset0();
		
		markChanged();
	}
	
	@Override
	public ILayer createLayer(String id) {
		if (id == null) id = DEFAULT_LAYER_ID;
		
		State state = getState();
		ILayer layer = state.getLayer(id);
		if (layer != null && !layer.isDestroyed()) {
			return null;
		}
		
		return createLayer(state, id);
	}
	
	protected ILayer createLayer(State state, String id) {
		ILayer layer = new Layer(width, height);
		state.setLayer(id, layer);
		markChanged();
		return layer;		
	}
	
	@Override
	public boolean update(IInput input, double effectSpeed) {
		tempArray = getState().getLayers(tempArray, 1);
		for (int n = 0; n < tempArray.length; n++) {
			ILayer l = tempArray[n];
			if (l == null) break; //The array can only contain nulls at the end
			tempArray[n] = null; //Null the array indices to allow garbage collection
			
			if (!l.isDestroyed()) {
				final double x = l.getX();
				final double y = l.getY();
				input.translate(-x, -y);
				try {
					if (l.update(input, effectSpeed)) {
						markChanged();
					}
				} finally {
					input.translate(x, y);
				}
			}
		}
				
		return consumeChanged();
	}

	@Override
	public void draw(IRenderer r) {
		r.render(null);
		r.reset();
				
		tempArray = getState().getLayers(tempArray, -1);
		for (int n = 0; n < tempArray.length; n++) {
			ILayer l = tempArray[n];
			if (l == null) break; //The array can only contain nulls at the end
			tempArray[n] = null; //Null the array indices to allow garbage collection
			
			if (!l.isDestroyed()) {
				l.draw(r);
				r.render(l.getBounds());
				r.reset();
			}
		}
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
		oldState.clear();
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
	public Map<String, ILayer> getLayers() {
		return getState().getLayers();		
	}
	
	@Override
	public ILayer getLayer(String id) {
		if (id == null) id = DEFAULT_LAYER_ID;
		
		ILayer layer = getState().getLayer(id);
		if (layer == null || layer.isDestroyed()) {
			if (DEFAULT_LAYER_ID.equals(id)) {
				//Create default layer if it doesn't exist already
				layer = createLayer(DEFAULT_LAYER_ID);
			}
		}
		
		return layer;
	}

	@Override
	public ILayer getTopLayer() {
		ILayer layer = getState().getTopLayer();
		if (layer == null || layer.isDestroyed()) {
			getLayer(DEFAULT_LAYER_ID);
		}
		return layer;
	}
		
	//Setters

	//Inner Classes
	@LuaSerializable
	private static class State implements Serializable {

		private static final long serialVersionUID = BaseImageState.serialVersionUID;
				
		private final Map<String, ILayer> layers;
		private final LayersSorted backToFront;
		
		public State() {
			layers = new HashMap<String, ILayer>();
			backToFront = new LayersSorted(layers);
		}

		//Functions
		protected void invalidatePreSorted() {
			backToFront.invalidateSorting();
		}
		
		public void clear() {
			ILayer[] ls = layers.values().toArray(new ILayer[layers.size()]);
			layers.clear();
			invalidatePreSorted();
			for (ILayer layer : ls) {
				layer.destroy();
			}
		}
		
		protected void removeDestroyedLayers() {
			Iterator<Entry<String, ILayer>> itr = layers.entrySet().iterator();
			while (itr.hasNext()) {
				Entry<String, ILayer> entry = itr.next();
				if (entry.getValue().isDestroyed()) {
					itr.remove();
					invalidatePreSorted();
				}
			}
		}
		
		//Getters
		public Map<String, ILayer> getLayers() {
			removeDestroyedLayers();
			return new HashMap<String, ILayer>(layers);			
		}
		
		public ILayer[] getLayers(ILayer[] out, int zDirection) {
			removeDestroyedLayers();

			if (zDirection > 0) {
				return backToFront.getSorted(out, true);
			} else if (zDirection < 0) {
				return backToFront.getSorted(out, false);
			}
			return layers.values().toArray(out != null ? out : new ILayer[layers.size()]);
		}

		public ILayer getTopLayer() {
			return backToFront.getTopLayer();
		}
		
		public ILayer getLayer(String id) {
			ILayer layer = layers.get(id);
			if (layer != null && layer.isDestroyed()) {
				removeDestroyedLayers();
				layer = null;
			}
			return layer;
		}
		
		//Setters
		public ILayer setLayer(String id, ILayer layer) {		
			ILayer oldLayer = layers.put(id, layer);
			if (oldLayer != null) {
				oldLayer.destroy();
			}
			invalidatePreSorted();
			return layer;
		}

	}
	
	@LuaSerializable
	private static final class LayersSorted extends PreSortedState<ILayer> implements Serializable {

		private static final long serialVersionUID = BaseImpl.serialVersionUID;
		
		private static final Comparator<ILayer> zBackToFrontComparator = new Comparator<ILayer>() {
			public int compare(ILayer l1, ILayer l2) {
				return (int)l2.getZ() - (int)l1.getZ();
			}
		};
		
		private final Map<?, ? extends ILayer> layers;
		
		public LayersSorted(Map<?, ? extends ILayer> ls) {
			layers = ls;
		}
		
		@Override
		protected ILayer[] newArray(int length) {
			return new ILayer[length];
		}

		@Override
		protected ILayer[] getUnsorted(ILayer[] out) {
			return layers.values().toArray(out);
		}

		@Override
		public int size() {
			return layers.size();
		}

		@Override
		protected Comparator<? super ILayer> getComparator() {
			return zBackToFrontComparator;
		}
		
		public ILayer getTopLayer() {
			ILayer[] sorted = initSorted();
			for (int n = size()-1; n >= 0; n--) {
				ILayer l = sorted[n];
				if (l != null && !l.isDestroyed()) {
					return l;
				}
			}
			return null;
		}
		
	}
	
}
