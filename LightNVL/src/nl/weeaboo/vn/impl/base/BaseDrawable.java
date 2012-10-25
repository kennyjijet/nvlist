package nl.weeaboo.vn.impl.base;

import java.io.IOException;
import java.io.ObjectInputStream;

import nl.weeaboo.common.Rect2D;
import nl.weeaboo.vn.BlendMode;
import nl.weeaboo.vn.IDrawable;
import nl.weeaboo.vn.IInput;
import nl.weeaboo.vn.ILayer;
import nl.weeaboo.vn.IPixelShader;
import nl.weeaboo.vn.math.Matrix;

public abstract class BaseDrawable implements IDrawable {

	private static final long serialVersionUID = BaseImpl.serialVersionUID;
	
	private boolean changed;	
	private boolean destroyed;
	private boolean visible;
	private boolean clipEnabled;
	private double x, y;
	private short z;
	private double rgba[] = {1.0, 1.0, 1.0, 1.0};
	private BlendMode blendMode;
	private IPixelShader pixelShader;

	private transient int colorARGBInt;
	private transient Matrix transform;
	
	public BaseDrawable() {
		blendMode = BlendMode.DEFAULT;
		visible = true;
		clipEnabled = true;
		
		initTransients();
	}
	
	//Functions
	private void initTransients() {
		colorARGBInt = BaseImpl.packRGBAtoARGB(rgba[0], rgba[1], rgba[2], rgba[3]);
	}
	
	private void readObject(ObjectInputStream in) throws IOException, ClassNotFoundException {
		in.defaultReadObject();

		initTransients();
	}
	
	@Override
	public void destroy() {
		destroyed = true;
		markChanged();
	}
	
	@Override
	public boolean update(ILayer layer, IInput input, double effectSpeed) {
		if (pixelShader != null) {
			if (pixelShader.update(effectSpeed)) {
				markChanged();
			}
		}
		return consumeChanged();
	}
	
	protected void invalidateTransform() {
		transform = null;
	}
	
	protected void markChanged() {
		changed = true;
	}
	
	protected boolean consumeChanged() {
		boolean result = changed;
		changed = false;
		return result;
	}
	
	//Getters
	@Override
	public final boolean isDestroyed() {
		return destroyed;
	}

	@Override
	public final boolean isVisible() {
		return isVisible(0);
	}
	
	@Override
	public boolean isVisible(double minAlpha) {
		return visible && getAlpha() >= minAlpha;
	}
	
	@Override
	public final double getX() {
		return x;
	}

	@Override
	public final double getY() {
		return y;
	}

	@Override
	public final short getZ() {
		return z;
	}
	
	@Override
	public final int getColorRGB() {
		return getColorARGB() & 0xFFFFFF;
	}
	
	@Override
	public final int getColorARGB() {
		return colorARGBInt;
	}

	@Override
	public final double getRed() {
		return rgba[0];
	}

	@Override
	public final double getGreen() {
		return rgba[1];
	}

	@Override
	public final double getBlue() {
		return rgba[2];
	}
	
	@Override
	public final double getAlpha() {
		return rgba[3];
	}
	
	@Override
	public final BlendMode getBlendMode() {
		return blendMode;
	}
	
	@Override
	public final boolean isClipEnabled() {
		return clipEnabled;
	}
	
	@Override
	public final IPixelShader getPixelShader() {
		return pixelShader;
	}

	protected Matrix createTransform() {
		return Matrix.translationMatrix(x, y);
	}
	
	public final Matrix getTransform() {
		if (transform == null) {
			transform = createTransform();
		}
		return transform;
	}
	
	@Override
	public Rect2D getBounds() {
		return new Rect2D(x, y, getWidth(), getHeight());
	}
	
	@Override
	public boolean contains(double px, double py) {
		return getBounds().contains(px, py);
	}
	
	//Setters
	@Override
	public final void setX(double x) {
		setPos(x, y);
	}
	
	@Override
	public final void setY(double y) {
		setPos(x, y);
	}
	
	@Override
	public void setZ(short z) {
		if (this.z != z) {
			this.z = z;
			
			markChanged();
		}
	}
	
	@Override
	public final void setWidth(double w) {
		setSize(w, getHeight());
	}
	
	@Override
	public final void setHeight(double h) {
		setSize(getWidth(), h);
	}

	@Override
	public void setPos(double x, double y) {
		if (this.x != x || this.y != y) {
			this.x = x;
			this.y = y;
			
			markChanged();
			invalidateTransform();
		}
	}
	
	@Override
	public final void setColor(double r, double g, double b) {
		setColor(r, g, b, rgba[3]);
	}
	
	@Override
	public void setColor(double r, double g, double b, double a) {
		if (rgba[0] != r || rgba[1] != g || rgba[2] != b || rgba[3] != a) {
			rgba[0] = r;
			rgba[1] = g;
			rgba[2] = b;
			rgba[3] = a;
			colorARGBInt = BaseImpl.packRGBAtoARGB(rgba[0], rgba[1], rgba[2], rgba[3]);
			
			markChanged();
		}
	}

	@Override
	public final void setColorRGB(int rgb) {
		int ri = (rgb>>16)&0xFF;
		int gi = (rgb>> 8)&0xFF;
		int bi = (rgb    )&0xFF;
		
		setColor(Math.max(0, Math.min(1, ri/255.0)),
				Math.max(0, Math.min(1, gi/255.0)),
				Math.max(0, Math.min(1, bi/255.0)));
	}
	
	@Override
	public final void setColorARGB(int argb) {
		int ai = (argb>>24)&0xFF;
		int ri = (argb>>16)&0xFF;
		int gi = (argb>> 8)&0xFF;
		int bi = (argb    )&0xFF;
		
		setColor(Math.max(0, Math.min(1, ri/255.0)),
				Math.max(0, Math.min(1, gi/255.0)),
				Math.max(0, Math.min(1, bi/255.0)),
				Math.max(0, Math.min(1, ai/255.0)));
	}
	
	@Override
	public final void setAlpha(double a) {
		setColor(rgba[0], rgba[1], rgba[2], a);
	}
	
	@Override
	public void setVisible(boolean v) {
		if (visible != v) {
			visible = v;
			markChanged();
		}
	}

	@Override
	public void setBlendMode(BlendMode mode) {
		if (mode == null) throw new IllegalArgumentException("BlendMode must not be null");
		
		if (blendMode != mode) {
			blendMode = mode;
			
			markChanged();
		}
	}
	
	@Override
	public void setClipEnabled(boolean clip) {
		if (clipEnabled != clip) {
			clipEnabled = clip;
			
			markChanged();
		}
	}
	
	@Override
	public void setPixelShader(IPixelShader ps) {
		if (pixelShader != ps) {
			pixelShader = ps;
			
			markChanged();
		}
	}
	
}
