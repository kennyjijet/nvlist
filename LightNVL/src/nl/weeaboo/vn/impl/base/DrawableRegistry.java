package nl.weeaboo.vn.impl.base;

import java.io.Externalizable;
import java.io.IOException;
import java.io.InvalidObjectException;
import java.io.ObjectInput;
import java.io.ObjectOutput;
import java.util.IdentityHashMap;
import java.util.Map.Entry;

import nl.weeaboo.lua2.io.LuaSerializable;
import nl.weeaboo.vn.IDrawable;
import nl.weeaboo.vn.ILayer;

/**
 * Counts the number of references to drawables from layers. This allows us to
 * efficiently and immediately determine when a drawable is no longer referenced
 * from a layer and should therefore be destroyed.
 */

@LuaSerializable
final class DrawableRegistry implements Externalizable {

	private static final long serialVersionUID = BaseImpl.serialVersionUID;

	//--- Manual serialization, don't add/change properties ---------------
	private IdentityHashMap<IDrawable, Info> meta;
	//--- Manual serialization, don't add/change properties ---------------
	
	public DrawableRegistry() {
		meta = new IdentityHashMap<IDrawable, Info>();
	}
	
	//Functions
	@Override
	public void writeExternal(ObjectOutput out) throws IOException {
		out.writeByte(1); //version
		
		out.writeInt(meta.size());
		for (Entry<IDrawable, Info> entry : meta.entrySet()) {
			out.writeObject(entry.getKey());
			out.writeObject(entry.getValue());
		}
	}

	@Override
	public void readExternal(ObjectInput in) throws IOException, ClassNotFoundException {
		int version = in.readByte() & 0xFF;
		if (version != 1) {
			throw new InvalidObjectException("Incompatible version: " + version);
		}
		
		int size = in.readInt();
		for (int n = 0; n < size; n++) {
			IDrawable key = (IDrawable)in.readObject();
			Info info = (Info)in.readObject();
			meta.put(key, info);
		}
	}
	
	private static void logf(String format, Object... args) {
		//System.out.println(String.format(format, args));
	}
	
	public void addReference(IDrawable d) {
		if (d == null) throw new NullPointerException();
		
		Info info = meta.get(d);
		if (info == null) {
			info = new Info();
			meta.put(d, info);
			logf("addMeta(%s, new_meta_size=%d)", d, meta.size());
		} else {
			info.refcount++;
		}
		
		logf("addReference(%s, new_refcount=%d)", d, info.refcount);
	}
	
	public void removeReference(IDrawable d) {
		if (d == null) throw new NullPointerException();

		Info info = meta.get(d);
		if (info == null) throw new RuntimeException("Object not tracked"); //return; //Object not tracked
		
		info.refcount--;
		logf("removeReference(%s, new_refcount=%d)", d, info.refcount);
		
		if (info.refcount <= 0) {
			meta.remove(d);
			logf("removeMeta(%s, new_meta_size=%d)", d, meta.size());
			
			destroyReferenced(d);
		}
	}
	
	protected void destroyReferenced(IDrawable d) {
		logf("destroyReferenced(%s)", d);
		d.destroy();
	}
	
	//Getters
	public ILayer getParentLayer(IDrawable d) {
		Info info = meta.get(d);
		if (info == null) throw new RuntimeException("Object not tracked"); //return null; //Object not tracked
		
		return info.parentLayer;
	}
	
	//Setters
	/**
	 * @return The previous parent layer recorded for <code>d</code>.
	 */
	public Layer setParentLayer(IDrawable d, Layer parentLayer) {
		Info info = meta.get(d);
		if (info == null) {
			throw new RuntimeException("Object not tracked"); //return null;
		}

		if (!parentLayer.contains(d)) {
			throw new IllegalArgumentException("Called setParentLayer(), but the given layer doesn't contain the given drawable -- add the drawable to the layer first.");
		}
		
		if (parentLayer == info.parentLayer) {
			return null; //Parent layer stored is already correct
		}
		
		Layer oldLayer = info.parentLayer;
		info.parentLayer = parentLayer;		
		return oldLayer;
	}
	
	//Inner Classes
	@LuaSerializable
	private static class Info implements Externalizable {
	
		//--- Manual serialization, don't add/change properties ---------------
		Layer parentLayer;
		int refcount;
		//--- Manual serialization, don't add/change properties ---------------
		
		public Info() {
			refcount = 1;
		}

		@Override
		public void writeExternal(ObjectOutput out) throws IOException {
			out.writeObject(parentLayer);
			out.writeInt(refcount);
		}

		@Override
		public void readExternal(ObjectInput in) throws IOException, ClassNotFoundException {
			parentLayer = (Layer)in.readObject();
			refcount = in.readInt();
		}
		
	}
	
}
