package nl.weeaboo.vn.impl.nvlist;

import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.image.BufferedImage;
import java.nio.IntBuffer;

import nl.weeaboo.awt.ImageUtil;
import nl.weeaboo.gl.text.AWTParagraphRenderer;
import nl.weeaboo.gl.text.GLTextRendererStore;
import nl.weeaboo.gl.texture.GLGeneratedTexture;
import nl.weeaboo.lua2.io.LuaSerializable;
import nl.weeaboo.textlayout.TextLayout;
import nl.weeaboo.vn.ITexture;
import nl.weeaboo.vn.impl.base.TextureTextRenderer;

import com.jogamp.common.nio.Buffers;

//Inner Classes
@LuaSerializable class TextureTR extends TextureTextRenderer<TextLayout, GLGeneratedTexture> {

	private static final long serialVersionUID = NVListImpl.serialVersionUID;
	
	private final ImageFactory imgfac;
	private final GLTextRendererStore trStore;
	
	private transient BufferedImage tempImage;
	private transient IntBuffer tempPixels;
	
	public TextureTR(ImageFactory imgfac, GLTextRendererStore trStore) {
		this.imgfac = imgfac;
		this.trStore = trStore;
	}
	
	@Override
	protected void destroyTexture(GLGeneratedTexture texture) {
		//Can't manually dispose GLTexRect, must wait for GC
	}

	@Override
	protected GLGeneratedTexture createTexture(int w, int h) {
		return imgfac.createGLTexture(null, w, h, 0, 0, 0);
	}

	@Override
	protected ITexture createTextureWrapper(GLGeneratedTexture tex, double sx, double sy) {
		return imgfac.createTexture(tex, sx, sy);
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
	protected void renderLayoutToTexture(TextLayout layout, GLGeneratedTexture tex) {
		//System.out.println("RENDER: " + layout.getText().replace("\n", "") + " -> " + tex);
		
		int startLine = getStartLine();
		int endLine = getEndLine();
		double visibleChars = getVisibleChars();
		
		BufferedImage image = getTempImage(tex.getTexWidth(), tex.getTexHeight());
		Graphics2D g = image.createGraphics();
		g.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
		g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
		
		//System.out.println(getLayoutMaxWidth() + " " + getLayoutWidth() + " " + tex.getTexWidth());
		
		AWTParagraphRenderer pr = trStore.createAWTParagraphRenderer();
		pr.setBounds(0, 0, getLayoutMaxWidth(), getLayoutMaxHeight());
		pr.setLineOffset(startLine);
		pr.setVisibleLines(endLine - startLine);
		pr.setVisibleChars(visibleChars);
		
		//System.out.printf("start=%d, end=%d, visibleChars=%.1f\n", startLine, endLine, visibleChars);
		
		pr.drawLayout(g, layout);
		g.dispose();
		
		IntBuffer pixels = getTempPixels(tex.getTexWidth() * tex.getTexHeight());
		ImageUtil.getPixelsPre(image, pixels, 0, tex.getTexWidth());
		tex.setARGB(pixels);
	}

	@Override
	protected TextLayout createLayout(double width, double height) {
		AWTParagraphRenderer pr = trStore.createAWTParagraphRenderer();
		pr.setDefaultStyle(pr.getDefaultStyle().extend(getDefaultStyle()));
		return pr.getLayout(getText(), width);
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
	protected double getLayoutWidth(int startLine, int endLine) {			
		TextLayout layout = getLayout();
		return TextDrawable.getLayoutWidth(layout, startLine, endLine);
	}
		
	@Override
	public double getLayoutHeight(int startLine, int endLine) {
		TextLayout layout = getLayout();
		return layout.getHeight(Math.max(0, startLine), Math.min(layout.getNumLines(), endLine));
	}
	
}