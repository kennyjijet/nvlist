package nl.weeaboo.vn;

import java.util.Collection;

public interface IButtonDrawable extends IDrawable {

	// === Functions ===========================================================
	public boolean consumePress();
	public void addActivationKeys(int... key);
	public void removeActivationKeys(int... key);
	
	// === Getters =============================================================
	public boolean isRollover();
	public boolean isPressed();
	public boolean isEnabled();
	public boolean isSelected();
	public boolean isToggle();
	public boolean isKeyboardFocus();
	public double getPadding();
	public Collection<Integer> getActivationKeys();
	public ITexture getNormalTexture();
	public ITexture getRolloverTexture();
	public ITexture getPressedTexture();
	public ITexture getPressedRolloverTexture();
	public ITexture getDisabledTexture();
	public ITexture getDisabledPressedTexture();
	
	// === Setters =============================================================
	public void setNormalTexture(ITexture i);
	public void setRolloverTexture(ITexture i);
	public void setPressedTexture(ITexture i);
	public void setPressedRolloverTexture(ITexture i);
	public void setDisabledTexture(ITexture i);
	public void setDisabledPressedTexture(ITexture i);
	public void setEnabled(boolean e);
	public void setPadding(double p);
	public void setSelected(boolean s);
	public void setToggle(boolean t);
	public void setKeyboardFocus(boolean f);
	
}
