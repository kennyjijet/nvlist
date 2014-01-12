package nl.weeaboo.vn.impl.lua;

import nl.weeaboo.lua2.io.LuaSerializable;
import nl.weeaboo.lua2.lib.LuaLibrary;
import nl.weeaboo.lua2.lib.LuajavaLib;
import nl.weeaboo.vn.IPixelShader;
import nl.weeaboo.vn.IShaderFactory;
import nl.weeaboo.vn.impl.base.BaseNotifier;

import org.luaj.vm2.Varargs;

@LuaSerializable
public class LuaShaderLib extends LuaLibrary {

	private static final long serialVersionUID = LuaImpl.serialVersionUID;

	private static final String[] NAMES = {
		"createGLSLShader",      //Can't change name, breaks code in LuaNovel
		"getGLSLVersion",        //Can't change name, breaks code in LuaNovel
		"isGLSLVersionSupported" //Can't change name, breaks code in LuaNovel
	};

	private static final int INIT                  = 0;
	private static final int CREATE_GLSL_SHADER    = 1;
	private static final int GET_GLSL_VERSION      = 2;
	private static final int IS_GLSL_VERSION_SUPPORTED = 3;
	
	private final IShaderFactory fac;
	private final BaseNotifier ntf;
	
	public LuaShaderLib(BaseNotifier ntf, IShaderFactory fac) {
		this.fac = fac;
		this.ntf = ntf;
	}
	
	@Override
	protected LuaLibrary newInstance() {
		return new LuaShaderLib(ntf, fac);
	}

	@Override
	public Varargs invoke(Varargs args) {
		switch (opcode) {
		case INIT: return initLibrary("Shader", NAMES, 1);
		case CREATE_GLSL_SHADER: return createGLSLShader(args);
		case GET_GLSL_VERSION: return getGLSLVersion(args);
		case IS_GLSL_VERSION_SUPPORTED: return isGLSLVersionSupported(args);
		default: return super.invoke(args);
		}
	}
	
	protected Varargs createGLSLShader(Varargs args) {
		String filename = args.checkjstring(1);
		IPixelShader shader = fac.createGLSLShader(filename);
		return LuajavaLib.toUserdata(shader, shader.getClass());
	}
	
	protected Varargs getGLSLVersion(Varargs args) {
		String version = fac.getGLSLVersion();
		return valueOf(version != null ? version : "0");
	}
	
	protected Varargs isGLSLVersionSupported(Varargs args) {
		String version = args.tojstring(1);
		if (version == null) {
			version = "0";
		}
		return valueOf(fac.isGLSLVersionSupported(version));
	}	
}
