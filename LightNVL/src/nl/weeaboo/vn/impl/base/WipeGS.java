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
public class WipeGS extends BaseShader implements IGeometryShader {
		
	private static final long serialVersionUID = BaseImpl.serialVersionUID;
	
	private final boolean fadeIn;
	private final int dir; //Numpad numbers indicate direction, supported values: 4,6,8,2
	private final double span;
	
	public WipeGS(boolean fadeIn, int dir, double span) {
		super(true);
		
		this.fadeIn = fadeIn;
		this.dir = dir;
		this.span = span;
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
			d.drawFadeQuad(z, clip, blend, argb, tex, trans, offset.x, offset.y, w, h, ps,
					dir, fadeIn, span, getTime());
		}
	}

	//Getters
	
	//Setters
	
}
