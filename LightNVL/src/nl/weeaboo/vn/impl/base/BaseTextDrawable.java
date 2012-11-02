package nl.weeaboo.vn.impl.base;

import java.io.IOException;
import java.io.ObjectInputStream;

import nl.weeaboo.common.Rect2D;
import nl.weeaboo.styledtext.StyledText;
import nl.weeaboo.styledtext.TextStyle;
import nl.weeaboo.vn.BlendMode;
import nl.weeaboo.vn.IDrawBuffer;
import nl.weeaboo.vn.IDrawable;
import nl.weeaboo.vn.IInput;
import nl.weeaboo.vn.ILayer;
import nl.weeaboo.vn.ITextDrawable;
import nl.weeaboo.vn.ITextRenderer;
import nl.weeaboo.vn.RenderEnv;
import nl.weeaboo.vn.layout.LayoutUtil;
import nl.weeaboo.vn.math.Matrix;

public abstract class BaseTextDrawable extends BaseDrawable implements ITextDrawable {

	private static final long serialVersionUID = BaseImpl.serialVersionUID;
	
	protected final ITextRenderer textRenderer;
	
	private StyledText text;
	private TextStyle defaultStyle;
	private transient int startLine;
	private transient double visibleChars;
	private double textSpeed;
	private double width, height;
	private double pad;
	private double backgroundRGBA[] = {0, 0, 0, 0};
	private int backgroundARGBInt;
	private int anchor;
	private IDrawable cursor;
	private boolean cursorAuto;
	private boolean cursorAutoPos;
	private double targetCursorAlpha;
	
	protected BaseTextDrawable(ITextRenderer tr) {
		textRenderer = tr;
		
		text = StyledText.EMPTY_STRING;
		textSpeed = -1;
		anchor = 7;
		defaultStyle = TextStyle.defaultInstance();
		
		initTransients();
	}

	//Functions
	private void initTransients() {
		visibleChars = (textSpeed >= 0 ? 0 : 999999);
		backgroundARGBInt = BaseImpl.packRGBAtoARGB(backgroundRGBA[0], backgroundRGBA[1], backgroundRGBA[2], backgroundRGBA[3]);
	}
	
	private void readObject(ObjectInputStream in) throws IOException, ClassNotFoundException {
		in.defaultReadObject();

		initTransients();
	}
	
	@Override
	public void destroy() {
		if (!isDestroyed()) {
			textRenderer.destroy();
			
			super.destroy();
		}
	}
	
	@Override
	public boolean update(ILayer layer, IInput input, double effectSpeed) {
		if (super.update(layer, input, effectSpeed)) {
			markChanged();
		}
		
		if (!getCurrentLinesFullyVisible()) {
			setVisibleChars(textSpeed >= 0 ? getVisibleChars()+textSpeed : 999999);
		}
		textRenderer.setVisibleText(getStartLine(), getVisibleChars());
		
		targetCursorAlpha = 0;
		updateCursorPos();
		updateCursorAlpha(effectSpeed);
				
		return consumeChanged();
	}
	
	@Override
	public void draw(IDrawBuffer d) {		
		short z = getZ();
		boolean clip = isClipEnabled();
		BlendMode blend = getBlendMode();
		int argb = getColorARGB();
		Matrix transform = getTransform();
		
		int bgColor = getBackgroundColorARGB();
		int bgAlpha = ((bgColor>>24)&0xFF);
		if (bgAlpha > 0) {
			if (getAlpha() < 1) {
				bgAlpha = Math.max(0, Math.min(255, (int)Math.round(bgAlpha * getAlpha())));
			}
			if (bgAlpha > 0) {
				int c = (bgAlpha<<24)|(bgColor&0xFFFFFF);
				d.drawQuad((short)(z+1), clip, blend, c, null,
						transform, 0, 0, getWidth(), getHeight(),
						getPixelShader());
			}
		}
		
		double pad = getPadding();
		double x = getX() + pad + LayoutUtil.alignAnchorX(getInnerWidth(), getTextWidth(), anchor);
		double y = getY() + pad + LayoutUtil.alignAnchorY(getInnerHeight(), getTextHeight(), anchor);
		textRenderer.draw(d, z, clip, blend, argb, x, y);
		updateCursorPos();
	}
	
	public void updateCursorPos() {
		if (cursor == null) {
			return;
		}
		
		if (cursorAutoPos) {
			double pad = getPadding();
			cursor.setPos(getX() + pad + getCursorX(), getY() + pad + getCursorY());
		}
		cursor.setClipEnabled(isClipEnabled());
	}
	
	public void updateCursorAlpha(double effectSpeed) {
		if (cursor == null) {
			return;
		}
		
		if (getText().length() > 0 && getCurrentLinesFullyVisible()) {
			targetCursorAlpha = getAlpha();
		} else {
			cursor.setAlpha(0);
		}
		
		if (cursorAuto) {
			double newAlpha = targetCursorAlpha;				
			if (textSpeed >= 0 && textSpeed < getMaxVisibleChars()) {
				double deltaAlpha = newAlpha - cursor.getAlpha();
				if (deltaAlpha != 0) {
					deltaAlpha = Math.max(-.1, Math.min(.1, .1 * deltaAlpha)) * effectSpeed;
					if (Math.abs(deltaAlpha * 255) > 1) {
						newAlpha = cursor.getAlpha() + deltaAlpha;
					}
				}
			}
			cursor.setAlpha(Math.min(getAlpha(), newAlpha));
			cursor.setVisible(isVisible());
		}		
	}

