package nl.weeaboo.vn;

public class EmptyInput implements IInput {

	private static EmptyInput instance;
	
	private EmptyInput() {		
	}
	
	//Functions
	public static synchronized EmptyInput getInstance() {
		if (instance == null) {
			instance = new EmptyInput();
		}
		return instance;
	}
	
	@Override
	public void translate(double dx, double dy) {
		//Does nothing, mouse X/Y is always -1
	}
	
	@Override
	public boolean isIdle() {
		return true;
	}
	
	//Getters
	@Override
	public boolean consumeKey(int keycode) {
		return false;
	}

	@Override
	public boolean isKeyHeld(int keycode) {
		return false;
	}

	@Override
	public long getKeyHeldTime(int keycode) {
		return 0;
	}

	@Override
	public boolean isKeyPressed(int keycode) {
		return false;
	}

	@Override
	public double getMouseX() {
		return -1;
	}

	@Override
	public double getMouseY() {
		return -1;
	}

	@Override
	public boolean consumeMouse() {
		return false;
	}

	@Override
	public boolean isMouseHeld() {
		return false;
	}

	@Override
	public long getMouseHeldTime() {
		return 0;
	}

	@Override
	public boolean isMousePressed() {
		return false;
	}

	@Override
	public boolean consumeConfirm() {
		return false;
	}
	
	@Override
	public boolean consumeCancel() {
		return false;
	}
	
	@Override
	public boolean consumeTextContinue() {
		return false;
	}

	@Override
	public boolean consumeEffectSkip() {
		return false;
	}

	@Override
	public boolean consumeTextLog() {
		return false;
	}
	
	@Override
	public boolean consumeSaveScreen() {
		return false;
	}

	@Override
	public boolean consumeLoadScreen() {
		return false;
	}
	
	@Override
	public boolean isQuickRead() {
		return false;
	}

	@Override
	public boolean isQuickReadAlt() {
		return false;
	}
	
	@Override
	public boolean consumeViewCG() {
		return false;
	}
	
	@Override
	public boolean isEnabled() {
		return false;
	}
	
	@Override
	public boolean consumeUp() {
		return false;
	}

	@Override
	public boolean consumeDown() {
		return false;
	}

	@Override
	public boolean consumeLeft() {
		return false;
	}

	@Override
	public boolean consumeRight() {
		return false;
	}

	@Override
	public boolean isUpHeld() {
		return false;
	}

	@Override
	public boolean isDownHeld() {
		return false;
	}

	@Override
	public boolean isLeftHeld() {
		return false;
	}

	@Override
	public boolean isRightHeld() {
		return false;
	}
	
	@Override
	public boolean isConfirmHeld() {
		return false;
	}

	@Override
	public boolean isCancelHeld() {
		return false;
	}
	
	@Override
	public int getMouseScroll() {
		return 0;
	}

	//Setters
	@Override
	public void setEnabled(boolean e) {
	}
	
}
