package nl.weeaboo.vn;

import nl.weeaboo.common.Insets2D;

public interface IPanel extends IContainer {

	// === Functions ===========================================================
	
	
	// === Getters =============================================================
		
	/**
	 * @see #setMargin(double, double, double, double)
	 */
	public Insets2D getMargin();

	/**
	 * @see #setBorderInsets(Insets2D)
	 */
	public Insets2D getBorderInsets();
	
	// === Setters =============================================================
		
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

	/**
	 * Sets the background image. It will be drawn stretched to whatever size
	 * the panel requires.
	 */
	public void setBackground(ITexture tex);
	
	/**
	 * Sets the border images. If no border insets have been set, uses the
	 * textures unscaled, otherwise the images will be scaled to fit the border
	 * insets.
	 * 
	 * @param sides Textures to use for the sides of the border (top, right,
	 *        bottom, left).
	 * @param corners Images to use for the corners of the border (top-right,
	 *        bottom-right, bottom-left, top-left).
	 */
	public void setBorder(ITexture[] sides, ITexture[] corners);

	/**
	 * @see #setBorderInsets(double, double, double, double)
	 */
	public void setBorderInsets(double pad); //Calls setBorderInsets(top, right, bottom, left)

	/**
	 * @see #setBorderInsets(double, double, double, double)
	 */
	public void setBorderInsets(double vertical, double horizontal); //Calls setBorderInsets(top, right, bottom, left)
	
	/**
	 * Explicitly sets the border insets. If a border is set for this panel, the
	 * border is scaled to fit these insets.
	 */
	public void setBorderInsets(double top, double right, double bottom, double left);
	
}
