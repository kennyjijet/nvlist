package nl.weeaboo.vn;

import java.io.Serializable;

import nl.weeaboo.common.Rect2D;

public interface ITexture extends Serializable {

	// === Functions ===========================================================
	
	// === Getters =============================================================
	
	/**
	 * @return The texture width in image state coordinates
	 */
	public double getWidth();
	
	/**
	 * @return The texture height in image state coordinates
	 */	
	public double getHeight();
	
	/**
	 * @return The scale factor from pixel size to virtual size
	 */
	public double getScaleX();
	
	/**
	 * @return The scale factor from pixel size to virtual size
	 */
	public double getScaleY();
	
	/**
	 * @return The texture mapping coordinates used by the underlying graphics
	 *         system.
	 */
	public Rect2D getUV();
	
	// === Setters =============================================================
	
}
