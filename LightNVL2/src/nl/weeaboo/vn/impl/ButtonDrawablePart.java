package nl.weeaboo.vn.impl;


/*
class ButtonDrawablePart extends DrawablePart implements IButtonDrawablePart {

    private static final long serialVersionUID = BaseImpl.serialVersionUID;

    private final IButtonPart model;
    private final ITextRenderer textRenderer;

    private ITexture normalTexture;
    private ITexture rolloverTexture;
    private ITexture pressedTexture;
    private ITexture pressedRolloverTexture;
    private ITexture disabledTexture;
    private ITexture disabledPressedTexture;

    private StyledText stext = StyledText.EMPTY_STRING;
    private TextStyle defaultStyle = RenderUtil.CENTERED_STYLE;
    private double verticalAlign = 0.5;
    private double touchMargin;

    public ButtonDrawablePart(IButtonPart model, ITextRenderer tr) {
        this.model = model;
        this.textRenderer = tr;

        tr.setDefaultStyle(defaultStyle);

        initTransients();
    }

    //Functions
    private void initTransients() {
        model.setChangeListener(new IChangeListener() {
            @Override
            public void onChanged() {
                updateTexture();
            }
        });
    }

    private void readObject(ObjectInputStream in) throws IOException, ClassNotFoundException {
        in.defaultReadObject();

        initTransients();
    }

    @Override
    public void onDetached(World w) {
        super.onDetached(w);

        textRenderer.destroy();
    }

    @Override
    public void update() {
        super.update();

        textRenderer.update();
    }

    protected void updateTexture() {
        TODO: Determine which texture to use, call setTexture
    }

    @Override
    public void draw(IDrawBuffer d) {
        updateTexture();

        TODO: Draw image

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
    public void extendDefaultStyle(TextStyle ts) {
        setDefaultStyle(getDefaultStyle().extend(ts));
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

    @Override
    public StyledText getText() {
        return stext;
    }

    @Override
    public TextStyle getDefaultStyle() {
        return defaultStyle;
    }

    @Override
    public boolean contains(double px, double py) {
        double x = getX() - touchMargin;
        double y = getY() - touchMargin;
        double w = getWidth() + touchMargin * 2;
        double h = getHeight() + touchMargin * 2;
        if (Double.isNaN(w) || Double.isNaN(h)) {
            return false;
        }

        return px >= x && px < x + w && py >= y && py < y + h;
    }

    @Override
    public double getTouchMargin() {
        return touchMargin;
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
    public void setTouchMargin(double p) {
        if (touchMargin != p) {
            touchMargin = p;

            markChanged();
        }
    }

    @Override
    public void setNormalTexture(ITexture tex) {
        if (normalTexture != tex) {
            normalTexture = tex;

            updateTexture();
            markChanged();
        }
    }

    @Override
    public void setRolloverTexture(ITexture tex) {
        if (rolloverTexture != tex) {
            rolloverTexture = tex;

            updateTexture();
            markChanged();
        }
    }

    @Override
    public void setPressedTexture(ITexture tex) {
        if (pressedTexture != tex) {
            pressedTexture = tex;

            updateTexture();
            markChanged();
        }
    }

    @Override
    public void setPressedRolloverTexture(ITexture tex) {
        if (pressedRolloverTexture != tex) {
            pressedRolloverTexture = tex;

            updateTexture();
            markChanged();
        }
    }

    @Override
    public void setDisabledTexture(ITexture tex) {
        if (disabledTexture != tex) {
            disabledTexture = tex;

            updateTexture();
            markChanged();
        }
    }

    @Override
    public void setDisabledPressedTexture(ITexture tex) {
        if (disabledPressedTexture != tex) {
            disabledPressedTexture = tex;

            updateTexture();
            markChanged();
        }
    }

    @Override
    public void setRenderEnv(IRenderEnv env) {
        super.setRenderEnv(env);

        textRenderer.setRenderEnv(env);
    }

}
*/