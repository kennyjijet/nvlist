package nl.weeaboo.vn.impl.base;

import static nl.weeaboo.vn.IDrawBuffer.DEFAULT_UV;

import java.nio.ByteBuffer;
import java.nio.ShortBuffer;

import nl.weeaboo.common.Area2D;
import nl.weeaboo.common.Dim;
import nl.weeaboo.common.Rect2D;
import nl.weeaboo.vn.IDrawBuffer;
import nl.weeaboo.vn.IImageDrawable;
import nl.weeaboo.vn.IInterpolator;
import nl.weeaboo.vn.INotifier;
import nl.weeaboo.vn.ITexture;
import nl.weeaboo.vn.impl.base.TriangleGrid.TextureWrap;
import nl.weeaboo.vn.layout.LayoutUtil;

public abstract class BaseBitmapTween extends BaseImageTween {

	private static final long serialVersionUID = BaseImpl.serialVersionUID;
	
	protected final int interpMax;
	protected final INotifier notifier;
	private final String fadeFilename;
	private final double range;
	private final IInterpolator interpolator;
	//private final boolean fadeTexLerp; //Linear interpolation
	private final boolean fadeTexTile; //Texture tiling
	
	//--- Initialized in prepare() ---
	private int[] interpolation;
	private Dim remapTexSize;
	private TriangleGrid grid;
	private transient ByteBuffer remapBuffer;
	
	public BaseBitmapTween(boolean is16Bit, INotifier ntf, String fadeFilename, double duration, double range,
			IInterpolator interpolator, boolean fadeTexTile)
	{	
		super(duration);
		
		this.interpMax = (is16Bit ? 65535 : 255);
		this.notifier = ntf;
		this.fadeFilename = fadeFilename;
		this.range = range;
		this.interpolator = interpolator;
		this.fadeTexTile = fadeTexTile;
	}
	
	//Functions
	private void resetPrepared() {
		interpolation = null;
		remapTexSize = null;
		grid = null;
		remapBuffer = null;
	}
	
	@Override
	protected void doPrepare() {
		resetPrepared();
		
		super.doPrepare();
		
		//Get interpolation function values
		interpolation = new int[interpMax+1];
		interpolation[0] = 0;
		for (int n = 1; n < interpMax; n++) {
			int i = Math.round(interpMax * interpolator.remap(n / (float)interpMax));
			interpolation[n] = Math.max(0, Math.min(interpMax, i));
		}
		interpolation[interpMax] = interpMax;
		
		//Get shader
		prepareShader();
		
		//Get texRects
		prepareTextures(new ITexture[] {getStartTexture(), getEndTexture()});
		Rect2D bounds = getBounds();
		
		//Create fade texture
		ITexture fadeTex = null;
		if (fadeFilename != null) {
			fadeTex = prepareFadeTexture(fadeFilename);
		}		
		if (fadeTex == null) {
			fadeTex = prepareDefaultFadeTexture(interpMax / 2);
		}

		//Create remap texture
		remapTexSize = (interpMax > 255 ? new Dim(256, 256) : new Dim(1, 256));
		prepareRemapTexture(remapTexSize.w, remapTexSize.h);
		
		//Create geometry
		Area2D drawableUV = drawable.getUV();
		
		ITexture tex0 = getStartTexture();
		Rect2D bounds0 = LayoutUtil.getBounds(tex0, getStartAlignX(), getStartAlignY());
		Area2D texBounds0 = BaseRenderer.combineUV(drawableUV, tex0 != null ? tex0.getUV() : DEFAULT_UV);
		TextureWrap wrap0 = TextureWrap.CLAMP;
		ITexture tex1 = getEndTexture();
		Rect2D bounds1 = LayoutUtil.getBounds(tex1, getEndAlignX(), getEndAlignY());
		Area2D texBounds1 = BaseRenderer.combineUV(drawableUV, tex1 != null ? tex1.getUV() : DEFAULT_UV);
		TextureWrap wrap1 = TextureWrap.CLAMP;
		Rect2D bounds2 = (fadeTexTile ? LayoutUtil.getBounds(fadeTex, 0, 0) : Rect2D.combine(bounds0, bounds1));
		Area2D texBounds2 = new Area2D(0, 0, 1, 1);
		if (fadeTex != null) {
			Rect2D b = LayoutUtil.getBounds(fadeTex, 0, 0);
			Area2D uv = fadeTex.getUV();
			if (fadeTexTile) {
				texBounds2 = new Area2D(uv.x, uv.y, uv.w*bounds.w/b.w, uv.h*bounds.h/b.h);
			} else {
				double sx = bounds.w/b.w, sy = bounds.h/b.h;
				double w, h;
				if (sx >= sy) {
					w = uv.w; h = sy/sx*uv.h;
				} else {
					h = uv.h; w = sx/sy*uv.w;
				}
				texBounds2 = new Area2D(uv.x+(1-w)/2, uv.y+(1-h)/2, w, h);
			}
		}
		TextureWrap wrap2 = (fadeTexTile ? TextureWrap.REPEAT_BOTH : TextureWrap.CLAMP);
		
		grid = TriangleGrid.layout3(
				bounds0.toArea2D(), texBounds0, wrap0,
				bounds1.toArea2D(), texBounds1, wrap1,
				bounds2.toArea2D(), texBounds2, wrap2);
		
		updateRemapTex(); //Needs to be called here, we don't know if update() will be called before draw()
	}
	
