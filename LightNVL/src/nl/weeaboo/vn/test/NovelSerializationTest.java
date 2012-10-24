package nl.weeaboo.vn.test;

import java.io.Externalizable;
import java.io.Serializable;
import java.lang.reflect.Array;
import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.util.Arrays;
import java.util.Collection;
import java.util.Comparator;
import java.util.EnumMap;
import java.util.IdentityHashMap;
import java.util.Map.Entry;
import java.util.Set;
import java.util.TreeMap;

import nl.weeaboo.lua2.io.LuaSerializer;
import nl.weeaboo.vn.impl.lua.LuaNovel;

public class NovelSerializationTest {
	
	public void testNovel(LuaSerializer ls, LuaNovel novel, Set<Class<?>> whitelist) throws IllegalArgumentException, IllegalAccessException {
		IdentityHashMap<Object, ObjectMeta> found = new IdentityHashMap<Object, ObjectMeta>();
		reachAttributes(found, ls, novel);

		TreeMap<Object, ObjectMeta> map = new TreeMap<Object, ObjectMeta>(
			new Comparator<Object>() {
				public int compare(Object o1, Object o2) {
					Class<?> c1 = o1.getClass();
					Class<?> c2 = o2.getClass();
					
					if (c1.isPrimitive() && !c2.isPrimitive()) return -1;
					if (!c1.isPrimitive() && c2.isPrimitive()) return 1;
					
					return c1.getName().compareTo(c2.getName());
				}					
			});
		map.putAll(found);		
		
		StringBuilder errbuf = new StringBuilder();
		for (Entry<Object, ObjectMeta> entry : map.entrySet()) {
			Class<?> c = entry.getKey().getClass();
			System.out.println(c.getName() + " " + entry.getValue());
			if (!whitelist.contains(c)) {
				errbuf.append("[not whitelisted] " + c.getName() + "\n");
			} else if (!Serializable.class.isAssignableFrom(c)
				&& !Externalizable.class.isAssignableFrom(c))
			{
				errbuf.append("[not serializable] " + c.getName() + "\n");
			}
		}
		System.err.println(errbuf.toString());
	}
	
	public static void reachObject(IdentityHashMap<Object, ObjectMeta> found, LuaSerializer ls,
			Object parent, Object obj) throws IllegalArgumentException, IllegalAccessException
	{
		if (obj == null) return;
		
		Class<?> clazz = obj.getClass();
		
		if (ls.getEnvironment().getId(obj) != null) {
			return; //Don't follow objects in environment
		//} else if (clazz == ClassMetaTable.class) {
		//	return; //Don't follow ClassInfo.MetaTable objects
		} else if (clazz.isPrimitive()) {
			return; //Don't follow primitives
		} else if (clazz == Boolean.class || clazz == Byte.class || clazz == Short.class
				|| clazz == Integer.class || clazz == Long.class || clazz == Float.class
				|| clazz == Double.class || clazz == String.class
				|| clazz == EnumMap.class)
		{
			return; //Don't follow primitives
		}
		
		ObjectMeta meta = found.get(obj);
		if (meta == null) {
			found.put(obj, meta = new ObjectMeta(parent));
			reachAttributes(found, ls, obj);
		} else {
			meta.referenced(parent);
		}		
	}
	protected static void reachAttributes(IdentityHashMap<Object, ObjectMeta> found, LuaSerializer ls,
			Object obj) throws IllegalArgumentException, IllegalAccessException
	{
		Class<?> clazz = obj.getClass();
		if (clazz.isArray()) {
			int len = Array.getLength(obj);
			for (int n = 0; n < len; n++) {
				reachObject(found, ls, obj, Array.get(obj, n));
			}
		} else {		
			while (clazz != null) {
				for (Field f : clazz.getDeclaredFields()) {
					f.setAccessible(true);
					if ((f.getModifiers() & (Modifier.STATIC|Modifier.TRANSIENT)) == 0) {
						reachObject(found, ls, obj, f.get(obj));
					}
				}
				clazz = clazz.getSuperclass();
			}
		}
	}

	//Inner Classes
	public static class ObjectMeta {
		
		private IdentityHashMap<Object, Boolean> parents;
		private int references;
		
		public ObjectMeta(Object parent) {
			parents = new IdentityHashMap<Object, Boolean>();
			parents.put(parent, Boolean.TRUE);
			references = 1;
		}
		
		private void referenced(Object parent) {
			if (parent != null) parents.put(parent, Boolean.TRUE);
			references++;
		}
		
		public Collection<Object> getParents() { return parents.keySet(); }
		
		public int getReferenceCount() { return references; }	
		
		public String toString() {
			Class<?> ps[] = new Class<?>[parents.size()];
			int t = 0;
			for (Object obj : parents.keySet()) {
				ps[t++] = obj.getClass();
			}
			
			return String.format("[parents=%s, refs=%d]", Arrays.toString(ps), references);
		}
	}
	
}
