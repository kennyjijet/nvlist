package nl.weeaboo.vn.impl.nvlist;

import static javax.media.opengl.GL.GL_FLOAT;
import static javax.media.opengl.GL.GL_TEXTURE0;
import static javax.media.opengl.GL.GL_TRIANGLE_STRIP;
import static javax.media.opengl.fixedfunc.GLPointerFunc.GL_TEXTURE_COORD_ARRAY;
import static javax.media.opengl.fixedfunc.GLPointerFunc.GL_VERTEX_ARRAY;

import java.nio.ByteBuffer;
import java.nio.FloatBuffer;

import javax.media.opengl.GL;
import javax.media.opengl.GL2;
import javax.media.opengl.GL2ES1;

import nl.weeaboo.common.Area2D;
import nl.weeaboo.common.Rect;
import nl.weeaboo.common.Rect2D;
import nl.weeaboo.gl.GLBlendMode;
import nl.weeaboo.gl.GLDraw;
import nl.weeaboo.gl.GLManager;
import nl.weeaboo.gl.GLUtil;
import nl.weeaboo.gl.SpriteBatch;
import nl.weeaboo.gl.jogl.GLScreenshot;
import nl.weeaboo.gl.jogl.JoglGLManager;
import nl.weeaboo.gl.tex.GLTexRect;
import nl.weeaboo.gl.tex.GLTexture;
import nl.weeaboo.gl.tex.GLWritableTexture;
import nl.weeaboo.gl.text.ParagraphRenderer;
import nl.weeaboo.io.BufferUtil;
import nl.weeaboo.textlayout.TextLayout;
import nl.weeaboo.vn.BlendMode;
import nl.weeaboo.vn.IDistortGrid;
import nl.weeaboo.vn.IPixelShader;
import nl.weeaboo.vn.IRenderer;
import nl.weeaboo.vn.IScreenshot;
import nl.weeaboo.vn.ITexture;
import nl.weeaboo.vn.RenderCommand;
import nl.weeaboo.vn.RenderEnv;
import nl.weeaboo.vn.impl.base.BaseRenderer;
import nl.weeaboo.vn.impl.base.RenderStats;
import nl.weeaboo.vn.impl.base.TriangleGrid;
import nl.weeaboo.vn.math.Matrix;

public class Renderer extends BaseRenderer {

	private final GLManager glm;
	private final ParagraphRenderer pr;
	private final ImageFactory imgfac;
	private final FadeQuadRenderer fadeQuadRenderer;
	private final BlendQuadRenderer blendQuadRenderer;
	private final DistortQuadRenderer distortQuadRenderer;
	
	private final DrawBuffer drawBuffer;
	
	//--- Properties only valid between renderBegin() and renderEnd() beneath this line ---
	private GL2ES1 gl;
	private GLDraw glDraw;
	private int buffered;
	private TextureAdapter quadTexture;
	private SpriteBatch quadBatch;
	private float[] tempFloat = new float[8]; //Temporary var
	private volatile ByteBuffer triangleGridTemp;
	//-------------------------------------------------------------------------------------
	
	public Renderer(GLManager glm, ParagraphRenderer pr, ImageFactory imgfac, RenderEnv env, RenderStats stats) {
		super(env, stats);
		
		this.glm = glm;
		this.pr = pr;
		this.imgfac = imgfac;
		this.fadeQuadRenderer = new FadeQuadRenderer(this);
		this.blendQuadRenderer = new BlendQuadRenderer(this);
		this.distortQuadRenderer = new DistortQuadRenderer(this);
		
		this.drawBuffer = new DrawBuffer(env);
		
		quadBatch = new SpriteBatch(1024);
	}
	
	//Functions
	public static Renderer cast(IRenderer r) {
		if (r == null) return null;
		if (r instanceof Renderer) return (Renderer)r;
		throw new ClassCastException("Supplied renderer is of an invalid class: " + r.getClass() + ", expected: " + Renderer.class);
	}
	
