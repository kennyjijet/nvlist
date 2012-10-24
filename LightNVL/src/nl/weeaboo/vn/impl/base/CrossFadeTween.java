package nl.weeaboo.vn.impl.base;

import nl.weeaboo.common.Rect2D;
import nl.weeaboo.lua2.io.LuaSerializable;
import nl.weeaboo.vn.BlendMode;
import nl.weeaboo.vn.IInterpolator;
import nl.weeaboo.vn.IPixelShader;
import nl.weeaboo.vn.IRenderer;
import nl.weeaboo.vn.math.Matrix;

@LuaSerializable
public class CrossFadeTween extends BaseImageTween {
		
	private static final long serialVersionUID = BaseImpl.serialVersionUID;

	private final IInterpolator interpolator;
	
	public CrossFadeTween(double duration, IInterpolator i) {
		super(duration);
		
		interpolator = (i != null ? i : Interpolators.LINEAR);
	}
	
	//Functions	
	@Override
	public void draw(IRenderer r) {
		BaseRenderer rr = (BaseRenderer)r;
	
		short z = drawable.getZ();
		boolean clip = drawable.isClipEnabled();
		BlendMode blend = drawable.getBlendMode();
		int argb = drawable.getColorARGB();
		Matrix trans = drawable.getTransform();
		IPixelShader ps = drawable.getPixelShader();
		double frac = interpolator.remap((float)getNormalizedTime());
		
		if (frac <= 0 || getEndTexture() == null) {
			Rect2D bounds = LayoutUtil.getBounds(getStartTexture(), getStartAlignX(), getStartAlignY());
			rr.drawQuad(z, clip, blend, argb, getStartTexture(), trans,
					bounds.x, bounds.y, bounds.w, bounds.h, ps);
		} else if (frac >= 1 || getStartTexture() == null) {
			Rect2D bounds = LayoutUtil.getBounds(getEndTexture(), getEndAlignX(), getEndAlignY());
			rr.drawQuad(z, clip, blend, argb, getEndTexture(), trans,
					bounds.x, bounds.y, bounds.w, bounds.h, ps);
		} else {
			rr.drawBlendQuad(z, clip, blend, argb,
					getStartTexture(), getStartAlignX(), getStartAlignY(),
					getEndTexture(), getEndAlignX(), getEndAlignY(),
					trans, ps,
					frac);
		}
	}
	
	//Getters
	
	//Setters
	
}
