package nl.weeaboo.vn.impl.nvlist;

import static nl.weeaboo.gl.GLConstants.GL_LUMINANCE;
import static nl.weeaboo.gl.GLConstants.GL_LUMINANCE16;
import static nl.weeaboo.gl.GLConstants.GL_UNSIGNED_BYTE;
import static nl.weeaboo.gl.GLConstants.GL_UNSIGNED_SHORT;

import java.awt.image.BufferedImage;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.ObjectStreamException;
import java.io.Serializable;
import java.nio.Buffer;
import java.nio.IntBuffer;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import nl.weeaboo.gl.GLInfo;
import nl.weeaboo.gl.jogl.JoglTextureData;
import nl.weeaboo.gl.jogl.JoglTextureStore;
import nl.weeaboo.gl.tex.GLTexRect;
import nl.weeaboo.gl.tex.GLTexture;
import nl.weeaboo.gl.tex.GLWritableTexture;
import nl.weeaboo.gl.tex.ITextureData;
import nl.weeaboo.gl.tex.MipmapData;
import nl.weeaboo.gl.text.GlyphManager;
import nl.weeaboo.io.EnvironmentSerializable;
import nl.weeaboo.lua2.io.LuaSerializable;
import nl.weeaboo.vn.IAnalytics;
import nl.weeaboo.vn.IButtonDrawable;
import nl.weeaboo.vn.INotifier;
import nl.weeaboo.vn.IScreenshot;
import nl.weeaboo.vn.ISeenLog;
import nl.weeaboo.vn.ITextRenderer;
import nl.weeaboo.vn.ITexture;
import nl.weeaboo.vn.impl.base.BaseImageFactory;
import nl.weeaboo.vn.impl.lua.LuaEventHandler;
import nl.weeaboo.vn.impl.lua.LuaNovelUtil;

@LuaSerializable
public class ImageFactory extends BaseImageFactory implements Serializable {

	private final EnvironmentSerializable es;
	private final IAnalytics analytics;
	private final JoglTextureStore texStore;
	private final GlyphManager glyphManager;
	private final boolean renderTextToTexture;
	
	private int imgWidth, imgHeight;
	private int subTexLim; //Max size to try and put in a GLPackedTexture instead of generating a whole new texture.
	private boolean isTextRightToLeft;
	
	public ImageFactory(JoglTextureStore ts, GlyphManager gman, IAnalytics an,
			LuaEventHandler eh, ISeenLog sl, INotifier ntf, int w, int h, boolean renderTextToTexture)
	{
		super(eh, sl, ntf, w, h);
		
		this.analytics = an;
		this.texStore = ts;
		this.glyphManager = gman;		
		this.imgWidth = w;
		this.imgHeight = h;
		this.subTexLim = 128;
		this.renderTextToTexture = renderTextToTexture;		
		this.es = new EnvironmentSerializable(this);

		setDefaultExts("ktx", "png", "jpg", "jng");
	}
	
	//Functions
	private Object writeReplace() throws ObjectStreamException {	
		return es.writeReplace();
	}
	
	@Override
	protected void preloadNormalized(String filename) {
		texStore.preload(filename, false);
	}
		
	@Override
	public ImageDrawable createImageDrawable() {
		return new ImageDrawable();
	}

	@Override
	public TextDrawable createTextDrawable() {
		return new TextDrawable(createTextRenderer());
	}

	@Override
	public IButtonDrawable createButtonDrawable() {
		return new ButtonDrawable(createTextRenderer(), eventHandler);
	}
	
	protected ITextRenderer createTextRenderer() {
		ITextRenderer tr;
		if (renderTextToTexture) {
			tr = new TextureTR(this, glyphManager);
		} else {
			tr = new GlyphTR(this, glyphManager);
		}
		tr.setRightToLeft(isTextRightToLeft);
		return tr;
	}
	
	@Override
	public IScreenshot screenshot(short z, boolean isVolatile) {
		return new Screenshot(z, isVolatile);
	}
	
	public ITexture createTexture(GLTexture tex, double sx, double sy) {
		return createTexture(tex != null ? tex.getSubRect(null) : null, sx, sy);
	}

	public ITexture createTexture(GLTexRect tr, double sx, double sy) {
		if (tr == null) {
			return null;
		}

		TextureAdapter ta = new TextureAdapter(this);
		ta.setTexRect(tr, sx, sy);
		//System.out.println(ta.getWidth()+"x"+ta.getHeight() + " " + tr.getRect() + " " + tr.getUV() + " " + tr.getTexture().getTexWidth()+"x"+tr.getTexture().getTexHeight());
		return ta;
	}
	
