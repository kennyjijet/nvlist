package nl.weeaboo.vn.impl.nvlist;

import java.io.ObjectStreamException;
import java.io.Serializable;

import nl.weeaboo.gl.shader.GLShader;
import nl.weeaboo.gl.shader.IShaderStore;
import nl.weeaboo.io.EnvironmentSerializable;
import nl.weeaboo.lua2.io.LuaSerializable;
import nl.weeaboo.vn.IPixelShader;
import nl.weeaboo.vn.impl.base.BaseNotifier;
import nl.weeaboo.vn.impl.base.BaseShaderFactory;

@LuaSerializable
public class ShaderFactory extends BaseShaderFactory implements Serializable {
	
	private final IShaderStore shStore;
	private final EnvironmentSerializable es;
	
	public ShaderFactory(BaseNotifier ntf, IShaderStore ss) {
		super(ntf);
		
		shStore = ss;		
		es = new EnvironmentSerializable(this);
	}
	
	//Functions
	private Object writeReplace() throws ObjectStreamException {	
		return es.writeReplace();
	}
	
	@Override
	public IPixelShader createGLSLShader(String filename) {
		GLShader sh = getGLShader(filename);
		if (sh == null) {
			notifier.d("Unable to find shader file: " + filename);
			return null;
		}
		
		return new GLSLPS(this, sh);
	}
	
	//Getters
	public GLShader getGLShader(String filename) {
		return shStore.getShader(filename);
	}

	@Override
	public String getGLSLVersion() {
		return shStore.getGLInfo().getGLSLVersion();
	}
	
	//Setters
	
}
