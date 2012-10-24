package nl.weeaboo.vn.impl.lua;

import java.io.ObjectStreamException;
import java.io.Serializable;

import nl.weeaboo.io.EnvironmentSerializable;
import nl.weeaboo.lua2.io.LuaSerializable;
import nl.weeaboo.vn.impl.base.BaseStorage;

@LuaSerializable
public class SavepointStorage extends BaseStorage implements Serializable {

	private static final long serialVersionUID = LuaImpl.serialVersionUID;

	private final EnvironmentSerializable es;
	
	public SavepointStorage() {		
		this.es = new EnvironmentSerializable(this);
	}
	
	//Functions
	private Object writeReplace() throws ObjectStreamException {	
		return es.writeReplace();
	}
	
	//Getters
	
	//Setters
	
}
