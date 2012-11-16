package nl.weeaboo.vn;

public interface IHardwarePS extends IPixelShader {

	public void removeParam(String name);
	
	public void setParam(String name, ITexture tex);
	public void setParam(String name, float value);
	public void setParam(String name, float[] values, int off, int len);
	
}
