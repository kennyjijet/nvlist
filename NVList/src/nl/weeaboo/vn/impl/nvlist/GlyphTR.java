package nl.weeaboo.vn.impl.nvlist;

import nl.weeaboo.gl.text.GlyphManager;
import nl.weeaboo.gl.text.ParagraphRenderer;
import nl.weeaboo.lua2.io.LuaSerializable;
import nl.weeaboo.styledtext.layout.LineElement;
import nl.weeaboo.styledtext.layout.TextLayout;
import nl.weeaboo.vn.BlendMode;
import nl.weeaboo.vn.IDrawBuffer;
import nl.weeaboo.vn.impl.base.AbstractTextRenderer;

@LuaSerializable
class GlyphTR extends AbstractTextRenderer<TextLayout> {

	private static final long serialVersionUID = NVListImpl.serialVersionUID;

	private final GlyphManager glyphManager;
	
	public GlyphTR(ImageFactory imgfac, GlyphManager trStore) {
		this.glyphManager = trStore;
	}
	
	@Override
	public float increaseVisibleChars(float textSpeed) {
		return TextDrawable.increaseVisibleChars(this, getLayout(), getStartLine(), getVisibleChars(), textSpeed);
	}
	
	@Override
	public void draw(IDrawBuffer d, short z, boolean clipEnabled, BlendMode blendMode, int argb,
			double dx, double dy)
	{
		DrawBuffer dd = DrawBuffer.cast(d);
		dd.drawText(z, clipEnabled, blendMode, argb, getLayout(),
				getStartLine(), getEndLine(), getVisibleChars(),
				dx + getPadLeft(), dy);
		
		//System.out.println(getMaxWidth() + "x" + getMaxHeight() + " " + getTextLeading() + " " + getTextWidth());
	}

	@Override
	protected TextLayout createLayout(float width, float height) {
		ParagraphRenderer pr = glyphManager.createParagraphRenderer();
		pr.setRightToLeft(isRightToLeft());
		pr.setDefaultStyle(pr.getDefaultStyle().extend(getDefaultStyle()));
		
		/*
		ExtensibleGlyphStore egs = pr.getGlyphStore();
		egs.setOverride(1, new AbstractGlyphStore() {
			@Override
			public IGlyph getGlyph(TextStyle style, String chars) {
				GLTexRect tr = imgfac.getTexRect("anim/anim01", null);
				float h = style.getFontSize();
				float w = h;
				if (tr != null) {
					Dim2D d = ScaleUtil.scaleProp(tr.getWidth(), tr.getHeight(), w, h);
					w = (float)d.w;
					h = (float)d.h;
				}
				return new TextureGlyph("X", tr, new Area2D(0, 0, w, h), w, h);
			}
		});
		*/
				
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
		LineElement le = getLayoutHitElement(cx, cy);
		return (le != null ? le.getStyle().getTags() : null);
	}
	
}