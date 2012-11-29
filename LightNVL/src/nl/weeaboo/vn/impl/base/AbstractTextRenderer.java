package nl.weeaboo.vn.impl.base;

import nl.weeaboo.styledtext.StyledText;
import nl.weeaboo.styledtext.TextStyle;
import nl.weeaboo.vn.IDrawable;
import nl.weeaboo.vn.ITextRenderer;
import nl.weeaboo.vn.RenderEnv;

public abstract class AbstractTextRenderer<L> implements ITextRenderer {

	private static final long serialVersionUID = BaseImpl.serialVersionUID;

	private StyledText stext;
	private TextStyle defaultStyle;
	private int startLine;
	private float visibleChars;
	private float width, height;
	private float displayScale;
	private boolean isRightToLeft;
	private IDrawable cursor;	
	
	private /*transient*/ RenderEnv renderEnv;
	
	private transient L layout;
	private boolean changed;
	
	public AbstractTextRenderer() {
		stext = StyledText.EMPTY_STRING;
		defaultStyle = TextStyle.defaultInstance();
		visibleChars = 999999;
		displayScale = 1;
	}
	
	//Functions
	@Override
	public void destroy() {
		layout = null;
	}
		
	protected void invalidateLayout() {
		layout = null;
		markChanged();
	}
		
	protected void onVisibleTextChanged() {	
		markChanged();
	}
	
	protected void onDisplayScaleChanged() {		
		markChanged();
	}
	
	protected abstract L createLayout(float width, float height);
	
	protected void markChanged() {
		changed = true;
	}
	
	protected boolean consumeChanged() {
		boolean result = changed;
		changed = false;
		return result;
	}
	
	@Override
	public boolean update() {
		return consumeChanged();
	}
	
	//Getters	
	protected IDrawable getCursor() {
		return cursor;
	}
	
	protected L getLayout() {
		if (layout == null) {
			layout = createLayout(getLayoutMaxWidth(), getLayoutMaxHeight());
		}
		return layout;
	}

	protected StyledText getText() {
		return stext;
	}
	
	protected TextStyle getDefaultStyle() {
		return defaultStyle;
	}
	
	protected int getStartLine() {
		return startLine;
	}
		
	protected float getVisibleChars() {
		return visibleChars;
	}
	
	protected float getDisplayScale() {
		return displayScale;
	}
	
	@Override
	public float getMaxWidth() {
		return width;
	}
	
	@Override
	public float getMaxHeight() {
		return height;
	}
		
	protected float getCursorWidth() {
		return (cursor != null ? (float)cursor.getWidth() : 0);		
	}
	
	protected int getLayoutMaxWidth() {
		return (int)Math.ceil(width - getCursorWidth());
	}	
	protected int getLayoutMaxHeight() {
		return (int)Math.ceil(height);
	}

	protected final float getLayoutWidth() {
		return getLayoutWidth(startLine, getEndLine());		
	}
	protected final float getLayoutWidth(int startLine, int endLine) {
		return getLayoutMaxWidth() - getLayoutLeading(startLine, endLine) - getLayoutTrailing(startLine, endLine);		
	}	
	protected final float getLayoutHeight() {
		return getLayoutHeight(startLine, getEndLine());		
	}	
	protected final float getLayoutLeading() {
		return getLayoutLeading(startLine, getEndLine());		
	}	
	protected final float getLayoutLeading(int startLine, int endLine) {
		startLine = Math.max(0, startLine);
		endLine = Math.min(getLineCount(), endLine);
		if (endLine <= startLine) {
			return 0;
		}
		
		float w = Float.MAX_VALUE;
		for (int line = startLine; line < endLine; line++) {
			w = Math.min(w, getLayoutLeading(line));
		}		
		return w;
	}
	
	protected final float getLayoutTrailing() {
		return getLayoutTrailing(startLine, getEndLine());		
	}	
	protected final float getLayoutTrailing(int startLine, int endLine) {
		startLine = Math.max(0, startLine);
		endLine = Math.min(getLineCount(), endLine);
		if (endLine <= startLine) {
			return 0;
		}
		
		float w = Float.MAX_VALUE;
		for (int line = startLine; line < endLine; line++) {
			w = Math.min(w, getLayoutTrailing(line));
		}		
		return w;
	}
	
	protected abstract float getLayoutWidth(int line);
	protected abstract float getLayoutHeight(int startLine, int endLine);	
	protected abstract float getLayoutLeading(int line);
	protected abstract float getLayoutTrailing(int line);
	
	@Override
	public float getTextLeading() {
		return getTextLeading(startLine, getEndLine());
	}
	
	@Override
	public float getTextLeading(int startLine, int endLine) {
		return getLayoutLeading(startLine, endLine);
	}

	@Override
	public float getTextWidth() {
		return getTextWidth(startLine, getEndLine());
	}
	
	@Override
	public float getTextWidth(int startLine, int endLine) {
		return getLayoutWidth(startLine, endLine);
	}
	
	@Override
	public float getLineWidth(int line) {
		return getLayoutWidth(line);
	}	
		
	@Override
	public float getTextTrailing() {
		return getTextTrailing(startLine, getEndLine());
	}
	
	@Override
	public float getTextTrailing(int startLine, int endLine) {
		return getLayoutTrailing(startLine, endLine);
	}
	
	@Override
	public float getTextHeight() {
		return getTextHeight(startLine, getEndLine());
	}
	
	@Override
	public float getTextHeight(int startLine, int endLine) {
		return getLayoutHeight(startLine, endLine);
	}
	
	@Override
	public boolean isRightToLeft() {
		return isRightToLeft;
	}
	
	protected RenderEnv getRenderEnv() {
		return renderEnv;
	}
	
	protected double getPadLeft() {	
		return (isRightToLeft ? getCursorWidth() : 0);
	}
	
	//Setters
	@Override
	public void setMaxSize(float w, float h) {
		if (width != w || height != h) {
			width = w;
			height = h;
			
			invalidateLayout();
		}
	}
	
	@Override
	public void setText(StyledText st) {
		if (!stext.equals(st)) {
			stext = st;
			invalidateLayout();
		}
	}
	
	@Override
	public void setDefaultStyle(TextStyle ts) {
		if (!defaultStyle.equals(ts)) {
			defaultStyle = ts;
			invalidateLayout();
		}
	}
	
	@Override
	public void setVisibleText(int sl, float vc) {
		if (startLine != sl || visibleChars != vc) {
			startLine = sl;
			visibleChars = vc;
			onVisibleTextChanged();
		}
	}
	
	@Override
	public void setCursor(IDrawable c) {
		cursor = c;
	}
		
	@Override
	public void setRenderEnv(RenderEnv env) {
		if (renderEnv != env) {
			renderEnv = env;
			
			displayScale = (float)env.getScale();
			onDisplayScaleChanged();
		}
	}
	
	@Override
	public void setRightToLeft(boolean rtl) {
		if (isRightToLeft != rtl) {
			isRightToLeft = rtl;
			invalidateLayout();
		}
	}
	
}