	protected void onSizeChanged() {
		textRenderer.setMaxSize(getInnerWidth(), getInnerHeight());
		updateCursorPos();
	}
	
	protected void onTextChanged() {
		textRenderer.setText(text);
		updateCursorPos();
	}
	
	protected void onVisibleTextChanged() {
		textRenderer.setVisibleText(startLine, visibleChars);
		updateCursorPos();
	}
	
	private boolean isInstantTextSpeed() {
		return textSpeed < 0 || textSpeed >= 100000;		
	}
	
	protected void onRenderEnvChanged() {
		super.onRenderEnvChanged();
		
		RenderEnv env = getRenderEnv();
		textRenderer.setDisplayScale(env != null ? env.getScale() : 1);
	}
	
	//Getters
	@Override
	public Rect2D getBounds() {
		double pad = getPadding();
		double x = getX() + LayoutUtil.alignAnchorX(getInnerWidth(), getTextWidth(), anchor);
		double y = getY() + LayoutUtil.alignAnchorY(getInnerHeight(), getTextHeight(), anchor);
		double w, h;
		if (getBackgroundAlpha() > 0) {
			w = getWidth();
			h = getHeight();
		} else {
			w = getTextWidth() + pad*2;
			h = getTextHeight() + pad*2;
		}
		return new Rect2D(x, y, Double.isNaN(w) ? 0 : w, Double.isNaN(h) ? 0 : h);
	}
	
	@Override
	public StyledText getText() {
		return text;
	}
	
	@Override
	public int getStartLine() {
		return startLine;
	}
	
	@Override
	public int getEndLine() {
		return textRenderer.getEndLine();
	}
	
	@Override
	public int getLineCount() {
		return textRenderer.getLineCount();
	}
	
	@Override
	public double getTextSpeed() {
		return textSpeed;
	}
	
	@Override
	public boolean getCurrentLinesFullyVisible() {
		return visibleChars < 0 || visibleChars >= getMaxVisibleChars();
	}
	
	@Override
	public boolean getFinalLineFullyVisible() {
		return getEndLine() >= getLineCount() && getCurrentLinesFullyVisible();		
	}
	
	@Override
	public int getMaxVisibleChars() {
		return getCharOffset(getEndLine()) - getCharOffset(startLine);		
	}
	
	@Override
	public double getVisibleChars() {
		return visibleChars;
	}
	
	@Override
	public double getWidth() {
		return width;
	}
	
	@Override
	public double getHeight() {
		return height;
	}
	
	@Override
	public double getInnerWidth() {
		return width - pad*2;
	}
	
	@Override
	public double getInnerHeight() {
		return height - pad*2;
	}
	
	@Override
	public double getPadding() {
		return pad;
	}
	
	@Override
	public double getTextWidth() {
		return getTextWidth(startLine, getEndLine());
	}
	
	public double getTextWidth(int startLine, int endLine) {
		return textRenderer.getTextWidth(startLine, endLine);
	}
	
	@Override
	public double getTextHeight() {
		return getTextHeight(startLine, getEndLine());
	}
	
	@Override
	public double getTextHeight(int startLine, int endLine) {
		return textRenderer.getTextHeight(startLine, endLine);
	}

	@Override
	public int getCharOffset(int line) {
		return textRenderer.getCharOffset(line);
	}
	
	private int getCursorLine() {
		int sl = getStartLine();
		int el = getEndLine();
		for (int line = el-1; line >= sl; line--) {
			double w = textRenderer.getTextWidth(line, line+1);
			if (w > 0) {
				return line;
			}
		}
		return sl;
	}
	
	protected double getCursorX() {
		if (getLineCount() == 0) return 0;

		int cl = getCursorLine();
		return textRenderer.getTextWidth(cl, cl+1);
	}

	protected double getCursorY() {
		if (getLineCount() == 0) return 0;
		
		int sl = getStartLine();
		int cl = getCursorLine();
		double height = getTextHeight(sl, cl+1);
		
		double cursorHeight = 0;
		if (getCursor() != null) {
			cursorHeight = getCursor().getHeight();
		}
		
		return height - cursorHeight;
	}
	
	@Override
	public int getBackgroundColorRGB() {
		return getBackgroundColorARGB() & 0xFFFFFF;
	}
	
	@Override
	public int getBackgroundColorARGB() {
		return backgroundARGBInt;
	}

	@Override
	public double getBackgroundRed() {
		return backgroundRGBA[0];
	}

	@Override
	public double getBackgroundGreen() {
		return backgroundRGBA[1];
	}

	@Override
	public double getBackgroundBlue() {
		return backgroundRGBA[2];
	}
	
