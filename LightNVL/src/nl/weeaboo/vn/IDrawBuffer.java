package nl.weeaboo.vn;

import nl.weeaboo.common.Rect2D;
import nl.weeaboo.vn.impl.base.RenderEnv;
import nl.weeaboo.vn.math.Matrix;

public interface IDrawBuffer {

	public void reset();
	
	public void draw(IImageDrawable img);

	public void drawWithTexture(IImageDrawable id, ITexture tex, double alignX, double alignY,
			IGeometryShader gs, IPixelShader ps);
	
	public void drawQuad(short z, boolean clipEnabled, BlendMode blendMode, int argb,
			ITexture tex, Matrix trans, double x, double y, double w, double h, IPixelShader ps);
	
	public void drawQuad(short z, boolean clipEnabled, BlendMode blendMode, int argb,
			ITexture tex, Matrix trans, double x, double y, double w, double h,
			double u, double v, double uw, double vh, IPixelShader ps);

	public void drawFadeQuad(short z, boolean clipEnabled, BlendMode blendMode, int argb,
			ITexture tex, Matrix trans, double x, double y, double w, double h, IPixelShader ps,
			int dir, boolean fadeIn, double span, double time);
	
	public void drawBlendQuad(short z, boolean clipEnabled, BlendMode blendMode, int argb,
			ITexture tex0, double alignX0, double alignY0,
			ITexture tex1, double alignX1, double alignY1,
			Matrix trans, IPixelShader ps,
			double frac);
	
	public void drawDistortQuad(short z, boolean clipEnabled, BlendMode blendMode, int argb,
			ITexture tex, Matrix trans, double x, double y, double w, double h, IPixelShader ps,
			IDistortGrid distortGrid, Rect2D clampBounds);
	
	/**
	 * Schedules a screenshot to be taken during rendering.
	 * 
	 * @param ss The screenshot object to fill with pixels.
	 * @param clip If <code>true</code>, takes a screenshot of just the current
	 *        clipped area. Otherwise, takes a screenshot of the entire render
	 *        area.
	 */
	public void screenshot(IScreenshot ss, boolean clip);
	
	public RenderEnv getEnv();
	
	public void startLayer(ILayer layer);
	
}
