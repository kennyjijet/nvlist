package nl.weeaboo.vn;

import nl.weeaboo.common.Insets2D;
import nl.weeaboo.common.Rect2D;
import nl.weeaboo.vn.layout.ILayout;
import nl.weeaboo.vn.layout.ILayoutConstraints;

public interface IPanel extends IDrawable {

	// === Functions ===========================================================
	
	/**
	 * Adds <code>d</code> to this panel. 
	 */
	public void add(IDrawable d); //Calls add(d, c)
	
	/**
	 * Adds <code>d</code> to this panel with the given layout constraints. 
	 */
	public void add(IDrawable d, ILayoutConstraints c);

	/**
	 * Removes <code>d</code> from this panel. 
	 */
	public void remove(IDrawable d);
	
	/**
	 * Performs an immediate relayout of the drawables contained within this
	 * panel.
	 */
	public void layout();

	/**
	 * Marks the layout as invalid. A relayout will be triggered during next
	 * execution of frame's {@link #update(ILayer, IInput, double)} at the
	 * latest.
	 */
	public void invalidateLayout();
	
	// === Getters =============================================================
		
	/**
	 * @see #getLayout() 
	 */
	public ILayout getLayout();
	
	/**
	 * Returns if <code>d</code> is currently added to this panel. 
	 */
	public boolean contains(IDrawable d);
	
	/**
	 * @see #setPadding(double, double, double, double) 
	 */
	public Insets2D getPadding();

	/**
	 * @see #setMargin(double, double, double, double)
	 */
	public Insets2D getMargin();
	
	/**
	 * @return <code>width - padding(left/right) - margin(left/right)</code>
	 */
	public double getInnerWidth();

	/**
	 * @return <code>height - padding(top/bottom) - margin(top/bottom)</code>
	 */
	public double getInnerHeight();
	
	/**
	 * @return <code>(x+margin.left+padding.left, y+margin.top+padding.top, innerWidth, innerHeight)</code>
	 */
	public Rect2D getInnerBounds();
	
	// === Setters =============================================================
	
	/**
	 * Changes the layout algorithm.
	 */
	public void setLayout(ILayout layout);
	
	/**
	 * @see #setPadding(double, double, double, double)
	 */
	public void setPadding(double pad); //Calls setPadding(top, right, bottom, left)

	/**
	 * @see #setPadding(double, double, double, double)
	 */
	public void setPadding(double vertical, double horizontal); //Calls setPadding(top, right, bottom, left)
	
	/**
	 * Changes the padding between the inner size and the border.
	 */
	public void setPadding(double top, double right, double bottom, double left);
	
	/**
	 * @see #setMargin(double, double, double, double)
	 */
	public void setMargin(double pad); //Calls setMargin(top, right, bottom, left)

	/**
	 * @see #setMargin(double, double, double, double)
	 */
	public void setMargin(double vertical, double horizontal); //Calls setMargin(top, right, bottom, left)
	
	/**
	 * Changes the margin between the border and external bounds.
	 */
	public void setMargin(double top, double right, double bottom, double left);
	
}
