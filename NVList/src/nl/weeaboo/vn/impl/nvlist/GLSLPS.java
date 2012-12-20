package nl.weeaboo.vn.impl.nvlist;

import java.io.Serializable;

import javax.media.opengl.GL;
import javax.media.opengl.GL2ES2;

import nl.weeaboo.gl.GLManager;
import nl.weeaboo.gl.shader.GLShader;
import nl.weeaboo.gl.texture.GLTexture;
import nl.weeaboo.lua2.io.LuaSerializable;
import nl.weeaboo.vn.IRenderer;
import nl.weeaboo.vn.ITexture;
import nl.weeaboo.vn.impl.base.BaseHardwarePS;

@LuaSerializable
public class GLSLPS extends BaseHardwarePS implements Serializable {
		
	private static final long serialVersionUID = NVListImpl.serialVersionUID;

	private final GLShader shader;
	
	public GLSLPS(ShaderFactory shfac, GLShader sh) {
		super(shfac);
		
		this.shader = sh;
	}

	//Functions 
	@Override
	protected boolean startShader(IRenderer r) {
		if (shader == null) {
			return false;
		}
		
		Renderer rr = Renderer.cast(r);
		GLManager glm = rr.getGLManager();
		shader.forceLoad(glm);
		glm.setShader(shader);

		GLTexture tex = glm.getTexture();
		if (tex == null) {
			applyTextureParam(r, "tex", 0, 0);
			//applyShaderParam(r, "texSize", new float[2]);
		} else {
			applyTextureParam(r, "tex", 0, tex.getTexId());
			//applyShaderParam(r, "texSize", new float[] {tex.getCropWidth(), tex.getCropHeight()});
		}
		
		return true;
	}

	@Override
	protected void stopShader(IRenderer r) {
		Renderer rr = Renderer.cast(r);
		GLManager glm = rr.getGLManager();
		glm.setShader(null);
	}

	@Override
	protected void resetTextures(IRenderer r, int... texIndices) {
		Renderer rr = Renderer.cast(r);
		GLManager glm = rr.getGLManager();
		GL gl = glm.getGL();
		for (int texIndex : texIndices) {
			gl.glActiveTexture(GL.GL_TEXTURE0 + texIndex);
			gl.glBindTexture(GL.GL_TEXTURE_2D, 0);
		}
		gl.glActiveTexture(GL.GL_TEXTURE0);
	}
	
	@Override
	protected void applyTextureParam(IRenderer r, String name, int texIndex, ITexture tex) {
		int texId = 0;
		if (tex instanceof TextureAdapter) {
			Renderer rr = Renderer.cast(r);
			TextureAdapter ta = (TextureAdapter)tex;
			ta.forceLoad(rr.getGLManager());
			texId = ta.getTexId();
		}
		applyTextureParam(r, name, texIndex, texId);
	}
	
	protected void applyTextureParam(IRenderer r, String name, int texIndex, int texId) {		
		shader.setTextureParam(getGL(r), name, texIndex, texId);
	}

	@Override
	protected void applyFloat1Param(IRenderer r, String name, float v1) {
		shader.setFloatParam(getGL(r), name, v1);
	}

	@Override
	protected void applyFloat2Param(IRenderer r, String name, float v1, float v2) {
		shader.setVec2Param(getGL(r), name, v1, v2);
	}

	@Override
	protected void applyFloat3Param(IRenderer r, String name, float v1, float v2, float v3) {
		shader.setVec3Param(getGL(r), name, v1, v2, v3);
	}

	@Override
	protected void applyFloat4Param(IRenderer r, String name, float v1, float v2, float v3, float v4) {
		shader.setVec4Param(getGL(r), name, v1, v2, v3, v4);
	}

	//Getters
	protected GL2ES2 getGL(IRenderer r) {
		GLManager glm = Renderer.cast(r).getGLManager();
		return glm.getGL().getGL2ES2();
	}
	
	//Setters
	
}
