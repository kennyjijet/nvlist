package nl.weeaboo.vn;

import java.io.Externalizable;
import java.io.IOException;
import java.io.ObjectInput;
import java.io.ObjectOutput;

import nl.weeaboo.common.Area2D;
import nl.weeaboo.common.Insets2D;
import nl.weeaboo.common.Rect2D;
import nl.weeaboo.lua2.io.LuaSerializable;
import nl.weeaboo.vn.math.Matrix;

@LuaSerializable
public final class NinePatch implements Externalizable {

	private static final int IDX_BACKGROUND  = 0;
	private static final int IDX_SIDE_TOP    = 1;
	private static final int IDX_SIDE_RIGHT  = 2;
	private static final int IDX_SIDE_BOTTOM = 3;
	private static final int IDX_SIDE_LEFT   = 4;
	private static final int IDX_CORNER_TOP_RIGHT    = 5;
	private static final int IDX_CORNER_BOTTOM_RIGHT = 6;
	private static final int IDX_CORNER_BOTTOM_LEFT  = 7;
	private static final int IDX_CORNER_TOP_LEFT     = 8;
	
	private ITexture[] textures;
	
	public NinePatch() {
		textures = new ITexture[9];
	}
	
	//Functions
	@Override
	public void writeExternal(ObjectOutput out) throws IOException {
		out.writeInt(textures.length);
		for (int n = 0; n < textures.length; n++) {
			out.writeObject(textures[n]);
		}
	}

	@Override
	public void readExternal(ObjectInput in) throws IOException, ClassNotFoundException {
		int len = in.readInt();
		for (int n = 0; n < len; n++) {
			textures[n] = (ITexture)in.readObject();
		}
	}
	
	public void draw(IDrawBuffer d, short z, boolean clip, BlendMode blend, int argb, Matrix trans,
			Rect2D r, Insets2D i, IPixelShader ps)
	{
		Area2D uv = IDrawBuffer.DEFAULT_UV;
		
		ITexture bg = getBackground();
		if (bg != null) {
			Area2D bounds = new Area2D(r.x+i.left, r.y+i.top, r.w-i.left-i.right, r.h-i.top-i.bottom);
			d.drawQuad(z, clip, blend, argb, bg, trans, bounds, uv, ps);
		}
		
		ITexture ctl = getCornerTopLeft();
		if (ctl != null) {
			Area2D bounds = new Area2D(r.x, r.y, i.left, i.top);
			d.drawQuad(z, clip, blend, argb, ctl, trans, bounds, uv, ps);
		}
		ITexture ctr = getCornerTopRight();
		if (ctr != null) {
			Area2D bounds = new Area2D(r.x+r.w-i.right, r.y, i.right, i.top);
			d.drawQuad(z, clip, blend, argb, ctr, trans, bounds, uv, ps);
		}
		ITexture cbl = getCornerBottomLeft();
		if (cbl != null) {
			Area2D bounds = new Area2D(r.x, r.y+r.h-i.bottom, i.left, i.bottom);
			d.drawQuad(z, clip, blend, argb, cbl, trans, bounds, uv, ps);
		}
		ITexture cbr = getCornerBottomRight();
		if (cbr != null) {
			Area2D bounds = new Area2D(r.x+r.w-i.right, r.y+r.h-i.bottom, i.right, i.bottom);
			d.drawQuad(z, clip, blend, argb, cbr, trans, bounds, uv, ps);
		}

		ITexture top = getSideTop();
		if (top != null) {
			Area2D bounds = new Area2D(r.x+i.left, r.y, r.w-i.left-i.right, i.top);
			d.drawQuad(z, clip, blend, argb, top, trans, bounds, uv, ps);
		}
		ITexture bottom = getSideBottom();
		if (bottom != null) {
			Area2D bounds = new Area2D(r.x+i.left, r.y+r.h-i.bottom, r.w-i.left-i.right, i.bottom);
			d.drawQuad(z, clip, blend, argb, bottom, trans, bounds, uv, ps);
		}
		ITexture left = getSideLeft();		
		if (left != null) {
			Area2D bounds = new Area2D(r.x, r.y+i.top, i.left, r.h-i.top-i.bottom);
			d.drawQuad(z, clip, blend, argb, left, trans, bounds, uv, ps);
		}
		ITexture right = getSideRight();
		if (right != null) {
			Area2D bounds = new Area2D(r.x+r.w-i.right, r.y+i.top, i.right, r.h-i.top-i.bottom);
			d.drawQuad(z, clip, blend, argb, right, trans, bounds, uv, ps);		
		}
	}
	
