package nl.weeaboo.vn.impl.base;

import nl.weeaboo.vn.BlendMode;
import nl.weeaboo.vn.IPixelShader;
import nl.weeaboo.vn.ITexture;
import nl.weeaboo.vn.math.Matrix;

public class FadeQuadCommand extends BaseRenderCommand {

	public static final byte id = ID_FADE_QUAD_COMMAND;
	
	public final ITexture tex;
	public final Matrix transform;
	public final double x, y, w, h;
	public final IPixelShader ps;
	public final int dir;
	public final boolean fadeIn;
	public final double span;
	public final double frac;
	
	public FadeQuadCommand(short z, boolean clipEnabled, BlendMode blendMode,
		int argb, ITexture tex, Matrix trans, double x, double y, double w, double h,
		IPixelShader ps, int dir, boolean fadeIn, double span, double frac)
	{
		super(id, z, clipEnabled, blendMode, argb, tex != null ? (byte)tex.hashCode() : 0);
		
		this.tex = tex;
		this.transform = trans;
		this.x = x;
		this.y = y;
		this.w = w;
		this.h = h;
		this.ps = ps;
		this.dir = dir;
		this.fadeIn = fadeIn;
		this.span = span;
		this.frac = frac;
	}
	
}
