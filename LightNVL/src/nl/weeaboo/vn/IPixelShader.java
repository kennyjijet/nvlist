package nl.weeaboo.vn;

public interface IPixelShader extends IShader {

	public void preDraw(IRenderer r);
	public void postDraw(IRenderer r);
	
	public void removeParam(String name);
	
	public void setParam(String name, ITexture tex);
	public void setParam(String name, float value);
	public void setParam(String name, float[] values, int off, int len);
	
}
