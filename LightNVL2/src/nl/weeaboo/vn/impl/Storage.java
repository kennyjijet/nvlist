package nl.weeaboo.vn.impl;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import nl.weeaboo.common.Checks;
import nl.weeaboo.vn.IStorage;
import nl.weeaboo.vn.StoragePrimitive;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class Storage implements IStorage {

    private static Logger LOG = LoggerFactory.getLogger(Storage.class);

    private Map<String, StoragePrimitive> properties = new HashMap<String, StoragePrimitive>();

    public Storage() {
    }

    public Storage(IStorage storage) {
        this();

        for (String key : storage.getKeys()) {
            properties.put(key, storage.get(key));
        }
    }

    //Functions
    public static Storage fromMap(Map<String, StoragePrimitive> map) {
        Storage result = new Storage();
        for (Entry<String, StoragePrimitive> entry : map.entrySet()) {
            result.set(entry.getKey(), entry.getValue());
        }
        return result;
    }

    private void checkKey(String key) {
        Checks.checkNotNull(key, "Key must not be null");
    }

    protected void onChanged() {
    }

    @Override
    public void clear() {
        LOG.trace(this + ": Clearing storage");

        properties.clear();
        onChanged();
    }

    @Override
    public StoragePrimitive remove(String key) {
        StoragePrimitive removed = properties.remove(key);
        if (removed != null) {
            LOG.trace(this + ": Remove " + key);
            onChanged();
        }
        return removed;
    }

    @Override
    public void addAll(IStorage val) {
        addAll("", val);
    }

    @Override
    public void addAll(String key, IStorage val) {
        //Be careful that adding an object to itself doesn't cause problems
        String prefix = (key.length() > 0 ? key + "." : key);
        IStorage s = val;
        for (String subkey : s.getKeys()) {
            set(prefix + subkey, s.get(subkey));
        }
    }

    //Getters
    @Override
    public Collection<String> getKeys() {
        return getKeys(null);
    }

    @Override
    public Collection<String> getKeys(String prefix) {
        List<String> result = new ArrayList<String>();
        for (String key : properties.keySet()) {
            if (prefix == null || key.startsWith(prefix)) {
                result.add(key);
            }
        }
        return Collections.unmodifiableCollection(result);
    }

    @Override
    public boolean contains(String key) {
        return get(key) != null;
    }

    @Override
    public StoragePrimitive get(String key) {
        checkKey(key);

        StoragePrimitive result = properties.get(key);

        LOG.trace(this + ": get(key=" + key + ") = " + result);

        return result;
    }

    @Override
    public boolean getBoolean(String key, boolean defaultValue) {
        StoragePrimitive obj = get(key);

        if (obj != null) {
            return obj.toBoolean(defaultValue);
        }
        return defaultValue;
    }

    @Override
    public int getInt(String key, int defaultValue) {
        return (int)getDouble(key, defaultValue);
    }

    @Override
    public float getFloat(String key, float defaultValue) {
        return (float)getDouble(key, defaultValue);
    }

    @Override
    public double getDouble(String key, double defaultValue) {
        StoragePrimitive obj = get(key);

        if (obj != null) {
            return obj.toDouble(defaultValue);
        }
        return defaultValue;
    }

    @Override
    public String getString(String key, String defaultValue) {
        StoragePrimitive obj = get(key);

        if (obj != null) {
            return obj.toString(defaultValue);
        }
        return defaultValue;
    }

    //Setters
    @Override
    public void set(String key, StoragePrimitive val) {
        checkKey(key);

        StoragePrimitive oldval;
        if (val == null) {
            oldval = remove(key);
        } else {
            oldval = properties.put(key, val);
        }

        LOG.trace(this + ": set(key=" + key + ") = " + val);

        if (oldval != val && (oldval == null || !oldval.equals(val))) {
            onChanged();
        }
    }

    @Override
    public void setBoolean(String key, boolean val) {
        set(key, StoragePrimitive.fromBoolean(val));
    }

    @Override
    public void setInt(String key, int val) {
        setDouble(key, val);
    }

    @Override
    public void setFloat(String key, float val) {
        setDouble(key, val);
    }

    @Override
    public void setDouble(String key, double val) {
        set(key, StoragePrimitive.fromDouble(val));
    }

    @Override
    public void setString(String key, String val) {
        set(key, StoragePrimitive.fromString(val));
    }

}
