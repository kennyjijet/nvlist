package nl.weeaboo.vn.impl.base;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import nl.weeaboo.common.Rect2D;
import nl.weeaboo.vn.IHardwarePS;
import nl.weeaboo.vn.IRenderer;
import nl.weeaboo.vn.ITexture;
import nl.weeaboo.vn.RenderEnv;

public abstract class BaseHardwarePS extends BaseShader implements IHardwarePS, Serializable {

	private static final long serialVersionUID = BaseImpl.serialVersionUID;

	protected final BaseShaderFactory shfac;
	
	private final Map<String, Serializable> params;
	private final List<ITexture> textures;
	
	private transient boolean shading;
	
	protected BaseHardwarePS(BaseShaderFactory fac) {
		super(false);
		
		shfac = fac;
		params = new HashMap<String, Serializable>();
		textures = new ArrayList<ITexture>(4); //Random removal sometimes required, but very small size makes ArrayList good enough.
	}
	
	//Functions
	@Override
	public void preDraw(IRenderer r) {
		RenderEnv env = r.getEnv();
		
		if (startShader(r)) {
			shading = true;
			
			applyShaderParam(r, "time", getTime());
			applyShaderParam(r, "screen", getScreenParamValue(env));
			for (Entry<String, Serializable> entry : params.entrySet()) {
				try {
					applyShaderParam(r, entry.getKey(), entry.getValue());
				} catch (IllegalArgumentException iae) {
					params.remove(entry.getKey());					
					throw iae; //Must exit loop now to avoid ConcurrentModificationException
				}
			}
		}
	}
	
	@Override
	public void postDraw(IRenderer r) {
		if (shading) {
			shading = false;
			
			int t = 0;
			int[] texIndices = new int[textures.size()];
			for (ITexture tex : textures) {
				texIndices[t++] = getTextureIndex(tex);
			}
			resetTextures(r, texIndices);
			
			stopShader(r);
		}
	}

	protected abstract boolean startShader(IRenderer r);
	
	protected abstract void stopShader(IRenderer r);

	protected abstract void resetTextures(IRenderer r, int... texIndices);
	
	protected void applyShaderParam(IRenderer r, String name, Object value) {
		if (value instanceof ITexture) {
			ITexture tex = (ITexture)value;
			applyTextureParam(r, name, getTextureIndex(tex), tex);
		} else if (value instanceof float[]) {
			float[] f = (float[])value;
			if (f.length == 1) {
				applyFloat1Param(r, name, f[0]);
			} else if (f.length == 2) {
				applyFloat2Param(r, name, f[0], f[1]);
			} else if (f.length == 3) {
				applyFloat3Param(r, name, f[0], f[1], f[2]);
			} else if (f.length >= 4) {
				applyFloat4Param(r, name, f[0], f[1], f[2], f[3]);
			} else {
				throw new IllegalArgumentException("Unsupported float param array length: " + f.length);
			}
		} else if (value instanceof Rect2D) {
			Rect2D rect = (Rect2D)value;
			applyFloat4Param(r, name, (float)rect.x, (float)rect.y, (float)rect.w, (float)rect.h);
		} else if (value instanceof Number) {
			applyFloat1Param(r, name, ((Number)value).floatValue());
		} else {
			throw new IllegalArgumentException("Unsupported param type: " + (value != null ? value.getClass() : null));
		}
	}
	
	protected abstract void applyTextureParam(IRenderer r, String name, int texIndex, ITexture tex);
	protected abstract void applyFloat1Param(IRenderer r, String name, float v1);
	protected abstract void applyFloat2Param(IRenderer r, String name, float v1, float v2);
	protected abstract void applyFloat3Param(IRenderer r, String name, float v1, float v2, float v3);
	protected abstract void applyFloat4Param(IRenderer r, String name, float v1, float v2, float v3, float v4);
	
	@Override
	public void removeParam(String name) {
		Object removed = params.remove(name);
		if (removed != null) onParamRemoved(name, removed);
	}
	
	protected void onParamRemoved(String name, Object value) {
		if (value instanceof ITexture) {
			if (!params.values().contains(value)) {
				textures.remove(value); //Texture no longer referenced, remove
			}
		}
	}
	protected void onParamAdded(String name, Object value) {
		if (value instanceof ITexture) {
			ITexture tex = (ITexture)value;
			if (!textures.contains(tex)) {
				textures.add(tex);
			}
		}
	}
	
	//Getters
	protected Collection<ITexture> getTextures() {
		return Collections.unmodifiableCollection(textures);
	}
	
	protected int getTextureIndex(ITexture tex) {
		int index = textures.indexOf(tex);
		return (index < 0 ? -1 : 1+index);
	}
	
	protected Rect2D getScreenParamValue(RenderEnv env) {
		Rect2D screen;
		if (env != null && env.sw > 0 && env.sh > 0) {
			screen = new Rect2D(-1+2*env.rx/env.sw, -1+2*env.ry/env.sh, 2*env.rw/env.sw, 2*env.rh/env.sh);
		} else {
			screen = new Rect2D(0, 0, 1, 1);
		}
		return screen;
	}
	
	//Setters
	protected void setParam0(String name, Serializable obj) {
		Object removed = params.put(name, obj);
		if (removed != null) onParamRemoved(name, removed);
		onParamAdded(name, obj);
	}
	
	@Override
	public void setParam(String name, ITexture tex) {
		if (tex == null) {
			removeParam(name);
		} else {
			setParam0(name, tex);
		}
	}

	@Override
	public void setParam(String name, float value) {
		setParam(name, new float[] {value}, 0, 1);
	}

	@Override
	public void setParam(String name, float[] values, int off, int len) {
		if (values == null) {
			removeParam(name);
		} else {
			if (len <= 0 || len > 4) {
				throw new IllegalArgumentException("Float params must have: 0 <= len <= 4, given: " + len);
			}
			
			float[] copy = new float[len];
			for (int n = 0; n < len; n++) {
				copy[n] = values[off+n];
			}
			setParam0(name, copy);
		}
	}
	
}
