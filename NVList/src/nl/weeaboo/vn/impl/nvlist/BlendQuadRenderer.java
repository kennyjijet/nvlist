package nl.weeaboo.vn.impl.nvlist;

import static javax.media.opengl.GL.GL_SRC_ALPHA;
import static javax.media.opengl.GL.GL_SRC_COLOR;
import static javax.media.opengl.GL.GL_TEXTURE0;
import static javax.media.opengl.GL.GL_TEXTURE1;
import static javax.media.opengl.GL.GL_TEXTURE_2D;
import static javax.media.opengl.GL2ES1.GL_COMBINE;
import static javax.media.opengl.GL2ES1.GL_COMBINE_ALPHA;
import static javax.media.opengl.GL2ES1.GL_COMBINE_RGB;
import static javax.media.opengl.GL2ES1.GL_CONSTANT;
import static javax.media.opengl.GL2ES1.GL_INTERPOLATE;
import static javax.media.opengl.GL2ES1.GL_MODULATE;
import static javax.media.opengl.GL2ES1.GL_OPERAND0_ALPHA;
import static javax.media.opengl.GL2ES1.GL_OPERAND0_RGB;
import static javax.media.opengl.GL2ES1.GL_OPERAND1_ALPHA;
import static javax.media.opengl.GL2ES1.GL_OPERAND1_RGB;
import static javax.media.opengl.GL2ES1.GL_OPERAND2_ALPHA;
import static javax.media.opengl.GL2ES1.GL_OPERAND2_RGB;
import static javax.media.opengl.GL2ES1.GL_PREVIOUS;
import static javax.media.opengl.GL2ES1.GL_PRIMARY_COLOR;
import static javax.media.opengl.GL2ES1.GL_SRC0_ALPHA;
import static javax.media.opengl.GL2ES1.GL_SRC0_RGB;
import static javax.media.opengl.GL2ES1.GL_SRC1_ALPHA;
import static javax.media.opengl.GL2ES1.GL_SRC1_RGB;
import static javax.media.opengl.GL2ES1.GL_SRC2_ALPHA;
import static javax.media.opengl.GL2ES1.GL_SRC2_RGB;
import static javax.media.opengl.GL2ES1.GL_TEXTURE_ENV;
import static javax.media.opengl.GL2ES1.GL_TEXTURE_ENV_COLOR;
import static javax.media.opengl.GL2ES1.GL_TEXTURE_ENV_MODE;

import javax.media.opengl.GL2ES1;

import nl.weeaboo.common.Area2D;
import nl.weeaboo.common.Rect2D;
import nl.weeaboo.gl.GLDraw;
import nl.weeaboo.gl.GLInfo;
import nl.weeaboo.gl.GLManager;
import nl.weeaboo.gl.jogl.JoglGLManager;
import nl.weeaboo.vn.IDrawBuffer;
import nl.weeaboo.vn.IPixelShader;
import nl.weeaboo.vn.ITexture;
import nl.weeaboo.vn.impl.base.BaseRenderer;
import nl.weeaboo.vn.impl.base.BlendQuadHelper;
import nl.weeaboo.vn.impl.base.TriangleGrid;
import nl.weeaboo.vn.impl.base.TriangleGrid.TextureWrap;
import nl.weeaboo.vn.math.Matrix;

public class BlendQuadRenderer extends BlendQuadHelper {

	public static String[] REQUIRED_EXTENSIONS = new String[] {
		"GL_ARB_texture_env_crossbar",
		"GL_ARB_texture_env_combine"
	};
	
	private final Renderer renderer;
	
	public BlendQuadRenderer(Renderer r) {
		super(r);
		
		renderer = r;
	}

	//Functions
	@Override
	protected void renderQuad(ITexture tex, Area2D uv, Matrix transform, int mixColorARGB, Rect2D bounds,
			IPixelShader ps)
	{
		GLManager glm = renderer.getGLManager();
		GLDraw glDraw = glm.getGLDraw();
		glDraw.pushColor();
		glDraw.mixColor(mixColorARGB);
		uv = BaseRenderer.combineUV(uv, (tex != null ? tex.getUV() : IDrawBuffer.DEFAULT_UV));
		renderer.renderQuad(tex, transform, bounds.toArea2D(), uv, ps);
		glDraw.popColor();	
	}

