package nl.weeaboo.vn.impl.nvlist;

import nl.weeaboo.gl.text.GLTextRendererStore;
import nl.weeaboo.gl.text.ParagraphRenderer;
import nl.weeaboo.lua2.io.LuaSerializable;
import nl.weeaboo.textlayout.TextLayout;
import nl.weeaboo.vn.BlendMode;
import nl.weeaboo.vn.IDrawBuffer;
import nl.weeaboo.vn.impl.base.AbstractTextRenderer;

@LuaSerializable class GlyphTR extends AbstractTextRenderer<TextLayout> {

	private static final long serialVersionUID = NVListImpl.serialVersionUID;

	private final GLTextRendererStore trStore;
	
	public GlyphTR(ImageFactory imgfac, GLTextRendererStore trStore) {
		this.trStore = trStore;
	}
	
	@Override
	public void draw(IDrawBuffer d, short z, boolean clipEnabled, BlendMode blendMode, int argb,
			double dx, double dy)
	{
		DrawBuffer dd = DrawBuffer.cast(d);
		dd.drawText(z, clipEnabled, blendMode, argb, getLayout(),
				getStartLine(), getEndLine(), getVisibleChars(),
				dx, dy, null);
	}

	@Override
	protected TextLayout createLayout(double width, double height) {
		ParagraphRenderer pr = trStore.createParagraphRenderer();			
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
		return TextDrawable.getLayoutRight(layout, startLine, endLine);
	}
		
	@Override
	public double getLayoutHeight(int startLine, int endLine) {
		TextLayout layout = getLayout();
		return layout.getHeight(Math.max(0, startLine), Math.min(layout.getNumLines(), endLine));
	}
	
}