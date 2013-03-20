package nl.weeaboo.vn.impl.base;

import nl.weeaboo.common.Area2D;
import nl.weeaboo.common.Rect2D;
import nl.weeaboo.vn.BlendMode;
import nl.weeaboo.vn.IDistortGrid;
import nl.weeaboo.vn.IPixelShader;
import nl.weeaboo.vn.ITexture;
import nl.weeaboo.vn.math.Matrix;

public class DistortQuadCommand extends BaseRenderCommand {

	public static final byte id = ID_DISTORT_QUAD_COMMAND;

	public final ITexture tex;
	public final Matrix transform;
	public final Area2D bounds;
	public final Area2D uv;
	public final IPixelShader ps;
	public final IDistortGrid grid;
	public final Rect2D clampBounds;
	
	public DistortQuadCommand(short z, boolean clipEnabled, BlendMode blendMode, int argb,
			ITexture tex, Matrix trans, Area2D bounds, Area2D uv, IPixelShader ps,
			IDistortGrid grid, Rect2D clampBounds)
	{
		super(id, z, clipEnabled, blendMode, argb, tex != null ? (byte)tex.hashCode() : 0);
		
		this.tex = tex;
		this.transform = trans;
		this.bounds = bounds;
		this.uv = uv;
		this.ps = ps;
		
		this.grid = grid;
		this.clampBounds = clampBounds;
	}
	
}
