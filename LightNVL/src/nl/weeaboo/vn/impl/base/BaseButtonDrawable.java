package nl.weeaboo.vn.impl.base;

import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

import nl.weeaboo.vn.IButtonDrawable;
import nl.weeaboo.vn.IDrawBuffer;
import nl.weeaboo.vn.IInput;
import nl.weeaboo.vn.ILayer;
import nl.weeaboo.vn.ITexture;
import nl.weeaboo.vn.math.IPolygon;
import nl.weeaboo.vn.math.MutableMatrix;
import nl.weeaboo.vn.math.Polygon;

public abstract class BaseButtonDrawable extends BaseImageDrawable implements IButtonDrawable {

	private static final long serialVersionUID = BaseImpl.serialVersionUID;
	
	private final boolean isTouchScreen;
	private boolean rollover;
	private boolean keyArmed, mouseArmed;
	private boolean enabled;	
	private boolean selected;
	private boolean toggle;
	private boolean keyboardFocus;
	private int pressEvents;
	private double padding;
	private Set<Integer> activationKeys;
	private ITexture normalTexture;
	private ITexture rolloverTexture;
	private ITexture pressedTexture;
	private ITexture pressedRolloverTexture;
	private ITexture disabledTexture;
	private ITexture disabledPressedTexture;
	private double alphaEnableThreshold;
	
	protected BaseButtonDrawable(boolean isTouchScreen) {
		this.isTouchScreen = isTouchScreen;
		
		enabled = true;
		activationKeys = new HashSet<Integer>();
		alphaEnableThreshold = 0.9;
	}
	
	//Functions
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
		boolean isDisabled = !isEnabled();
		boolean isPressed = (isPressed() || isSelected());
		boolean isRollover = isRollover() && !isTouchScreen;
		
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
		boolean contains = layer.contains(x, y) && contains(x, y) && visibleEnough;
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
		
		updateTexture();
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
	public double getPadding() {
		return padding;
	}
	
	@Override
	protected IPolygon createCollisionShape() {
		double padding = getPadding();
		
		MutableMatrix mm = getTransform().mutableCopy();
		mm.translate(getAlignOffsetX(), getAlignOffsetY());
		return new Polygon(mm.immutableCopy(), -padding, -padding,
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
	
	//Setters	
	@Override
	public void setEnabled(boolean e) {
		if (enabled != e) {
			enabled = e;
			if (!enabled) rollover = false;
			markChanged();
		}
	}
	
	@Override
	public void setPadding(double p) {
		if (padding != p) {
			padding = p;
			
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
	
}
