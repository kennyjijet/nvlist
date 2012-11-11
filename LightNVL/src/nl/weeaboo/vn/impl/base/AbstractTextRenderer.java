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
	private double visibleChars;
	private double width, height;
	private double displayScale;
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
	
	protected abstract L createLayout(double width, double height);
	
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
		
	protected double getVisibleChars() {
		return visibleChars;
	}
	
	protected double getDisplayScale() {
		return displayScale;
	}
	
	protected double getMaxWidth() {
		return width;
	}
	
	protected double getMaxHeight() {
		return height;
	}
		
	protected int getLayoutMaxWidth() {
		IDrawable cursor = getCursor();
		return (int)Math.ceil(width - (cursor != null ? cursor.getWidth() : 0));
	}
	
	protected int getLayoutMaxHeight() {
		return (int)Math.ceil(height);
	}
	
	protected double getLayoutWidth() {
		return getLayoutWidth(startLine, getEndLine());
	}

	protected abstract double getLayoutWidth(int startLine, int endLine);	
	
	protected double getLayoutHeight() {
		return getLayoutHeight(startLine, getEndLine());		
	}

	protected abstract double getLayoutHeight(int startLine, int endLine);	
	
	@Override
	public double getTextWidth() {
		return getTextWidth(startLine, getEndLine());
	}

	@Override
	public double getTextWidth(int startLine, int endLine) {
		return getLayoutWidth(startLine, getEndLine());
	}
	
	@Override
	public double getTextHeight() {
		return getTextHeight(startLine, getEndLine());
	}
	
	@Override
	public double getTextHeight(int startLine, int endLine) {
		return getLayoutHeight(startLine, endLine);
	}
	
	protected RenderEnv getRenderEnv() {
		return renderEnv;
	}
	
	//Setters
	@Override
	public void setMaxSize(double w, double h) {
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
	public void setVisibleText(int sl, double vc) {
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
			
			displayScale = env.getScale();
			onDisplayScaleChanged();
		}
	}
	
}
