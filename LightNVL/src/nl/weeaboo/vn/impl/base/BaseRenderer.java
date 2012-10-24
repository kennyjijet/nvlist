package nl.weeaboo.vn.impl.base;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import nl.weeaboo.collections.MergeSort;
import nl.weeaboo.common.Rect;
import nl.weeaboo.common.Rect2D;
import nl.weeaboo.vn.BlendMode;
import nl.weeaboo.vn.IGeometryShader;
import nl.weeaboo.vn.IImageDrawable;
import nl.weeaboo.vn.IPixelShader;
import nl.weeaboo.vn.IRenderer;
import nl.weeaboo.vn.IScreenshot;
import nl.weeaboo.vn.ITexture;
import nl.weeaboo.vn.RenderCommand;
import nl.weeaboo.vn.math.Matrix;
import nl.weeaboo.vn.math.Vec2;

public abstract class BaseRenderer implements IRenderer {
	
	private final int vw, vh;
	private final int rx, ry, rw, rh;
	private final int sw, sh;
	private final double scale;
	
	protected final RenderStats renderStats;	
	protected final List<RenderCommand> commands;

	private transient BaseRenderCommand[] tempArray;
	
	protected BaseRenderer(int vw, int vh, int rx, int ry, int rw, int rh, int sw, int sh,
			RenderStats stats)
	{
		this.vw = vw;
		this.vh = vh;
		this.rx = rx;
		this.ry = ry;
		this.rw = rw;
		this.rh = rh;
		this.sw = sw;
		this.sh = sh;

		scale = Math.min(rw / (double)vw, rh / (double)vh);		
		renderStats = stats;

		commands = new ArrayList<RenderCommand>(256);
	}
	
	//Functions	
	@Override
	public void reset() {		
		commands.clear();
	}
	
	@Override
	public void screenshot(IScreenshot ss, boolean clip) {
		draw(new ScreenshotRenderCommand(ss, clip));
	}
	
	@Override
	public void draw(IImageDrawable id) {
		drawWithTexture(id, id.getTexture(), id.getImageAlignX(), id.getImageAlignY(),
				id.getGeometryShader(), id.getPixelShader());
	}
	
	public void drawWithTexture(IImageDrawable id, ITexture tex, double alignX, double alignY,
			IGeometryShader gs, IPixelShader ps)
	{		
		if (gs == null) {
			Vec2 offset = LayoutUtil.getImageOffset(tex, alignX, alignY);
			drawQuad(id.getZ(), id.isClipEnabled(), id.getBlendMode(), id.getColorARGB(),
					tex, id.getTransform(), offset.x, offset.y,
					id.getUnscaledWidth(), id.getUnscaledHeight(), ps);
		} else {
			gs.draw(this, id, tex, alignX, alignY, ps);
		}
	}
	
	public void drawQuad(short z, boolean clipEnabled, BlendMode blendMode, int argb,
			ITexture tex, Matrix trans, double x, double y, double w, double h, IPixelShader ps)
	{
		drawQuad(z, clipEnabled, blendMode, argb, tex, trans, x, y, w, h, 0, 0, 1, 1, ps);
	}
	
	public void drawQuad(short z, boolean clipEnabled, BlendMode blendMode, int argb,
			ITexture tex, Matrix trans, double x, double y, double w, double h,
			double u, double v, double uw, double vh, IPixelShader ps)
	{	
		draw(new QuadRenderCommand(z, clipEnabled, blendMode, argb, tex,
				trans, x, y, w, h, u, v, uw, vh, ps));
	}

	public void drawFadeQuad(short z, boolean clipEnabled, BlendMode blendMode, int argb,
			ITexture tex, Matrix trans, double x, double y, double w, double h, IPixelShader ps,
			int dir, boolean fadeIn, double span, double time)
	{
		draw(new FadeQuadCommand(z, clipEnabled, blendMode, argb, tex,
				trans, x, y, w, h, ps, dir, fadeIn, span, time));
	}
	
	public void drawBlendQuad(short z, boolean clipEnabled, BlendMode blendMode, int argb,
			ITexture tex0, double alignX0, double alignY0,
			ITexture tex1, double alignX1, double alignY1,
			Matrix trans, IPixelShader ps,
			double frac)
	{
		draw(new BlendQuadCommand(z, clipEnabled, blendMode, argb,
				tex0, alignX0, alignY0,
				tex1, alignX1, alignY1,
				trans, ps,
				frac));
	}
	