	@Override
	public ITexture createTexture(int[] argb, int w, int h, double sx, double sy) {
		if (w <= subTexLim && h <= subTexLim) {
			return createTexture(createGLTexRect(argb, w, h), sx, sy);
		} else {
			GLWritableTexture tex = createGLTexture(w, h, 0, 0, 0, 0);
			if (argb != null) {
				tex.setPixels(texStore.newARGB8TextureData(argb, w, h));
			}
			return createTexture(tex, sx, sy);
		}
	}
	
	public GLWritableTexture createGLTexture(int w, int h, int minF, int magF, int wrapS, int wrapT) {
		return texStore.newWritableTexture(w, h, 0, 0, 0, 0);
	}
	public GLTexRect createGLTexRect(int[] argb, int w, int h) {
		return texStore.newTexRect(argb, w, h);
	}	
	
	public ITextureData newARGB8TextureData(IntBuffer argb, int w, int h) {
		return texStore.newARGB8TextureData(argb, true, w, h);
	}
	public ITextureData newGray8TextureData(Buffer buf, int w, int h) {
		MipmapData mdata = new MipmapData(texStore, buf, w);
		int ifmt = GL_LUMINANCE;
		int fmt = GL_LUMINANCE;
		int glType = GL_UNSIGNED_BYTE;
		return new JoglTextureData(w, h, ifmt, fmt, glType, texStore, Arrays.asList(mdata));		
	}
	public ITextureData newGray16TextureData(Buffer buf, int w, int h) {
		MipmapData mdata = new MipmapData(texStore, buf, w * 2);
		int ifmt = GL_LUMINANCE16;
		int fmt = GL_LUMINANCE;
		int glType = GL_UNSIGNED_SHORT;
		return new JoglTextureData(w, h, ifmt, fmt, glType, texStore, Arrays.asList(mdata));		
	}
	
	//Getters
	@Override
	protected boolean isValidFilename(String id) {
		if (id == null) return false;
		
		return texStore.isValidTexRect(id);
	}

	public GLTexRect getTexRect(String filename, String[] luaStack) {
		return getTexRectNormalized(filename, normalizeFilename(filename), luaStack);
	}
	
	protected GLTexRect getTexRectNormalized(String filename, String normalized, String[] luaStack) {
		if (normalized == null) {
			return null;
		}
		
		long loadNanos = 0L;
		
		GLTexRect tr;
		if (!texStore.isLoaded(normalized)) {
			long t0 = System.nanoTime();			
			tr = texStore.getTexRect(normalized);
			loadNanos = System.nanoTime() - t0;			
		} else {
			tr = texStore.getTexRect(normalized);
		}
		
		if (tr != null) {
			String callSite = LuaNovelUtil.getNearestLVNSrcloc(luaStack);
			if (callSite != null) {
				analytics.logImageLoad(callSite, filename, loadNanos);
				//System.out.println("Image Load: " + filename);
			}
		}
		
		return tr;
	}
	
	@Override
	protected ITexture getTextureNormalized(String filename, String normalized, String[] luaStack) {
		GLTexRect tr = getTexRectNormalized(filename, normalized, luaStack);

		//Returning null prevents reloading the image if it's available in a different resolution only
		//if (tr == null) {
		//	return null;
		//}
		
		TextureAdapter ta = new ImageTextureAdapter(this, normalized);
		double scale = getImageScale();
		ta.setTexRect(tr, scale, scale);
		return ta;
	}
		
	public BufferedImage readBufferedImage(String filename) throws IOException {
		String normalized = normalizeFilename(filename);
		if (normalized == null) {
			throw new FileNotFoundException(filename);
		}
		
		return texStore.readBufferedImage(normalized);
	}
		
	public boolean isGLExtensionAvailable(String ext) {
		GLInfo info = texStore.getGLInfo();
		return info != null && info.isExtensionAvailable(ext);
	}

	@Override
	protected List<String> getFiles(String folder) {
		List<String> out = new ArrayList<String>();
		try {
			texStore.getFiles(out, folder, true);
		} catch (IOException e) {
			notifier.d("Folder doesn't exist or can't be read: " + folder, e);
		}
		return out;
	}
	
	public double getImageScale() {
		return Math.min(width / (double)imgWidth, height / (double)imgHeight);
	}
	
	public boolean isTextRightToLeft() {
		return isTextRightToLeft;
	}
	
	//Setters
	public void setImageSize(int iw, int ih) {
		imgWidth = iw;
		imgHeight = ih;
	}
	
	public void setTextRightToLeft(boolean rtl) {
		isTextRightToLeft = rtl;
	}
	
}
