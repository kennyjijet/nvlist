package nl.weeaboo.vn.impl.base;

import nl.weeaboo.lua2.io.LuaSerializable;
import nl.weeaboo.vn.BlendMode;
import nl.weeaboo.vn.IDrawBuffer;
import nl.weeaboo.vn.IGeometryShader;
import nl.weeaboo.vn.IImageDrawable;
import nl.weeaboo.vn.IPixelShader;
import nl.weeaboo.vn.ITexture;
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
		double frac = getTime();
		
		Vec2 offset = LayoutUtil.getImageOffset(tex, alignX, alignY);			
		
		if (frac <= 0 || frac >= 1) {
			if ((frac >= 1 && fadeIn) || (frac <= 0 && !fadeIn)) {
				d.drawQuad(z, clip, blend, argb, tex, trans, offset.x, offset.y, w, h, ps);
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

				if (dir == 4 || dir == 6) {
					d.drawQuad(z, clip, blend, argb, tex, trans,
							offset.x+(u+a)*w, offset.y, w*(b-a), h,
							u+a, 0, (b-a), 1.0, ps);
						
					u += t;										
				} else {
					d.drawQuad(z, clip, blend, argb, tex, trans,
							offset.x, offset.y+(v+a)*h, w, h*(b-a),
							0, v+a, 1.0, (b-a), ps);
						
					v += t;										
				}				
			}
		}
	}

	//Getters
	
	//Setters
	
}
