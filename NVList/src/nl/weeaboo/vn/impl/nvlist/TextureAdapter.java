package nl.weeaboo.vn.impl.nvlist;

import nl.weeaboo.common.Rect2D;
import nl.weeaboo.gl.GLManager;
import nl.weeaboo.gl.texture.GLTexRect;
import nl.weeaboo.gl.texture.GLTexture;
import nl.weeaboo.lua2.io.LuaSerializable;
import nl.weeaboo.vn.ITexture;

@LuaSerializable
public class TextureAdapter implements ITexture {

	private static final long serialVersionUID = NVListImpl.serialVersionUID;
	
	protected final ImageFactory imgfac;
	protected GLTexRect tr;
	private double scaleX, scaleY;
	
	public TextureAdapter(ImageFactory fac) {
		this.imgfac = fac;
	}
	
	public void forceLoad(GLManager glm) {
		if (tr != null) {
			tr = tr.forceLoad(glm);
			setTexRect(tr, scaleX, scaleY);
		}
	}
	
	@Override
	public int hashCode() {
		return (tr != null ? tr.hashCode() : 0);
	}
	
	@Override
	public boolean equals(Object obj) {
		if (obj instanceof TextureAdapter && obj.getClass().equals(getClass())) {
			TextureAdapter ta = (TextureAdapter)obj;
			return (tr == ta.tr || (tr != null && tr.equals(ta.tr)))
				&& scaleX == ta.scaleX
				&& scaleY == ta.scaleY;
		}
		return false;
	}
	
	@Override
	public String toString() {
		String trString = "null";
		if (tr != null) {
			if (tr.getPath() != null) {
				trString = tr.getPath();
			} else {
				trString = tr.getWidth() + "x" + tr.getHeight();
			}
		}
		return String.format("TextureAdapter(%s)", trString);
	}
	
	public int getTexId() {
		GLTexture tex = getTexture();
		return (tex != null ? tex.getTexId() : 0);
	}
	
	public GLTexture getTexture() {
		if (tr == null) return null;
		return tr.getTexture();
	}
	public GLTexRect getTexRect() {
		return tr;
	}	
	
	@Override
	public Rect2D getUV() {
		if (tr == null) return new Rect2D(0, 0, 1, 1);
		return tr.getUV();
	}
	
	@Override
	public double getWidth() {
		if (tr == null) return 0;
		return tr.getWidth() * scaleX;
	}
			
	@Override
	public double getHeight() {
		if (tr == null) return 0;
		return tr.getHeight() * scaleY;
	}
	
	public double getScaleX() {
		return scaleX;
	}
	
	public double getScaleY() {
		return scaleY;
	}
	
	void setTexRect(GLTexRect tr, double sx, double sy) {
		this.tr = tr;
		this.scaleX = sx;
		this.scaleY = sy;
	}
	
}