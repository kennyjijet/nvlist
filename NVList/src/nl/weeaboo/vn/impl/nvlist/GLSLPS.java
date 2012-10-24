package nl.weeaboo.vn.impl.nvlist;

import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;

import javax.media.opengl.GL2ES2;

import nl.weeaboo.common.Dim;
import nl.weeaboo.common.Dim2D;
import nl.weeaboo.common.Rect;
import nl.weeaboo.common.Rect2D;
import nl.weeaboo.common.StringUtil;
import nl.weeaboo.gl.GLManager;
import nl.weeaboo.gl.shader.GLShader;
import nl.weeaboo.gl.texture.GLTexture;
import nl.weeaboo.lua2.io.LuaSerializable;
import nl.weeaboo.lua2.lib.LuaLibrary;
import nl.weeaboo.lua2.lib.LuajavaLib;
import nl.weeaboo.vn.IPixelShader;
import nl.weeaboo.vn.IRenderer;
import nl.weeaboo.vn.ITexture;
import nl.weeaboo.vn.impl.base.BaseNotifier;
import nl.weeaboo.vn.impl.base.BaseShader;

import org.luaj.vm2.LuaValue;
import org.luaj.vm2.Varargs;

@LuaSerializable
public class GLSLPS extends BaseShader implements IPixelShader {
		
	private static final long serialVersionUID = NVListImpl.serialVersionUID;

	protected final ImageFactory imageFactory;
	protected final BaseNotifier notifier;
	protected final String filename;
	
	private final Map<String, Object> params;
	private final ITexture[] textures;
	
	private transient GLShader shader;
	
	public GLSLPS(ImageFactory fac, BaseNotifier ntf, String filename) {
		super(false);
		
		this.imageFactory = fac;
		this.notifier = ntf;
		this.filename = filename;
		
		params = new HashMap<String, Object>();
		textures = new ITexture[3];
	}

	//Functions 
	public static void install(LuaValue globals, final ImageFactory ifac, final BaseNotifier ntf) {
		globals.load(new GLSLLib(ifac, ntf));
	}
	
	@Override
	public void preDraw(IRenderer r) {
		Renderer rr = (Renderer)r;
		GLManager glm = rr.getGLManager();
		
		if (shader == null) {
			shader = imageFactory.getGLShader(filename);
		}
		
		if (shader != null) {
			int rx = r.getRealX();
			int ry = r.getRealY();
			int rw = r.getRealWidth();
			int rh = r.getRealHeight();
			double sw = r.getScreenWidth();
			double sh = r.getScreenHeight();
			Rect2D screen;
			if (sw > 0 && sh > 0) {
				screen = new Rect2D(-1+2*rx/sw, -1+2*ry/sh, 2*rw/sw, 2*rh/sh);
			} else {
				screen = new Rect2D(0, 0, 1, 1);
			}
			
			shader.forceLoad(glm);
			glm.setShader(shader);

			GLTexture tex = glm.getTexture();
			if (tex != null) {
				tex = tex.forceLoad(glm);
			}
			GL2ES2 gl2 = glm.getGL().getGL2ES2();
			shader.setTextureParam(gl2, "tex", 0, tex != null ? tex.getTexId() : 0);
			
			applyShaderParam(glm, shader, "time", getTime());
			applyShaderParam(glm, shader, "screen", screen);
			for (Entry<String, Object> entry : params.entrySet()) {
				try {
					applyShaderParam(glm, shader, entry.getKey(), entry.getValue());
				} catch (IllegalArgumentException iae) {
					params.remove(entry.getKey());					
					throw iae; //Must exit loop now to avoid ConcurrentModificationException
				}
			}
		}
	}

	@Override
	public void postDraw(IRenderer r) {
		Renderer rr = (Renderer)r;
		GLManager glm = rr.getGLManager();
		
		glm.setShader(null);
	}

