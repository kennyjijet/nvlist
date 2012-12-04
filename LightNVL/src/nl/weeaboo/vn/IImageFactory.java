package nl.weeaboo.vn;

import java.util.Collection;


public interface IImageFactory {

	// === Functions ===========================================================
	
	/**
	 * Creates a new image drawable.
	 */
	public IImageDrawable createImageDrawable();
	
	/**
	 * Creates a text drawable.
	 */
	public ITextDrawable createTextDrawable();
	
	/**
	 * Creates a button drawable without an image. It can't be clicked until an
	 * image is set.
	 */
	public IButtonDrawable createButtonDrawable();

	/**
	 * Warning: does not add the screenshot to any screenshot buffer and the
	 * screenshot will not be taken until you do.
	 * 
	 * @param z The Z-coordinate to take the screenshot at.
	 * @param isVolatile Settings <code>volatile<code> to <code>true</code>
	 *        allows the screenshot to enable optimizations which could cause it
	 *        to lose its pixels at any time.
	 * @return A new screenshot object.
	 */
	public IScreenshot screenshot(short z, boolean isVolatile);

	/**
	 * Tells the runtime that the image specified by the given filename will be
	 * used in the near future.
	 * 
	 * @param filename The file to load
	 */
	public void preload(String filename);
	
	// === Getters =============================================================

	/**
	 * Creates a texture object from the specified filename.
	 * 
	 * @param filename The file to load
	 * @param callStack The Lua callstack from which the preload function was
	 *        called.
	 * @param suppressErrors If <code>true</code> doesn't log any errors that
	 *        may occur.
	 */
	public ITexture getTexture(String filename, String[] callStack, boolean suppressErrors);
	
	/**
	 * Creates a texture from the given image data. The <code>scaleX</code> and
	 * <code>scaleY</code> factors scale from pixel coordinates to the
	 * coordinates of image state.
	 */
	public ITexture createTexture(int argb[], int w, int h, double scaleX, double scaleY);
	
	/**
	 * Creates a texture from a screenshot. 
	 */
	public ITexture toTexture(IScreenshot ss);
	
	/**
	 * Returns the paths for all image files in the specified folder
	 */
	public Collection<String> getImageFiles(String folder);
	
	// === Setters =============================================================
	
}
