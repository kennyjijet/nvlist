package nl.weeaboo.vn.impl.base;

import java.io.Externalizable;
import java.io.IOException;
import java.io.ObjectInput;
import java.io.ObjectOutput;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;

import nl.weeaboo.lua2.io.LuaSerializable;
import nl.weeaboo.vn.IDrawable;

@LuaSerializable
final class LayerContents implements Externalizable {
	
	//--- Uses manual serialization, don't add variables ---
	private Set<IDrawable> drawables;
	private PreSortedState<IDrawable> backToFront;
	//--- Uses manual serialization, don't add variables ---
	
	public LayerContents() {
		drawables = new HashSet<IDrawable>();
		backToFront = new DrawablesSorted(drawables);
	}
	
	//Functions
	@Override
	public void writeExternal(ObjectOutput out) throws IOException {
		out.writeObject(drawables);
		out.writeObject(backToFront);
	}

	@Override
	@SuppressWarnings("unchecked")
	public void readExternal(ObjectInput in) throws IOException, ClassNotFoundException {
		drawables = (Set<IDrawable>)in.readObject();
		backToFront = (PreSortedState<IDrawable>)in.readObject();
	}
	
	public boolean add(IDrawable d) {
		if (drawables.add(d)) {
			invalidatePreSorted();
			return true;
		} else {
			return false;
		}
	}
	
	public boolean remove(IDrawable d) {
		if (drawables.remove(d)) {
			invalidatePreSorted();
			return true;
		} else {
			return false;
		}
	}
	
	public Collection<IDrawable> removeDestroyed() {
		//Remove destroyed objects
		Collection<IDrawable> result = null;
		if (!drawables.isEmpty()) {
			for (Iterator<IDrawable> itr = drawables.iterator(); itr.hasNext(); ) {
				IDrawable d = itr.next();
				if (d == null || d.isDestroyed()) {
					itr.remove();					
					invalidatePreSorted();
					
					if (d != null) {
						if (result == null) result = new HashSet<IDrawable>();
						result.add(d);
					}
				}
			}
		}
		
		if (result == null) {
			result = Collections.emptySet();
		}
		return result;
	}
	
	public Collection<IDrawable> clear() {
		if (size() == 0) {
			return Collections.emptySet();
		}
		
		Set<IDrawable> removed = new HashSet<IDrawable>();
		removed.addAll(drawables);		
		drawables.clear();
		invalidatePreSorted();
		return removed;
	}
	
	public IDrawable[] getDrawables(IDrawable[] out, int zDirection) {
		if (zDirection > 0) {
			return backToFront.getSorted(out, true);
		} else if (zDirection < 0) {
			return backToFront.getSorted(out, false);
		}
		return drawables.toArray(out != null ? out : new IDrawable[drawables.size()]);
	}	
	
	protected void invalidatePreSorted() {
		backToFront.invalidateSorting();
	}
	
	//Getters
	public boolean contains(IDrawable d) {
		return drawables.contains(d);
	}
	
	public int size() {
		return drawables.size();
	}
			
	//Setters

	//Inner Classes
	@LuaSerializable
	protected static final class DrawablesSorted extends PreSortedState<IDrawable> implements Externalizable {
		
		//--- Uses manual serialization, don't add variables ---
		private Collection<? extends IDrawable> drawables;
		//--- Uses manual serialization, don't add variables ---
		
		public DrawablesSorted() {
			drawables = Collections.emptySet();
		}
		public DrawablesSorted(Collection<? extends IDrawable> ds) {
			drawables = ds;
		}
				
		@Override
		public void writeExternal(ObjectOutput out) throws IOException {
			out.writeObject(drawables);
		}

		@Override
		@SuppressWarnings("unchecked")
		public void readExternal(ObjectInput in) throws IOException, ClassNotFoundException {
			drawables = (Collection<? extends IDrawable>)in.readObject();
		}		
		
		@Override
		protected IDrawable[] newArray(int length) {
			return new IDrawable[length];
		}

		@Override
		protected IDrawable[] getUnsorted(IDrawable[] out) {
			return drawables.toArray(out);
		}

		@Override
		public int size() {
			return drawables.size();
		}

		@Override
		protected Comparator<? super IDrawable> getComparator() {
			return Layer.zBackToFrontComparator;
		}
		
	}
}
