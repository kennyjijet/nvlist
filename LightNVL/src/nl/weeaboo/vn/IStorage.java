package nl.weeaboo.vn;

import java.io.IOException;
import java.io.ObjectInput;
import java.io.ObjectOutput;

public interface IStorage {

	// === Functions ===========================================================
	public void clear();
	public Object remove(String key);
	
	public void load(ObjectInput in) throws ClassNotFoundException, IOException;
	public void save(ObjectOutput out) throws IOException;
	
	// === Getters =============================================================
	public Object get(String key);
	public String[] getKeys(String prefix);
	public boolean getBoolean(String key, boolean defaultValue);
	public int getInt(String key, int defaultValue);
	public long getLong(String key, long defaultValue);
	public float getFloat(String key, float defaultValue);
	public double getDouble(String key, double defaultValue);
	public String getString(String key, String defaultValue);
	
	// === Setters =============================================================
	public void set(String key, Object val);
	public void setBoolean(String key, boolean val);
	public void setInt(String key, int val);
	public void setLong(String key, long val);
	public void setFloat(String key, float val);
	public void setDouble(String key, double val);
	public void setString(String key, String val);

}