	public void drawDistortQuad(short z, boolean clipEnabled, BlendMode blendMode, int argb,
			ITexture tex, Matrix trans, double x, double y, double w, double h, IPixelShader ps,
			DistortGrid distortGrid, Rect2D clampBounds)
	{
		draw(new DistortQuadCommand(z, clipEnabled, blendMode, argb,
				tex, trans, x, y, w, h, ps,
				distortGrid, clampBounds));
	}
	
	public void draw(RenderCommand cmd) {
		commands.add(cmd);		
	}
	
	public final void render(Rect2D bounds) {
		if (commands.isEmpty()) {
			return;
		}
		
		if (renderStats != null) {
			renderStats.startRender();
		}
		
		final int rx = getRealX();
		final int ry = getRealY();
		final int rw = getRealWidth();
		final int rh = getRealHeight();
		//final int sw = getScreenWidth();
		final int sh = getScreenHeight();

		if (tempArray == null) {
			tempArray = new BaseRenderCommand[commands.size()];
		}
		tempArray = commands.toArray(tempArray);
		final int len = commands.size();
		
		// Merge sort is only faster than Arrays.sort() for small (up to ~1000
		// elements) arrays or when the input is nearly sorted. Since both of
		// these are typically the case...
		MergeSort.sort(tempArray, 0, len);

		//Setup clipping
		Rect screenClip = new Rect(rx, sh - ry - rh, rw, rh);
		final int cx, cy, cw, ch; //Clip rect in screen coords
		if (bounds == null) {
			cx = screenClip.x; cy = screenClip.y; cw = screenClip.w; ch = screenClip.h;
		} else {
			cw = Math.max(0, Math.min(rw, (int)Math.floor(bounds.w * getScale())));
			ch = Math.max(0, Math.min(rh, (int)Math.floor(bounds.h * getScale())));			
			cx = rx + Math.max(0, Math.min(rw, (int)Math.ceil(bounds.x * getScale())));
			int ucy = ry + Math.max(0, Math.min(rh, (int)Math.ceil(bounds.y * getScale())));
			cy = sh - ucy - ch;
		}		
		Rect layerClip = new Rect(cx, cy, cw, ch);
		renderBegin(bounds, screenClip, layerClip);

		boolean clipping = true;
		BlendMode blendMode = BlendMode.DEFAULT;
		int foreground = 0xFFFFFFFF;
		
		//Render buffered commands
		long renderStatsTimestamp = 0;
		for (int n = 0; n < len; n++) {
			BaseRenderCommand cmd = tempArray[n];			
			
			//Clipping changed
			if (cmd.clipEnabled != clipping) {
				clipping = cmd.clipEnabled;
				renderSetClip(clipping);
			}
			
			//Blend mode changed
			if (cmd.blendMode != blendMode) {
				blendMode = cmd.blendMode;
				renderSetBlendMode(blendMode);
			}
			
			//Foreground color changed
			if (cmd.argb != foreground) {
				foreground = cmd.argb;
				renderSetColor(foreground);
			}
			
			//Don't render fully transparent objects
			if (((foreground>>24)&0xFF) == 0) {
				continue;
			}
			
			//Perform command-specific rendering
			if (renderStats != null) {
				renderStatsTimestamp = System.nanoTime();
			}

			preRenderCommand(cmd);
			if (cmd.id == QuadRenderCommand.id) {
				QuadRenderCommand qrc = (QuadRenderCommand)cmd;
				renderQuad(qrc.tex, qrc.transform,
						qrc.x, qrc.y, qrc.width, qrc.height, qrc.ps,
						qrc.u, qrc.v, qrc.uw, qrc.vh);
			} else if (cmd.id == BlendQuadCommand.id) {
				BlendQuadCommand bqc = (BlendQuadCommand)cmd;
				renderBlendQuad(bqc.tex0, bqc.alignX0, bqc.alignY0,
						bqc.tex1, bqc.alignX1, bqc.alignY1,
						bqc.frac, bqc.transform, bqc.ps);
			} else if (cmd.id == FadeQuadCommand.id) {
				FadeQuadCommand fqc = (FadeQuadCommand)cmd;
				renderFadeQuad(fqc.tex, fqc.transform, fqc.argb, fqc.argb&0xFFFFFF,
						fqc.x, fqc.y, fqc.w, fqc.h,
						fqc.ps, fqc.dir, fqc.fadeIn, fqc.span, fqc.frac);
			} else if (cmd.id == DistortQuadCommand.id) {
				DistortQuadCommand dqc = (DistortQuadCommand)cmd;
				renderDistortQuad(dqc.tex, dqc.transform, dqc.argb,
						dqc.x, dqc.y, dqc.w, dqc.h, dqc.ps,
						dqc.grid, dqc.clampBounds);
			} else if (cmd.id == ScreenshotRenderCommand.id) {
				ScreenshotRenderCommand src = (ScreenshotRenderCommand)cmd;
				renderScreenshot(src.ss, (src.clipEnabled ? layerClip : screenClip));
			} else if (cmd.id == CustomRenderCommand.id) {
				CustomRenderCommand crc = (CustomRenderCommand)cmd;
				renderCustom(crc);
			} else if (!renderUnknownCommand(cmd)) {
				throw new RuntimeException("Unable to process render command (id=" + cmd.id + ")");
			}
			postRenderCommand(cmd);
			
			if (renderStats != null) {
				renderStats.log(cmd, System.nanoTime()-renderStatsTimestamp);
			}
		}
		
		Arrays.fill(tempArray, 0, len, null); //Null array to allow garbage collection
				
		renderEnd();
		
		if (renderStats != null) {
			renderStats.stopRender();
		}		
	}
	