	protected void applyShaderParam(GLManager glm, GLShader shader, String name, Object value) {
		GL2ES2 gl2 = glm.getGL().getGL2ES2();
		
		if (value instanceof TextureAdapter) {
			TextureAdapter ta = (TextureAdapter)value;
			ta.forceLoad(glm);
			int slot = 1 + onTexParamAdded(ta);
			shader.setTextureParam(gl2, name, slot, ta.getTexId());
			//System.out.println("set tex param " + slot + " " + name + " " + ta.getTexId());
		} else if (value instanceof float[]) {
			float[] f = (float[])value;
			//System.out.println(name + " " + Arrays.toString(f));
			if (f.length == 1) {
				shader.setFloatParam(gl2, name, f[0]);
			} else if (f.length == 2) {
				shader.setVec2Param(gl2, name, f, 0);
			} else if (f.length == 3) {
				shader.setVec3Param(gl2, name, f, 0);
			} else if (f.length >= 4) {
				shader.setVec4Param(gl2, name, f, 0);
			}
		} else if (value instanceof Rect2D) {
			Rect2D r = (Rect2D)value;
			shader.setVec4Param(gl2, name, (float)r.x, (float)r.y, (float)r.w, (float)r.h);
		} else if (value instanceof Rect) {
			Rect r = (Rect)value;
			shader.setVec4Param(gl2, name, r.x, r.y, r.w, r.h);
		} else if (value instanceof Dim2D) {
			Dim2D d = (Dim2D)value;
			shader.setVec2Param(gl2, name, (float)d.w, (float)d.h);
		} else if (value instanceof Dim) {
			Dim d = (Dim)value;
			shader.setVec2Param(gl2, name, d.w, d.h);
		} else if (value instanceof Number) {
			shader.setFloatParam(gl2, name, ((Number)value).floatValue());
		} else {
			throw new IllegalArgumentException("Unsupported param type: " + (value != null ? value.getClass() : null));
		}
	}
	
	private void onTexParamRemoved(ITexture tex) {
		if (!params.values().contains(tex)) {
			//Texture is no longer used, we can clear its slot
			for (int n = 0; n < textures.length; n++) {
				if (textures[n] == tex) {
					textures[n] = null;
				}
			}
		}		
	}
	
	private int onTexParamAdded(ITexture tex) {
		//Check if already added
		for (int n = 0; n < textures.length; n++) {
			if (textures[n] == tex) {
				return n;
			}
		}
		
		//Add to empty slot
		for (int n = 0; n < textures.length; n++) {
			if (textures[n] == null) {
				textures[n] = tex;
				return n;
			}
		}
		
		throw new IllegalStateException("Max number of texture params reached: " + textures.length);
	}
	
	//Getters
	
	//Setters
	public void setParam(String name, ITexture tex) {
		Object removed;
		if (tex == null) {
			removed = params.remove(name);
		} else {
			removed = params.put(name, tex);
		}
		
		if (removed instanceof ITexture) {
			onTexParamRemoved((ITexture)removed);
		}
		if (tex != null) {
			onTexParamAdded(tex);
		}
	}
	public void setParam(String name, float[] values) {
		Object removed;
		if (values == null) {
			removed = params.remove(name);
		} else {
			removed = params.put(name, values);
		}

		if (removed instanceof ITexture) {
			onTexParamRemoved((ITexture)removed);
		}
	}
	
	//Inner Classes
	@LuaSerializable
	private static class GLSLLib extends LuaLibrary {
		
		private static final long serialVersionUID = NVListImpl.serialVersionUID;

		private static final String[] NAMES = {
			"new",
			"getVersion",
			"isVersionSupported"
		};

		private static final int INIT                  = 0;
		private static final int NEW                   = 1;
		private static final int GET_VERSION           = 2;
		private static final int IS_VERSION_SUPPORTED  = 3;
		
		private final ImageFactory fac;
		private final BaseNotifier ntf;
		
		private GLSLLib(ImageFactory fac, BaseNotifier ntf) {
			this.fac = fac;
			this.ntf = ntf;
		}
		
		@Override
		protected LuaLibrary newInstance() {
			return new GLSLLib(fac, ntf);
		}

		@Override
		public Varargs invoke(Varargs args) {
			switch (opcode) {
			case INIT: return initLibrary("GLSL", NAMES, 1);
			case NEW: return newInstance(args);
			case GET_VERSION: return getVersion(args);
			case IS_VERSION_SUPPORTED: return isVersionSupported(args);
			default: return super.invoke(args);
			}
		}
		
		protected Varargs newInstance(Varargs args) {
			String filename = args.checkjstring(1);
			GLSLPS shader = new GLSLPS(fac, ntf, filename);
			return LuajavaLib.toUserdata(shader, shader.getClass());
		}
		
		protected Varargs getVersion(Varargs args) {
			String version = fac.getGlslVersion();
			return valueOf(version != null ? version : "0");
		}
		
		protected Varargs isVersionSupported(Varargs args) {
			String a = args.tojstring(1);
			String b = fac.getGlslVersion();
			return valueOf(b != null && !b.equals("") && StringUtil.compareVersion(a, b) <= 0);
		}
		
	}
	
}
