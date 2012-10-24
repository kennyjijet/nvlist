package nl.weeaboo.vn;

public interface IGeometryShader extends IShader {

	// === Functions ===========================================================
	public void draw(IRenderer r, IImageDrawable image, ITexture tex,
			double alignX, double alignY, IPixelShader ps);
	
	// === Getters =============================================================
	
	// === Setters =============================================================

}