	@Override
	protected void renderBegin() {
		gl = JoglGLManager.getGL(glm);
		glDraw = glm.getGLDraw();
		
		gl.glPushMatrix();
		glDraw.pushBlendMode();
		glDraw.pushColor();
		
		gl.glShadeModel(GL2.GL_SMOOTH); //Required for TriangleGrid		
		gl.glEnable(GL2ES1.GL_SCISSOR_TEST);
		glDraw.setTexture(null);
		glDraw.setBlendMode(GLBlendMode.DEFAULT);
		glDraw.setColor(0xFFFFFFFF);
		
		quadBatch.init(glm);
		
		buffered = 0;
		quadTexture = null;		
	}
	
	@Override
	protected void renderEnd() {
		quadTexture = null;
		
		gl.glDisable(GL2ES1.GL_SCISSOR_TEST);
		glDraw.setTexture(null, true);
		glDraw.popBlendMode();
		glDraw.popColor();
		gl.glPopMatrix();		
	}
		
	@Override
	protected void setClip(boolean c) {
		if (c) {
			gl.glEnable(GL2ES1.GL_SCISSOR_TEST);
		} else {
			gl.glDisable(GL2ES1.GL_SCISSOR_TEST);
		}
	}
	
	@Override
	protected void setColor(int argb) {
		glDraw.setColor(argb);
	}

	@Override
	protected void setBlendMode(BlendMode bm) {
		switch (bm) {
		case DEFAULT: glDraw.setBlendMode(GLBlendMode.DEFAULT); break;
		case ADD:     glDraw.setBlendMode(GLBlendMode.ADD); break;
		case OPAQUE:  glDraw.setBlendMode(null); break;
		}
	}	
	
	protected void renderSetTexture(ITexture tex) {
		TextureAdapter ta = (TextureAdapter)tex;		
		if (quadTexture != tex && (quadTexture == null || ta == null || quadTexture.glId() != ta.glId())) {
			flushQuadBatch();
		}
		
		quadTexture = ta;		
		if (ta != null) {
			ta.glTryLoad(glm);
			glDraw.setTexture(ta.getTexture());
		} else {
			glDraw.setTexture(null);
		}		
	}
	
	@Override
	public void renderQuad(ITexture itex, Matrix t, Area2D bounds, Area2D uv, IPixelShader ps) {
		renderSetTexture(itex);
		
		double u = uv.x;
		double v = uv.y;
		double uw = uv.w;
		double vh = uv.h;
		if (itex != null) {
			TextureAdapter ta = (TextureAdapter)itex;
			if (ta.glId() != 0) {
				Area2D texUV = ta.getUV();
				u  = texUV.x + u * texUV.w;
				v  = texUV.y + v * texUV.h;
				uw = texUV.w * uw;
				vh = texUV.h * vh;
			}
		}

		boolean allowBuffer = (ps == null);
		if (!allowBuffer) {
			flushQuadBatch();
		}
		
		if (ps != null) ps.preDraw(this);
		
		renderQuad(allowBuffer, t, bounds.x, bounds.y, bounds.w, bounds.h, u, v, uw, vh);
			
		if (ps != null) ps.postDraw(this);
	}
	
	private void renderQuad(boolean allowBuffer, Matrix t, double x, double y, double w, double h,
			double u, double v, double uw, double vh)
	{
		if (t.hasShear()) {
			if (allowBuffer) {
				quadBatch.setColor(glDraw.getColor());

				tempFloat[0] = tempFloat[6] = (float)(x  );
				tempFloat[2] = tempFloat[4] = (float)(x+w);
				tempFloat[1] = tempFloat[3] = (float)(y  );
				tempFloat[5] = tempFloat[7] = (float)(y+h);
				t.transform(tempFloat, 0, 8);
				quadBatch.draw(tempFloat, (float)u, (float)v, (float)uw, (float)vh);

				buffered++;				
				if (quadBatch.getRemaining() <= 0) {
					flushQuadBatch();
				}
			} else {
				gl.glPushMatrix();		
				gl.glMultMatrixf(t.toGLMatrix(), 0);
				glDraw.fillRect(x, y, w, h, u, v, uw, vh);
				gl.glPopMatrix();
			}
		} else {
			double sx = t.getScaleX();
			double sy = t.getScaleY();
			x = x * sx + t.getTranslationX();
			y = y * sy + t.getTranslationY();
			w = w * sx;
			h = h * sy;
			
			if (allowBuffer) {
				quadBatch.setColor(glDraw.getColor());
				quadBatch.draw((float)x, (float)y, (float)w, (float)h, (float)u, (float)v, (float)uw, (float)vh);

				buffered++;				
				if (quadBatch.getRemaining() <= 0) {
					flushQuadBatch();
				}
			} else {			
				glDraw.fillRect(x, y, w, h, u, v, uw, vh);
			}
			
			//System.out.printf("%.2f, %.2f, %.2f, %.2f\n", x, y, w, h);
		}		
	}
	
