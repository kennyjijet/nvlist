package nl.weeaboo.vn.impl.base;

import nl.weeaboo.lua2.io.LuaSerializable;
import nl.weeaboo.vn.IDrawBuffer;
import nl.weeaboo.vn.IGeometryShader;
import nl.weeaboo.vn.IPixelShader;
import nl.weeaboo.vn.IShader;

@LuaSerializable
public class ShaderImageTween extends BaseImageTween {

	private static final long serialVersionUID = BaseImpl.serialVersionUID;

	private final IGeometryShader gs1;
	private final IPixelShader ps1;
	private final IGeometryShader gs2;
	private final IPixelShader ps2;
	
	public ShaderImageTween(double duration, IGeometryShader gs1, IPixelShader ps1,
			IGeometryShader gs2, IPixelShader ps2)
	{
		super(duration);
		
		this.gs1 = gs1;
		this.ps1 = ps1;
		this.gs2 = gs2;
		this.ps2 = ps2;
	}
	
	//Functions
	protected boolean updateShader(IShader shader, double effectSpeed) {
		if (shader == null) return false;		
		shader.setTime(getNormalizedTime());
		return shader.update(effectSpeed);
	}
	
	@Override
	public boolean update(double effectSpeed) {
		boolean changed = super.update(effectSpeed);
		
		changed |= updateShader(gs1, effectSpeed);
		changed |= updateShader(ps1, effectSpeed);
		changed |= updateShader(gs2, effectSpeed);
		changed |= updateShader(ps2, effectSpeed);
		
		return consumeChanged() || changed;
	}
	
	@Override
	public void draw(IDrawBuffer d) {
		super.draw(d);
		
		if (getStartTexture() != null) {
			IGeometryShader gs1 = (this.gs1 != null ? this.gs1 : drawable.getGeometryShader());
			IPixelShader ps1 = (this.ps1 != null ? this.ps1 : drawable.getPixelShader());
			d.drawWithTexture(drawable, getStartTexture(), getStartAlignX(), getStartAlignY(), gs1, ps1);
		}
		if (getEndTexture() != null) {
			IGeometryShader gs2 = (this.gs2 != null ? this.gs2 : drawable.getGeometryShader());
			IPixelShader ps2 = (this.ps2 != null ? this.ps2 : drawable.getPixelShader());
			d.drawWithTexture(drawable, getEndTexture(), getEndAlignX(), getEndAlignY(), gs2, ps2);
		}
		
		//System.out.println(getStartImage() + " " + getEndImage() + " " + getNormalizedTime());
	}
	
	//Getters
	
	//Setters
	
}