	/**
	 * Must: 
	 * <ul>
	 *   <li>Change the transformation matrix according to the given bounds.</li>
	 *   <li>Enable GL_SCISSOR_TEST with the given glScissorBox.</li>
	 *   <li>Enable GL_BLEND and set the blend mode to (GL_ONE, GL_ONE_MINUS_SRC_ALPHA).</li>
	 *   <li>Set glColor to opaque white.</li>
	 * </ul> 
	 */
	protected abstract void renderBegin(Rect2D bounds, Rect screenClip, Rect layerClip);
	
	protected abstract void renderEnd();
		
	protected void preRenderCommand(BaseRenderCommand cmd) {		
	}
	
	protected void postRenderCommand(BaseRenderCommand cmd) {		
	}
	
	public abstract void renderQuad(ITexture itex, Matrix transform,
			double x, double y, double w, double h, IPixelShader ps,
			double u, double v, double uw, double vh);
	
	public abstract void renderBlendQuad(
			ITexture tex0, double alignX0, double alignY0,
			ITexture tex1, double alignX1, double alignY1,
			double frac, Matrix transform, IPixelShader ps);
	
	public abstract void renderFadeQuad(ITexture tex, Matrix transform, int color0, int color1,
			double x, double y, double w, double h, IPixelShader ps,
			int dir, boolean fadeIn, double span, double frac);
	
	public abstract void renderDistortQuad(ITexture tex, Matrix transform, int argb,
			double x, double y, double w, double h, IPixelShader ps,
			DistortGrid grid, Rect2D clampBounds);
	
	public abstract void renderTriangleGrid(TriangleGrid grid);
	
	public abstract void renderScreenshot(IScreenshot out, Rect glScreenRect);

	protected void renderCustom(CustomRenderCommand cmd) {
		cmd.render(this);
	}
	
	protected abstract boolean renderUnknownCommand(RenderCommand cmd);
	
	protected abstract void renderSetClip(boolean c);
	protected abstract void renderSetColor(int argb);
	protected abstract void renderSetBlendMode(BlendMode bm);
	
	//Getters
	@Override
	public int getWidth() {
		return vw;
	}

	@Override
	public int getHeight() {
		return vh;
	}

	@Override
	public int getRealX() {
		return rx;
	}
	
	@Override
	public int getRealY() {
		return ry;
	}
	
	@Override
	public int getRealWidth() {
		return rw;
	}

	@Override
	public int getRealHeight() {
		return rh;
	}
	
	@Override
	public int getScreenWidth() {
		return sw;		
	}
	
	@Override
	public int getScreenHeight() {
		return sh;
	}
	
	@Override
	public double getScale() {
		return scale;
	}
	
	//Setters
	
}