	void renderText(GLManager glm, TextLayout layout, double x, double y,
			int startLine, int endLine, double visibleChars, IPixelShader ps)
	{
		if (ps != null) ps.preDraw(this);
		
		//GL2ES1 gl = glm.getGL();		
		//gl.glPushMatrix();

		glDraw.pushBlendMode();
		glDraw.setBlendMode(GLBlendMode.DEFAULT);
		pr.setLineOffset(startLine);
		pr.setVisibleLines(endLine - startLine);
		pr.setVisibleChars((float)visibleChars);
		pr.drawLayout(glm, layout, Math.round((float)x), Math.round((float)y));
		glDraw.popBlendMode();
		
		//gl.glPopMatrix();
		
		if (ps != null) ps.postDraw(this);		
	}
	
	@Override
	public void renderScreenshot(IScreenshot out, Rect glScreenRect) {
		Screenshot ss = (Screenshot)out;
		
		if (ss.isVolatile()) {
			GLWritableTexture glTex = imgfac.createGLTexture(glScreenRect.w, glScreenRect.h, 0, 0, 0, 0);
			glTex = glTex.glTryLoad(glm);
			GLTexRect glTexRect = glTex.getSubRect(GLUtil.TEXRECT_FLIPPED);

			glDraw.setTexture(glTex);
			//The teximage may not have been created even if the texture id exists
			gl.glTexImage2D(GL.GL_TEXTURE_2D, 0, GL.GL_RGBA, glTex.getTexWidth(), glTex.getTexHeight(),
					0, GL.GL_RGBA, GL.GL_BYTE, null);
			gl.glCopyTexSubImage2D(GL.GL_TEXTURE_2D, 0, 0, 0, glScreenRect.x, glScreenRect.y,
					glScreenRect.w, glScreenRect.h);
			
			ITexture tex = imgfac.createTexture(glTexRect, env.vw / (double)env.rw, env.vh / (double)env.rh);
			
			ss.setVolatilePixels(tex, env.rw, env.rh);
		} else {
			GLScreenshot gss = new GLScreenshot();
			gss.set(glm, glScreenRect);		
			int[] argb = BufferUtil.toArray(gss.getARGB());
			ss.setPixels(argb, gss.getWidth(), gss.getHeight(), env.rw, env.rh);
		}
	}
	
	@Override
	public void renderBlendQuad(ITexture tex0, double alignX0, double alignY0, ITexture tex1, double alignX1,
			double alignY1, double frac, Area2D uv, Matrix transform, IPixelShader ps)
	{
		blendQuadRenderer.renderBlendQuad(tex0, alignX0, alignY0, tex1, alignX1, alignY1, frac, uv, transform, ps);
	}

	@Override
	public void renderFadeQuad(ITexture tex, Matrix transform, int color0, int color1,
			Area2D bounds, Area2D uv, IPixelShader ps,
			int dir, boolean fadeIn, double span, double frac)
	{
		fadeQuadRenderer.renderFadeQuad(tex, transform, color0, color1, bounds, uv,
				ps, dir, fadeIn, span, frac);
	}
	
	@Override
	public void renderDistortQuad(ITexture tex, Matrix transform, int argb,
			Area2D bounds, Area2D uv, IPixelShader ps,
			IDistortGrid grid, Rect2D clampBounds)
	{
		distortQuadRenderer.renderDistortQuad(tex, transform, argb, bounds, uv,
				ps, grid, clampBounds);
	}
	
