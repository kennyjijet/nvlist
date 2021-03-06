package nl.weeaboo.vn;

import java.util.Collection;

import nl.weeaboo.common.Insets2D;
import nl.weeaboo.common.Rect2D;
import nl.weeaboo.vn.layout.ILayout;
import nl.weeaboo.vn.layout.ILayoutComponent;
import nl.weeaboo.vn.layout.ILayoutConstraints;

public interface IContainer extends IDrawable {

	// === Functions ===========================================================
	
	/**
	 * Adds <code>d</code> to this panel. 
	 */
	public void add(IDrawable d); //Calls add(lc)
	
	/**
	 * Adds <code>d</code> to this panel with the given layout constraints. 
	 */
	public void add(IDrawable d, ILayoutConstraints c); //Calls add(lc)

	/**
	 * Adds <code>lc</code> to this panel. 
	 */	
	public void add(ILayoutComponent lc);
	
	/**
	 * Removes <code>d</code> from this panel. 
	 */
	public void remove(IDrawable d);
	
	/**
	 * Removes <code>lc</code> from this panel.
	 */
	public void remove(ILayoutComponent lc);
	
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
	
	/**
	 * Calls {@link #layout()} if necessary.
	 */
	public void validateLayout();
	
	// === Getters =============================================================
		
	/**
	 * @see #getLayout() 
	 */
	public ILayout getLayout();
	
	/**
	 * Returns all drawables added to this container. 
	 */
	public IDrawable[] getDrawableComponents(IDrawable[] out);
	
	/**
	 * Returns all layout components added to this container. 
	 */
	public Collection<ILayoutComponent> getLayoutComponents();
	
	/**
	 * Returns if <code>d</code> is currently added to this panel. 
	 */
	public boolean contains(IDrawable d);
	
	/**
	 * Returns if <code>lc</code> is currently added to this panel. 
	 */
	public boolean contains(ILayoutComponent lc);
	
	/**
	 * @see #setPadding(double, double, double, double) 
	 */
	public Insets2D getPadding();
	
	public double getInnerWidth();

	public double getInnerHeight();
	
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
	
}
