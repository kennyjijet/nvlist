package nl.weeaboo.vn;

import nl.weeaboo.common.Rect2D;
import nl.weeaboo.vn.layout.ILayoutComponent;


public interface IViewport extends IContainer {

	// === Functions ===========================================================
	
	/**
	 * Scrolls the viewport such that <code>d</code> comes fully into view.
	 */
	public void scrollToVisible(IDrawable d);
	
	/**
	 * Scrolls the viewport such that <code>lc</code> comes fully into view.
	 */
	public void scrollToVisible(ILayoutComponent lc);
	
	/**
	 * Scrolls the viewport such that <code>(rx, ry, rw, rh)</code> comes fully
	 * into view.
	 */
	public void scrollToVisible(double rx, double ry, double rw, double rh);
	
	// === Getters =============================================================
	
	/**
	 * @return The layer used to hold the drawables contained within the
	 *         scrollable area of this viewport.
	 */
	public ILayer getLayer();
	
	/**
	 * @see #setFadingEdges(double, int, ITexture, ITexture)
	 */
	public double getFadeSize();
	
	/**
	 * @see #setFadingEdges(double, int, ITexture, ITexture)
	 */
	public int getFadeColorRGB();
	
	/**
	 * @see #setFadingEdges(double, int, ITexture, ITexture)
	 */
	public ITexture getFadeTop();
	
	/**
	 * @see #setFadingEdges(double, int, ITexture, ITexture)
	 */
	public ITexture getFadeBottom();
	
	/**
	 * @see #setScroll(double, double)
	 */
	public double getScrollX();
	
	/**
	 * @see #setScroll(double, double)
	 */
	public double getScrollY();
	
	/**
	 * @see #setDragSnap(double) 
	 */
	public double getDragSnap();
	
	/**
	 * @see #setVirtualBounds(double, double, double, double)
	 */
	public Rect2D getVirtualBounds();
	
	public boolean canScrollY();	
	public boolean canScrollX();
	public boolean hasScrollBarX();
	public boolean hasScrollBarY();
	
	// === Setters =============================================================
	
	/**
	 * Explicitly sets the bounds of the scrollable portion of the viewport.
	 */
	public void setVirtualBounds(double x, double y, double w, double h);
	
	/**
	 * Sets the texture used for the fading effect at the top/bottom, indicating
	 * when content sticks outside the visible area in that direction.
	 * 
	 * @param size The maximum height for the vertical fading edges.
	 * @param colorRGB The RGB color used to tint the fading edges, use
	 *        <code>0xFFFFFF</code> for no tinting.
	 * @param top The texture to use for the top fading edge, or
	 *        <code>null</code> if you don't want a top fading edge.
	 * @param bottom The texture to use for the bottom fading edge, or
	 *        <code>null</code> if you don't want a bottom fading edge.
	 */
	public void setFadingEdges(double size, int colorRGB, ITexture top, ITexture bottom);

	/**
	 * Sets the scroll offset.
	 * 
	 * @param sx Scroll amount in the x direction.
	 * @param sy Scroll amount in the y direction.
	 */
	public void setScroll(double sx, double sy);
	
	/**
	 * Sets the scroll offset as a fraction of the maximum range.
	 * 
	 * @param fracX Scroll fraction (<code>0.0</code>-<code>1.0</code>) in the x
	 *        direction.
	 * @param fracY Scroll fraction (<code>0.0</code>-<code>1.0</code>) in the y
	 *        direction.
	 */
	public void setScrollFrac(double fracX, double fracY);
	
	/**
	 * Determines the snapback speed (between <code>0.0</code> and
	 * <code>1.0</code>) when letting go after dragging the visible area outside
	 * the virtual bounds. When set to <code>1.0</code>, disallows dragging
	 * outside the virtual bounds entirely.
	 */
	public void setDragSnap(double ds);

	/**
	 * Enables a horizontal scroll bar.
	 * 
	 * @param height The desired scroll bar height.
	 * @param bar Texture to use for the scroll bar background.
	 * @param thumb Texture to use for the scroll bar thumb (position indicator).
	 * @param marginTop Size of the empty margin around the scroll bar.
	 * @param marginRight Size of the empty margin around the scroll bar.
	 * @param marginBottom Size of the empty margin around the scroll bar.
	 * @param marginLeft Size of the empty margin around the scroll bar.
	 */
	public void setScrollBarX(double height, ITexture bar, ITexture thumb,
			double marginTop, double marginRight, double marginBottom, double marginLeft);
	
	/**
	 * Enables a vertical scroll bar.
	 * 
	 * @param width The desired scroll bar width.
	 * @param bar Texture to use for the scroll bar background.
	 * @param thumb Texture to use for the scroll bar thumb (position indicator).
	 * @param marginTop Size of the empty margin around the scroll bar.
	 * @param marginRight Size of the empty margin around the scroll bar.
	 * @param marginBottom Size of the empty margin around the scroll bar.
	 * @param marginLeft Size of the empty margin around the scroll bar.
	 */
	public void setScrollBarY(double width, ITexture bar, ITexture thumb,
			double marginTop, double marginRight, double marginBottom, double marginLeft);
	
}
