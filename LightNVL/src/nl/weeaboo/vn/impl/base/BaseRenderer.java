package nl.weeaboo.vn.impl.base;

import nl.weeaboo.common.Rect;
import nl.weeaboo.common.Rect2D;
import nl.weeaboo.vn.BlendMode;
import nl.weeaboo.vn.IDistortGrid;
import nl.weeaboo.vn.IDrawBuffer;
import nl.weeaboo.vn.ILayer;
import nl.weeaboo.vn.IPixelShader;
import nl.weeaboo.vn.IRenderer;
import nl.weeaboo.vn.IScreenshot;
import nl.weeaboo.vn.ITexture;
import nl.weeaboo.vn.RenderCommand;
import nl.weeaboo.vn.RenderEnv;
import nl.weeaboo.vn.math.Matrix;

public abstract class BaseRenderer implements IRenderer {
		
	protected final RenderEnv env;
	protected final RenderStats renderStats;	
	
	/** This boolean is <code>true</code> when inside a call to render() */
	protected boolean rendering;
	
	//--- Properties only valid while render==true beneath this line ----------------------
	private boolean clipping;
	private BlendMode blendMode;
	private int foreground;
	//-------------------------------------------------------------------------------------
	
	protected BaseRenderer(RenderEnv env, RenderStats stats) {
		this.env = env;
		this.renderStats = stats;
		
		renderReset();
	}
	
	//Functions	
	@Override
	public void reset() {
		renderReset();
	}
	
