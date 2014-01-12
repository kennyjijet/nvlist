package nl.weeaboo.vn.impl.nvlist;

import static nl.weeaboo.gl.GLConstants.GL_NEAREST;

import java.nio.ByteBuffer;
import java.nio.IntBuffer;
import java.util.Arrays;

import nl.weeaboo.gl.GLDraw;
import nl.weeaboo.gl.GLManager;
import nl.weeaboo.gl.GLUtil;
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
		super(true, ntf, fadeFilename, duration, range, i, fadeTexTile);
		
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
	protected ByteBuffer initRemapPixels(ByteBuffer current, int requiredBytes) {
		return GLUtil.newDirectByteBuffer(requiredBytes);
	}
	
	@Override
	protected void prepareRemapTexture(int w, int h) {
		remapTex = imgfac.createGLTexture(w, h, GL_NEAREST, GL_NEAREST, 0, 0);
	}

	@Override
	protected void updateRemapTex(ByteBuffer pixels, boolean is16Bit) {
		if (is16Bit) {
			remapTex.setPixels(imgfac.newGray16TextureData(pixels, remapTex.getTexWidth(), remapTex.getTexHeight()));
		} else {
			remapTex.setPixels(imgfac.newGray8TextureData(pixels, remapTex.getTexWidth(), remapTex.getTexHeight()));
		}
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
			GLShader oldShader = glDraw.getShader();			
			shader.glTryLoad(glm);
			glDraw.setShader(shader);
			
			//Initialize shader
			shader.setTextureUniform(glm, "src0",  0, texId(texs[0]));
			shader.setTextureUniform(glm, "src1",  1, texId(texs[1]));
			shader.setTextureUniform(glm, "fade",  2, texId(fadeTex));
			shader.setTextureUniform(glm, "remap", 3, texId(remapTex));
			
			//Render geometry
			glDraw.pushMatrix();
			glDraw.multMatrixf(transform.toGLMatrix(), 0);
			rr.renderTriangleGrid(grid, null);
			glDraw.popMatrix();

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
