package nl.weeaboo.vn;

import nl.weeaboo.vn.math.Matrix;

public interface ITransformable extends IDrawable {

	// === Functions ===========================================================
	
	// === Getters =============================================================
	
	public double getUnscaledWidth();
	
	public double getUnscaledHeight();
	
	/**
	 * @see #getTransform()
	 */
	public Matrix getBaseTransform();
	
	/**
	 * The transform is obtained by modifying the base transform with this
	 * drawable's X/Y position, scale, rotation. The image align isn't included
	 * in the transform.
	 * 
	 * @return The final transformation matrix used when rendering this
	 *         drawable.
	 * @see #getBaseTransform()
	 */
	public Matrix getTransform();

	/**
	 * @return The current rotation between <code>0</code> and <code>512</code>.
	 */
	public double getRotation();
	
	public double getScaleX();
	public double getScaleY();
	
	public double getAlignX();
	public double getAlignY();
	
	/**
	 * @return {@link #getAlignX()} multiplied by the current width.
	 */
	public double getAlignOffsetX();

	/**
	 * @return {@link #getAlignY()} multiplied by the current height.
	 */
	public double getAlignOffsetY();
	
	// === Setters =============================================================
	
	/**
	 * @see #getTransform()
	 */
	public void setBaseTransform(Matrix transform);
	
	/**
	 * @param rot The new rotation between <code>0</code> and <code>512</code>.
	 */
	public void setRotation(double rot);
	
	public void setScale(double s); //Calls setScale(sx, sy)
	public void setScale(double sx, double sy);
	
	/**
	 * Changes the display position of this drawable relative to its X/Y
	 * coordinates. An x-align value of <code>0.0</code> uses the left,
	 * <code>0.5</code> uses the center and <code>1.0</code> is right-aligned.
	 * 
	 * @param xFrac Relative display position from the X-coordinate.
	 * @param yFrac Relative display position from the Y-coordinate.
	 */
	public void setAlign(double xFrac, double yFrac);
	
}
