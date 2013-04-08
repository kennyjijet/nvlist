package nl.weeaboo.vn.impl.nvlist;

import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.image.BufferedImage;
import java.nio.IntBuffer;

import nl.weeaboo.awt.ImageUtil;
import nl.weeaboo.game.desktop.AWTParagraphRenderer;
import nl.weeaboo.gl.tex.GLWritableTexture;
import nl.weeaboo.gl.text.GlyphManager;
import nl.weeaboo.lua2.io.LuaSerializable;
import nl.weeaboo.textlayout.LineElement;
import nl.weeaboo.textlayout.ParagraphLayouter;
import nl.weeaboo.textlayout.TextLayout;
import nl.weeaboo.vn.ITexture;
import nl.weeaboo.vn.impl.base.TextureTextRenderer;

import com.jogamp.common.nio.Buffers;

@LuaSerializable
public class TextureTR extends TextureTextRenderer<TextLayout> {

	private static final long serialVersionUID = NVListImpl.serialVersionUID;
	
	private final ImageFactory imgfac;
	private final GlyphManager glyphManager;
	
	private transient BufferedImage tempImage;
	private transient IntBuffer tempPixels;
	
	public TextureTR(ImageFactory imgfac, GlyphManager gman) {
		super(true);
		
		this.imgfac = imgfac;
		this.glyphManager = gman;
	}
	
	@Override
	protected void destroyTexture(ITexture texture) {
		//We don't know for sure if we can dispose the texture itself, let the GC figure it out
	}

	@Override
	protected ITexture createTexture(int w, int h, float sx, float sy) {
		GLWritableTexture inner = imgfac.createGLTexture(w, h, 0, 0, 0, 0);
		return imgfac.createTexture(inner, sx, sy);
	}
	
	private BufferedImage getTempImage(int w, int h) {
		if (tempImage == null || tempImage.getWidth() != w || tempImage.getHeight() != h) {
			if (tempImage != null) {
				tempImage.flush();
			}
			tempImage = new BufferedImage(w, h, BufferedImage.TYPE_INT_ARGB_PRE);
		} else {
			Graphics2D g = tempImage.createGraphics();
			g.setBackground(new Color(0, 0, 0, 0));
			g.clearRect(0, 0, tempImage.getWidth(), tempImage.getHeight());
			g.dispose();
		}
		return tempImage;
	}
	
	private IntBuffer getTempPixels(int count) {
		if (tempPixels == null || tempPixels.capacity() < count) {
			tempPixels = Buffers.newDirectIntBuffer(count);
		}
		tempPixels.rewind();
		tempPixels.limit(count);
		return tempPixels;
	}
	
	@Override
	protected void renderLayoutToTexture(TextLayout layout, ITexture tex) {
		//System.out.println("RENDER: " + layout.getText().replace("\n", "") + " -> " + tex);
		
		final int sl = getStartLine();
		final int el = getEndLine();
		final double visibleChars = getVisibleChars();
		
		TextureAdapter ta = (TextureAdapter)tex;
		GLWritableTexture inner = (GLWritableTexture)ta.getTexture();
		final int tw = inner.getTexWidth();
		final int th = inner.getTexHeight();

		BufferedImage image = getTempImage(tw, th);
		Graphics2D g = image.createGraphics();
		g.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
		g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
		
		//System.out.println(getLayoutMaxWidth() + " " + getLayoutWidth() + " " + tex.getTexWidth());
		
		double offsetX = (isRightToLeft() ? -getLayoutTrailing(sl, el) : -getLayoutLeading(sl, el));
		
		AWTParagraphRenderer pr = newAWTParagraphRenderer();
		pr.setBounds(offsetX, 0, getLayoutMaxWidth(), getLayoutMaxHeight());
		pr.setLineOffset(sl);
		pr.setVisibleLines(el - sl);
		pr.setVisibleChars((float)visibleChars);
		
		//System.out.printf("start=%d, end=%d, visibleChars=%.1f\n", startLine, endLine, visibleChars);
		
		pr.drawLayout(g, layout, 0, 0);
		g.dispose();
		
		IntBuffer pixels = getTempPixels(tw * th);
		ImageUtil.getPixelsPre(image, pixels, 0, tw);
		inner.setPixels(imgfac.newARGB8TextureData(pixels, tw, th));
	}

	@Override
	protected TextLayout createLayout(float width, float height) {
		AWTParagraphRenderer pr = newAWTParagraphRenderer();
		pr.setRightToLeft(isRightToLeft());
		pr.setDefaultStyle(pr.getDefaultStyle().extend(getDefaultStyle()));
		return pr.getLayout(getText(), (float)width);
	}
	
	protected AWTParagraphRenderer newAWTParagraphRenderer() {
		return new AWTParagraphRenderer(glyphManager, new ParagraphLayouter());
	}
			
	@Override
	public int getEndLine() {
		TextLayout layout = getLayout();
		return TextDrawable.getEndLine(layout, getStartLine(), getLayoutMaxHeight());
	}
	
	@Override
	public int getLineCount() {
		TextLayout layout = getLayout();
		return layout.getNumLines();
	}
			
	@Override
	public int getCharOffset(int line) {
		TextLayout layout = getLayout();
		return layout.getCharOffset(Math.max(0, Math.min(layout.getNumLines(), line)));			
	}
	
	@Override
	protected float getLayoutLeading(int line) {			
		TextLayout layout = getLayout();
		return (isRightToLeft() ? layout.getPadRight(line) : layout.getPadLeft(line));
	}
	
	@Override
	protected float getLayoutTrailing(int line) {			
		TextLayout layout = getLayout();
		return (isRightToLeft() ? layout.getPadLeft(line) : layout.getPadRight(line));
	}
		
	@Override
	public float getLayoutWidth(int line) {
		TextLayout layout = getLayout();
		return (line >= 0 && line < layout.getNumLines() ? layout.getLineWidth(line) : 0);
	}
	
	@Override
	public float getLayoutHeight(int startLine, int endLine) {
		TextLayout layout = getLayout();
		return layout.getHeight(Math.max(0, startLine), Math.min(layout.getNumLines(), endLine));
	}
	
	public LineElement getLayoutHitElement(float cx, float cy) {
		return TextDrawable.getLayoutHitElement(getLayout(), getStartLine(), getEndLine(), cx - getPadLeft(), cy);
	}
	
	@Override
	public int[] getHitTags(float cx, float cy) {
		LineElement le = getLayoutHitElement(cx * getDisplayScale(), cy * getDisplayScale());
		return (le != null ? le.getStyle().getTags() : null);
	}
	
}