package nl.weeaboo.vn.impl.base;

import nl.weeaboo.common.Insets2D;
import nl.weeaboo.common.Rect2D;
import nl.weeaboo.lua2.io.LuaSerializable;
import nl.weeaboo.vn.BlendMode;
import nl.weeaboo.vn.IDrawBuffer;
import nl.weeaboo.vn.IPanel;
import nl.weeaboo.vn.IPixelShader;
import nl.weeaboo.vn.ITexture;
import nl.weeaboo.vn.NinePatch;
import nl.weeaboo.vn.math.Matrix;

@LuaSerializable
public abstract class BasePanel extends BaseContainer implements IPanel {

	private static final long serialVersionUID = BaseImpl.serialVersionUID;
	
	private Insets2D margin, _borderInsets;
	private NinePatch images;
	
	public BasePanel() {
		margin = new Insets2D(0, 0, 0, 0);
		images = new NinePatch();
	}
	
	//Functions
	@Override
	public void draw(IDrawBuffer d) {
		super.draw(d);
		
		Rect2D r = new Rect2D(margin.left, margin.top,
				getWidth() - margin.left - margin.right,
				getHeight() - margin.top - margin.bottom);
		Insets2D bi = getBorderInsets();
		
		short z = getZ();
		boolean clip = isClipEnabled();
		BlendMode blend = getBlendMode();
		int argb = getColorARGB();
		Matrix trans = getTransform();
		IPixelShader ps = getPixelShader();
		
		images.draw(d, z, clip, blend, argb, trans, r, bi, ps);
	}
	
	//Getters
	@Override
	public double getInnerWidth() {
		Insets2D padding = getPadding();
		Insets2D borderInsets = getBorderInsets();
		return Math.max(0, getWidth()				
				- borderInsets.left - borderInsets.right
				- padding.left - padding.right
				- margin.left - margin.right);
	}
	
	@Override
	public double getInnerHeight() {
		Insets2D padding = getPadding();
		Insets2D borderInsets = getBorderInsets();
		return Math.max(0, getHeight()
				- padding.top - padding.bottom
				- borderInsets.top - borderInsets.bottom
				- margin.top - margin.bottom);
	}

	@Override
	public Rect2D getInnerBounds() {
		Insets2D padding = getPadding();
		Insets2D borderInsets = getBorderInsets();
		return new Rect2D(
				getX() + margin.left + borderInsets.left + padding.left,
				getY() + margin.top + borderInsets.top + padding.top,
				getInnerWidth(), getInnerHeight());
	}

	@Override
	public Insets2D getMargin() {
		return margin;
	}
	
	@Override
	public Insets2D getBorderInsets() {
		if (_borderInsets == null) {
			return (images != null ? images.getInsets() : new Insets2D(0, 0, 0, 0));
		}
		return _borderInsets;
	}
	
	//Setters
	@Override
	public void setMargin(double pad) {
		setMargin(pad, pad, pad, pad);
	}

	@Override
	public void setMargin(double vertical, double horizontal) {
		setMargin(vertical, horizontal, vertical, horizontal);
	}

	@Override
	public void setMargin(double top, double right, double bottom, double left) {
		if (margin.top != top || margin.right != right || margin.bottom != bottom || margin.left != left) {
			margin = new Insets2D(top, right, bottom, left);			
			markChanged();
			invalidateLayout();
		}
	}
	
	@Override
	public void setBorderInsets(double pad) {
		setBorderInsets(pad, pad, pad, pad);
	}

	@Override
	public void setBorderInsets(double vertical, double horizontal) {
		setBorderInsets(vertical, horizontal, vertical, horizontal);
	}
	
	@Override
	public void setBorderInsets(double top, double right, double bottom, double left) {
		if (_borderInsets == null || _borderInsets.top != top || _borderInsets.right != right
				|| _borderInsets.bottom != bottom || _borderInsets.left != left)
		{
			_borderInsets = new Insets2D(top, right, bottom, left);
			markChanged();
			invalidateLayout();
		}
	}

	@Override
	public void setBackground(ITexture tex) {
		images.setBackground(tex);
		markChanged();
		if (_borderInsets == null) invalidateLayout();
	}
	
	@Override
	public void setBorder(ITexture[] sides, ITexture[] corners) {
		images.setSides(sides);
		images.setCorners(corners);
		markChanged();
		if (_borderInsets == null) invalidateLayout();
	}
	
}
