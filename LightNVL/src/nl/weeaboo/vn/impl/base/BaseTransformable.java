package nl.weeaboo.vn.impl.base;

import nl.weeaboo.common.Rect2D;
import nl.weeaboo.lua2.io.LuaSerializable;
import nl.weeaboo.vn.ITransformable;
import nl.weeaboo.vn.layout.LayoutUtil;
import nl.weeaboo.vn.math.IPolygon;
import nl.weeaboo.vn.math.Matrix;
import nl.weeaboo.vn.math.MutableMatrix;
import nl.weeaboo.vn.math.Polygon;

@LuaSerializable
public abstract class BaseTransformable extends BaseDrawable implements ITransformable {

	private static final long serialVersionUID = BaseImpl.serialVersionUID;
	
	private double rotation;
	private double scaleX, scaleY;
	private double imageAlignX, imageAlignY;
	private double unscaledWidth, unscaledHeight;
	private Matrix baseTransform;

	private transient IPolygon collisionShape;
	private transient Rect2D _bounds;

	public BaseTransformable() {
		scaleX = scaleY = 1;
		baseTransform = Matrix.identityMatrix();		
	}
	
	//Functions
	@Override
	protected Matrix createTransform() {
		MutableMatrix m = baseTransform.mutableCopy();
		m.translate(getX(), getY());
		m.scale(getScaleX(), getScaleY());
		m.rotate(getRotation());
		return m.immutableCopy();
	}
	
	protected IPolygon createCollisionShape() {
		Matrix transform = getTransform();
		double dx = getAlignOffsetX();
		double dy = getAlignOffsetY();
		if (dx != 0 || dy != 0) {
			MutableMatrix mm = transform.mutableCopy();
			mm.translate(dx, dy);
			transform = mm.immutableCopy();
		}
		return new Polygon(transform, 0, 0, getUnscaledWidth(), getUnscaledHeight());
	}
	
	protected void invalidateCollisionShape() {
		collisionShape = null;
	}
	
	@Override
	protected void invalidateTransform() {
		super.invalidateTransform();
		
		invalidateBounds();
		invalidateCollisionShape();
	}
	
	protected void invalidateBounds() {
		_bounds = null;
	}
	
	//Getters
	@Override
	public double getUnscaledWidth() {
		return unscaledWidth;
	}
	
	@Override
	public double getUnscaledHeight() {
		return unscaledHeight;
	}
	
	@Override
	public double getWidth() {
		return getScaleX() * getUnscaledWidth();
	}

	@Override
	public double getHeight() {
		return getScaleY() * getUnscaledHeight();
	}
	
	@Override
	public double getRotation() {
		return rotation;
	}
	
	@Override
	public double getScaleX() {
		return scaleX;
	}
	
	@Override
	public double getScaleY() {
		return scaleY;
	}
	
	@Override
	public double getAlignX() {
		return imageAlignX;
	}
	
	@Override
	public double getAlignY() {
		return imageAlignY;
	}
	
	@Override
	public final double getAlignOffsetX() {
		return LayoutUtil.getOffset(getUnscaledWidth(), getAlignX());
	}
	
	@Override
	public final double getAlignOffsetY() {
		return LayoutUtil.getOffset(getUnscaledHeight(), getAlignY());
	}
	
	@Override
	public Matrix getBaseTransform() {
		return baseTransform;
	}
	
	@Override
	public Rect2D getBounds() {
		if (_bounds == null) {
			float xa = (float)getAlignOffsetX();
			float xb = xa + (float)getUnscaledWidth();
			float ya = (float)getAlignOffsetY();
			float yb = ya + (float)getUnscaledHeight();
			
			Matrix transform = getTransform();
			float[] coords = new float[] {xa, ya, xb, ya, xa, yb, xb, yb};
			transform.transform(coords, 0, coords.length);
			
			xa = Float.POSITIVE_INFINITY;
			xb = Float.NEGATIVE_INFINITY;
			ya = Float.POSITIVE_INFINITY;
			yb = Float.NEGATIVE_INFINITY;
			for (int n = 0; n < coords.length; n+=2) {
				xa = Math.min(xa, coords[n  ]);
				xb = Math.max(xb, coords[n  ]);
				ya = Math.min(ya, coords[n+1]);
				yb = Math.max(yb, coords[n+1]);
			}
			
			double w = xb-xa;
			double h = yb-ya;
			_bounds = new Rect2D(xa, ya, Double.isNaN(w) ? 0 : w, Double.isNaN(h) ? 0 : h);
		}
		return _bounds;
	}
	
	@Override
	public boolean contains(double cx, double cy) {
		IPolygon p = getCollisionShape();
		if (p == null) {
			return false;
		}
		
		//System.out.println(p.getBoundingRect() + " " + p.contains(cx, cy));
		
		return p.contains(cx, cy);
	}
	
	protected final IPolygon getCollisionShape() {
		if (collisionShape == null) {
			collisionShape = createCollisionShape();
		}
		return collisionShape;
	}
	
	//Setters
	public void setBaseTransform(MutableMatrix transform) {
		setBaseTransform(transform.immutableCopy());
	}
	
	@Override
	public void setBaseTransform(Matrix transform) {
		if (!baseTransform.equals(transform)) {
			baseTransform = transform;
			
			markChanged();
			invalidateTransform();
		}
	}

	@Override
	public void setRotation(double rot) {
		if (rotation != rot) {
			rotation = rot;
			
			markChanged();
			invalidateTransform();
		}
	}
	
	@Override
	public final void setScale(double s) {
		setScale(s, s);
	}
	
	@Override
	public void setScale(double sx, double sy) {
		if (scaleX != sx || scaleY != sy) {
			scaleX = sx;
			scaleY = sy;
			
			markChanged();
			invalidateTransform();
		}
	}
	
	@Override
	public void setSize(double w, double h) {
		setScale(w / getUnscaledWidth(), h / getUnscaledHeight());
	}
	
	protected void setUnscaledSize(double w, double h) {
		if (unscaledWidth != w || unscaledHeight != h) {
			unscaledWidth = w;
			unscaledHeight = h;
			markChanged();
			invalidateBounds();
		}
	}
	

	@Override
	public void setBounds(double x, double y, double w, double h) {
		setAlign(0, 0);
		setRotation(0);
		setPos(x, y);
		setSize(w, h);
	}
	
	@Override
	public void setAlign(double xFrac, double yFrac) {
		if (imageAlignX != xFrac || imageAlignY != yFrac) {
			imageAlignX = xFrac;
			imageAlignY = yFrac;
						
			markChanged();
			invalidateBounds();
		}
	}
	
}
