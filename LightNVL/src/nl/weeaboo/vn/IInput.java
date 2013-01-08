package nl.weeaboo.vn;

public interface IInput {
	
	/**
	 * Translates the coordinate system by <code>(dx, dy)</code>. Useful for
	 * changing the origin of the mouse position.
	 */
	public void translate(double dx, double dy);
	
	/**
	 * @param keycode The key to check/change the state of
	 * @return Like #isKeyPressed(int), but makes further calls to
	 *         #isKeyPressed(int) and #consumeKey(int) return false.
	 */
	public boolean consumeKey(int keycode);
	
	/**
	 * @param keycode The key to check the state of
	 * @return If the specified key is currently pressed
	 */
	public boolean isKeyHeld(int keycode, boolean allowConsumedPress);

	/**
	 * @return The time in milliseconds that the specified key has been
	 *         continuously held.
	 */
	public long getKeyHeldTime(int keycode, boolean allowConsumedPress);
	
	/**
	 * @param keycode The key to check the state of
	 * @return If the specified key got pressed since the last frame
	 */
	public boolean isKeyPressed(int keycode);

	public double getMouseX();
	public double getMouseY();

	public boolean consumeMouse();
	public boolean isMouseHeld(boolean allowConsumedPress);
	public long getMouseHeldTime(boolean allowConsumedPress);
	public boolean isMousePressed();
	public int getMouseScroll();

	public boolean consumeUp();
	public boolean consumeDown();
	public boolean consumeLeft();
	public boolean consumeRight();
	public boolean consumeConfirm();
	public boolean consumeCancel();
	public boolean consumeTextContinue();
	public boolean consumeEffectSkip();
	public boolean consumeTextLog();
	public boolean consumeViewCG();
	public boolean consumeSaveScreen();
	public boolean consumeLoadScreen();
	
	public boolean isUpHeld();
	public boolean isDownHeld();
	public boolean isLeftHeld();
	public boolean isRightHeld();
	public boolean isQuickRead();
	public boolean isQuickReadAlt();
	public boolean isConfirmHeld();
	public boolean isCancelHeld();
	
	/**
	 * @return <code>true</code> If the user hasn't performed any type of input
	 *         this frame (no button presses, no mouse moves).
	 */
	public boolean isIdle();

	public boolean isEnabled();
	
	public void setEnabled(boolean e);
	
}

