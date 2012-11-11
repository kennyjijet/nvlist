package nl.weeaboo.vn.impl.base;

import nl.weeaboo.common.Rect2D;
import nl.weeaboo.vn.IDrawBuffer;
import nl.weeaboo.vn.IImageDrawable;
import nl.weeaboo.vn.IImageTween;
import nl.weeaboo.vn.ITexture;
import nl.weeaboo.vn.layout.LayoutUtil;
import nl.weeaboo.vn.math.Vec2;

public class BaseImageTween implements IImageTween {

	private static final long serialVersionUID = BaseImpl.serialVersionUID;
	
	private boolean changed;
	private double time;
	private final double duration;
	private boolean prepared;
	private boolean finished;
	
	protected IImageDrawable drawable;
	
	private ITexture startTexture;
	private double startAlignX, startAlignY;
	private ITexture endTexture;
	private int endAnchor;
	private double endAlignX, endAlignY;
	private transient Rect2D bounds;
		
	protected BaseImageTween(double duration) {
		this.duration = duration;

		endAnchor = 7;
	}
	
	//Functions
	protected void markChanged() {
		changed = true;
	}
	
	protected boolean consumeChanged() {
		boolean result = changed;
		changed = false;
		return result;
	}
	
	@Override
	public final void prepare() {
		if (drawable == null) throw new IllegalStateException("Image tween not initialized yet, must call setStartImage() and setEndImage() before use.");
		
		if (!prepared) {
			prepared = true;
			markChanged();
			doPrepare();
		}
	}
	
	protected void doPrepare() {		
	}

	@Override
	public final void finish() {
		if (drawable == null) throw new IllegalStateException("Image tween not initialized yet, must call setStartImage() and setEndImage() before use.");

		if (!finished) {
			finished = true;
			markChanged();
			doFinish();
		}
	}
	
	protected void doFinish() {
		drawable.setTexture(endTexture);
		drawable.setAlign(endAlignX, endAlignY);
	}
	
	@Override
	public boolean update(double effectSpeed) {
		if (drawable == null) throw new IllegalStateException("Image tween not initialized yet, must call setStartImage() and setEndImage() before use.");

		if (!isPrepared()) {
			prepare();
		}
		
		if (!isFinished()) {
			if (time < duration) {
				if (effectSpeed != 0) {
					time += effectSpeed;
					markChanged();
				}
			} else {
				finish();
			}
		}
		
		return consumeChanged();
	}
	
	@Override
	public void draw(IDrawBuffer d) {
		if (drawable == null) throw new IllegalStateException("Image tween not initialized yet, must call setStartImage() and setEndImage() before use.");
	}
	
	protected Rect2D calculateBounds() {
		return LayoutUtil.getBounds(startTexture, startAlignX, startAlignY,
					endTexture, endAlignX, endAlignY);
	}
	
	protected void invalidateBounds() {
		bounds = null;
	}
	
	//Getters
	@Override
	public boolean isPrepared() {
		return prepared;
	}

	@Override
	public boolean isFinished() {
		return finished;
	}
	
	@Override
	public double getTime() {
		return time;
	}
	
	@Override
	public double getDuration() {
		return duration;
	}
	
	@Override
	public double getNormalizedTime() {
		if (duration == 0) return 1.0;
		return Math.max(0, Math.min(1, time / (double)duration));
	}	
	
	@Override
	public ITexture getStartTexture() {
		return startTexture;
	}

	@Override
	public double getStartAlignX() {
		return startAlignX;
	}
	
	@Override
	public double getStartAlignY() {
		return startAlignY;
	}
	
	@Override
	public ITexture getEndTexture() {
		return endTexture;
	}
	
	@Override
	public double getEndAlignX() {
		return endAlignX;
	}
	
	@Override
	public double getEndAlignY() {
		return endAlignY;
	}
	
	protected final Rect2D getBounds() {
		if (bounds == null) {
			bounds = calculateBounds();
		}
		return bounds;
	}
	
	@Override
	public double getWidth() {
		return getBounds().w;
	}
	
	@Override
	public double getHeight() {
		return getBounds().h;
	}
		
	//Setters
	@Override
	public void setStartImage(IImageDrawable id) {
		drawable = id;
		startTexture = id.getTexture();
		startAlignX = id.getAlignX();
		startAlignY = id.getAlignY();
		
		if (endAnchor > 0 && endTexture != null) { //Determine alignment from anchor			
			Rect2D base = LayoutUtil.getBounds(startTexture, startAlignX, startAlignY);
			Vec2 align = LayoutUtil.alignSubRect(base, endTexture.getWidth(), endTexture.getHeight(), endAnchor);
			endAlignX = align.x;
			endAlignY = align.y;
		}
		
		invalidateBounds();
		markChanged();
	}

	@Override
	public void setEndImage(ITexture tex) {
		setEndImage(tex, 7);
	}

	@Override
	public void setEndImage(ITexture tex, int anchor) {
		endTexture = tex;
		endAnchor = anchor;
		
		invalidateBounds();
		markChanged();
	}
	
	@Override
	public void setEndImage(ITexture tex, double alignX, double alignY) {
		endTexture = tex;
		endAnchor = -1;
		endAlignX = alignX;
		endAlignY = alignY;
		
		invalidateBounds();
		markChanged();		
	}
	
}
