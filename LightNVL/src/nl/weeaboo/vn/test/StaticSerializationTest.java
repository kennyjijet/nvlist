package nl.weeaboo.vn.test;

import java.io.Externalizable;
import java.io.Serializable;
import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.util.Arrays;
import java.util.Collection;
import java.util.Comparator;
import java.util.HashSet;
import java.util.IdentityHashMap;
import java.util.Map.Entry;
import java.util.Set;
import java.util.TreeMap;

import nl.weeaboo.lua2.io.LuaSerializer;
import nl.weeaboo.vn.impl.lua.LuaNovel;

import org.junit.Test;

public class StaticSerializationTest {
	
	@Test
	public void testBaseImpl() {
		testImpl(new LuaSerializer(), LuaNovel.class);
	}
	
	public void testImpl(LuaSerializer ls, Class<?> clazz) {
		Set<Class<?>> externalEnv = new HashSet<Class<?>>();
		for (Object obj : ls.getEnvironment().toMap().keySet()) {
			externalEnv.add(obj.getClass());
		}
		
		TreeMap<Class<?>, ClassMeta> map = new TreeMap<Class<?>, ClassMeta>(
			new Comparator<Class<?>>() {
				public int compare(Class<?> c1, Class<?> c2) {
					if (c1.isPrimitive() && !c2.isPrimitive()) return -1;
					if (!c1.isPrimitive() && c2.isPrimitive()) return 1;
					
					return c1.getName().compareTo(c2.getName());
				}					
			});
		map.putAll(findReachableTypes(externalEnv, clazz));
		
		StringBuilder errbuf = new StringBuilder();
		for (Entry<Class<?>, ClassMeta> entry : map.entrySet()) {
			Class<?> c = entry.getKey();
			System.out.println(c.getName() + " " + entry.getValue());
			if (!c.isPrimitive()
				&& !Serializable.class.isAssignableFrom(c)
				&& !Externalizable.class.isAssignableFrom(c))
			{
				errbuf.append("[not serializable] " + c.getName() + "\n");
			}
		}
		System.err.println(errbuf.toString());
	}
	
	public static IdentityHashMap<Class<?>, ClassMeta> findReachableTypes(
			Set<Class<?>> externalEnv, Class<?> clazz)
	{
		IdentityHashMap<Class<?>, ClassMeta> found = new IdentityHashMap<Class<?>, ClassMeta>();
		findReachableTypes(found, externalEnv, null, clazz);
		return found;
	}
	private static void findReachableTypes(IdentityHashMap<Class<?>, ClassMeta> found,
			Set<Class<?>> externalEnv, Class<?> parent, Class<?> clazz)
	{
		while (clazz != null && clazz.isArray()) {
			clazz = clazz.getComponentType();
		}
		
		ClassMeta meta = found.get(clazz);
		if (meta != null) {
			meta.referenced(parent);
			return;
		} else if (externalEnv.contains(clazz)) {
			return;
		}
		
		found.put(clazz, meta = new ClassMeta(parent));
		while (clazz != null) {
			for (Field f : clazz.getDeclaredFields()) {
				if ((f.getModifiers() & (Modifier.STATIC|Modifier.TRANSIENT)) == 0) {
					findReachableTypes(found, externalEnv, clazz, f.getType());
				}
			}
			clazz = clazz.getSuperclass();
		}
	}
	
	//Inner Classes
	public static class ClassMeta {
		
		private Set<Class<?>> parents;
		private int references;
		
		public ClassMeta(Class<?> parent) {
			parents = new HashSet<Class<?>>();
			parents.add(parent);
			references = 1;
		}
		
		private void referenced(Class<?> parent) {
			parents.add(parent);
			references++;
		}
		
		public Collection<Class<?>> getParents() { return parents; }
		
		public int getReferenceCount() { return references; }	
		
		public String toString() {
			return String.format("[parents=%s, refs=%d]", Arrays.toString(parents.toArray()), references);
		}
	}
	
}
