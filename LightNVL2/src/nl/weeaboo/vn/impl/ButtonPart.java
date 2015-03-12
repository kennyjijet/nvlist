package nl.weeaboo.vn.impl;

import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

import nl.weeaboo.vn.entity.IButtonPart;
import nl.weeaboo.vn.script.IScriptFunction;

public class ButtonPart extends NovelPart implements IButtonPart {

    private static final long serialVersionUID = BaseImpl.serialVersionUID;

    private final ChangeHelper changeHelper = new ChangeHelper();

    private boolean rollover;
    private boolean keyArmed, mouseArmed;
    private boolean enabled = true;
    private boolean selected;
    private boolean toggle;
    private boolean keyboardFocus;
    private int pressEvents;
    private Set<Integer> activationKeys = new HashSet<Integer>();
    private double alphaEnableThreshold = 0.9;

    private IScriptFunction clickHandler;

    public ButtonPart() {
    }

    //Functions
    protected final void fireChanged() {
        changeHelper.fireChanged();
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

    /*
    public boolean handleInput() {
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

        eventHandler.addEvent(clickHandler);
    }
    */

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
        pressEvents = 0;

        if (consumed) {
            fireChanged();
        }

        return consumed;
    }

    /*
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
        if (!activationKeys.isEmpty()) {
            for (Integer key : activationKeys) {
                if (input.consumeKey(key)) {
                    mouseArmed = false;
                    keyArmed = true;
                    markChanged();
                    return;
                }
            }
        }
    }
    */

    //Getters
    /*
    protected boolean isInputHeld(IInput input) {
        if (input.isMouseHeld(true)) {
            return true;
        }
        if (keyboardFocus && input.isConfirmHeld()) {
            return true;
        }
        if (!activationKeys.isEmpty()) {
            for (Integer key : activationKeys) {
                if (input.isKeyHeld(key, true)) {
                    return true;
                }
            }
        }
        return false;
    }
    */

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
    public Collection<Integer> getActivationKeys() {
        return Collections.unmodifiableSet(activationKeys);
    }

    @Override
    public IScriptFunction getClickHandler() {
        return clickHandler;
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
    public double getAlphaEnableThreshold() {
        return alphaEnableThreshold;
    }

    //Setters
    @Override
    public void setEnabled(boolean e) {
        if (enabled != e) {
            enabled = e;
            if (!enabled) {
                rollover = false;
            }
            fireChanged();
        }
    }

    @Override
    public void setSelected(boolean s) {
        if (selected != s) {
            selected = s;
            fireChanged();
        }
    }

    @Override
    public void setToggle(boolean t) {
        if (toggle != t) {
            toggle = t;
            fireChanged();
        }
    }

    @Override
    public void setKeyboardFocus(boolean f) {
        if (keyboardFocus != f) {
            keyboardFocus = f;
            if (!keyboardFocus) {
                keyArmed = false;
            }
            fireChanged();
        }
    }

    @Override
    public void setAlphaEnableThreshold(double ae) {
        if (alphaEnableThreshold != ae) {
            alphaEnableThreshold = ae;
            fireChanged();
        }
    }

    @Override
    public void setClickHandler(IScriptFunction func) {
        if (clickHandler != func) {
            clickHandler = func;
            fireChanged();
        }
    }

    /**
     * @see ChangeHelper#setChangeListener(IChangeListener)
     */
    void setChangeListener(IChangeListener cl) {
        changeHelper.setChangeListener(cl);
    }

}
