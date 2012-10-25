package nl.weeaboo.vn;

public interface IImageDrawable extends ITransformable {
	
	// === Functions ===========================================================
	
	// === Getters =============================================================
	public ITexture getTexture();	
	public IGeometryShader getGeometryShader();
	
	// === Setters =============================================================
	public void setTexture(ITexture i); //Calls setTexture(i, anchor)
	public void setTexture(ITexture i, int anchor); //Calls setTexture(i, imageAlignX, imageAlignY)
	public void setTexture(ITexture i, double imageAlignX, double imageAlignY);
	public void setTween(IImageTween t);
	public void setGeometryShader(IGeometryShader gs);
		
}
