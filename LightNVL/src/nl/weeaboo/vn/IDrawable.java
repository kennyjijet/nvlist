package nl.weeaboo.vn;

import java.io.Serializable;

public interface IDrawable extends Serializable {

	// === Functions ===========================================================
	
	/**
	 * Marks this drawable as destroyed.
	 */
	public void destroy();

	/**
	 * @param layer The layer containing this object.
	 * @param input Object containing keyboard/mouse/other input data.
	 * @param effectSpeed The suggested relative animation speed.
	 * @param layer The layer containing this drawable.
	 * @return <code>true</code> If the state changed.
	 */
	public boolean update(ILayer layer, IInput input, double effectSpeed);
	
	/**
	 * @param r The renderer to use for drawing.
	 */
	public void draw(IRenderer r);

	// === Getters =============================================================
	
	public boolean isDestroyed();
	
	public double getX();
	public double getY();
	public short getZ();
	public double getWidth();
	public double getHeight();
	
	public int getColorARGB();
	public int getColorRGB();
	public double getRed();
	public double getGreen();
	public double getBlue();
	public double getAlpha();
	
	public BlendMode getBlendMode();
	public boolean isClipEnabled();

	public IPixelShader getPixelShader();
	
	// === Setters =============================================================
	
	public void setX(double x);
	public void setY(double y);
	public void setPos(double x, double y);
	public void setZ(short z);
	
	public void setColor(double r, double g, double b);
	public void setColor(double r, double g, double b, double a);
	public void setColorRGB(int rgb);
	public void setColorARGB(int argb);
	public void setAlpha(double a);
	
	public void setBlendMode(BlendMode m);
	public void setClipEnabled(boolean clip);
	
	public void setPixelShader(IPixelShader ps);
	
}
