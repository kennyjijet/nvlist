package nl.weeaboo.vn.impl.base;

import java.io.Serializable;

import nl.weeaboo.lua2.io.LuaSerializable;
import nl.weeaboo.lua2.lib.LuajavaLib;
import nl.weeaboo.vn.IDrawBuffer;
import nl.weeaboo.vn.IGeometryShader;
import nl.weeaboo.vn.IImageDrawable;
import nl.weeaboo.vn.IImageFxLib;
import nl.weeaboo.vn.IPixelShader;
import nl.weeaboo.vn.ITexture;

import org.luaj.vm2.Varargs;
import org.luaj.vm2.lib.VarArgFunction;

@LuaSerializable
public class BlurGS extends BaseShader implements IGeometryShader {
		
	private static final long serialVersionUID = BaseImpl.serialVersionUID;
	
	private final IImageFxLib fxlib;
	private BlurImageCache cache;
	private BlendGS blendShader;
	private boolean interpolate;
	
	private ITexture baseTexture;
	private int levels;
	private int kernelSize;
	private int extendDirs;
	
	private ITexture[] blurredTextures;
	private double activeTextureIndex;
	private ITexture tex0, tex1;
	
	public BlurGS(IImageFxLib fxlib) {
		this(fxlib, new BlurImageCache(fxlib));
	}
	public BlurGS(IImageFxLib fxlib, BlurImageCache cache) {
		super(true);
		
		if (cache == null) throw new IllegalArgumentException("BlurImageCache may not be null");
		
		this.fxlib = fxlib;
		this.cache = cache;
		this.blendShader = new BlendGS();
		
		interpolate = true;
		
		levels = 1;
		kernelSize = 8;
		extendDirs = 0;
	}
	
	//Functions
	@Override
	public void draw(IDrawBuffer d, IImageDrawable image, ITexture tex,
			double alignX, double alignY, IPixelShader ps)
	{
		setBaseTexture(tex);

		ITexture oldtex = image.getTexture();
		try {			
			if (baseTexture != null) {
				applyTexture();
				tex = tex0;
				image.setTexture(tex, 5);
			}
			
			blendShader.draw(d, image, tex, image.getAlignX(), image.getAlignY(), ps);
		} finally {		
			image.setTexture(oldtex, 5);
		}
	}
			
	protected void invalidateTextures() {
		blurredTextures = null;
	}
	
	protected void applyTexture() {		
		int index0 = (int)Math.floor(Math.min(999999, Math.max(0, activeTextureIndex)));
		int index1 = index0 + 1;

		ITexture[] blurred = getTextures();
		if (blurred == null) {
			return;
		}
		
		tex0 = blurred[Math.max(0, Math.min(blurred.length-1, index0))];
		tex1 = blurred[Math.max(0, Math.min(blurred.length-1, index1))];
		
		blendShader.setEndTexture(tex1);
		if (interpolate) {
			blendShader.setTime(activeTextureIndex - index0);
		} else {
			blendShader.setTime(0);
		}
	}
	
	@Override
	public void onTimeChanged() {
		setActiveTexture(getTime() * Math.max(0, levels-1));
		
		super.onTimeChanged();
	}
	
	//Getters
	protected ITexture[] getTextures() {
		if (baseTexture == null) {
			return null;
		}		
		if (blurredTextures == null) {			
			blurredTextures = cache.blurMultiple(baseTexture, levels, kernelSize, extendDirs);
		}
		return blurredTextures;
	}

	public ITexture getTexture(int level) {
		ITexture[] blurred = getTextures();
		return level >= 0 && level < blurred.length ? blurred[level] : null;
	}
	
	public int getLevels() {
		return levels;
	}
	
	public int getKernelSize() {
		return kernelSize;
	}
	
	public int getExtendDirs() {
		return extendDirs;
	}
	
	public double getPaddingScaleX() {
		ITexture[] blurred = getTextures();
		if (blurred == null || blurred.length == 0) {
			throw new IllegalStateException("Unable to deduce padding before base texture is set");
		}
		
		int level = 0;
		double pad = getPadding(level);
		ITexture tex = blurred[level];
		return (tex.getWidth()+pad*2) / tex.getWidth();
	}
	public double getPaddingScaleY() {
		ITexture[] blurred = getTextures();
		if (blurred == null || blurred.length == 0) {
			throw new IllegalStateException("Unable to deduce padding before base texture is set");
		}
		
		int level = 0;
		double pad = getPadding(level);
		ITexture tex = blurred[level];
		return (tex.getHeight()+pad*2) / tex.getHeight();
	}
	public double getPadding(int level) {
		return fxlib.getBlurMultiplePad(level, getLevels(), getKernelSize());
	}
	
	//Setters	
	public void setBaseTexture(ITexture tex) {
		if (baseTexture != tex) {
			baseTexture = tex;
			
			invalidateTextures();
		}
	}
	
	public void setActiveTexture(double index) {
		if (activeTextureIndex != index) {
			activeTextureIndex = index;
		}
		applyTexture();
	}
	
	public void setLevels(int l) {
		if (levels != l) {
			levels = l;
			invalidateTextures();
		}
	}
	
	public void setKernelSize(int k) {
		if (kernelSize != k) {
			kernelSize = k;
			invalidateTextures();
		}
	}
	
	public void setExtendDirs(int d) {
		if (extendDirs != d) {
			extendDirs = d;
			invalidateTextures();
		}
	}
	
	public void setInterpolate(boolean i) {
		if (interpolate != i) {
			interpolate = i;
			applyTexture();
		}
	}
	
	
	//Inner Classes
	@LuaSerializable
	public static final class LuaConstructorFunction extends VarArgFunction implements Serializable {
		
		private static final long serialVersionUID = 1L;

		private final IImageFxLib fxlib;
		
		public LuaConstructorFunction(IImageFxLib fx) {
			fxlib = fx;
		}
		
		@Override
		public Varargs invoke(Varargs args) {
			BlurImageCache cache = args.touserdata(1, BlurImageCache.class);
			if (cache == null) cache = new BlurImageCache(fxlib);
			
			BlurGS shader = new BlurGS(fxlib, cache);
			return LuajavaLib.toUserdata(shader, BlurGS.class);
		}
		
	}
	
}
