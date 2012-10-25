package nl.weeaboo.vn.impl.base;

import java.util.Arrays;

import nl.weeaboo.common.Dim;
import nl.weeaboo.common.Rect2D;
import nl.weeaboo.vn.IDrawBuffer;
import nl.weeaboo.vn.IImageDrawable;
import nl.weeaboo.vn.IInterpolator;
import nl.weeaboo.vn.INotifier;
import nl.weeaboo.vn.ITexture;
import nl.weeaboo.vn.impl.base.TriangleGrid.TextureWrap;

public abstract class BaseBitmapTween extends BaseImageTween {

	private static final long serialVersionUID = BaseImpl.serialVersionUID;
	
	protected static final int INTERP_MAX = 65535;
	
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
	private transient int[] remapTemp;
	
	public BaseBitmapTween(INotifier ntf, String fadeFilename, double duration, double range,
			IInterpolator interpolator, boolean fadeTexTile)
	{	
		super(duration);
		
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
		remapTemp = null;
	}
	
	@Override
	protected void doPrepare() {
		resetPrepared();
		
		super.doPrepare();
		
		//Get interpolation function values
		interpolation = new int[INTERP_MAX+1];
		interpolation[0] = 0;
		for (int n = 1; n < INTERP_MAX; n++) {
			int i = Math.round(INTERP_MAX * interpolator.remap(n / (float)INTERP_MAX));
			interpolation[n] = (short)Math.max(0, Math.min(INTERP_MAX, i));
		}
		interpolation[INTERP_MAX] = INTERP_MAX;
		
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
			fadeTex = prepareDefaultFadeTexture(INTERP_MAX / 2);
		}

		//Create remap texture
		remapTexSize = new Dim(256, 256);
		prepareRemapTexture(remapTexSize.w, remapTexSize.h);
		
		//Create geometry
		ITexture tex0 = getStartTexture();
		Rect2D bounds0 = LayoutUtil.getBounds(tex0, getStartAlignX(), getStartAlignY());
		Rect2D texBounds0 = (tex0 != null ? tex0.getUV() : new Rect2D(0, 0, 1, 1));
		TextureWrap wrap0 = TextureWrap.CLAMP;
		ITexture tex1 = getEndTexture();
		Rect2D bounds1 = LayoutUtil.getBounds(tex1, getEndAlignX(), getEndAlignY());
		Rect2D texBounds1 = (tex1 != null ? tex1.getUV() : new Rect2D(0, 0, 1, 1));
		TextureWrap wrap1 = TextureWrap.CLAMP;
		Rect2D bounds2 = (fadeTexTile ? LayoutUtil.getBounds(fadeTex, 0, 0) : Rect2D.combine(bounds0, bounds1));
		Rect2D texBounds2 = new Rect2D(0, 0, 1, 1);
		if (fadeTex != null) {
			Rect2D b = LayoutUtil.getBounds(fadeTex, 0, 0);
			Rect2D uv = fadeTex.getUV();
			if (fadeTexTile) {
				texBounds2 = new Rect2D(uv.x, uv.y, uv.w*bounds.w/b.w, uv.h*bounds.h/b.h);
			} else {
				double sx = bounds.w/b.w, sy = bounds.h/b.h;
				double w, h;
				if (sx >= sy) {
					w = uv.w; h = sy/sx*uv.h;
				} else {
					h = uv.h; w = sx/sy*uv.w;
				}
				texBounds2 = new Rect2D(uv.x+(1-w)/2, uv.y+(1-h)/2, w, h);
			}
		}
		TextureWrap wrap2 = (fadeTexTile ? TextureWrap.REPEAT_BOTH : TextureWrap.CLAMP);
		
		grid = TriangleGrid.layout3(bounds0, texBounds0, wrap0,
				bounds1, texBounds1, wrap1, bounds2, texBounds2, wrap2);
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
	
	private boolean updateRemapTex() {
		double maa = INTERP_MAX * getNormalizedTime() * (1 + range);
		double mia = maa - INTERP_MAX * range;
		//System.out.println(mia + "~" + maa);
		int minA = Math.min(INTERP_MAX, Math.max(0, (int)Math.round(mia)));
		int maxA = Math.min(INTERP_MAX, Math.max(0, (int)Math.round(maa)));

		int requiredLen = remapTexSize.w * remapTexSize.h;
		if (remapTemp == null || remapTemp.length < requiredLen) {
			remapTemp = new int[requiredLen];
		}
		
		Arrays.fill(remapTemp, 0, minA, INTERP_MAX);		
		Arrays.fill(remapTemp, maxA, remapTemp.length, 0);		
		double inc = INTERP_MAX / (maa - mia);
		double cur = (minA - mia) * inc;			
		for (int n = minA; n <= maxA && n <= INTERP_MAX; n++) {
			int ar = Math.max(0, Math.min(INTERP_MAX, (int)Math.round(cur)));
			remapTemp[n] = INTERP_MAX - interpolation[ar];				
			cur += inc;
		}
		
		//Add an alpha channel for debugging purposes
		for (int n = 0; n < remapTemp.length; n++) {
			remapTemp[n] = 0xFF000000 | remapTemp[n];
		}
		
		updateRemapTex(remapTemp);
		
		return true;
	}
	
	protected abstract void updateRemapTex(int[] argb);
	
	@Override
	public void draw(IDrawBuffer d) {
		super.draw(d);
				
		draw(BaseDrawBuffer.cast(d), drawable, grid);
	}
	
	protected abstract void draw(BaseDrawBuffer d, IImageDrawable drawable, TriangleGrid grid);
		
	//Getters
	
	//Setters
	
}
