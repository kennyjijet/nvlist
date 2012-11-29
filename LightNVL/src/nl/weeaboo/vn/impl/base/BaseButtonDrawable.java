package nl.weeaboo.vn.impl.base;

import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

import nl.weeaboo.styledtext.StyledText;
import nl.weeaboo.styledtext.TextStyle;
import nl.weeaboo.vn.BlendMode;
import nl.weeaboo.vn.IButtonDrawable;
import nl.weeaboo.vn.IDrawBuffer;
import nl.weeaboo.vn.IInput;
import nl.weeaboo.vn.ILayer;
import nl.weeaboo.vn.ITextRenderer;
import nl.weeaboo.vn.ITexture;
import nl.weeaboo.vn.RenderEnv;
import nl.weeaboo.vn.layout.LayoutUtil;
import nl.weeaboo.vn.math.IPolygon;
import nl.weeaboo.vn.math.Matrix;
import nl.weeaboo.vn.math.MutableMatrix;
import nl.weeaboo.vn.math.Polygon;
import nl.weeaboo.vn.math.Vec2;

public abstract class BaseButtonDrawable extends BaseImageDrawable implements IButtonDrawable {

	private static final long serialVersionUID = BaseImpl.serialVersionUID;
	
	private boolean rollover;
	private boolean keyArmed, mouseArmed;
	private boolean enabled;	
	private boolean selected;
	private boolean toggle;
	private boolean keyboardFocus;
	private int pressEvents;
	private double touchMargin;
	private Set<Integer> activationKeys;
	private ITexture normalTexture;
	private ITexture rolloverTexture;
	private ITexture pressedTexture;
	private ITexture pressedRolloverTexture;
	private ITexture disabledTexture;
	private ITexture disabledPressedTexture;
	private double alphaEnableThreshold;

	private ITextRenderer textRenderer;
	private StyledText stext;
	private TextStyle defaultStyle;
	private int textAnchor;
	
	protected BaseButtonDrawable(ITextRenderer tr) {
		enabled = true;
		activationKeys = new HashSet<Integer>();
		alphaEnableThreshold = 0.9;
		
		textRenderer = tr;
		stext = StyledText.EMPTY_STRING;
		defaultStyle = TextStyle.defaultInstance();
		textAnchor = 5;
	}
	
	//Functions
	@Override
	public void destroy() {
		if (!isDestroyed()) {
			textRenderer.destroy();
			
			super.destroy();
		}
	}
	
	@Override
	public void addActivationKeys(int... keys) {
		for (int key : keys) {
			activationKeys.add(key);
		}
	}

	@Override
	public void removeActivationKeys(int... keys) {
		for (int key : keys) {
			if (activationKeys.remove(key)) {
				keyArmed = false;
			}
		}
	}
	
	protected void updateTexture() {
		RenderEnv env = getRenderEnv();
		boolean isDisabled = !isEnabled();
		boolean isPressed = (isPressed() || isSelected());
		boolean isRollover = isRollover() && (env == null || !env.isTouchScreen);
		
		//System.out.println("pressed " + isPressed + " | rollover " + rollover);
		
		if (isDisabled && isPressed && disabledPressedTexture != null) {
			setTexture(disabledPressedTexture);
		} else if (isDisabled && disabledTexture != null) {
			setTexture(disabledTexture);
		} else if (isPressed && isRollover && pressedRolloverTexture != null) {
			setTexture(pressedRolloverTexture);
		} else if (isPressed && pressedTexture != null) {
			setTexture(pressedTexture);
		} else if (isRollover && rolloverTexture != null) {
			setTexture(rolloverTexture);
		} else if (normalTexture != null) {
			setTexture(normalTexture);
		}		
	}
	
