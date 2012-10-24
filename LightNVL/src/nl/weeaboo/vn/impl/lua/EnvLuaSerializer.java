package nl.weeaboo.vn.impl.lua;

import java.io.IOException;
import java.io.OutputStream;

import nl.weeaboo.io.EnvironmentSerializable;
import nl.weeaboo.io.EnvironmentSerializable.Environment;
import nl.weeaboo.lua2.io.LuaSerializer;
import nl.weeaboo.lua2.io.ObjectSerializer;

public class EnvLuaSerializer extends LuaSerializer {

	private final Environment env;
	
	public EnvLuaSerializer() {
		env = EnvironmentSerializable.getEnvironment();
	}

	//Functions
	@Override
	protected void serializerInitAsync() {
		EnvironmentSerializable.setEnvironment(env);
		
		super.serializerInitAsync();
	}
	
	@Override
	public ObjectSerializer openSerializer(OutputStream out) throws IOException {
		ObjectSerializer os = super.openSerializer(out);
		os.setAllowedClasses(EnvironmentSerializable.RefEnvironment.class);
		return os;
	}
	//Getters
	
	//Setters
	
}
