package nl.weeaboo.vn.impl.nvlist;

import java.nio.FloatBuffer;
import java.nio.IntBuffer;

import javax.media.opengl.GL;
import javax.media.opengl.GL2ES1;

import nl.weeaboo.gl.GLManager;
import nl.weeaboo.gl.jogl.JoglGLDraw;
import nl.weeaboo.gl.jogl.JoglGLManager;
import nl.weeaboo.vn.IPixelShader;
import nl.weeaboo.vn.ITexture;
import nl.weeaboo.vn.impl.base.FadeQuadHelper;
import nl.weeaboo.vn.math.Matrix;

public class FadeQuadRenderer extends FadeQuadHelper {

	private final Renderer renderer;
	
	public FadeQuadRenderer(Renderer r) {
		super(r);
		
		renderer = r;
	}

	//Functions
	@Override
	protected void renderTriangleStrip(ITexture tex, Matrix transform, IPixelShader ps,
			FloatBuffer vertices, FloatBuffer texcoords, IntBuffer colors, int count)
	{
		GLManager glm = renderer.getGLManager();
		JoglGLDraw glDraw = JoglGLManager.getGLDraw(glm);
		GL2ES1 gl = JoglGLManager.getGL(glm);

		if (tex != null) {
			TextureAdapter ta = (TextureAdapter)tex;
			ta.glTryLoad(glm);
			glDraw.setTexture(ta.getTexture());
		} else {
			glDraw.setTexture(null);
		}
				
		glDraw.pushMatrix();		
		glDraw.multMatrixf(transform.toGLMatrix(), 0);

		glDraw.useDefaultShader();
		if (ps != null) {
        	ps.preDraw(renderer);
        }
		
        gl.glEnableClientState(GL2ES1.GL_VERTEX_ARRAY);
        gl.glEnableClientState(GL2ES1.GL_TEXTURE_COORD_ARRAY);		
        gl.glEnableClientState(GL2ES1.GL_COLOR_ARRAY);
        
        gl.glVertexPointer(2, GL.GL_FLOAT, 0, vertices);
        gl.glTexCoordPointer(2, GL.GL_FLOAT, 0, texcoords);
        gl.glColorPointer(4, GL.GL_UNSIGNED_BYTE, 0, colors);
        gl.glDrawArrays(GL.GL_TRIANGLE_STRIP, 0, count);

        gl.glDisableClientState(GL2ES1.GL_VERTEX_ARRAY);	        
        gl.glDisableClientState(GL2ES1.GL_TEXTURE_COORD_ARRAY);	        
        gl.glDisableClientState(GL2ES1.GL_COLOR_ARRAY);
	
        if (ps != null) {
        	ps.postDraw(renderer);
        }
        
		glDraw.popMatrix();
	}
	
	//Getters
	
	//Setters
	
}
