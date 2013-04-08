package nl.weeaboo.vn.impl.nvlist;

import static nl.weeaboo.gl.GLConstants.GL_NEAREST;

import java.awt.image.BufferedImage;
import java.nio.Buffer;
import java.nio.IntBuffer;
import java.nio.ShortBuffer;
import java.util.Arrays;

import javax.media.opengl.GL2;

import nl.weeaboo.common.Dim;
import nl.weeaboo.common.ScaleUtil;
import nl.weeaboo.gl.GLDraw;
import nl.weeaboo.gl.GLManager;
import nl.weeaboo.gl.jogl.JoglGLManager;
import nl.weeaboo.gl.shader.GLShader;
import nl.weeaboo.gl.tex.GLTexRect;
import nl.weeaboo.gl.tex.GLTexture;
import nl.weeaboo.gl.tex.GLWritableTexture;
import nl.weeaboo.gl.tex.ITextureData;
import nl.weeaboo.lua2.io.LuaSerializable;
import nl.weeaboo.vn.IImageDrawable;
import nl.weeaboo.vn.IInterpolator;
import nl.weeaboo.vn.INotifier;
import nl.weeaboo.vn.IRenderer;
import nl.weeaboo.vn.ITexture;
import nl.weeaboo.vn.impl.base.BaseBitmapTween;
import nl.weeaboo.vn.impl.base.BaseDrawBuffer;
import nl.weeaboo.vn.impl.base.CustomRenderCommand;
import nl.weeaboo.vn.impl.base.TriangleGrid;
import nl.weeaboo.vn.math.Matrix;

import com.jogamp.opengl.util.GLBuffers;

@LuaSerializable
public class BitmapTween extends BaseBitmapTween {

	private static final long serialVersionUID = NVListImpl.serialVersionUID;
	
	private static final String requiredGLSLVersion = "1.1";
	
	private final ImageFactory imgfac;
	private final ShaderFactory shfac;
	
	//--- Initialized in prepare() ---
	private GLShader shader;
	private GLTexRect[] texs;
	private GLTexRect fadeTex;
	private GLWritableTexture remapTex;
	
	public BitmapTween(INotifier ntf, ImageFactory imgfac, ShaderFactory shfac,
			String fadeFilename, double duration,
			double range, IInterpolator i, boolean fadeTexTile)
	{	
		super(ntf, fadeFilename, duration, range, i, fadeTexTile);
		
		this.imgfac = imgfac;
		this.shfac = shfac;
	}
	
	//Functions
	public static boolean isAvailable(ShaderFactory shfac) {
		return shfac.isGLSLVersionSupported(requiredGLSLVersion);
	}
	
	private void resetPrepared() {
		shader = null;
		texs = null;
		fadeTex = null;
		if (remapTex != null) {
			remapTex.glUnload();
			remapTex = null;
		}
	}
	
	@Override
	protected void doPrepare() {
		resetPrepared();
		
		super.doPrepare();
	}
	
	@Override
	protected void doFinish() {
		resetPrepared();
		
		super.doFinish();
	}	
	
	@Override
	protected void prepareShader() {
		shader = shfac.getGLShader("bitmap-tween");
	}

	@Override
	protected void prepareTextures(ITexture[] itexs) {
		texs = new GLTexRect[itexs.length];
		for (int n = 0; n < itexs.length; n++) {
			if (itexs[n] instanceof TextureAdapter) {
				TextureAdapter ta = (TextureAdapter)itexs[n];
				texs[n] = ta.getTexRect();
			}
		}		
	}

	@Override
	protected ITexture prepareFadeTexture(String filename) {
		TextureAdapter ta = (TextureAdapter)imgfac.getTexture(filename, null, false);
		fadeTex = ta.getTexRect();
		return ta;
	}

	protected BufferedImage fadeImageSubRect(BufferedImage img, int targetW, int targetH) {
		final int iw = img.getWidth();
		final int ih = img.getHeight();
		Dim d = ScaleUtil.scaleProp(targetW, targetH, iw, ih);
		return img.getSubimage((iw-d.w)/2, (ih-d.h)/2, d.w, d.h);
	}
	