	@Override
	public boolean update(ILayer layer, IInput input, double effectSpeed) {
		if (super.update(layer, input, effectSpeed)) {
			markChanged();
		}

		boolean visibleEnough = isVisible(alphaEnableThreshold);
		
		double x = input.getMouseX();
		double y = input.getMouseY();
				
		boolean inputHeld = isInputHeld(input);
		boolean contains = (!isClipEnabled() || layer.containsRel(x, y)) && contains(x, y) && visibleEnough;
		boolean r = contains && (mouseArmed || keyArmed || !inputHeld);
		if (rollover != r) {
			rollover = r;
			markChanged();
		}
		
		if (isEnabled() && visibleEnough) {
			consumeInput(input, contains);
			
			if ((mouseArmed || keyArmed) && !inputHeld) {
				if ((mouseArmed && contains) || keyArmed) {
					onPressed();
				}
				mouseArmed = keyArmed = false;
				markChanged();				
			}
		} else {
			pressEvents = 0;
			
			if (mouseArmed) {
				mouseArmed = false;
				markChanged();				
			}			
			if (keyArmed) {
				keyArmed = false;
				markChanged();				
			}
		}
		
		r = contains && (mouseArmed || keyArmed || !inputHeld);
		if (rollover != r) {
			rollover = r;
			markChanged();
		}
		
		updateTexture();
		
		if (textRenderer.update()) {
			markChanged();
		}
		
		return consumeChanged();
	}
	
	protected void onPressed() {
		if (isToggle()) {
			setSelected(!isSelected());
		}
		pressEvents++;
	}
	
	@Override
	public void draw(IDrawBuffer d) {
		updateTexture();
		
		super.draw(d);
		
		if (stext.length() > 0) {
			short z = getZ();
			boolean clip = isClipEnabled();
			BlendMode blend = getBlendMode();
			int argb = getColorARGB();
			
			Vec2 trPos = new Vec2();
			getTextRendererAbsoluteXY(trPos);
			textRenderer.draw(d, (short)(z-1), clip, blend, argb, trPos.x, trPos.y);
		}
	}
	
	@Override
	protected void invalidateTransform() {
		super.invalidateTransform();
		textRenderer.setMaxSize((float)getWidth(), (float)getHeight());
	}
		
	@Override
	public void cancelMouseArmed() {
		mouseArmed = false;
	}
	
	@Override
	public boolean consumePress() {
		// We could consume only one press, or let this method return the number
		// of consumed presses or something. Let's just consume all of them for
		// now...
		
		boolean consumed = (pressEvents > 0);		
		if (consumed) {
			markChanged();
		}

		pressEvents = 0;
				
		return consumed;
	}
	
	protected void consumeInput(IInput input, boolean mouseContains) {
		if (mouseContains && input.consumeMouse()) {
			mouseArmed = true;
			keyArmed = false;
			markChanged();
			return;
		}
		if (keyboardFocus && input.consumeConfirm()) {
			mouseArmed = false;
			keyArmed = true;
			markChanged();
			return;
		}
		for (Integer key : activationKeys) {
			if (input.consumeKey(key)) {
				mouseArmed = false;
				keyArmed = true;
				markChanged();
				return;
			}
		}
	}
	
	protected void getTextRendererAbsoluteXY(Vec2 out) {
		getTextRendererXY(out);
		out.x += getX() + touchMargin;
		out.y += getY() + touchMargin;
	}
	protected void getTextRendererXY(Vec2 out) {
		LayoutUtil.getTextRendererXY(out, getWidth(), getHeight(), textRenderer, textAnchor);
	}
	
	protected boolean isInputHeld(IInput input) {
		if (input.isMouseHeld()) {
			return true;
		}
		if (keyboardFocus && input.isConfirmHeld()) {
			return true;
		}
		for (Integer key : activationKeys) {
			if (input.isKeyHeld(key)) {
				return true;
			}
		}
		return false;
	}
	
	//Getters
	@Override
	public StyledText getText() {
		return stext;
	}
		
	@Override
	public TextStyle getDefaultStyle() {
		return defaultStyle;
	}
	
	@Override
	public boolean isRollover() {
		return rollover;
	}

	@Override
	public boolean isPressed() {
		return keyArmed || (rollover && mouseArmed);
	}
	
	@Override
	public boolean isEnabled() {
		return enabled;
	}
	
	@Override
	public double getTouchMargin() {
		return touchMargin;
	}
	
