package nl.weeaboo.vn.impl.base;

import nl.weeaboo.common.Area2D;
import nl.weeaboo.vn.BlendMode;
import nl.weeaboo.vn.IPixelShader;
import nl.weeaboo.vn.ITexture;
import nl.weeaboo.vn.math.Matrix;

public class BlendQuadCommand extends BaseRenderCommand {

	public static final byte id = ID_BLEND_QUAD_COMMAND;

	public final ITexture tex0;
	public final double alignX0, alignY0;
	public final ITexture tex1;
	public final double alignX1, alignY1;
	public final Matrix transform;
	public final Area2D uv;
	public final IPixelShader ps;
	public final double frac;
	
	public BlendQuadCommand(short z, boolean clipEnabled, BlendMode blendMode, int argb,
		ITexture tex0, double alignX0, double alignY0,
		ITexture tex1, double alignX1, double alignY1,
		Matrix transform, Area2D uv, IPixelShader ps,
		double frac)
	{
		super(id, z, clipEnabled, blendMode, argb, tex0 != null ? (byte)tex0.hashCode() : 0);
		
		this.tex0 = tex0;
		this.alignX0 = alignX0;
		this.alignY0 = alignY0;
		this.tex1 = tex1;
		this.alignX1 = alignX1;
		this.alignY1 = alignY1;
		this.transform = transform;
		this.uv = uv;
		this.ps = ps;
		this.frac = frac;
	}
	
}
