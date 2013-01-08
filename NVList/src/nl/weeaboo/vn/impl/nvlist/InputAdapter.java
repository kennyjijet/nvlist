package nl.weeaboo.vn.impl.nvlist;

import java.awt.event.MouseEvent;

import nl.weeaboo.game.input.UserInput;
import nl.weeaboo.game.input.VKey;
import nl.weeaboo.io.EnvironmentSerializable;
import nl.weeaboo.lua2.io.LuaSerializable;
import nl.weeaboo.vn.IInput;

@LuaSerializable
public class InputAdapter extends EnvironmentSerializable implements IInput {

	private final UserInput input;
	
	public InputAdapter(UserInput i) {
		input = i;
	}
	
	@Override
	public void translate(double dx, double dy) {
		input.setMousePos(input.getMouseX()+dx, input.getMouseY()+dy);
	}
	
	@Override
	public boolean consumeKey(int keycode) {
		return input.consumeKey(keycode);
	}

	@Override
	public boolean isKeyHeld(int keycode, boolean allowConsumedPress) {
		return input.isKeyHeld(keycode, allowConsumedPress);
	}

	@Override
	public long getKeyHeldTime(int keycode, boolean allowConsumedPress) {
		return input.getKeyHeldTime(keycode, allowConsumedPress);
	}
	
	@Override
	public boolean isKeyPressed(int keycode) {
		return input.isKeyPressed(keycode);
	}

	@Override
	public double getMouseX() {
		return input.getMouseX();
	}

	@Override
	public double getMouseY() {
		return input.getMouseY();
	}

	@Override
	public boolean consumeMouse() {
		return input.consumeMouse();
	}
	
	protected boolean consumeMouseScroll(int dir) {
		return input.consumeMouseScroll(dir);
	}

	@Override
	public boolean isMouseHeld(boolean allowConsumedPress) {
		return input.isMouseHeld(allowConsumedPress);
	}

	@Override
	public long getMouseHeldTime(boolean allowConsumedPress) {
		return input.getMouseHeldTime(allowConsumedPress);
	}
	
	@Override
	public boolean isMousePressed() {
		return input.isMousePressed();
	}
	
	@Override
	public int getMouseScroll() {
		return input.getMouseScroll();
	}
	
	@Override
	public boolean consumeUp() {
		return consumeKey(VKey.UP.toKeyCode(1));
	}

	@Override
	public boolean consumeDown() {
		return consumeKey(VKey.DOWN.toKeyCode(1));
	}

	@Override
	public boolean consumeLeft() {
		return consumeKey(VKey.LEFT.toKeyCode(1));
	}

	@Override
	public boolean consumeRight() {
		return consumeKey(VKey.RIGHT.toKeyCode(1));
	}
	
	@Override
	public boolean isConfirmHeld() {
		return isKeyHeld(VKey.BUTTON2.toKeyCode(1), false);
	}
	
	@Override
	public boolean consumeConfirm() {
		return consumeKey(VKey.BUTTON2.toKeyCode(1));
	}

	@Override
	public boolean isCancelHeld() {
		return input.isMouseHeld(MouseEvent.BUTTON3, false)
			|| isKeyHeld(VKey.BUTTON3.toKeyCode(1), false);
	}
	
	@Override
	public boolean consumeCancel() {
		return input.consumeMouse(MouseEvent.BUTTON3)
			|| consumeKey(VKey.BUTTON3.toKeyCode(1));
	}
	
	@Override
	public boolean consumeTextLog() {
		return consumeMouseScroll(-1)
			|| consumeLeft()
			|| consumeUp();
	}
	
	@Override
	public boolean consumeTextContinue() {
		return consumeMouse()
			|| consumeMouseScroll(1)
			|| consumeRight()
			|| consumeDown()
			|| consumeKey(VKey.BUTTON2.toKeyCode(1));
	}

	@Override
	public boolean consumeEffectSkip() {
		return consumeTextContinue();
	}

	@Override
	public boolean consumeViewCG() {
		return consumeKey(VKey.BUTTON4.toKeyCode(1));
	}
	
	@Override
	public boolean consumeLoadScreen() {
		return consumeKey(VKey.BUTTON5.toKeyCode(1));
	}
	
	@Override
	public boolean consumeSaveScreen() {
		return consumeKey(VKey.BUTTON6.toKeyCode(1));
	}
	
	@Override
	public boolean isUpHeld() {
		return isKeyHeld(VKey.UP.toKeyCode(1), false);
	}

	@Override
	public boolean isDownHeld() {
		return isKeyHeld(VKey.DOWN.toKeyCode(1), false);
	}

	@Override
	public boolean isLeftHeld() {
		return isKeyHeld(VKey.LEFT.toKeyCode(1), false);
	}

	@Override
	public boolean isRightHeld() {
		return isKeyHeld(VKey.RIGHT.toKeyCode(1), false);
	}
	
	@Override
	public boolean isQuickRead() {
		return getMouseHeldTime(false) > 1000
			|| getKeyHeldTime(VKey.BUTTON2.toKeyCode(1), false) > 1500
			|| isKeyHeld(VKey.BUTTON1.toKeyCode(1), false);
	}
	
	@Override
	public boolean isQuickReadAlt() {
		if (!isQuickRead()) return false;
		return isKeyHeld(VKey.BUTTON7.toKeyCode(1), false);
	}
	
	@Override
	public boolean isIdle() {
		return input.isIdle();
	}
	
	@Override
	public boolean isEnabled() {
		return input.isEnabled();
	}

	@Override
	public void setEnabled(boolean e) {
		input.setEnabled(e);
	}
	
}
