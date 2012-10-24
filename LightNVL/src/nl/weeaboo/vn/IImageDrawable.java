package nl.weeaboo.vn;

import nl.weeaboo.vn.math.Matrix;

public interface IImageDrawable extends IDrawable {
	
	// === Functions ===========================================================
	
	// === Getters =============================================================
	public ITexture getTexture();	
	public IGeometryShader getGeometryShader();
	public double getUnscaledWidth();
	public double getUnscaledHeight();
	public Matrix getTransform();
	public Matrix getBaseTransform();
	public double getRotation();
	public double getScaleX();
	public double getScaleY();
	public double getImageAlignX();
	public double getImageAlignY();
	public double getImageOffsetX();
	public double getImageOffsetY();
	public boolean contains(double cx, double cy);
	
	// === Setters =============================================================
	public void setTexture(ITexture i);
	public void setTexture(ITexture i, int anchor);
	public void setTexture(ITexture i, double imageAlignX, double imageAlignY);
	public void setTween(IImageTween t);
	public void setGeometryShader(IGeometryShader gs);
	public void setBaseTransform(Matrix transform);
	
	/**
	 * @param rot The new rotation between <code>0</code> and <code>512</code>
	 */
	public void setRotation(double rot);
	
	public void setScale(double s);
	public void setScale(double sx, double sy);
	public void setImageAlign(double xFrac, double yFrac);
	public void setSize(double w, double h);
	public void setBounds(double x, double y, double w, double h);
	
}
