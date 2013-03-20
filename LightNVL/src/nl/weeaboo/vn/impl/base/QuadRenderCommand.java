package nl.weeaboo.vn.impl.base;

import nl.weeaboo.common.Area2D;
import nl.weeaboo.vn.BlendMode;
import nl.weeaboo.vn.IPixelShader;
import nl.weeaboo.vn.ITexture;
import nl.weeaboo.vn.math.Matrix;

public class QuadRenderCommand extends BaseRenderCommand {

	public static final byte id = ID_QUAD_RENDER_COMMAND;
	
	public final ITexture tex;
	public final Matrix transform;
	public final Area2D bounds;
	public final Area2D uv;
	public final IPixelShader ps;
	
	public QuadRenderCommand(short z, boolean clipEnabled, BlendMode blendMode,
			int argb, ITexture tex, Matrix trans, Area2D bounds, Area2D uv, IPixelShader ps)
	{
		this(id, z, clipEnabled, blendMode, argb, tex, trans, bounds, uv, ps);
	}

	protected QuadRenderCommand(byte id, short z, boolean clipEnabled, BlendMode blendMode,
			int argb, ITexture tex, Matrix trans, Area2D bounds, Area2D uv, IPixelShader ps)
	{
		super(id, z, clipEnabled, blendMode, argb, (tex != null ? (byte)tex.hashCode() : 0));
		
		this.tex = tex;
		this.transform = trans;
		this.bounds = bounds;
		this.uv = uv;
		this.ps = ps;
	}
	
}
