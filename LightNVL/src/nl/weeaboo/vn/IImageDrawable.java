package nl.weeaboo.vn;

import nl.weeaboo.common.Area2D;

public interface IImageDrawable extends ITransformable {
	
	// === Functions ===========================================================
	
	// === Getters =============================================================
	public ITexture getTexture();	
	public IGeometryShader getGeometryShader();
	public Area2D getUV();
	
	// === Setters =============================================================
	public void setTexture(ITexture i); //Calls setTexture(i, anchor)
	public void setTexture(ITexture i, int anchor); //Calls setTexture(i, imageAlignX, imageAlignY)
	public void setTexture(ITexture i, double imageAlignX, double imageAlignY);
	public void setTween(IImageTween t);
	public void setGeometryShader(IGeometryShader gs);
	public void setUV(double w, double h);
	public void setUV(double x, double y, double w, double h);
	public void setUV(Area2D uv);
	
}
