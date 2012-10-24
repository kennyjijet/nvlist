package nl.weeaboo.vn.impl.base;

import nl.weeaboo.common.Rect2D;
import nl.weeaboo.vn.IPixelShader;
import nl.weeaboo.vn.ITexture;
import nl.weeaboo.vn.math.Matrix;

public abstract class BlendQuadHelper {

	protected final BaseRenderer renderer;
	
	public BlendQuadHelper(BaseRenderer r) {
		renderer = r;
	}
	
	//Functions
	public void renderBlendQuad(
			ITexture tex0, double alignX0, double alignY0,
			ITexture tex1, double alignX1, double alignY1,
			double frac, Matrix transform, IPixelShader ps)
	{
		float f = 1f - (float)frac;

		Rect2D bounds0 = LayoutUtil.getBounds(tex0, alignX0, alignY0);
		Rect2D bounds1 = LayoutUtil.getBounds(tex1, alignX1, alignY1);
		
		if (ps != null) ps.preDraw(renderer);
		
		boolean drawTex0 = (tex0 != null && f > 0);
		boolean drawTex1 = (tex1 != null && f < 1);
		if (!drawTex0 || !drawTex1 || isFallbackRequired()) {
			//Fallback for OpenGL < 1.4 or when some textures can't/needn't be drawn
			if (drawTex0) {
				if (drawTex1) {
					//renderQuad(tex0, transform, toARGB(1, 1, 1, f), bounds0);
					renderQuad(tex0, transform, toARGB(1, 1, 1, 1), bounds0); //This will draw the old image fully opaque under the new one, but prevents the background from showing through mid-blend.
					renderQuad(tex1, transform, toARGB(1, 1, 1, 1-f), bounds1);
				} else {
					renderQuad(tex0, transform, toARGB(1, 1, 1, f), bounds0);
				}
			} else if (drawTex1) {
				renderQuad(tex1, transform, toARGB(1, 1, 1, 1-f), bounds1);
			}
		} else {
			renderMultitextured(tex0, bounds0, tex1, bounds1, transform, f);
		}
	
		if (ps != null) ps.postDraw(renderer);
	}
	
	protected static int toARGB(float r, float g, float b, float a) {
		int ri = Math.max(0, Math.min(255, Math.round(r*255f)))<<16;
		int gi = Math.max(0, Math.min(255, Math.round(g*255f)))<<8;
		int bi = Math.max(0, Math.min(255, Math.round(b*255f)));
		int ai = Math.max(0, Math.min(255, Math.round(a*255f)))<<24;
		
		return ai | ri | gi | bi;
	}
	
	protected abstract void renderQuad(ITexture tex, Matrix transform, int mixColorARGB, Rect2D bounds);
	
	protected abstract void renderMultitextured(ITexture tex0, Rect2D bounds0,
			ITexture tex1, Rect2D bounds1, Matrix transform, float tex0Factor);
	
	//Getters
	protected abstract boolean isFallbackRequired();
	
	//Setters
    
}