	@Override
	protected ITexture prepareDefaultFadeTexture(int colorARGB) {
		int w = 16, h = 16;
		int[] argb = new int[w * h];
		Arrays.fill(argb, colorARGB);
		
		GLWritableTexture tex = imgfac.createGLTexture(w, h, GL_NEAREST, GL_NEAREST, 0, 0);
		ITextureData tdata = imgfac.newARGB8TextureData(IntBuffer.wrap(argb), tex.getTexWidth(), tex.getTexHeight());
		tex.setPixels(tdata);
		fadeTex = tex.getSubRect(null);
		return imgfac.createTexture(fadeTex, 1, 1);
	}

	@Override
	protected ShortBuffer initRemapPixels(ShortBuffer current, int requiredLen) {
		return GLBuffers.newDirectShortBuffer(requiredLen);
	}
	
	@Override
	protected void prepareRemapTexture(int w, int h) {
		remapTex = imgfac.createGLTexture(w, h, GL_NEAREST, GL_NEAREST, 0, 0);
	}

	@Override
	protected void updateRemapTex(Buffer pixels) {
		remapTex.setPixels(imgfac.newGray16TextureData(pixels, remapTex.getTexWidth(), remapTex.getTexHeight()));
	}

	@Override
	protected void draw(BaseDrawBuffer d, IImageDrawable img, TriangleGrid grid) {
		d.draw(new RenderCommand(img, texs, fadeTex, remapTex, shader, grid));
	}
	
	//Getters
	
	//Setters
	
	//Inner Classes
	protected static final class RenderCommand extends CustomRenderCommand {

		private final Matrix transform;
		private GLTexRect[] texs;
		private GLTexRect fadeTex;
		private GLTexture remapTex;
		private final GLShader shader;
		private final TriangleGrid grid;
		
		public RenderCommand(IImageDrawable id, GLTexRect[] texs, GLTexRect fadeTex,
				GLTexture remapTex, GLShader shader, TriangleGrid grid)
		{
			super(id.getZ(), id.isClipEnabled(), id.getBlendMode(), id.getColorARGB(),
					id.getPixelShader(), (byte)0);
			
			this.transform = id.getTransform();
			this.texs = texs.clone();
			this.fadeTex = fadeTex;
			this.remapTex = remapTex;
			this.shader = shader;
			this.grid = grid;
			
			if (shader == null || grid == null) {
				throw new NullPointerException();
			}
		}

		@Override
		protected void renderGeometry(IRenderer r) {
			Renderer rr = Renderer.cast(r);
			GLManager glm = rr.getGLManager();
			GLDraw glDraw = glm.getGLDraw();
			GL2 gl = JoglGLManager.getGL(glm).getGL2();
						
			GLTexture oldTexture = glDraw.getTexture();
			glDraw.setTexture(null);
			
			//Force load textures
			for (int n = 0; n < texs.length; n++) {
				GLTexRect tr = texs[n];
				if (tr != null) {
					tr = tr.glTryLoad(glm);
				}
			}
			fadeTex = fadeTex.glTryLoad(glm);
			remapTex = remapTex.glTryLoad(glm);
			
			//Force load shader
			shader.glTryLoad(glm);
			GLShader oldShader = glDraw.getShader();
			glDraw.setShader(shader);
			
			//Initialize shader
			shader.setTextureParam(glm, "src0",  0, texId(texs[0]));
			shader.setTextureParam(glm, "src1",  1, texId(texs[1]));
			shader.setTextureParam(glm, "fade",  2, texId(fadeTex));
			shader.setTextureParam(glm, "remap", 3, texId(remapTex));
			
			//Render geometry
			gl.glPushMatrix();
			gl.glMultMatrixf(transform.toGLMatrix(), 0);
			rr.renderTriangleGrid(grid);
			gl.glPopMatrix();

			//Disable shader
			glDraw.setShader(oldShader);
						
			//Restore previous texture
			glDraw.setTexture(oldTexture);
		}
		
		private static final int texId(GLTexRect tr) {
			return texId(tr != null ? tr.getTexture() : null);
		}
		private static final int texId(GLTexture tex) {
			return (tex != null ? tex.glId() : 0);
		}
		
	}

}