	@Override
	public double getBackgroundAlpha() {
		return backgroundRGBA[3];
	}
	
	@Override
	public TextStyle getDefaultStyle() {
		return defaultStyle;
	}
	
	@Override
	public IDrawable getCursor() {
		return cursor;
	}
		
	//Setters
	@Override
	public void setText(String t) {
		setText(new StyledText(t, defaultStyle));
	}
	
	@Override
	public void setText(StyledText t) {
		if (!text.equals(t)) {
			text = t;			
			
			startLine = 0;
			if (cursorAuto) {
				cursor.setAlpha(isInstantTextSpeed() ? getAlpha() : 0);
			}
			setVisibleChars(isInstantTextSpeed() ? 999999 : 0);
			markChanged();
			onTextChanged();
		}
	}
	
	@Override
	public void setStartLine(int sl) {
		sl = Math.max(0, Math.min(getLineCount(), sl));
		
		if (startLine != sl) {
			startLine = sl;
			setVisibleChars(isInstantTextSpeed() ? 999999 : 0);
			markChanged();
			onVisibleTextChanged();
		}
	}
	
	@Override
	public void setVisibleChars(double vc) {
		if (visibleChars != vc) {
			visibleChars = vc;
			markChanged();
			onVisibleTextChanged();
		}
	}
	
	@Override
	public void setTextSpeed(double ts) {
		if (Double.isNaN(ts) || ts <= 0) {
			throw new IllegalArgumentException("Textspeed must be > 0");
		}

		if (textSpeed != ts) {
			textSpeed = ts;
			markChanged();
		}
	}
	
	@Override
	public void setSize(double w, double h) {
		if (width != w || height != h) {
			width = w;
			height = h;
			
			markChanged();
			onSizeChanged();
		}
	}
	
	@Override
	public void setBounds(double x, double y, double w, double h) {
		setPos(x, y);
		setSize(w, h);
	}
	
	@Override
	public void setPadding(double p) {
		if (pad != p) {
			pad = p;

			markChanged();
			onSizeChanged();
		}
	}
	
	@Override
	public void setBackgroundColor(double r, double g, double b) {
		setBackgroundColor(r, g, b, backgroundRGBA[3]);
	}
	
	@Override
	public void setBackgroundColor(double r, double g, double b, double a) {
		if (backgroundRGBA[0] != r || backgroundRGBA[1] != g || backgroundRGBA[2] != b || backgroundRGBA[3] != a) {
			backgroundRGBA[0] = r;
			backgroundRGBA[1] = g;
			backgroundRGBA[2] = b;
			backgroundRGBA[3] = a;
			backgroundARGBInt = BaseImpl.packRGBAtoARGB(backgroundRGBA[0], backgroundRGBA[1], backgroundRGBA[2], backgroundRGBA[3]);
			
			markChanged();
		}
	}

	@Override
	public void setBackgroundColorRGB(int rgb) {
		int ri = (rgb>>16)&0xFF;
		int gi = (rgb>> 8)&0xFF;
		int bi = (rgb    )&0xFF;
		
		setBackgroundColor(Math.max(0, Math.min(1, ri/255.0)),
				Math.max(0, Math.min(1, gi/255.0)),
				Math.max(0, Math.min(1, bi/255.0)));
	}
	
	@Override
	public void setBackgroundColorARGB(int argb) {
		int ai = (argb>>24)&0xFF;
		int ri = (argb>>16)&0xFF;
		int gi = (argb>> 8)&0xFF;
		int bi = (argb    )&0xFF;
		
		setBackgroundColor(Math.max(0, Math.min(1, ri/255.0)),
				Math.max(0, Math.min(1, gi/255.0)),
				Math.max(0, Math.min(1, bi/255.0)),
				Math.max(0, Math.min(1, ai/255.0)));
	}
	
	@Override
	public void setBackgroundAlpha(double a) {
		setBackgroundColor(backgroundRGBA[0], backgroundRGBA[1], backgroundRGBA[2], a);
	}
		
	@Override
	public void setDefaultStyle(TextStyle ts) {
		if (ts == null) throw new IllegalArgumentException("setDefaultStyle() must not be called with a null argument.");
		
		if (defaultStyle != ts && (defaultStyle == null || !defaultStyle.equals(ts))) {
			defaultStyle = ts;
			textRenderer.setDefaultStyle(ts);
			markChanged();
		}
	}
	
	@Override
	public void setAnchor(int a) {
		if (anchor != a) {
			anchor = a;
			markChanged();
		}
	}
	
	@Override
	public void setCursor(IDrawable d, boolean autoConfig, boolean autoPos) {
		if (cursor != d || cursorAuto != autoConfig || cursorAutoPos != autoPos) {
			cursor = d;			
			cursorAuto = autoConfig;
			cursorAutoPos = autoPos;
			
			if (cursor != null && autoConfig) {
				cursor.setZ((short)(getZ() - 1));
				cursor.setAlpha(targetCursorAlpha);
			}
			
			textRenderer.setCursor(cursorAutoPos ? cursor : null);
			
			markChanged();
			onSizeChanged();
		}
	}
			
}
