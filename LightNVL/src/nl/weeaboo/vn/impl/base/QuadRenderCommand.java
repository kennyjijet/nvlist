package nl.weeaboo.vn.impl.base;

import nl.weeaboo.vn.BlendMode;
import nl.weeaboo.vn.IPixelShader;
import nl.weeaboo.vn.ITexture;
import nl.weeaboo.vn.math.Matrix;

public class QuadRenderCommand extends BaseRenderCommand {

	public static final byte id = ID_QUAD_RENDER_COMMAND;
	
	public final ITexture tex;
	public final Matrix transform;
	public final double x, y, width, height;
	public final double u, v, uw, vh;
	public final IPixelShader ps;
	
	public QuadRenderCommand(short z, boolean clipEnabled, BlendMode blendMode,
			int argb, ITexture tex, Matrix trans, double x, double y, double w, double h,
			IPixelShader ps)
	{
		this(z, clipEnabled, blendMode, argb, tex, trans, x, y, w, h, 0, 0, 1.0, 1.0, ps);
	}
	public QuadRenderCommand(short z, boolean clipEnabled, BlendMode blendMode,
			int argb, ITexture tex, Matrix trans, double x, double y, double w, double h,
			double u, double v, double uw, double vh,
			IPixelShader ps)
	{
		this(id, z, clipEnabled, blendMode, argb, tex, trans, x, y, w, h, u, v, uw, vh, ps);
	}

	protected QuadRenderCommand(byte id, short z, boolean clipEnabled, BlendMode blendMode,
			int argb, ITexture tex, Matrix trans, double x, double y, double w, double h,
			double u, double v, double uw, double vh,
			IPixelShader ps)
	{
		super(id, z, clipEnabled, blendMode, argb, (tex != null ? (byte)tex.hashCode() : 0));
		
		this.tex = tex;
		this.transform = trans;
		this.x = x;
		this.y = y;
		this.width = w;
		this.height = h;
		this.u = u;
		this.v = v;
		this.uw = uw;
		this.vh = vh;
		this.ps = ps;
	}
	
}
