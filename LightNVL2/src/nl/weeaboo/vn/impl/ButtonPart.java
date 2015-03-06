package nl.weeaboo.vn.impl;


/*
public class ButtonPart extends Part implements IButtonPart {

    private static final long serialVersionUID = BaseImpl.serialVersionUID;

    private static final TextStyle DEFAULT_STYLE;

    static {
        MutableTextStyle mts = new MutableTextStyle();
        mts.setAnchor(5);
        DEFAULT_STYLE = mts.immutableCopy();
    }

    private final ITextRenderer textRenderer;

    private boolean rollover;
    private boolean keyArmed, mouseArmed;
    private boolean enabled = true;
    private boolean selected;
    private boolean toggle;
    private boolean keyboardFocus;
    private int pressEvents;
    private double touchMargin;
    private Set<Integer> activationKeys = new HashSet<Integer>();
    private ITexture normalTexture;
    private ITexture rolloverTexture;
    private ITexture pressedTexture;
    private ITexture pressedRolloverTexture;
    private ITexture disabledTexture;
    private ITexture disabledPressedTexture;
    private double alphaEnableThreshold = 0.9;
    private LuaFunction clickHandler;

    private StyledText stext = StyledText.EMPTY_STRING;
    private TextStyle defaultStyle = DEFAULT_STYLE;
    private double verticalAlign = 0.5;

    private boolean changed;

    public ButtonPart(ITextRenderer tr) {
        tr.setDefaultStyle(DEFAULT_STYLE);
        textRenderer = tr;

        TODO: Needs an imagePart that does the rendering?
    }

    //Functions
    @Override
    public void onDetached(World w) {
        super.onDetached(w);

        textRenderer.destroy();
    }

    protected void markChanged() {
        changed = true;
    }

    protected boolean consumeChanged() {
        boolean result = changed;
        changed = false;
        return result;
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

        eventHandler.addEvent(clickHandler);
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

    @Override
    public void extendDefaultStyle(TextStyle style) {
        setDefaultStyle(getDefaultStyle().extend(style));
    }

    //Getters
    protected void getTextRendererAbsoluteXY(Vec2 out) {
        getTextRendererXY(out);
        out.x += getX() + touchMargin;
        out.y += getY() + touchMargin;
    }
    protected void getTextRendererXY(Vec2 out) {
        LayoutUtil.getTextRendererXY(out, getWidth(), getHeight(), textRenderer, verticalAlign);
    }

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

    @Override
    public double getAlphaEnableThreshold() {
        return alphaEnableThreshold;
    }

    //Setters
    @Override
    public void setText(String s) {
        setText(new StyledText(s != null ? s : ""));
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
    @Deprecated
    public void setTextAnchor(int a) {
        if (a >= 7 && a <= 9) {
            setVerticalAlign(0);
        } else if (a >= 4 && a <= 6) {
            setVerticalAlign(.5);
        } else {
            setVerticalAlign(1);
        }
    }

    @Override
    public void setVerticalAlign(double valign) {
        if (verticalAlign != valign) {
             verticalAlign = valign;
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
    public void setAlphaEnableThreshold(double ae) {
        if (alphaEnableThreshold != ae) {
            alphaEnableThreshold = ae;
            markChanged();
        }
    }

    @Override
    public void setRenderEnv(RenderEnv env) {
        super.setRenderEnv(env);
        textRenderer.setRenderEnv(env);
    }

    public void setClickHandler(LuaFunction func) {
        clickHandler = func;
    }

}
*/