	@Override
	protected void renderMultitextured(ITexture tex0, Rect2D bounds0, ITexture tex1, Rect2D bounds1,
			Area2D uv, Matrix transform, IPixelShader ps, float tex0Factor)
	{
		GLManager glm = renderer.getGLManager();
		GLDraw glDraw = glm.getGLDraw();
		GL2ES1 gl = JoglGLManager.getGL(glm);
		
		float f = tex0Factor;
		int texId0 = getTexId(tex0);
		int texId1 = getTexId(tex1);
		Area2D uv0 = BaseRenderer.combineUV(uv, tex0.getUV());
		Area2D uv1 = BaseRenderer.combineUV(uv, tex1.getUV());
		
		//Set texture 0		
		gl.glActiveTexture(GL_TEXTURE0);
		gl.glEnable(GL_TEXTURE_2D);
		gl.glBindTexture(GL_TEXTURE_2D, texId0);
		
		gl.glTexEnvfv(GL_TEXTURE_ENV, GL_TEXTURE_ENV_COLOR, new float[] {f, f, f, f}, 0);
		gl.glTexEnvi(GL_TEXTURE_ENV, GL_TEXTURE_ENV_MODE, GL_COMBINE);
		
		gl.glTexEnvi(GL_TEXTURE_ENV, GL_COMBINE_RGB, GL_INTERPOLATE);
		gl.glTexEnvi(GL_TEXTURE_ENV, GL_SRC0_RGB, GL_TEXTURE0);
		gl.glTexEnvi(GL_TEXTURE_ENV, GL_SRC1_RGB, GL_TEXTURE1);
		gl.glTexEnvi(GL_TEXTURE_ENV, GL_SRC2_RGB, GL_CONSTANT);
		gl.glTexEnvi(GL_TEXTURE_ENV, GL_OPERAND0_RGB, GL_SRC_COLOR);
		gl.glTexEnvi(GL_TEXTURE_ENV, GL_OPERAND1_RGB, GL_SRC_COLOR);
		gl.glTexEnvi(GL_TEXTURE_ENV, GL_OPERAND2_RGB, GL_SRC_COLOR);

		gl.glTexEnvi(GL_TEXTURE_ENV, GL_COMBINE_ALPHA, GL_INTERPOLATE);
		gl.glTexEnvi(GL_TEXTURE_ENV, GL_SRC0_ALPHA, GL_TEXTURE0);
		gl.glTexEnvi(GL_TEXTURE_ENV, GL_SRC1_ALPHA, GL_TEXTURE1);
		gl.glTexEnvi(GL_TEXTURE_ENV, GL_SRC2_ALPHA, GL_CONSTANT);
		gl.glTexEnvi(GL_TEXTURE_ENV, GL_OPERAND0_ALPHA, GL_SRC_ALPHA);
		gl.glTexEnvi(GL_TEXTURE_ENV, GL_OPERAND1_ALPHA, GL_SRC_ALPHA);
		gl.glTexEnvi(GL_TEXTURE_ENV, GL_OPERAND2_ALPHA, GL_SRC_ALPHA);
		
		// Set texture 1
		gl.glActiveTexture(GL_TEXTURE1);
		gl.glEnable(GL_TEXTURE_2D);
		gl.glBindTexture(GL_TEXTURE_2D, texId1);
		
		gl.glTexEnvfv(GL_TEXTURE_ENV, GL_TEXTURE_ENV_COLOR, new float[] {1, 1, 1, 1}, 0);
		gl.glTexEnvi(GL_TEXTURE_ENV, GL_TEXTURE_ENV_MODE, GL_COMBINE);
		
		gl.glTexEnvi(GL_TEXTURE_ENV, GL_COMBINE_RGB, GL_MODULATE);
		gl.glTexEnvi(GL_TEXTURE_ENV, GL_SRC0_RGB, GL_PREVIOUS);
		gl.glTexEnvi(GL_TEXTURE_ENV, GL_SRC1_RGB, GL_PRIMARY_COLOR);
		gl.glTexEnvi(GL_TEXTURE_ENV, GL_OPERAND0_RGB, GL_SRC_COLOR);
		gl.glTexEnvi(GL_TEXTURE_ENV, GL_OPERAND1_RGB, GL_SRC_COLOR);

		gl.glTexEnvi(GL_TEXTURE_ENV, GL_COMBINE_ALPHA, GL_MODULATE);
		gl.glTexEnvi(GL_TEXTURE_ENV, GL_SRC0_ALPHA, GL_PREVIOUS);
		gl.glTexEnvi(GL_TEXTURE_ENV, GL_SRC1_ALPHA, GL_PRIMARY_COLOR);
		gl.glTexEnvi(GL_TEXTURE_ENV, GL_OPERAND0_ALPHA, GL_SRC_ALPHA);
		gl.glTexEnvi(GL_TEXTURE_ENV, GL_OPERAND1_ALPHA, GL_SRC_ALPHA);
		
		//Render triangle grid
		glDraw.pushMatrix();
		glDraw.multMatrixf(transform.toGLMatrix(), 0);		
		TriangleGrid grid = TriangleGrid.layout2(
				bounds0.toArea2D(), uv0, TextureWrap.CLAMP,
				bounds1.toArea2D(), uv1, TextureWrap.CLAMP);
		renderer.renderTriangleGrid(grid, ps);
	    glDraw.popMatrix();
		
		//Reset texture
		gl.glActiveTexture(GL_TEXTURE1);
		gl.glTexEnvi(GL_TEXTURE_ENV, GL_TEXTURE_ENV_MODE, GL_MODULATE);
		gl.glDisable(GL_TEXTURE_2D);
		gl.glActiveTexture(GL_TEXTURE0);
		gl.glTexEnvi(GL_TEXTURE_ENV, GL_TEXTURE_ENV_MODE, GL_MODULATE);
		glDraw.setTexture(glDraw.getTexture(), true); //Make sure OpenGL and GLManager agree on which texture is current
	}
	
	//Getters
	protected int getTexId(ITexture tex) {
		GLManager glm = renderer.getGLManager();
		
		TextureAdapter ta = (TextureAdapter)tex;
		ta.glTryLoad(glm);
		return ta.glId();
	}
	
	@Override
	protected boolean isFallbackRequired() {
		GLManager glm = renderer.getGLManager();
		GLInfo info = glm.getGLInfo();		
		for (String ext : REQUIRED_EXTENSIONS) {
			if (!info.isExtensionAvailable(ext)) {
				return true;
			}
		}
		return false;
	}
	
	//Setters
	
}