	@Override
	protected void doFinish() {
		resetPrepared();
		
		super.doFinish();
	}	
	
	protected abstract void prepareShader();
	
	protected abstract void prepareTextures(ITexture[] itexs); 
	
	protected abstract ITexture prepareFadeTexture(String filename);

	protected abstract ITexture prepareDefaultFadeTexture(int colorARGB);
	
	protected abstract void prepareRemapTexture(int w, int h);
		
	@Override
	public boolean update(double effectSpeed) {
		boolean changed = super.update(effectSpeed);
		if (!isFinished()) {
			changed |= updateRemapTex();
		}
		return consumeChanged() || changed;
	}
	
	protected abstract ByteBuffer initRemapPixels(ByteBuffer current, int requiredBytes);
	
	private boolean updateRemapTex() {
		double maa = interpMax * getNormalizedTime() * (1 + range);
		double mia = maa - interpMax * range;
		//System.out.println(mia + "~" + maa);
		int minA = Math.min(interpMax, Math.max(0, (int)Math.round(mia)));
		int maxA = Math.min(interpMax, Math.max(0, (int)Math.round(maa)));

		int requiredPixels = remapTexSize.w * remapTexSize.h;
		
		if (interpMax <= 255) {
			remapBuffer = initRemapPixels(remapBuffer, requiredPixels);
			ByteBuffer buf = remapBuffer;
			
			byte max = (byte)interpMax;
			for (int n = 0; n < minA; n++) {
				buf.put(n, max);
			}
			
			double inc = interpMax / (maa - mia);
			double cur = (minA - mia) * inc;			
			for (int n = minA; n <= maxA && n <= interpMax; n++) {
				int ar = Math.max(0, Math.min(interpMax, (int)Math.round(cur)));
				buf.put(n, (byte)(interpMax - interpolation[ar]));				
				cur += inc;
			}
			
			byte min = (byte)0;
			for (int n = maxA; n < requiredPixels; n++) {
				buf.put(n, min);
			}			
		} else {
			remapBuffer = initRemapPixels(remapBuffer, requiredPixels * 2);
			ShortBuffer buf = remapBuffer.asShortBuffer();

			short max = (short)interpMax;
			for (int n = 0; n < minA; n++) {
				buf.put(n, max);
			}
			
			double inc = interpMax / (maa - mia);
			double cur = (minA - mia) * inc;			
			for (int n = minA; n <= maxA && n <= interpMax; n++) {
				int ar = Math.max(0, Math.min(interpMax, (int)Math.round(cur)));
				buf.put(n, (short)(interpMax - interpolation[ar]));				
				cur += inc;
			}
			
			short min = (short)0;
			for (int n = maxA; n < requiredPixels; n++) {
				buf.put(n, min);
			}			
		}
		
		//Add an alpha channel for debugging purposes
		//--No longer needed when using a luminance texture instead of RGBA
		//for (int n = 0; n < remapTemp.length; n++) {
		//	remapTemp[n] = 0xFF000000 | remapTemp[n];
		//}
		
		updateRemapTex(remapBuffer, interpMax > 255);
		
		return true;
	}
	
	protected abstract void updateRemapTex(ByteBuffer pixels, boolean is16Bit);
	
	@Override
	public void draw(IDrawBuffer d) {
		super.draw(d);
				
		draw(BaseDrawBuffer.cast(d), drawable, grid);
	}
	
	protected abstract void draw(BaseDrawBuffer d, IImageDrawable drawable, TriangleGrid grid);
		
	//Getters
	
	//Setters
	
}
