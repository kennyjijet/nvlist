package nl.weeaboo.vn.impl.base;

import static org.luaj.vm2.LuaValue.valueOf;
import nl.weeaboo.common.Rect2D;
import nl.weeaboo.lua2.io.LuaSerializable;
import nl.weeaboo.vn.BlendMode;
import nl.weeaboo.vn.IGeometryShader;
import nl.weeaboo.vn.IImageDrawable;
import nl.weeaboo.vn.IPixelShader;
import nl.weeaboo.vn.IRenderer;
import nl.weeaboo.vn.ITexture;
import nl.weeaboo.vn.math.Matrix;
import nl.weeaboo.vn.math.Vec2;

import org.luaj.vm2.LuaFunction;
import org.luaj.vm2.LuaValue;
import org.luaj.vm2.Varargs;

@LuaSerializable
public class DistortGS extends BaseShader implements IGeometryShader {
		
	private static final long serialVersionUID = BaseImpl.serialVersionUID;

	static final int MAX_SEGMENTS = 512;

	private final int width, height;
	private DistortGrid grid;
	private Rect2D clampBounds;
	
	public DistortGS(int w, int h) {
		super(false);
		
		//Clamp to reasonable max values to prevent excessive memory usage
		w = Math.min(w, MAX_SEGMENTS);
		h = Math.min(h, MAX_SEGMENTS);
		
		width = w;
		height = h;
		grid = new DistortGrid(width, height);
	}

	//Functions
	@Override
	public void draw(IRenderer r, IImageDrawable image, ITexture tex,
			double alignX, double alignY, IPixelShader ps)
	{
		BaseRenderer rr = (BaseRenderer)r;
				
		short z = image.getZ();
		boolean clip = image.isClipEnabled();
		BlendMode blend = image.getBlendMode();
		int argb = image.getColorARGB();
		Matrix trans = image.getTransform();
		double w = image.getUnscaledWidth();
		double h = image.getUnscaledHeight();		

		Vec2 offset = LayoutUtil.getImageOffset(tex, alignX, alignY);
		
		rr.drawDistortQuad(z, clip, blend, argb, tex, 
				trans, offset.x, offset.y, w, h, ps,
				grid, clampBounds);
	}
	
	//Getters
	public Rect2D getClampBounds() {
		return clampBounds;
	}
	
	//Setters
	public void set(LuaFunction func) {
		final LuaValue timeVal = valueOf(getTime());
		
		LuaValue[] args = new LuaValue[3];
		args[2] = timeVal;
		for (int y = 0; y <= height; y++) {
			args[1] = valueOf(1.0 * y / height);
			for (int x = 0; x <= width; x++) {
				args[0] = valueOf(1.0 * x / width);
				
				Varargs result = func.invoke(args);
				
				grid.setDistort(x, y, result.tofloat(1), result.tofloat(2));
			}
		}
		markChanged();
	}
	
	public void set(DistortGrid g) {
		for (int y = 0; y <= height; y++) {
			for (int x = 0; x <= width; x++) {
				grid.setDistort(x, y, g.getDistortX(x, y), g.getDistortY(x, y));
			}
		}
		markChanged();
	}
	
	public void setClampBounds(double x, double y, double w, double h) {
		setClampBounds(new Rect2D(x, y, w, h));
	}
	public void setClampBounds(Rect2D rect) {
		clampBounds = rect;
	}
	
}
