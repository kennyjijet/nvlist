package nl.weeaboo.vn;

import nl.weeaboo.vn.math.Vec2;


public interface IImageFxLib {

	/**
	 * Creates a blurred copy of <code>tex</code> using a blur with kernel size
	 * <code>k</code>
	 * 
	 * @param tex The texture to create a blurred copy of
	 * @param k The blur kernel size, determines the strength of the blur
	 * @param extendDirs An integer where each digit matches a numpad direction.
	 *        For example, <code>268</code> is <code>bottom, right, top</code>.
	 *        To blur all the pixel in an image, the image must be expanded
	 *        slightly. By default this padding consists of transparent pixels,
	 *        but in some cases repeating the nearest edge pixels is preferred.
	 *        The <code>extendDirs</code> parameter determines for which
	 *        directions to expand by repeating.
	 * @param cropDirs An integer where each digit matches a numpad direction.
	 *        For example, <code>286</code> is <code>left, top, right</code>.
	 *        Blurring smears the pixels of an image out over a larger area,
	 *        making it biger. Afterwards, we can crop to retain the image's
	 *        previous size.
	 * @return A blurred copy of the input texture
	 */
	public ITexture blur(ITexture tex, int k, int extendDirs, int cropDirs);
	
	/**
	 * @see #blur(ITexture, int, int, int) 
	 */
	public ITexture[] blurMultiple(ITexture tex, int minLevel, int levelCount, int k, int extendDirs);
	
	/**
	 * Returns the number of pixels added as padding for a single edge when
	 * generating the results of
	 * {@link #blurMultiple(ITexture, int, int, int, int)}.
	 */
	public int getBlurMultiplePad(int level, int levelCount, int k);
	
	/**
	 * Creates a copy of <code>tex</code>, with its RGB values modified by
	 * <code>addFraction</code>.
	 * 
	 * @param tex The source texture
	 * @param addFraction The increase in RGB (between <code>0.0</code> and
	 *        <code>1.0</code>)
	 * @return A modified copy of the texture
	 */
	public ITexture brighten(ITexture tex, double addFraction);
	
	/**
	 * Creates a copy of <code>tex</code>, downscaled by a factor
	 * <code>2<sup>level</sup></code>.
	 * 
	 * @param tex The source texture
	 * @param level The mipmap level
	 * @return A downscaled copy of the texture
	 */
	public ITexture mipmap(ITexture tex, int level);
	
	/**
	 * Multiplies the RGB(A) values in <code>tex</code> with the specified
	 * factors.
	 * 
	 * @param tex The source texture
	 * @param rgba A 3 or 4 length array used to multiply the RGB(A) values by.
	 * @return A modified copy of the texture
	 */
	public ITexture applyColorMatrix(ITexture tex, double[] rgba);
	
	/**
	 * Multiplies the RGB(A) values in <code>tex</code> with the specified
	 * factors.
	 * 
	 * @param tex The source texture
	 * @param r A 3 or 4 length array, <code>dR = r[0] * sR + r[1] * sG + r[2] * sB + r[3] * sA</code> 
	 * @param g A 3 or 4 length array, <code>dG = g[0] * sR + g[1] * sG + g[2] * sB + g[3] * sA</code> 
	 * @param b A 3 or 4 length array, <code>dB = b[0] * sR + b[1] * sG + b[2] * sB + b[3] * sA</code> 
	 * @param a A 3 or 4 length array, <code>dA = a[0] * sR + a[1] * sG + a[2] * sB + a[3] * sA</code> 
	 * @return A modified copy of the texture
	 */
	public ITexture applyColorMatrix(ITexture tex, double[] r, double[] g, double[] b, double[] a);
	
	/**
	 * Creates a cropped copy of <code>tex</code> based on the given cropping
	 * rectangle. The coordinates are clamped to the actual texture bounds.
	 * 
	 * @param tex The texture to create a cropped copy of
	 * @param x The x-coordinate for the crop rectangle
	 * @param y The y-coordinate for the crop rectangle
	 * @param w The width for the crop rectangle
	 * @param h The height for the crop rectangle
	 * @return A cropped copy of the texture
	 */
	public ITexture crop(ITexture tex, double x, double y, double w, double h);
	
	/**
	 * Renders the a number of textures on top of each other (using slow
	 * software rendering), resulting in a single new texture containing the result.
	 * 
	 * @param w The width for the output texture
	 * @param h The height for the output texture
	 * @param itexs An array of textures to combine. The first texture will be
	 *        rendered first, the second one on top of that, etc.
	 * @param offsets An array of offsets to render the textures at.
	 * @return A new texture containing the result.
	 */
	public ITexture composite(double w, double h, ITexture[] itexs, Vec2[] offsets);
	
}