	private static double maxWidth(ITexture a, ITexture b, ITexture c) {
		double w = 0;
		if (a != null) w = Math.max(w, a.getWidth());
		if (b != null) w = Math.max(w, b.getWidth());
		if (c != null) w = Math.max(w, c.getWidth());
		return w;
	}

	private static double maxHeight(ITexture a, ITexture b, ITexture c) {
		double h = 0;
		if (a != null) h = Math.max(h, a.getHeight());
		if (b != null) h = Math.max(h, b.getHeight());
		if (c != null) h = Math.max(h, c.getHeight());
		return h;
	}
	
	//Getters
	public Insets2D getInsets() {
		ITexture ctr = getCornerTopRight();
		ITexture cbr = getCornerBottomRight();
		ITexture cbl = getCornerBottomLeft();
		ITexture ctl = getCornerTopLeft();

		ITexture top = getSideTop();
		ITexture right = getSideRight();
		ITexture bottom = getSideBottom();
		ITexture left = getSideLeft();
		
		return new Insets2D(maxHeight(ctl, top, ctr), maxWidth(ctr, right, cbr),
				maxHeight(cbl, bottom, cbr), maxWidth(ctl, left, cbl));
	}
	
	public ITexture getBackground() {
		return get(IDX_BACKGROUND);
	}
	
	/**
	 * Returns all 4 sides: top, right, bottom, left. 
	 */
	public ITexture[] getSides() {
		return new ITexture[] {getSideTop(), getSideRight(), getSideBottom(), getSideLeft()};
	}
	public ITexture getSideTop() {
		return get(IDX_SIDE_TOP);
	}
	public ITexture getSideRight() {
		return get(IDX_SIDE_RIGHT);
	}
	public ITexture getSideBottom() {
		return get(IDX_SIDE_BOTTOM);
	}
	public ITexture getSideLeft() {
		return get(IDX_SIDE_LEFT);
	}	
		
	/**
	 * Returns all 4 corners: top-right, bottom-right, bottom-left, top-left. 
	 */
	public ITexture[] getCorners() {
		return new ITexture[] {getCornerTopRight(), getCornerBottomRight(), getCornerBottomLeft(), getCornerTopLeft()};
	}
	public ITexture getCornerTopRight() {
		return get(IDX_CORNER_TOP_RIGHT);
	}
	public ITexture getCornerBottomRight() {
		return get(IDX_CORNER_BOTTOM_RIGHT);
	}
	public ITexture getCornerBottomLeft() {
		return get(IDX_CORNER_BOTTOM_LEFT);
	}
	public ITexture getCornerTopLeft() {
		return get(IDX_CORNER_TOP_LEFT);
	}

	protected ITexture get(int index) {
		return textures[index];
	}

	//Setters
	public void setBackground(ITexture tex) {
		set(IDX_BACKGROUND, tex);
	}
	
	/**
	 * Sets all 4 sides: top, right, bottom, left. 
	 */
	public void setSides(ITexture[] texs) {
		setSideTop(texs[0]);
		setSideRight(texs[1]);
		setSideBottom(texs[2]);
		setSideLeft(texs[3]);
	}
	public void setSideTop(ITexture tex) {
		set(IDX_SIDE_TOP, tex);
	}
	public void setSideRight(ITexture tex) {
		set(IDX_SIDE_RIGHT, tex);
	}
	public void setSideBottom(ITexture tex) {
		set(IDX_SIDE_BOTTOM, tex);
	}
	public void setSideLeft(ITexture tex) {
		set(IDX_SIDE_LEFT, tex);
	}
	
	/**
	 * Sets all 4 corners: top-right, bottom-right, bottom-left, top-left. 
	 */
	public void setCorners(ITexture[] texs) {
		setCornerTopRight(texs[0]);
		setCornerBottomRight(texs[1]);
		setCornerBottomLeft(texs[2]);
		setCornerTopLeft(texs[3]);
	}
	public void setCornerTopRight(ITexture tex) {
		set(IDX_CORNER_TOP_RIGHT, tex);
	}
	public void setCornerBottomRight(ITexture tex) {
		set(IDX_CORNER_BOTTOM_RIGHT, tex);
	}
	public void setCornerBottomLeft(ITexture tex) {
		set(IDX_CORNER_BOTTOM_LEFT, tex);
	}
	public void setCornerTopLeft(ITexture tex) {
		set(IDX_CORNER_TOP_LEFT, tex);
	}
	
	protected void set(int index, ITexture tex) {
		textures[index] = tex;
	}
	
}
