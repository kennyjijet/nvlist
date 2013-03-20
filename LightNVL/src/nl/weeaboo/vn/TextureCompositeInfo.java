package nl.weeaboo.vn;

import java.io.Serializable;

import nl.weeaboo.lua2.io.LuaSerializable;
import nl.weeaboo.vn.math.Vec2;

@LuaSerializable
public class TextureCompositeInfo implements Serializable {

	private static final long serialVersionUID = 1L;
	
	private final ITexture tex;
	private double offsetX, offsetY;
	private boolean overwrite;
	
	public TextureCompositeInfo(ITexture tex) {
		this.tex = tex;
	}
	
	//Functions
	
	//Getters
	public ITexture getTexture() {
		return tex;
	}
	public Vec2 getOffset() {
		return new Vec2(offsetX, offsetY);
	}
	public double getOffsetX() {
		return offsetX;
	}
	public double getOffsetY() {
		return offsetY;
	}
	public boolean hasOffset() {
		return offsetX > .0001 || offsetY > .0001;
	}
	public boolean getOverwrite() {
		return overwrite;
	}
	
	//Setters
	public void setOffset(double x, double y) {
		offsetX = x;
		offsetY = y;
	}
	
	public void setOverwrite(boolean o) {
		overwrite = o;
	}
	
}
