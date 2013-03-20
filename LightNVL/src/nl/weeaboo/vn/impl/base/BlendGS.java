package nl.weeaboo.vn.impl.base;

import nl.weeaboo.common.Area2D;
import nl.weeaboo.common.Rect2D;
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
public class BlendGS extends BaseShader implements IGeometryShader {
		
	private static final long serialVersionUID = BaseImpl.serialVersionUID;
	
	private ITexture endTexture;
	private double endAlignX, endAlignY;
	private int endAnchor;
	
	public BlendGS() {
		super(true);
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
		Area2D uv = image.getUV();
		double frac = getTime();
		
		double endAlignX = this.endAlignX;
		double endAlignY = this.endAlignY;		
		if (endAnchor > 0 && endTexture != null) {
			Rect2D base = LayoutUtil.getBounds(tex, alignX, alignY);
			Vec2 align = LayoutUtil.alignSubRect(base, endTexture.getWidth(), endTexture.getHeight(), endAnchor);
			endAlignX = align.x;
			endAlignY = align.y;
		}
		
		if (frac <= 0) {
			if (tex != null) {
				Vec2 offset = LayoutUtil.getImageOffset(tex, alignX, alignY);
				Area2D bounds = new Area2D(offset.x, offset.y, w, h);
				d.drawQuad(z, clip, blend, argb, tex, trans, bounds, uv, ps);
			}
		} else if (frac >= 1) {
			if (endTexture != null) {
				Vec2 offset = LayoutUtil.getImageOffset(endTexture, endAlignX, endAlignY);
				Area2D bounds = new Area2D(offset.x, offset.y, w, h);
				d.drawQuad(z, clip, blend, argb, endTexture, trans, bounds, uv, ps);
			}
		} else {
			d.drawBlendQuad(z, clip, blend, argb, tex, alignX, alignY,
					endTexture, endAlignX, endAlignY, trans, uv, ps, frac);
		}
	}

	//Getters
	public ITexture getEndTexture() {
		return endTexture;
	}
	
	//Setters
	public void setEndTexture(ITexture tex) {
		setEndTexture(tex, 7);
	}
	public void setEndTexture(ITexture tex, int anchor) {		
		this.endTexture = tex;
		this.endAnchor = anchor;
	}
	public void setEndTexture(ITexture tex, double alignX, double alignY) {
		this.endTexture = tex;
		this.endAnchor = 0;
		this.endAlignX = alignX;
		this.endAlignY = alignY;
	}
	
}
