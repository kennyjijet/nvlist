package nl.weeaboo.vn.impl.base;

import nl.weeaboo.common.Rect2D;
import nl.weeaboo.vn.IGeometryShader;
import nl.weeaboo.vn.IImageDrawable;
import nl.weeaboo.vn.IImageTween;
import nl.weeaboo.vn.IInput;
import nl.weeaboo.vn.ILayer;
import nl.weeaboo.vn.IRenderer;
import nl.weeaboo.vn.ITexture;
import nl.weeaboo.vn.math.IPolygon;
import nl.weeaboo.vn.math.Matrix;
import nl.weeaboo.vn.math.MutableMatrix;
import nl.weeaboo.vn.math.Polygon;
import nl.weeaboo.vn.math.Vec2;

public abstract class BaseImageDrawable extends BaseDrawable implements IImageDrawable {

	private static final long serialVersionUID = BaseImpl.serialVersionUID;
	
	private IImageTween tween;
	private ITexture image;
	private IGeometryShader geometryShader;
	private double rotation;
	private double scaleX, scaleY;
	private double imageAlignX, imageAlignY;
	private Matrix baseTransform;
	private transient IPolygon collisionShape;
	
	public BaseImageDrawable() {
		scaleX = scaleY = 1;
		baseTransform = Matrix.identityMatrix();
	}
	
	//Functions
	@Override
	public void destroy() {
		super.destroy();
		
		//Allow some memory to be freed
		tween = null;
		image = null;
		geometryShader = null;
	}
	
	@Override
	public boolean update(ILayer layer, IInput input, double effectSpeed) {
		if (super.update(layer, input, effectSpeed)) {
			markChanged();
		}
		
		if (geometryShader != null) {
			if (geometryShader.update(effectSpeed)) {
				markChanged();
			}
		}
		
		if (tween != null) {
			if (tween.update(effectSpeed)) {
				markChanged();
			}
		}
		
		return consumeChanged();
	}
	
	@Override
	public void draw(IRenderer r) {
		if (tween != null && tween.isPrepared() && !tween.isFinished()) {
			tween.draw(r);
		} else {
			r.draw(this);
		}
	}
	
	protected void finishTween() {
		if (tween != null) {
			if (!tween.isFinished()) {
				tween.finish();
				markChanged();
			}
			tween = null;
		}		
	}
	
	@Override
	protected Matrix createTransform() {
		MutableMatrix m = baseTransform.mutableCopy();
		m.translate(getX(), getY());
		m.scale(getScaleX(), getScaleY());
		m.rotate(getRotation());
		return m.immutableCopy();
	}
	
	protected IPolygon createCollisionShape() {
		return new Polygon(getTransform(), 0, 0, getUnscaledWidth(), getUnscaledHeight());
	}
	
	protected void invalidateCollisionShape() {
		collisionShape = null;
	}
	
	@Override
	protected void invalidateTransform() {
		super.invalidateTransform();
		
		invalidateCollisionShape();
	}
	
	//Getters
	@Override
	public ITexture getTexture() {
		if (tween != null) return tween.getEndTexture();

		return image;
	}
	
	@Override
	public double getUnscaledWidth() {
		if (tween != null) return tween.getWidth();
		
		ITexture tex = getTexture();
		return (tex != null ? tex.getWidth() : 0);
	}
	
	@Override
	public double getWidth() {
		return getScaleX() * getUnscaledWidth();
	}

	@Override
	public double getUnscaledHeight() {
		if (tween != null) return tween.getHeight();

		ITexture tex = getTexture();
		return (tex != null ? tex.getHeight() : 0);
	}
	
	@Override
	public double getHeight() {
		return getScaleY() * getUnscaledHeight();
	}
	
	@Override
	public IGeometryShader getGeometryShader() {
		return geometryShader;
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
	public double getImageAlignX() {
		return imageAlignX;
	}
	
	@Override
	public double getImageAlignY() {
		return imageAlignY;
	}
	
	@Override
	public final double getImageOffsetX() {
		return LayoutUtil.getImageOffset(getUnscaledWidth(), getImageAlignX());
	}
	
	@Override
	public final double getImageOffsetY() {
		return LayoutUtil.getImageOffset(getUnscaledHeight(), getImageAlignY());
	}
	
	@Override
	public Matrix getBaseTransform() {
		return baseTransform;
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
	public void setTexture(ITexture i) {
		setTexture(i, 7);
	}
	
	@Override
	public void setTexture(ITexture i, int anchor) {
		double w0 = getUnscaledWidth();
		double h0 = getUnscaledHeight();
		double w1 = (i != null ? i.getWidth() : 0);
		double h1 = (i != null ? i.getHeight() : 0);
		Rect2D rect = LayoutUtil.getBounds(w0, h0, getImageAlignX(), getImageAlignY());			
		Vec2 align = LayoutUtil.alignSubRect(rect, w1, h1, anchor);
		setTexture(i, align.x, align.y);
	}
	
	@Override
	public void setTexture(ITexture i, double imageAlignX, double imageAlignY) {
		finishTween();
		
		if (image != i || getImageAlignX() != imageAlignX || getImageAlignY() != imageAlignY) {			
			image = i;
			setImageAlign(imageAlignX, imageAlignY);
			
			markChanged();
			invalidateCollisionShape();
			//invalidateTransform();
		}
	}
	
	@Override
	public void setTween(IImageTween t) {
		finishTween();
		
		if (tween != t) {
			t.setStartImage(this);

			tween = t;
			
			if (!tween.isPrepared()) {
				tween.prepare();
			}

			markChanged();
			//invalidateTransform();
		}
	}
	
	@Override
	public void setGeometryShader(IGeometryShader gs) {
		if (geometryShader != gs) {
			geometryShader = gs;
			
			markChanged();
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
	public void setScale(double s) {
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
	
	@Override
	public void setBounds(double x, double y, double w, double h) {
		setPos(x, y);
		setSize(w, h);
	}
	
	@Override
	public void setImageAlign(double xFrac, double yFrac) {
		if (imageAlignX != xFrac || imageAlignY != yFrac) {
			imageAlignX = xFrac;
			imageAlignY = yFrac;
			
			markChanged();
			//invalidateTransform();
		}
	}
		
}
