package nl.weeaboo.vn.impl.base;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;
import java.util.WeakHashMap;

import nl.weeaboo.lua2.io.LuaSerializable;
import nl.weeaboo.vn.IImageFxLib;
import nl.weeaboo.vn.ITexture;

@LuaSerializable
public class BlurImageCache implements Serializable {

	private static final long serialVersionUID = 1L;
	
	private final IImageFxLib imageFxLib;
	private transient WeakHashMap<ITexture, CacheEntry> cache;
	
	public BlurImageCache(IImageFxLib fxlib) {
		imageFxLib = fxlib;
		
		initTransients();
	}
	
	//Functions
	private void initTransients() {
		cache = new WeakHashMap<ITexture, CacheEntry>();
	}
	
	private void readObject(ObjectInputStream in) throws IOException, ClassNotFoundException {
		in.defaultReadObject();
		
		initTransients();
	}
	
	/**
	 * Removes all cached entries.
	 */
	public void clear() {
		cache.clear();
	}
	
	/**
	 * Removes all cached entries based on the given base texture. 
	 */
	public void remove(ITexture tex) {
		cache.remove(tex);
	}
	
	//Getters
	/**
	 * Functions exactly like
	 * {@link IImageFxLib#blurMultiple(ITexture, int, int, int, int)}, but
	 * caches its return values.
	 */
	public ITexture[] blurMultiple(ITexture itex, int levels, int k, int extendDirs) {
		CacheEntry entry = cache.get(itex);
		if (entry == null) {
			entry = new CacheEntry();
			cache.put(itex, entry);
		}

		ITexture[] blurred = new ITexture[levels];
		int count = entry.get(blurred, levels, k, extendDirs);
		if (count < levels) {
			//Generate any additional textures
			ITexture[] gen = imageFxLib.blurMultiple(itex, count, levels-count, k, extendDirs);
			for (ITexture t : gen) {
				blurred[count++] = t;
			}
			entry.put(levels, k, extendDirs, blurred);
		}
		return blurred;
	}
	
	//Setters
	
	//Inner Classes
	@LuaSerializable
	private static class CacheEntry implements Serializable {
				
		private static final long serialVersionUID = 1L;

		private Map<CKey, ITexture[]> map;
		
		public CacheEntry() {
			map = new HashMap<CKey, ITexture[]>();
		}
		
		private CKey createKey(int levels, int k, int extendDirs) {
			return new CKey(levels, k, extendDirs);
		}
		
		public int get(ITexture[] out, int levels, int k, int extendDirs) {
			ITexture[] texs = map.get(createKey(levels, k, extendDirs));
			int count = Math.min(levels, texs != null ? texs.length : 0);			
			for (int n = 0; n < count; n++) {
				out[n] = texs[n];
			}
			return count;
		}
		
		public void put(int levels, int k, int extendDirs, ITexture[] texs) {
			map.put(createKey(levels, k, extendDirs), texs);
		}
		
	}
	
	@LuaSerializable
	private static class CKey implements Serializable {

		private static final long serialVersionUID = 1L;
		
		private final int levels;
		private final int k;
		private final int extendDirs;		
		
		public CKey(int levels, int k, int extendDirs) {
			this.levels = levels;
			this.k = k;
			this.extendDirs = extendDirs;
		}
		
		@Override
		public int hashCode() {
			return (k<<16)|(levels<<8)|(extendDirs);
		}
		
		@Override
		public boolean equals(Object obj) {
			if (obj instanceof CKey) {
				CKey key = (CKey)obj;
				return levels == key.levels
					&& k == key.k
					&& extendDirs == key.extendDirs;
			}
			return false;
		}
		
	}
	
}
