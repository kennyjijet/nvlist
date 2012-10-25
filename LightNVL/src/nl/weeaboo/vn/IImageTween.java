package nl.weeaboo.vn;

import java.io.Serializable;

public interface IImageTween extends Serializable {

	// === Functions ===========================================================
	public void prepare();
	public boolean update(double effectSpeed);
	public void draw(IDrawBuffer d);
	public void finish();
	
	// === Getters =============================================================
	public boolean isPrepared();	
	public boolean isFinished();
	public double getNormalizedTime();
	public double getTime();
	public double getDuration();
	public double getWidth();
	public double getHeight();
	
	public ITexture getStartTexture();
	public double getStartAlignX();
	public double getStartAlignY();
	public ITexture getEndTexture();
	public double getEndAlignX();
	public double getEndAlignY();
	
	// === Setters =============================================================
	public void setStartImage(IImageDrawable id);
	public void setEndImage(ITexture img);
	public void setEndImage(ITexture img, int anchor);
	public void setEndImage(ITexture img, double imageAlignX, double imageAlignY);
	
}
