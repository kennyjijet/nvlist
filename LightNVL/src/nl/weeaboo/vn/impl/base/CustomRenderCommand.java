package nl.weeaboo.vn.impl.base;

import nl.weeaboo.vn.BlendMode;
import nl.weeaboo.vn.IPixelShader;
import nl.weeaboo.vn.IRenderer;

public abstract class CustomRenderCommand extends BaseRenderCommand {

	public static final byte id = ID_CUSTOM_RENDER_COMMAND;
	
	private IPixelShader ps;

	protected CustomRenderCommand(short z, boolean clipEnabled, BlendMode blendMode,
			int argb, IPixelShader ps, byte privateField)
	{
		super(id, z, clipEnabled, blendMode, argb, privateField);
		
		this.ps = ps;
	}
	
	//Functions
	public void render(IRenderer r) {
		if (ps != null) ps.preDraw(r);
		
		renderGeometry(r);
		
		if (ps != null) ps.postDraw(r);
	}
	
	protected abstract void renderGeometry(IRenderer r);
	
	//Getters
	
	//Setters
	
}