	@Override
	protected IPolygon createCollisionShape() {
		double padding = getTouchMargin();

		Matrix transform = getTransform();
		double dx = getAlignOffsetX();
		double dy = getAlignOffsetY();
		if (dx != 0 || dy != 0) {
			MutableMatrix mm = transform.mutableCopy();
			mm.translate(dx, dy);
			transform = mm.immutableCopy();
		}
		return new Polygon(transform, -padding, -padding,
				getUnscaledWidth()+padding*2, getUnscaledHeight()+padding*2);
	}
	
	@Override
	public Collection<Integer> getActivationKeys() {
		return Collections.unmodifiableSet(activationKeys);
	}
	
	@Override
	public ITexture getNormalTexture() {
		return normalTexture;
	}
	
	@Override
	public ITexture getRolloverTexture() {
		return rolloverTexture;
	}
	
	@Override
	public ITexture getPressedTexture() {
		return pressedTexture;
	}
	
	@Override
	public ITexture getPressedRolloverTexture() {
		return pressedRolloverTexture;
	}
	
	@Override
	public ITexture getDisabledTexture() {
		return disabledTexture;
	}
	
	@Override
	public ITexture getDisabledPressedTexture() {
		return disabledPressedTexture;
	}
	
	@Override
	public boolean isSelected() {
		return selected;
	}
	
	@Override
	public boolean isToggle() {
		return toggle;
	}
	
	@Override
	public boolean isKeyboardFocus() {
		return keyboardFocus;
	}
	
	@Override
	public double getTextWidth() {
		return textRenderer.getTextWidth();
	}
	
	@Override
	public double getTextHeight() {
		return textRenderer.getTextHeight();
	}
	
	//Setters
	@Override
	public void setText(String s) {
		setText(new StyledText(s));
	}
	
	@Override
	public void setText(StyledText st) {
		if (!stext.equals(st)) {
			stext = st;
			textRenderer.setText(stext);
			markChanged();
		}
	}
	
	@Override
	public void setTextAnchor(int a) {
		if (textAnchor != a) {
			textAnchor = a;
			markChanged();
		}
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
	public void setEnabled(boolean e) {
		if (enabled != e) {
			enabled = e;
			if (!enabled) rollover = false;
			markChanged();
		}
	}
	
	@Override
	public void setTouchMargin(double p) {
		if (touchMargin != p) {
			touchMargin = p;
			
			markChanged();
			invalidateCollisionShape();			
		}
	}
	
	@Override
	public void setNormalTexture(ITexture tex) {
		if (normalTexture != tex) {
			normalTexture = tex;
			if (getTexture() == null) {
				setTexture(normalTexture);
			}
			markChanged();
		}
	}
	
	@Override
	public void setRolloverTexture(ITexture tex) {
		if (rolloverTexture != tex) {
			rolloverTexture = tex;
			markChanged();
		}
	}
	
	@Override
	public void setPressedTexture(ITexture tex) {
		if (pressedTexture != tex) {
			pressedTexture = tex;
			markChanged();
		}
	}
	
	@Override
	public void setPressedRolloverTexture(ITexture tex) {
		if (pressedRolloverTexture != tex) {
			pressedRolloverTexture = tex;
			markChanged();
		}
	}
	
	@Override
	public void setDisabledTexture(ITexture tex) {
		if (disabledTexture != tex) {
			disabledTexture = tex;
			markChanged();
		}
	}
	
	@Override
	public void setDisabledPressedTexture(ITexture tex) {
		if (disabledPressedTexture != tex) {
			disabledPressedTexture = tex;
			markChanged();
		}
	}
	
	@Override
	public void setSelected(boolean s) {
		if (selected != s) {
			selected = s;
			markChanged();
		}
	}
	
	@Override
	public void setToggle(boolean t) {
		if (toggle != t) {
			toggle = t;
			markChanged();
		}
	}
	
	@Override
	public void setKeyboardFocus(boolean f) {
		if (keyboardFocus != f) {
			keyboardFocus = f;
			if (!keyboardFocus) {
				keyArmed = false;
			}
			markChanged();
		}
	}
	
	@Override
	public void setRenderEnv(RenderEnv env) {
		super.setRenderEnv(env);
		textRenderer.setRenderEnv(env);
	}
	
}
