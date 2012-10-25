package nl.weeaboo.vn.impl.base;

import java.io.IOException;
import java.io.ObjectInputStream;

import nl.weeaboo.styledtext.StyledText;
import nl.weeaboo.styledtext.TextStyle;
import nl.weeaboo.vn.IDrawable;
import nl.weeaboo.vn.IInput;
import nl.weeaboo.vn.ILayer;
import nl.weeaboo.vn.ITextDrawable;

public abstract class BaseTextDrawable extends BaseDrawable implements ITextDrawable {

	private static final long serialVersionUID = BaseImpl.serialVersionUID;
	
	private StyledText text;
	private TextStyle defaultStyle;
	private transient int startLine;
	private transient double visibleChars;
	private double textSpeed;
	private double width, height;
	private double pad;
	private int texWidth, texHeight;
	private transient boolean texDirty = true;
	private double backgroundRGBA[] = {0, 0, 0, 0};
	private int backgroundARGBInt;
	private int anchor;
	private IDrawable cursor;
	private boolean cursorAuto;
	private boolean cursorAutoPos;
	private double targetCursorAlpha;
	
	protected BaseTextDrawable(StyledText t) {
		text = t;
		textSpeed = -1;
		anchor = 7;
		defaultStyle = TextStyle.defaultInstance();
		
		initTransients();
	}

	//Functions
	private void initTransients() {
		visibleChars = (textSpeed >= 0 ? 0 : 999999);
		texDirty = true;
		backgroundARGBInt = BaseImpl.packRGBAtoARGB(backgroundRGBA[0], backgroundRGBA[1], backgroundRGBA[2], backgroundRGBA[3]);
	}
	
	private void readObject(ObjectInputStream in) throws IOException, ClassNotFoundException {
		in.defaultReadObject();

		initTransients();
	}
	
	@Override
	public boolean update(ILayer layer, IInput input, double effectSpeed) {
		if (super.update(layer, input, effectSpeed)) {
			markChanged();
		}
		
		if (!getCurrentLinesFullyVisible()) {
			markChanged();
			
			setVisibleChars(textSpeed >= 0 ? getVisibleChars()+textSpeed : 999999);
		}
		
		targetCursorAlpha = 0;
		updateCursorPos();
		updateCursorAlpha(effectSpeed);
				
		return consumeChanged();
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
	
	protected void updateTextureSize(RenderEnv env) {
		int texW = getTextureWidth();
		int texH = getTextureHeight();
		int targetW = (int)Math.round(getInnerWidth() * env.scale);
		int targetH = (int)Math.round(getInnerHeight() * env.scale);
		if (texW != targetW || texH != targetH) {
			setTextureSize(targetW, targetH);
		}		
	}

	protected abstract void onSizeChanged();
	
	protected abstract void onTextChanged();
	
	private boolean isInstantTextSpeed() {
		return textSpeed < 0 || textSpeed >= 100000;		
	}
	
	//Getters
	@Override
	public StyledText getText() {
		return text;
	}
	
	@Override
	public int getStartLine() {
		return startLine;
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
		return width - pad*2 - (cursor != null ? cursor.getWidth() : 0);
	}
	
	@Override
	public double getInnerHeight() {
		return height - pad*2;
	}
	
	@Override
	public double getPadding() {
		return pad;
	}
	
	protected int getTextureWidth() {
		return texWidth;
	}
	
	protected int getTextureHeight() {
		return texHeight;
	}
	
	protected boolean isTexDirty() {
		return texDirty;
	}
	
	@Override
	public double getTextHeight() {
		return getTextHeight(getStartLine(), getEndLine());
	}
	
	/**
	 * @return The width of the text in screen coordinates.
	 */
	public abstract int getLayoutWidth();
	
	/**
	 * @return The height of the text in screen coordinates.
	 */
	public int getLayoutHeight() {
		return getLayoutHeight(getStartLine(), getEndLine());		
	}
	
	/**
	 * @return The height of the text between the <code>start</code> and
	 *         <code>end</code> lines in screen coordinates.
	 */
	public abstract int getLayoutHeight(int start, int end);
	
	/**
	 * @return The X-coordinate of the right of the last visible char
	 */
	protected abstract double getCursorX();

	/**
	 * @return The Y-coordinate of the top of the last visible char
	 */
	protected abstract double getCursorY();
	
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
	public int getAnchor() {
		return anchor;
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
			texDirty = true;
			
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
			texDirty = true;			
		}
	}
	
	@Override
	public void setVisibleChars(double vc) {
		if (visibleChars != vc) {
			visibleChars = vc;
			markChanged();
			texDirty = true;
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
			texDirty = true;			
			onSizeChanged();
		}
	}
	
	protected void setTextureSize(int tw, int th) {
		if (texWidth != tw || texHeight != th) {
			texWidth = tw;
			texHeight = th;
			
			markChanged();
			texDirty = true;			
			onSizeChanged();
		}
	}
	
	@Override
	public void setPadding(double p) {
		if (pad != p) {
			pad = p;

			markChanged();
			texDirty = true;
			onSizeChanged();
		}
	}
	
	protected void setTexDirty(boolean td) {
		if (texDirty != td) {
			texDirty = td;
			markChanged();
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
			
			markChanged();
			texDirty = true;
			onSizeChanged();
		}
	}
			
}
