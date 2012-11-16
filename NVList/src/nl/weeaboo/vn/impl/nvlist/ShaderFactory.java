package nl.weeaboo.vn.impl.nvlist;

import java.io.ObjectStreamException;
import java.io.Serializable;

import nl.weeaboo.gl.shader.GLShader;
import nl.weeaboo.gl.shader.ShaderCache;
import nl.weeaboo.io.EnvironmentSerializable;
import nl.weeaboo.vn.IHardwarePS;
import nl.weeaboo.vn.impl.base.BaseNotifier;
import nl.weeaboo.vn.impl.base.BaseShaderFactory;

public class ShaderFactory extends BaseShaderFactory implements Serializable {
	
	private final ShaderCache shCache;
	private final EnvironmentSerializable es;
	
	public ShaderFactory(BaseNotifier ntf, ShaderCache sc) {
		super(ntf);
		
		shCache = sc;		
		es = new EnvironmentSerializable(this);
	}
	
	//Functions
	private Object writeReplace() throws ObjectStreamException {	
		return es.writeReplace();
	}
	
	@Override
	public IHardwarePS createGLSLShader(String filename) {
		GLShader sh = getGLShader(filename);
		if (sh == null) {
			notifier.d("Unable to find shader file: " + filename);
			return null;
		}
		
		return new GLSLPS(this, sh);
	}
	
	//Getters
	public GLShader getGLShader(String filename) {
		return shCache.get(filename);
	}

	@Override
	public String getGLSLVersion() {
		return shCache.getGLSLVersion();
	}
	
	//Setters
	
}
