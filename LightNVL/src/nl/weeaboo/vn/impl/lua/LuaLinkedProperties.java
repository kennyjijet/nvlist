package nl.weeaboo.vn.impl.lua;

import java.io.Externalizable;
import java.io.IOException;
import java.io.ObjectInput;
import java.io.ObjectOutput;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;

import nl.weeaboo.lua2.io.LuaSerializable;

import org.luaj.vm2.LuaValue;

@LuaSerializable
final class LuaLinkedProperties implements Externalizable {
	
	//--- Manual serialization, don't add/change properties ---------------
	private Map<String, LuaValue> buffered;
	//--- Manual serialization, don't add/change properties ---------------
	
	public LuaLinkedProperties() {
		buffered = new HashMap<String, LuaValue>();
	}
	
	@Override
	public void writeExternal(ObjectOutput out) throws IOException {
		out.writeInt(buffered.size());
		for (Entry<String, LuaValue> entry : buffered.entrySet()) {
			out.writeUTF(entry.getKey());
			out.writeObject(entry.getValue());
		}
	}

	@Override
	public void readExternal(ObjectInput in) throws IOException, ClassNotFoundException {
		buffered.clear();
		int cachedL = in.readInt();
		for (int n = 0; n < cachedL; n++) {
			String key = in.readUTF();
			LuaValue val = (LuaValue)in.readObject();
			buffered.put(key, val);
		}
	}
	
	public void clear() {
		buffered.clear();
	}
	
	public void flush(LuaValue globals) {
		if (globals == null) {
			throw new IllegalArgumentException("globals must be non-null for flush");
		}
		
		for (Entry<String, LuaValue> entry : buffered.entrySet()) {
			globals.rawset(entry.getKey(), entry.getValue());
			//System.out.println("FLUSH: " + entry.getKey() + " " + entry.getValue());
		}
		//buffered.clear();
	}
	
	public LuaValue readFromLua(LuaValue globals, String key) {
		if (globals == null) {
			return buffered.get(key);
		} else {
			//buffered.remove(key);
			return globals.rawget(key);
		}
	}
	
	public void writeToLua(LuaValue globals, String key, LuaValue val) {
		//System.out.println("WRITE: " + key + " " + val);

		if (globals != null) {
			globals.rawset(key, val);
			//buffered.remove(key);
		} else {
			buffered.put(key, val);
		}
	}
	
}
