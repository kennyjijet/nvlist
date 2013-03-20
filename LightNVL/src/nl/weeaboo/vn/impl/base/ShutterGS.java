package nl.weeaboo.vn.impl.base;

import nl.weeaboo.common.Area2D;
import nl.weeaboo.lua2.io.LuaSerializable;
import nl.weeaboo.vn.BlendMode;
import nl.weeaboo.vn.IDrawBuffer;
import nl.weeaboo.vn.IGeometryShader;
import nl.weeaboo.vn.IImageDrawable;
import nl.weeaboo.vn.IPixelShader;
import nl.weeaboo.vn.ITexture;
import nl.weeaboo.vn.layout.LayoutUtil;
import nl.weeaboo.vn.math.Matrix;
import nl.weeaboo.vn.math.Vec2;

@LuaSerializable
public class ShutterGS extends BaseShader implements IGeometryShader {
		
	private static final long serialVersionUID = BaseImpl.serialVersionUID;
	
	private final boolean fadeIn;
	private final int dir; //Numpad numbers indicate direction, supported values: 4,6,8,2
	private final int steps;
	
	public ShutterGS(boolean fadeIn, int dir, int steps) {
		super(true);
		
		this.fadeIn = fadeIn;
		this.dir = dir;
		this.steps = steps;
	}

	//Functions
	@Override
	public void draw(IDrawBuffer d, IImageDrawable image, ITexture tex,
			double alignX, double alignY, IPixelShader ps)
	{
		short z = image.getZ();
		boolean clip = image.isClipEnabled();
		BlendMode blend = image.getBlendMode();
		int argb = image.getColorARGB();
		Matrix trans = image.getTransform();
		double w = image.getUnscaledWidth();
		double h = image.getUnscaledHeight();
		Area2D imageUV = image.getUV();
		double frac = getTime();
		
		Vec2 offset = LayoutUtil.getImageOffset(tex, alignX, alignY);			
		
		if (frac <= 0 || frac >= 1) {
			if ((frac >= 1 && fadeIn) || (frac <= 0 && !fadeIn)) {
				Area2D bounds = new Area2D(offset.x, offset.y, w, h);
				d.drawQuad(z, clip, blend, argb, tex, trans, bounds, imageUV, ps);
			}
		} else {
			double t = 1.0 / steps;
			double tf = frac * t;

			double u = 0.0;
			double v = 0.0;
			for (int s = 0; s < steps; s++) {
				double a, b;
				if (dir == 6 || dir == 2) {
					if (fadeIn) {
						a = 0; b = tf;
					} else {
						a = tf; b = t;
					}
				} else {
					if (fadeIn) {
						a = t-tf; b = t;
					} else {
						a = 0; b = t-tf;
					}
				}

				Area2D bounds;
				Area2D uv;
				if (dir == 4 || dir == 6) {
					bounds = new Area2D(offset.x+(u+a)*w, offset.y, w*(b-a), h);
					uv = BaseRenderer.combineUV(new Area2D(u+a, 0, (b-a), 1.0), imageUV);
					u += t;										
				} else {
					bounds = new Area2D(offset.x, offset.y+(v+a)*h, w, h*(b-a));
					uv = BaseRenderer.combineUV(new Area2D(0, v+a, 1.0, (b-a)), imageUV);						
					v += t;										
				}
				d.drawQuad(z, clip, blend, argb, tex, trans, bounds, uv, ps);
			}
		}
	}

	//Getters
	
	//Setters
	
}