	@Override
	public void renderTriangleGrid(TriangleGrid grid) {
		final int rows = grid.getRows();
		final int cols = grid.getCols();
		final int texCount = grid.getTextures();
		final int verticesPerRow = cols * 2;
		
		//Reuse a single buffer, garbage collecting them is very, very slow.
		int vertBytes = verticesPerRow * 2 * 4;
		int texcoordBytes = verticesPerRow * 2 * 4;
		int requiredBytes = vertBytes + texCount * texcoordBytes;
		if (triangleGridTemp == null || triangleGridTemp.limit() < requiredBytes) {
			triangleGridTemp = GLUtil.newDirectByteBuffer(requiredBytes);
		}
				
		//Make sure the buffers can't be garbage collected while OpenGL is using it.
		final ByteBuffer raw = triangleGridTemp;
		FloatBuffer posBuffer = GLUtil.sliceBuffer(raw, 0, vertBytes).asFloatBuffer();
		FloatBuffer[] texBuffers = new FloatBuffer[texCount];
		for (int n = 0; n < texBuffers.length; n++) {
			texBuffers[n] = GLUtil.sliceBuffer(raw, vertBytes + n * texcoordBytes, texcoordBytes).asFloatBuffer();
		}
		
		gl.glEnableClientState(GL_VERTEX_ARRAY);
		for (int t = 0; t < texCount; t++) {
			gl.glClientActiveTexture(GL_TEXTURE0 + t);
			gl.glEnableClientState(GL_TEXTURE_COORD_ARRAY);
		}		
		for (int row = 0; row < rows; row++) {
			grid.getVertices(posBuffer, row);
			posBuffer.rewind();
			gl.glVertexPointer(2, GL_FLOAT, 0, posBuffer);
			for (int t = 0; t < texCount; t++) {
				grid.getTexCoords(texBuffers[t], t, row);
				texBuffers[t].rewind();
				gl.glClientActiveTexture(GL_TEXTURE0 + t);
			    gl.glTexCoordPointer(2, GL_FLOAT, 0, texBuffers[t]);
			}
		    gl.glDrawArrays(GL_TRIANGLE_STRIP, 0, verticesPerRow);
		}
		for (int n = texCount-1; n >= 0; n--) {
			gl.glClientActiveTexture(GL_TEXTURE0 + n);
			gl.glDisableClientState(GL_TEXTURE_COORD_ARRAY);
		}
	    gl.glDisableClientState(GL_VERTEX_ARRAY);		
	}
	
	@Override
	protected boolean renderUnknownCommand(RenderCommand cmd) {
		if (cmd.id == RenderTextCommand.id) {
			RenderTextCommand rtc = (RenderTextCommand)cmd;
			renderText(glm, rtc.textLayout, rtc.x, rtc.y,
					rtc.lineStart, rtc.lineEnd, rtc.visibleChars, rtc.ps);
			return true;
		}
		return false;
	}
	
	@Override
	protected void flushQuadBatch() {
		if (buffered > 0) {
			GLTexture qtex = (quadTexture != null ? ((TextureAdapter)quadTexture).getTexture() : null);
			GLTexture cur = glDraw.getTexture();
			if (qtex != cur) {
				qtex = qtex.glTryLoad(glm);
				glDraw.setTexture(qtex);
				quadBatch.flush(glm);
				glDraw.setTexture(cur);			
			} else {
				quadBatch.flush(glm);
			}
			
			if (renderStats != null) {
				renderStats.onRenderQuadBatch(buffered);
			}
			buffered = 0;
		}		
		quadTexture = null;
	}
		
	@Override
	protected void setClipRect(Rect glRect) {
		gl.glScissor(glRect.x, glRect.y, glRect.w, glRect.h);
	}
	
	@Override
	protected void translate(double dx, double dy) {
		glDraw.translate(dx, dy);
	}
	
	//Getters	
	public GLManager getGLManager() {
		if (!rendering) return null;
		return glm;
	}
	
	@Override
	public DrawBuffer getDrawBuffer() {
		return drawBuffer;
	}

	//Setters
	
}
