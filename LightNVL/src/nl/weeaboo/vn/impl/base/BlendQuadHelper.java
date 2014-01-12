package nl.weeaboo.vn.impl.base;

import nl.weeaboo.common.Area2D;
import nl.weeaboo.common.Rect2D;
import nl.weeaboo.vn.IPixelShader;
import nl.weeaboo.vn.IRenderer;
import nl.weeaboo.vn.ITexture;
import nl.weeaboo.vn.layout.LayoutUtil;
import nl.weeaboo.vn.math.Matrix;

public abstract class BlendQuadHelper {

	protected final IRenderer renderer;
	
	public BlendQuadHelper(IRenderer r) {
		renderer = r;
	}
	
	//Functions
	public void renderBlendQuad(
			ITexture tex0, double alignX0, double alignY0,
			ITexture tex1, double alignX1, double alignY1,
			double frac, Area2D uv, Matrix transform, IPixelShader ps)
	{
		float f = 1f - (float)frac;

		Rect2D bounds0 = LayoutUtil.getBounds(tex0, alignX0, alignY0);
		Rect2D bounds1 = LayoutUtil.getBounds(tex1, alignX1, alignY1);
		
		boolean drawTex0 = (tex0 != null && f > 0);
		boolean drawTex1 = (tex1 != null && f < 1);
		if (!drawTex0 || !drawTex1 || isFallbackRequired()) {
			//Fallback for OpenGL < 1.4 or when some textures can't/needn't be drawn
			if (drawTex0) {
				if (drawTex1) {
					renderQuad(tex0, uv, transform, toARGB(1, 1, 1, 1), bounds0, ps); //This will draw the old image fully opaque under the new one, but prevents the background from showing through mid-blend.
					renderQuad(tex1, uv, transform, toARGB(1, 1, 1, 1-f), bounds1, ps);
				} else {
					renderQuad(tex0, uv, transform, toARGB(1, 1, 1, f), bounds0, ps);
				}
			} else if (drawTex1) {
				renderQuad(tex1, uv, transform, toARGB(1, 1, 1, 1-f), bounds1, ps);
			}
		} else {
			renderMultitextured(tex0, bounds0, tex1, bounds1, uv, transform, ps, f);
		}
	}
	
	protected static int toARGB(float r, float g, float b, float a) {
		int ri = Math.max(0, Math.min(255, Math.round(r*255f)))<<16;
		int gi = Math.max(0, Math.min(255, Math.round(g*255f)))<<8;
		int bi = Math.max(0, Math.min(255, Math.round(b*255f)));
		int ai = Math.max(0, Math.min(255, Math.round(a*255f)))<<24;
		
		return ai | ri | gi | bi;
	}
	
	protected abstract void renderQuad(ITexture tex, Area2D uv, Matrix transform, int mixColorARGB,
			Rect2D bounds, IPixelShader hws);
	
	protected abstract void renderMultitextured(ITexture tex0, Rect2D bounds0,
			ITexture tex1, Rect2D bounds1, Area2D uv, Matrix transform,
			IPixelShader hws, float tex0Factor);
	
	//Getters
	protected abstract boolean isFallbackRequired();
	
	//Setters
    
}
