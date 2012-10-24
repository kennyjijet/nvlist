package nl.weeaboo.vn.impl.base;

import java.io.IOException;
import java.io.ObjectInput;
import java.io.ObjectInputStream;
import java.io.ObjectOutput;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import nl.weeaboo.lua2.lib.CoerceLuaToJava;
import nl.weeaboo.vn.IStorage;

import org.luaj.vm2.LuaTable;
import org.luaj.vm2.LuaValue;

public class BaseStorage implements IStorage, Serializable {

	private static final long serialVersionUID = BaseImpl.serialVersionUID;
	
	private transient Map<String, Object> properties;
	
	public BaseStorage() {
		initTransients();
	}
	
	//Functions
	private void initTransients() {
		properties = new HashMap<String, Object>();		
	}
	
	private void writeObject(ObjectOutputStream out) throws IOException {
		out.defaultWriteObject();
		save0(out);
	}
	private void save0(ObjectOutput out) throws IOException {
		out.writeInt(properties.size());
		for (Entry<String, Object> entry : properties.entrySet()) {
			out.writeUTF(entry.getKey());
			out.writeObject(entry.getValue());
		}
	}
	
	private void readObject(ObjectInputStream in) throws IOException, ClassNotFoundException {
		in.defaultReadObject();

		initTransients();
		
		load0(in);
	}
	private void load0(ObjectInput in) throws ClassNotFoundException, IOException {
		int size = in.readInt();
		for (int n = 0; n < size; n++) {
			String key = in.readUTF();
			Object val = in.readObject();
			properties.put(key, val);
		}		
	}
	
	private void checkKey(String key) {
		if (key == null) {
			throw new IllegalArgumentException("Key must not be null");		
		}
	}
	
	protected void onChanged() {
		
	}
	
	@Override
	public void load(ObjectInput in) throws ClassNotFoundException, IOException {
		load0(in);
	}
	
	@Override
	public void save(ObjectOutput out) throws IOException {
		save0(out);
	}
	
	@Override
	public void clear() {
		properties.clear();		
		onChanged();
	}

	@Override
	public Object remove(String key) {
		Object val = properties.remove(key);
		onChanged();
		return val;
	}
		
	//Getters
	@Override
	public Object get(String key) {
		return properties.get(key);
	}
	
	@Override
	public String[] getKeys(String prefix) {
		List<String> result = new ArrayList<String>();
		for (String key : properties.keySet()) {
			if (prefix == null || key.startsWith(prefix)) {
				result.add(key);
			}
		}
		return result.toArray(new String[result.size()]);
	}
	
	@Override
	public boolean getBoolean(String key, boolean defaultValue) {
		Object obj = get(key);
		return (obj instanceof Boolean ? (Boolean)obj : defaultValue);
	}

	@Override
	public int getInt(String key, int defaultValue) {
		Object obj = get(key);
		if (obj instanceof Number) {
			Number number = (Number)obj;
			return number.intValue();
		}
		return defaultValue;
	}

	@Override
	public long getLong(String key, long defaultValue) {
		Object obj = get(key);
		if (obj instanceof Number) {
			Number number = (Number)obj;
			return number.longValue();
		}
		return defaultValue;
	}

	@Override
	public float getFloat(String key, float defaultValue) {
		Object obj = get(key);
		if (obj instanceof Number) {
			Number number = (Number)obj;
			return number.floatValue();
		}
		return defaultValue;
	}

	@Override
	public double getDouble(String key, double defaultValue) {
		Object obj = get(key);
		if (obj instanceof Number) {
			Number number = (Number)obj;
			return number.doubleValue();
		}
		return defaultValue;
	}

	@Override
	public String getString(String key, String defaultValue) {
		Object obj = get(key);
		return (obj != null ? obj.toString() : defaultValue);
	}
	
	//Setters
	@Override
	public void set(String key, Object val) {
		checkKey(key);

		//System.out.println("set " + key + " " + val);
		
		if (val == null) {
			remove(key);
		} else if (val instanceof Boolean) {
			setBoolean(key, (Boolean)val);
		} else if (val instanceof Integer) {
			setInt(key, (Integer)val);
		} else if (val instanceof Long) {
			setLong(key, (Long)val);
		} else if (val instanceof Float) {
			setFloat(key, (Float)val);
		} else if (val instanceof Double) {
			setDouble(key, (Double)val);
		} else if (val instanceof String) {
			setString(key, (String)val);
		} else if (val instanceof Map<?, ?>) {
			String prefix = (key.length() > 0 ? key + "." : key);
			Map<?, ?> map = (Map<?, ?>)val;
			for (Entry<?, ?> entry : map.entrySet()) {
				set(prefix + entry.getKey(), entry.getValue());
			}
		} else if (val instanceof LuaTable) {
			String prefix = (key.length() > 0 ? key + "." : key);
			LuaTable table = (LuaTable)val;			
			for (LuaValue subkey : table.keys()) {
				Object subval = CoerceLuaToJava.coerceArg(table.get(subkey), Object.class);
				set(prefix + subkey.tojstring(), subval);
			}
		} else if (val instanceof IStorage) {
			//Be careful that adding an object to itself doesn't cause problems
			String prefix = (key.length() > 0 ? key + "." : key);
			IStorage s = (IStorage)val;			
			for (String subkey : s.getKeys(null)) {
				set(prefix + subkey, s.get(subkey));
			}
		} else {		
			throw new IllegalArgumentException("Unsupported value type: " + val.getClass());
		}
	}
	
	@Override
	public void setBoolean(String key, boolean val) {
		checkKey(key);		
		properties.put(key, val);
		onChanged();
	}

	@Override
	public void setInt(String key, int val) {
		checkKey(key);		
		properties.put(key, val);
		onChanged();
	}

	@Override
	public void setLong(String key, long val) {
		checkKey(key);		
		properties.put(key, val);
		onChanged();
	}

	@Override
	public void setFloat(String key, float val) {
		checkKey(key);		
		properties.put(key, val);
		onChanged();
	}

	@Override
	public void setDouble(String key, double val) {
		checkKey(key);		
		properties.put(key, val);
		onChanged();
	}

	@Override
	public void setString(String key, String val) {
		checkKey(key);		
		properties.put(key, val);
		onChanged();
	}
	
}