	private void renderReset() {
		clipping = true;
		blendMode = BlendMode.DEFAULT;
		foreground = 0xFFFFFFFF;
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
	@Override
	public void render(ILayer layer, IDrawBuffer d) {
		BaseDrawBuffer dd = BaseDrawBuffer.cast(d);
		
		rendering = true;
		try {		
			if (renderStats != null) {
				renderStats.startRender();
			}
			
			renderReset();
			renderBegin();
			renderLayer(layer, env.screenClip, env.screenClip.toRect2D(), dd);
			renderEnd();			
		} finally {
			rendering = false;			
			if (renderStats != null) {
				renderStats.stopRender();
			}
		}
	}
	
	/**
	 * Must initialize the OpenGL state to the expected defaults (no texture,
	 * scissor enabled, blendmode default, color 0xFFFFFFFF)
	 */
	protected abstract void renderBegin();
	
	protected abstract void renderEnd();
	
	protected void renderLayer(ILayer layer, Rect parentClip, Rect2D parentClip2D, BaseDrawBuffer buffer) {
		int cstart = buffer.getLayerStart(layer);
		int cend = buffer.getLayerEnd(layer);
		if (cend <= cstart) {
			return;
		}

		//Get sorted render commands
		BaseRenderCommand[] cmds = buffer.sortCommands(cstart, cend);
		
		//Setup clipping/translate
		final Rect2D bounds = layer.getBounds();
		final Rect2D layerClip2D;
		if (bounds == null) {
			layerClip2D = parentClip2D;
		} else {
			double bx0 = bounds.x * env.scale;
			double by0 = bounds.y * env.scale;
			double bx1 = (bounds.x+bounds.w) * env.scale;
			double by1 = (bounds.y+bounds.h) * env.scale;

			layerClip2D = new Rect2D(
				parentClip2D.x + Math.max(0, Math.min(parentClip2D.w, bx0)),
				parentClip2D.y + Math.max(0, Math.min(parentClip2D.h, parentClip2D.h-by1)),
				Math.max(0, Math.min(parentClip2D.w-bx0, bx1-bx0)),
				Math.max(0, Math.min(parentClip2D.h-by0, by1-by0))
			);
		}		
		final Rect layerClip = roundClipRect(layerClip2D);
		
		setClipRect(layerClip);
		translate(bounds.x, bounds.y);
		
		//Render buffered commands
		long renderStatsTimestamp = 0;
		for (int n = cstart; n < cend; n++) {
			BaseRenderCommand cmd = cmds[n];
			
			//Clipping changed
			if (cmd.clipEnabled != clipping) {
				clipping = cmd.clipEnabled;
				setClip(clipping);
			}
			
			//Blend mode changed
			if (cmd.blendMode != blendMode) {
				blendMode = cmd.blendMode;
				setBlendMode(blendMode);
			}
			
			//Foreground color changed
			if (cmd.argb != foreground) {
				foreground = cmd.argb;
				setColor(foreground);
			}
			
			//Perform command-specific rendering
			if (renderStats != null) {
				renderStatsTimestamp = System.nanoTime();
			}
			
			preRenderCommand(cmd);
			
			switch (cmd.id) {
			case LayerRenderCommand.id: {
				LayerRenderCommand lrc = (LayerRenderCommand)cmd;
				renderLayer(lrc.layer, layerClip, layerClip2D, buffer);
			} break;
			case QuadRenderCommand.id: {
				QuadRenderCommand qrc = (QuadRenderCommand)cmd;
				renderQuad(qrc.tex, qrc.transform,
						qrc.x, qrc.y, qrc.width, qrc.height, qrc.ps,
						qrc.u, qrc.v, qrc.uw, qrc.vh);
			} break;
			case BlendQuadCommand.id: {
				BlendQuadCommand bqc = (BlendQuadCommand)cmd;
				renderBlendQuad(bqc.tex0, bqc.alignX0, bqc.alignY0,
						bqc.tex1, bqc.alignX1, bqc.alignY1,
						bqc.frac, bqc.transform, bqc.ps);
			} break;
			case FadeQuadCommand.id: {
				FadeQuadCommand fqc = (FadeQuadCommand)cmd;
				renderFadeQuad(fqc.tex, fqc.transform, fqc.argb, fqc.argb&0xFFFFFF,
						fqc.x, fqc.y, fqc.w, fqc.h,
						fqc.ps, fqc.dir, fqc.fadeIn, fqc.span, fqc.frac);
			} break;
			case DistortQuadCommand.id: {
				DistortQuadCommand dqc = (DistortQuadCommand)cmd;
				renderDistortQuad(dqc.tex, dqc.transform, dqc.argb,
						dqc.x, dqc.y, dqc.w, dqc.h, dqc.ps,
						dqc.grid, dqc.clampBounds);
			} break;
			case CustomRenderCommand.id: {
				CustomRenderCommand crc = (CustomRenderCommand)cmd;
				renderCustom(crc);
			} break;
			case ScreenshotRenderCommand.id: {
				ScreenshotRenderCommand src = (ScreenshotRenderCommand)cmd;
				renderScreenshot(src.ss, src.clipEnabled ? layerClip : env.screenClip);
			} break;
			default: {
				if (!renderUnknownCommand(cmd)) {
					throw new RuntimeException("Unable to process render command (id=" + cmd.id + ")");
				}
			}
			}
			
			postRenderCommand(cmd);
			
			if (renderStats != null) {
				renderStats.log(cmd, System.nanoTime()-renderStatsTimestamp);
			}
		}
		
		translate(-bounds.x, -bounds.y);
		setClipRect(parentClip);
	}
			
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
			IDistortGrid grid, Rect2D clampBounds);
	
	public abstract void renderTriangleGrid(TriangleGrid grid);
	
	public abstract void renderScreenshot(IScreenshot out, Rect glScreenRect);

	protected void renderCustom(CustomRenderCommand cmd) {
		cmd.render(this);
	}
	
	protected abstract boolean renderUnknownCommand(RenderCommand cmd);
	
	protected abstract void setClip(boolean c);
	protected abstract void setColor(int argb);
	protected abstract void setBlendMode(BlendMode bm);
	
	protected abstract void setClipRect(Rect glRect);
	protected abstract void translate(double dx, double dy);
	
	protected Rect roundClipRect(Rect2D clip2D) {
		//Rounded to ints, resulting clip rect should be no bigger than the non-rounded version.
		int x0 = (int)Math.ceil(clip2D.x);		
		int y0 = (int)Math.ceil(clip2D.y);
		
		//We can't just floor() the w/h because the ceil() of the x/y would skew the result.
		int x1 = (int)Math.floor(clip2D.x+clip2D.w);
		int y1 = (int)Math.floor(clip2D.y+clip2D.h);
		
		return new Rect(x0, y0, Math.max(0, x1-x0), Math.max(0, y1-y0));
	}
	
	//Getters
	
	@Override
	public RenderEnv getEnv() {
		return env;
	}
	
	@Override
	public abstract BaseDrawBuffer getDrawBuffer();
	
	//Setters
	
}
